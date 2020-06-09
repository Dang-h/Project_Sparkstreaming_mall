package o.dh.mall_publisher.bean;

import lombok.Data;

@Data

public class OrderHourAmount {
	/**订单创建时间的时*/
	private String createHour;
	/**订单金额*/
	private Double sumOrderAmount;
}
