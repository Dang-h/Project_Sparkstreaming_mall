package realtime.bean

/**
 *
 * @param mid     设别唯一标识
 * @param uid     用户唯一标识
 * @param appid   应用id
 * @param area    城市
 * @param os      操作系统
 * @param ch      软件来源t
 * @param `type` 日志类型
 * @param vs      版本号
 * @param logDate 日志产生日期
 * @param logHour 日志产生在几时
 * @param ts      日志产生的时间戳
 */
case class StartUp(mid: String,
				   uid: String,
				   appid: String,
				   area: String,
				   os: String,
				   ch: String,
				   `type`: String,
				   vs: String,
				   var logDate: String,
				   var logHour: String,
				   ts: Long)
