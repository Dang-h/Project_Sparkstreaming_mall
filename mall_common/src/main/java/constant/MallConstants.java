package constant;

public class MallConstants {

	public static final String KAFKA_TOPIC_STARTUP = "MALL-STARTUP"; // 启动日志
	public static final String KAFKA_TOPIC_EVENT = "MALL-EVENT"; // 时间日志
	public static final String KAFKA_TOPIC_ORDER = "MALL-ORDER"; // 订单表
	public static final String KAFKA_TOPIC_ORDER_DETAIL = "MALL-ORDER-DETAIL"; // 订单详情
	public static final String KAFKA_TOPIC_USER_INFO = "MALL-USER-INFO"; // 用户信息
	public static final String KAFKA_TOPIC_NEW_ORDER = "MALL-NEW-ORDER"; // 新订单

	public static final String ES_INDEX_ALTER = "mall_coupon_alter";
	public static final String ES_INDEX_NEW_MID = "mall_new_mid";
	public static final String ES_INDEX_NEW_ORDER = "mall_new_order";
	public static final String ES_INDEX_SALE_DETAIL = "mall_sale_detail";
}
