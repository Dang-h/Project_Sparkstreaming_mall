package realtime.application

import com.alibaba.fastjson.JSON
import constant.MallConstants
import org.apache.hadoop.conf.Configuration
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import realtime.bean.OrderInfo
import realtime.util.MyKafkaUtil
import org.apache.phoenix.spark._

/**
 * 将订单数据从kafka的topic中取出来转存进HBase
 */
object OrderApp {
	def main(args: Array[String]): Unit = {

		val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("OrderApp")
		val ssc = new StreamingContext(sparkConf, Seconds(5))

		//从kafka获取数据
		val inputDStream = MyKafkaUtil.getKafkaStream(MallConstants.KAFKA_TOPIC_NEW_ORDER, ssc)

		//补充时间戳、敏感字段脱敏（电话、收件人地址···）
		val oderDStream: DStream[OrderInfo] = inputDStream.map {
			record => {
				//转成JSON对象以便处理（bean中添加case class）
				val orderInfo: OrderInfo = JSON.parseObject(record.value(), classOf[OrderInfo])

				//补充时间戳字段
				//获取创建日期字段：2019-08-02 06:00:25
				val dateArr: Array[String] = orderInfo.create_time.split(" ")
				//日期
				orderInfo.create_date = dateArr(0)
				//小时
				orderInfo.create_hour = dateArr(1).split(":")(0)

				//数据脱敏
				//13888745858 =>1388*******
				val tuple: (String, String) = orderInfo.consignee_tel.splitAt(4)
				orderInfo.consignee_tel = tuple._1 + "*******"
				orderInfo
			}
		}

		//TODO 增加一个额外的字段：  是否是用户首次下单 IS_FIRST_CONSUME

		//保存到HBase
		/*

		 */
		oderDStream.foreachRDD {
			rdd => {
				rdd.saveToPhoenix("MALL_ORDER_INFO",
					Seq("ID", "PROVINCE_ID", "CONSIGNEE", "ORDER_COMMENT",
						"CONSIGNEE_TEL", "ORDER_STATUS", "PAYMENT_WAY",
						"USER_ID", "IMG_URL", "TOTAL_AMOUNT", "EXPIRE_TIME",
						"DELIVERY_ADDRESS", "CREATE_TIME", "OPERATE_TIME",
						"TRACKING_NO", "PARENT_ORDER_ID", "OUT_TRADE_NO",
						"TRADE_BODY", "CREATE_DATE", "CREATE_HOUR"),
					new Configuration, Some("hadoop100,hadoop101,hadoop102:2181"))
			}
		}

		println("---------------启动---------------")
		ssc.start()
		ssc.awaitTermination()
	}

}