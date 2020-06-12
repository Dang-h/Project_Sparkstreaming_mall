package canal.handler;

import canal.utils.MyKafkaSender;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;
import constant.MallConstants;

import java.util.List;
import java.util.Random;


//业务的具体处理
public class CanalHandler {

	//定义具体参数
	//表名
	String tableName;
	//操作类型
	CanalEntry.EventType eventType;
	//数据列表
	List<CanalEntry.RowData> rowDataList;

	/**
	 * @param tableName   表名
	 * @param eventType   操作类型
	 * @param rowDataList 数据
	 * @return
	 */
	public CanalHandler(String tableName, CanalEntry.EventType eventType, List<CanalEntry.RowData> rowDataList) {
		this.tableName = tableName;
		this.eventType = eventType;
		this.rowDataList = rowDataList;
	}


	//根据业务类型类，发送到不同的kafka主题
	public void handle() {

		//将不同的表的变化切分成不同的主题保存到对应的topic里
		if (tableName.equals("order_info") && eventType.equals(CanalEntry.EventType.INSERT)) {
			//处理数据
			for (CanalEntry.RowData rowData : rowDataList) {
				//发送数据
				sendKafka(rowData, MallConstants.KAFKA_TOPIC_ORDER);
			}
			// 监控新增
		} else if (tableName.equals("order_detail") && eventType.equals(CanalEntry.EventType.INSERT)) {
			for (CanalEntry.RowData rowData : rowDataList) {
				sendKafka(rowData, MallConstants.KAFKA_TOPIC_ORDER_DETAIL);
			}
			// 监控新增和修改
		} else if (tableName.equals("user_info") && (eventType.equals(CanalEntry.EventType.INSERT) || eventType.equals(CanalEntry.EventType.UPDATE))) {
			for (CanalEntry.RowData rowData : rowDataList) {
				sendKafka(rowData, MallConstants.KAFKA_TOPIC_USER_INFO);
			}
		}
	}

	/**
	 * 发送数据到Kafka对应的Topic中
	 *
	 * @param rowData 行数据
	 * @param topic   发送到的kafka的主题
	 */
	private void sendKafka(CanalEntry.RowData rowData, String topic) {

		List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();

		JSONObject jsonObject = new JSONObject();

		for (CanalEntry.Column column : afterColumnsList) {
			System.out.println(column.getName() + " ---> " + column.getValue());

			//把每一个字段变成一个JSON字符串,并发送至Kafka对应的topic中
			jsonObject.put(column.getName(), column.getValue());
		}
		String rowJSON = jsonObject.toJSONString();
		MyKafkaSender.send(topic, rowJSON);

//		try {
//			Thread.sleep(new Random().nextInt(2) * 1000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}
}

