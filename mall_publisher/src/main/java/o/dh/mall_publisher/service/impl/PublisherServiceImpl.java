package o.dh.mall_publisher.service.impl;

import o.dh.mall_publisher.bean.OrderHourAmount;
import o.dh.mall_publisher.mapper.DAUMapper;
import o.dh.mall_publisher.mapper.OrderMapper;
import o.dh.mall_publisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PublisherServiceImpl implements PublisherService {

	@Autowired
	DAUMapper dauMapper;

	@Autowired
	OrderMapper orderMapper;

	@Override
	public Long getDauTotal(String date) {
		return dauMapper.getDauTotal(date);
	}

	@Override
	public Map<String, Long> getDauHourCount(String date) {
		List<Map> dauHourCountList = dauMapper.getDauHourCount(date);
		// 用于结果存放
		Map<String, Long> hourMap = new HashMap<>();

		for (Map map : dauHourCountList) {
			hourMap.put((String) map.get("LOGHOUR"), (Long) map.get("CNT"));
		}

		return hourMap;
	}

	@Override
	public Double getOrderAmount(String date) {
		return orderMapper.getOrderAmount(date);
	}

	@Override
	public Map<String, Double> getOrderHourAmount(String date) {
		//把list集合转换成map
		HashMap<String, Double> hourAmountMap = new HashMap<>();

		List<OrderHourAmount> orderHourAmountList = orderMapper.getOrderHourAmount(date);
		for (OrderHourAmount orderHourAmount : orderHourAmountList) {
			// (Hour, AmountOfHour)
			hourAmountMap.put(orderHourAmount.getCreateHour(), orderHourAmount.getSumOrderAmount());
		}

		return hourAmountMap;
	}
}
