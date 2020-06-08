package mock.application;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LogUploader {

	/**
	 * 把传入的log JSON串封装成一个http请求便于网络发送
	 *
	 * @param log
	 */
	public static void sendLogStream(String log) {
		try {
			//不同的日志类型对应不同的URL
			URL url = new URL("http://hadoop100:81/log");
//			URL url = new URL("http://localhost:8088/log");

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			//设置请求方式为post
			conn.setRequestMethod("POST");

			//时间头用来供server进行时钟校对的
			conn.setRequestProperty("clientTime", System.currentTimeMillis() + "");
			//允许上传数据
			conn.setDoOutput(true);
			//设置请求的头信息,设置内容类型为JSON
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			System.out.println("upload" + log);

			//输出流
			OutputStream out = conn.getOutputStream();
			out.write(("logString=" + log).getBytes());
			out.flush();
			out.close();
			int code = conn.getResponseCode();
			System.out.println(code);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
