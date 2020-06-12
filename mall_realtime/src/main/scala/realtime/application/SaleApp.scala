package realtime.application

import java.util

import com.alibaba.fastjson.JSON
import constant.MallConstants
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.json4s.native.Serialization
import realtime.bean.{OrderDetail, OrderInfo, SaleDetail}
import realtime.util.MyKafkaUtil
import redis.clients.jedis.Jedis

import scala.collection.mutable.ListBuffer

object SaleApp {
	def main(args: Array[String]): Unit = {
		val sparkConf: SparkConf = new SparkConf().setAppName("sale_app").setMaster("local[*]")
		val ssc = new StreamingContext(sparkConf, Seconds(5))

		// 获取数据
		val orderRecordDstream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(MallConstants.KAFKA_TOPIC_ORDER, ssc)
		val orderDetailRecordDstream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(MallConstants.KAFKA_TOPIC_ORDER_DETAIL, ssc)
		// orderRecord和orderDetailRecord关联，再反查userRecord
		val userRecordDstream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(MallConstants.KAFKA_TOPIC_USER_INFO, ssc)

		orderRecordDstream.foreachRDD(rdd => println(rdd.map(_.value()).collect().mkString("\n")))

		orderDetailRecordDstream.foreachRDD (rdd => println(rdd.map(_.value()).collect().mkString("\n")))


		// 将ConsumerRecord转给换成DStream
		val orderDstream: DStream[OrderInfo] = orderRecordDstream.map { record =>
			val jsonStr: String = record.value()
			val orderInfo: OrderInfo = JSON.parseObject(jsonStr, classOf[OrderInfo])

			//补充时间字段
			val datetimeArr: Array[String] = orderInfo.create_time.split(" ")
			orderInfo.create_date = datetimeArr(0)
			val hourStr: String = datetimeArr(1).split(":")(0)
			orderInfo.create_hour = hourStr

			//脱敏
			val tuple: (String, String) = orderInfo.consignee_tel.splitAt(4)
			orderInfo.consignee_tel = tuple._1 + "*******"

			orderInfo
		}

		val orderDetailDstream: DStream[OrderDetail] = orderDetailRecordDstream.map { record =>
			val jsonStr: String = record.value()
			val orderDetail: OrderDetail = JSON.parseObject(jsonStr, classOf[OrderDetail])

			orderDetail
		}

		// 转换成k-v结构以便join
		val orderInfoWithKeyDstream: DStream[(String, OrderInfo)] = orderDstream.map(orderInfo => (orderInfo.id, orderInfo))
		val orderDetailWithKeyDstream: DStream[(String, OrderDetail)] = orderDetailDstream.map(orderDetail => (orderDetail.id, orderDetail))

		// orderInfo JOIN orderDetail
		val fullJoinDstream: DStream[(String, (Option[OrderInfo], Option[OrderDetail]))] = orderInfoWithKeyDstream.fullOuterJoin(orderDetailWithKeyDstream)

		fullJoinDstream.flatMap {
			case (orderId, (orderInfoOpt, orderDetailOpt)) =>

				val saleDetailList: ListBuffer[SaleDetail] = ListBuffer[SaleDetail]()
				val jedis = new Jedis("hadoop101", 6379)
				//使用json4s 工具把orderInfo 解析为json
				implicit val formats = org.json4s.DefaultFormats

				// 如果主表：orderInfoOpt != none
				// 1 如果 从表:orderDetailOpt != none,关联从表
				// 2 把自己写入缓存(写入Redis)
				// 3 查询缓存(从Redis查)
				if (orderInfoOpt != None) {
					val orderInfo: OrderInfo = orderInfoOpt.get
					// 1
					if (orderDetailOpt != None) {
						val orderDetail: OrderDetail = orderDetailOpt.get
						// 合并成宽表
						val saleDetail = new SaleDetail(orderInfo, orderDetail)
						saleDetailList += saleDetail
					}
					// 2 type为set
					// key
					val orderInfoKey: String = "order_info:" + orderInfo.id
					// value(将caseClass解析成Json)
					val orderInfoJson: String = Serialization.write(orderInfo)
					jedis.setex(orderInfoKey, 3600, orderInfoJson)

					// 3
					val orderDetailId: String = "order_detail:" + orderInfo.id
					// 查缓存
					val orderDetailSet: util.Set[String] = jedis.smembers(orderDetailId)
					import scala.collection.JavaConversions._
					for (orderDetailJson <- orderDetailSet) {
						val orderDetail: OrderDetail = JSON.parseObject(orderDetailJson, classOf[OrderDetail])
						val saleDetail = new SaleDetail(orderInfo, orderDetail)
						saleDetailList += saleDetail
					}
				} else if (orderDetailOpt != None) {
					// 如果主表：orderDetailOpt != none
					// 1 把自己写入缓存(写入Redis)
					// 2 查询缓存(从Redis查)

					// 1
					val orderDetail: OrderDetail = orderDetailOpt.get
					// caseClass转JSON
					val orderDetailJSON: String = Serialization.write(orderDetail)
					val orderDetailKey: String = "order_detail:" + orderDetail.order_id
					// 写入Redis
					jedis.sadd(orderDetailKey, orderDetailJSON)
					// 设置过期时间
					jedis.expire(orderDetailKey, 3600)

					// 2
					val orderInfoKey: String = "order_info:" + orderDetail.order_id
					val orderInfoJson: String = jedis.get(orderInfoKey)
					if (orderInfoJson != null && orderDetailJSON.size > 0) {
						val orderInfo: OrderInfo = JSON.parseObject(orderInfoJson, classOf[OrderInfo])
						val saleDetail = new SaleDetail(orderInfo, orderDetail)
						saleDetailList += saleDetail
					}
				}

				jedis.close()

				saleDetailList
		}



		ssc.start()
		ssc.awaitTermination()
	}
}
