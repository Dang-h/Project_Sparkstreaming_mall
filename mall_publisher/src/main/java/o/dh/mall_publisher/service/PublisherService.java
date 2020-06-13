package o.dh.mall_publisher.service;

import java.util.Map;


public interface PublisherService {

	/**获取Dau总数*/
	public Long getDauTotal(String date);

	/**分时统计*/
	public Map<String, Long> getDauHourCount(String date);

	/**获取交易总额*/
	public Double getOrderAmount(String date);

	/**
	 * 交易额分时统计
	 * @param date 订单日期
	 * @return 订单分时统计数据
	 */
	public Map<String, Double> getOrderHourAmount(String date);

	/**
	 * 从ES查询订单信息
	 * @param date 订单日期
	 * @param keyword 关键词
	 * @param pageNo 页码
	 * @param pageSize 每页显示信息条数
	 * @return
	 */
	public Map<String, Object> getSaleDetailFromES(String date, String keyword, int pageNo, int pageSize);

}
