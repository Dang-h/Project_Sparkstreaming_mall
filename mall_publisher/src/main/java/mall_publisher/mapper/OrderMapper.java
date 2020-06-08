package mall_publisher.mapper;

import mall_publisher.bean.OrderHourAmount;

import java.util.List;

public interface OrderMapper {

	/** 获取订单金额*/
	public Double getOrderAmount(String date);

	/** 获取分时统计金额*/
	public List<OrderHourAmount> getOrderHourAmount(String date);
}
