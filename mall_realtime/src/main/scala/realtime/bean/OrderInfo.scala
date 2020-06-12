package realtime.bean

/**
 * 订单表
 *
 * @param id               订单id
 * @param consignee        收货人
 * @param consignee_tel    收件人电话
 * @param total_amount     总金额
 * @param order_status     订单状态
 * @param user_id          用户id
 * @param payment_way      付款方式
 * @param delivery_address 送货地址
 * @param order_comment    订单备注
 * @param out_trade_no     订单交易编号 （ 第三方支付用)
 * @param trade_body       订单描述 (第三方支付用)
 * @param create_time      创建时间
 * @param operate_time     操作时间
 * @param expire_time      失效时间
 * @param tracking_no      物流单编号
 * @param parent_order_id  父订单编号
 * @param img_url          图片路径
 * @param province_id      地区
 * @param create_date      订单创建日期
 * @param create_hour      订单创建小时
 */
case class OrderInfo(id: String,
					 consignee: String,
					 var consignee_tel: String,
					 total_amount: Double,
					 order_status: String,
					 user_id: String,
					 payment_way: String,
					 delivery_address: String,
					 order_comment: String,
					 out_trade_no: String,
					 trade_body: String,
					 create_time: String,
					 operate_time: String,
					 expire_time: String,
					 tracking_no: String,
					 parent_order_id: String,
					 img_url: String,
					 province_id: String,
					 var create_date: String,
					 var create_hour: String)


















