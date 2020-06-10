package realtime.bean

/**
 *
 * @param mid 设备唯一标识
 * @param uids 用户id
 * @param itemIds 商品编号
 * @param events 事件
 * @param ts 时间戳
 */
case class AlertInfo(mid: String,
					 uids: java.util.HashSet[String],
					 itemIds: java.util.HashSet[String],
					 events: java.util.List[String],
					 ts: Long)
