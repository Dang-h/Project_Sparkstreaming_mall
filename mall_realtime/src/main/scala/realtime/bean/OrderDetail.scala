package realtime.bean

/**
 * 订单明细表
 *
 * @param id          编号
 * @param order_id    订单编号
 * @param sku_name    sku名称
 * @param sku_id      skuId
 * @param order_price 订单价格
 * @param img_url     图片url
 * @param sku_num     购买个数
 */
case class OrderDetail(id: String,
					   order_id: String,
					   sku_name: String,
					   sku_id: String,
					   order_price: String,
					   img_url: String,
					   sku_num: String)
