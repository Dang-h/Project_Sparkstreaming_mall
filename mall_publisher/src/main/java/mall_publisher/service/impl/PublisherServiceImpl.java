package mall_publisher.service.impl;


import mall_publisher.mapper.DAUMapper;
import mall_publisher.mapper.OrderMapper;
import mall_publisher.service.PublisherService;
import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Autowired;

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
		Map<String,Long> hourMap=new HashMap<>();

		for (Map map : dauHourCountList) {
			hourMap.put((String)map.get("LOGHOUR"),(Long)map.get("CT"));
		}

		return hourMap;
	}

	@Override
	public Double getOrderAmount(String date) {
		return null;
	}

	@Override
	public Map<String, Double> getOrderHourAmount(String date) {
		return null;
	}

}
