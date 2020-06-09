package canal.client;

import canal.handler.CanalHandler;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.InvalidProtocolBufferException;

import java.net.InetSocketAddress;
import java.util.List;

public class CanalClient {
	public static void main(String[] args) {

		//创建连接器,与canal的服务端取得连接
		CanalConnector canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress("hadoop100", 11111), "example", "", "");

		while (true) {
			//建立连接
			canalConnector.connect();

			//订阅,监控sparkStreaming_mall库下所有表
			canalConnector.subscribe("sparkStreaming_mall.*");

			//抓取100个entry相当于100条sql，抓取100份数据
			Message message = canalConnector.get(100);

			//如果没有抓取到数据，冷静5秒
			if (message.getEntries().size() == 0) {
				System.out.println("我想静静");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else { //有数据，单独处理

				//提取entry数据
				for (CanalEntry.Entry entry : message.getEntries()) {

					//只有行变化才处理
					if (entry.getEntryType().equals(CanalEntry.EntryType.ROWDATA)) {
						CanalEntry.RowChange rowChange = null;

						try {
							//将entry里的数据反序列化，
							rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
						} catch (InvalidProtocolBufferException e) {
							e.printStackTrace();
						}

						//得到rowDataList
						assert rowChange != null;
						List<CanalEntry.RowData> rowDatasList = rowChange.getRowDatasList();
						//得到操作的表,不同的表可能需要发送到不同的topic中
						String tableName = entry.getHeader().getTableName();
						//得到操作的类型
						CanalEntry.EventType eventType = rowChange.getEventType();

						CanalHandler canalHandler = new CanalHandler(tableName, eventType, rowDatasList);

						canalHandler.handle();
					}

				}

			}
		}
	}
}
