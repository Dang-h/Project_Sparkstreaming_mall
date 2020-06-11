package realtime.application

import java.text.SimpleDateFormat
import java.util
import java.util.Date

import com.alibaba.fastjson.JSON
import constant.MallConstants
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import realtime.bean.{AlertInfo, EventInfo}
import realtime.util.{MyEsUtil, MyKafkaUtil}

import scala.util.control.Breaks._

/*
需求：同一设备，5分钟内三次及以上用不同账号登录并领取优惠劵，并且在登录到领劵过程中没有浏览商品。达到以上要求则产生一条预警日志。
同一设备，每分钟只记录一次预警。
 */
object AlertApp {
	/*
	  1 ) 5分钟内 --> 窗口大小  window      窗口 （窗口大小，滑动步长 ）   窗口大小 数据的统计范围    滑动步长 统计频率
	  2 ) 同一设备   groupBy  mid
	  3 ) 用不同账号登录并领取优惠劵     没有 浏览商品
	 	  map   变换结构 （预警结构）   经过判断  把是否满足预警条件的mid 打上标签
	  4 ) filter  把没有标签的过滤掉
	  5 ) 保存到ES
	*/
	def main(args: Array[String]): Unit = {

		val sparkConf: SparkConf = new SparkConf().setAppName("alert_app").setMaster("local[*]")
		val ssc = new StreamingContext(sparkConf, Seconds(5))

		// 获取Topic中的数据
		val inputDstream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(MallConstants.KAFKA_TOPIC_EVENT, ssc)

		// 将ConsumerRecord的数据转换成JSON
		val eventInfoDstream: DStream[EventInfo] = inputDstream.map { record =>
			val eventInfo: EventInfo = JSON.parseObject(record.value(), classOf[EventInfo])
			eventInfo
		}

		eventInfoDstream.cache()

		//    1 ) 5分钟内 --> 窗口大小  window      窗口 （窗口大小，滑动步长 ）   窗口大小 数据的统计范围    滑动步长 统计频率
		// FIXME 是否可以用
		val eventWindowDStream: DStream[EventInfo] = eventInfoDstream.window(Seconds(300), Seconds(5))
		//    2) 同一设备	按照mid分组
		val groupByMidDstream: DStream[(String, Iterable[EventInfo])] = eventWindowDStream.map(eventInfo =>
			(eventInfo.mid, eventInfo)).groupByKey()

		//    3 ) 用三次及以上不同账号登录并领取优惠劵     没有 浏览商品
		// 判断预警
		// 一个设备，登录账号>=3，领券过程中未浏览商品
		val checkedDstream: DStream[(Boolean, AlertInfo)] = groupByMidDstream.map {
			case (mid, eventInfoItr) =>

				// 领券的用户
				val couponUidSet = new util.HashSet[String]()
				// 领券的商品
				val itemsSet = new util.HashSet[String]()
				// 事件列表
				val eventList = new util.ArrayList[String]()
				// 点击事件标签
				var hasClickItem = false

				breakable(
					for (eventInfo: EventInfo <- eventInfoItr) {
						eventList.add(eventInfo.evid) //收集mid的所有操作事件
						if (eventInfo.evid == "coupon") {
							//点击购物券时 涉及登录账号
							couponUidSet.add(eventInfo.uid)
							itemsSet.add(eventInfo.itemid) //收集领取购物券的商品
						}
						if (eventInfo.evid == "clickItem") { //点击商品
							hasClickItem = true
							break() //如果有点击 直接退出
						}
					}
				)

				//判断 符合预警的条件    1)  点击购物券时 涉及登录账号 >=3   2) events not contain  clickItem
				//(标签，预警信息对象)-》(true, 预警数据)<---目标对象
				(couponUidSet.size >= 3 && !hasClickItem, AlertInfo(mid, couponUidSet, itemsSet, eventList, System.currentTimeMillis())) //（是否符合条件 ，日志信息）
		}

		checkedDstream.foreachRDD { rdd =>
			println(rdd.collect().mkString("\n"))
		}

		val alterDstream: DStream[AlertInfo] = checkedDstream.filter(_._1).map(_._2)

		alterDstream.foreachRDD { rdd =>
			println("-------------------")
			println("预警：")
			println(rdd.collect().mkString("\n"))
			println()
			println("-------------------")
		}


		// 保存到ES中
		alterDstream.foreachRDD { rdd =>
			rdd.foreachPartition { alertItr =>
				val list: List[AlertInfo] = alertItr.toList
				//提取主键(_id) --> mid + 时间; 同时 也利用主键进行去重
				val alterListWithId: List[(String, AlertInfo)] = list.map(alertInfo =>
					(alertInfo.mid + "_" + timestamp2Date(alertInfo.ts), alertInfo))

				//批量保存
				MyEsUtil.indexBulk(MallConstants.ES_INDEX_ALTER, alterListWithId)

			}

		}

		println("---------------启动---------------")

		ssc.start()
		ssc.awaitTermination()

	}

	def timestamp2Date(ts: Long): String = {
		val date: String = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date(ts))
		date
	}
}
