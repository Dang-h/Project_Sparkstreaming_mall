package realtime.application

import java.text.SimpleDateFormat
import java.util
import java.util.Date

import com.alibaba.fastjson.JSON
import constant.MallConstants
import org.apache.hadoop.conf.Configuration
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.phoenix.spark._
import org.apache.spark.SparkConf
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import realtime.bean.StartUp
import realtime.util.{MyKafkaUtil, RedisUtil}
import redis.clients.jedis.Jedis

object DAUApp {
	def main(args: Array[String]): Unit = {
		val sparkConf: SparkConf = new SparkConf().setAppName("dau_app").setMaster("local[*]")
		val ssc: StreamingContext = new StreamingContext(sparkConf, Seconds(5))

		// 获取日志
		val inputDStream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(MallConstants.KAFKA_TOPIC_STARTUP, ssc)

		//		// 测试数据是否读取到
		//		inputDStream.foreachRDD(rdd =>
		//			println(rdd.map(_.value()).collect().mkString("\n")))
		println("---------------需求一：统计日活---------------")
		/*
		统计日活：每个用户一天可能活跃多次，只能算一次。因此涉及到去重。
		去重分为3部分——两次过滤、一次保存清单：
			第一次过滤：批次之间进行去查重，利用Redis的Set数据结构去重
			第二次过滤：批次内去查重，使用算子
			保存清单到Redis：
		 */

		//为了接下来统计方便，给获取的日志添加Date和Hour。
		val startupLogDStream: DStream[StartUp] = inputDStream.map {
			record: ConsumerRecord[String, String] => {
				val startUpJsonString: String = record.value()
				val startUpLog: StartUp = JSON.parseObject(startUpJsonString, classOf[StartUp])

				// 获取时间并转换
				val dateTimeString: String = new SimpleDateFormat("yyyy-MM-dd HH").format(new Date(startUpLog.ts))

				// 添加Date和Hour
				val dateHour: Array[String] = dateTimeString.split(" ")
				startUpLog.logDate = dateHour(0)
				startUpLog.logHour = dateHour(1)

				startUpLog
			}
		}

		// 第一次去去重：批次间去重，数据导入Redis
		val filteredDStream1: DStream[StartUp] = startupLogDStream.transform {
			rdd => {
				println("过滤前数据量：" + rdd.count())

				val jedis: Jedis = RedisUtil.getJedisClient

				val dauKey: String = "dau:" + new SimpleDateFormat("yyyy-MM-dd").format(new Date())
				val dauSet: util.Set[String] = jedis.smembers(dauKey)
				// 创建广播变量，分发清单
				val dauBC: Broadcast[util.Set[String]] = ssc.sparkContext.broadcast(dauSet)
				jedis.close()

				// 过滤数据
				val filteredRDD: RDD[StartUp] = rdd.filter {
					// 获取到的日志和Redis中数据对比，看是或否包含对应的mid；包含就将其过滤
					startupLog => !dauBC.value.contains(startupLog.mid)
				}

				println("过滤后数据量：" + filteredRDD.count())

				filteredRDD
			}
		}

		// 同一批次内去重
		val filteredDStream2: DStream[StartUp] = filteredDStream1.map(log => (log.mid, log)).groupByKey().flatMap(_._2.toList.take(1))

		// 记录每天访问过的mid，存入Redis，形成一个清单
		filteredDStream2.foreachRDD {
			rdd => {
				rdd.foreachPartition {
					startupLogItr => {
						// 创建Redis连接
						val jedis: Jedis = RedisUtil.getJedisClient

						for (log <- startupLogItr) {
							// 设计Key，注意和第一次去重——批次间去重的dauKey一致
							val dauKey: String = "dau:" + log.logDate
							// 数据存入Redis,格式（dau:2020-06-04, mid_11）
							jedis.sadd(dauKey, log.mid)
						}

						// 释放连接
						jedis.close()
					}
				}
			}
		}

		// 通过Phoenix将数据存入HBase
		filteredDStream2.foreachRDD {
			rdd => {
				rdd.saveToPhoenix("USER.MALL_DAU",
					Seq("MID", "UID", "APPID", "AREA", "OS", "CH", "TYPE", "VS", "LOGDATE", "LOGHOUR", "TS"),
					new Configuration, Some("hadoop100,hadoop101,hadoop102:2181")
				)
			}
		}

		println("---------------启动---------------")
		ssc.start()
		ssc.awaitTermination()
	}
}