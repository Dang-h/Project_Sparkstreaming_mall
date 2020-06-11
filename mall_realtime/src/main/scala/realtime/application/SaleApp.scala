package realtime.application

import constant.MallConstants
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import realtime.util.MyKafkaUtil

object SaleApp {
	def main(args: Array[String]): Unit = {
		val sparkConf: SparkConf = new SparkConf().setAppName("sale_app").setMaster("local[*]")
		val ssc = new StreamingContext(sparkConf, Seconds(5))

		// 获取数据
		val orderRecordDstream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(MallConstants.KAFKA_TOPIC_ORDER, ssc)
		val orderDetailRecordDstream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(MallConstants.KAFKA_TOPIC_ORDER_DETAIL, ssc)
		// orderRecord和orderDetailRecord关联，再反查userRecord
		val userRecordDstream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(MallConstants.KAFKA_TOPIC_USER_INFO, ssc)
	}
}
