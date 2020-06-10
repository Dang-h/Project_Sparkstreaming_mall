package realtime.util

import java.util
import java.util.Objects

import io.searchbox.action.Action
import io.searchbox.client.{JestClient, JestClientFactory}
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.core.{Bulk, BulkResult, Index}

import collection.JavaConversions._

object MyEsUtil {
	private val ES_HOST = "http://hadoop100"
	private val ES_HTTP_PORT = 9200
	private var factory: JestClientFactory = null

	/**
	 * 获取客户端
	 *
	 * @return jestclient
	 */
	def getClient: JestClient = {
		if (factory == null) build()
		factory.getObject
	}

	/**
	 * 关闭客户端
	 */
	def close(client: JestClient): Unit = {
		if (!Objects.isNull(client)) try
			client.shutdownClient()
		catch {
			case e: Exception =>
				e.printStackTrace()
		}
	}

	/**
	 * 建立连接
	 */
	private def build(): Unit = {
		factory = new JestClientFactory
		factory.setHttpClientConfig(new HttpClientConfig.Builder(ES_HOST + ":" + ES_HTTP_PORT).multiThreaded(true)
		  .maxTotalConnection(20) //连接总数
		  .connTimeout(10000).readTimeout(10000).build)

	}

	// batch
	def indexBulk(indexName:String , dataList: List[(String,Any)]): Unit ={
		if(dataList.size>0){
			val jestClient: JestClient = getClient
			val bulkBuilder = new Bulk.Builder
			for ((id,data) <- dataList ) {
				val index = new Index.Builder(data).index(indexName).`type`("_doc").id(id) .build()
				bulkBuilder.addAction(index)
			}
			// local
			val bulk: Bulk = bulkBuilder.build()
			val items: util.List[BulkResult#BulkResultItem] = jestClient.execute(bulk).getFailedItems
			println("保存"+items.mkString(",")+"条")
			close(jestClient)

		}

	}

	// 测试数据插入
	def main(args: Array[String]): Unit = {

		val jestClient: JestClient = getClient
		/*
		PUT customer_test/_doc/1
		{
  			"customer_name":"zhang3",
  			"customer_amount":123.4
		}
		PUT _index/_type/_id
		{_source  (case class)}
		 */
		val index = new Index.Builder(CustomerTest("li4",1000.0)).index("customer_test").`type`("_doc").id("2")
		  .build()
		val message: String = jestClient.execute(index).getErrorMessage
		println(s"message = ${message}")
		close(jestClient)
	}

	case class CustomerTest(customer_name:String,customer_amount :Double)


}