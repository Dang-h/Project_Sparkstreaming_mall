package realtime.bean

/**
 *
 * @param mid 设备唯一标识
 * @param uid 用户标识
 * @param appid 应用id
 * @param area 地区
 * @param os 操作系统
 * @param ch 软件来源
 * @param `type` 日志类型
 * @param evid 事件id
 * @param pgid 当前页id
 * @param npgid 跳转页id
 * @param itemid 商品编号
 * @param logDate 日志产生日期
 * @param logHour 日志产生的时
 * @param ts 时间戳
 */
case class EventInfo(mid:String,
					 uid:String,
					 appid:String,
					 area:String,
					 os:String,
					 ch:String,
					 `type`:String,
					 evid:String ,
					 pgid:String ,
					 npgid:String ,
					 itemid:String,
					 var logDate:String,
					 var logHour:String,
					 var ts:Long)
