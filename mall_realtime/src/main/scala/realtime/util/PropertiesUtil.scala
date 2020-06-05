package realtime.util

import java.io.InputStreamReader
import java.util.Properties

/**
 * 读取配置文件
 */
object PropertiesUtil {

//	def main(args: Array[String]): Unit = {
//		val properties: Properties = PropertiesUtil.load("config.properties")
//
//		println(properties.getProperty("kafka.broker.list"))
//	}

	/**
	 * 传入一个文件，变成一个内存结构化(k-v)对象
	 * @param propertiesName 配置名称
	 * @return
	 */
	def load(propertiesName:String): Properties ={
		val prop=new Properties();
		//加载配置；
		prop.load(new InputStreamReader(Thread.currentThread().getContextClassLoader
		  .getResourceAsStream(propertiesName) , "UTF-8"))
		prop
	}

}
