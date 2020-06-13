package o.dh.mall_publisher.controller;

import com.alibaba.fastjson.JSON;
import o.dh.mall_publisher.bean.Option;
import o.dh.mall_publisher.bean.StatisticalGraph;
import o.dh.mall_publisher.service.PublisherService;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

//控制层，对外发布外界接口；实现Web接口
@RestController
public class PublisherController {

	@Autowired
	PublisherService publisherService;

	// http://logserver:8099/realtime-total?date=2020-06-09
	//查询使用GetMapping，路径为：realtime-total
	//读入传入参数date
	@GetMapping("realtime-total")
	public String getTotal(@RequestParam("date") String date) {
		//获取数据
		Long dauTotal = publisherService.getDauTotal(date);

		//根据返回数据格式，组合数据
		// [{"id":"dau","name":"新增日活","value":1200},{"id":"new_mid","name":"新增设备","value":233} ]
		//List中装有一个个Map，Map里存有键值对
		List<Map<String, java.io.Serializable>> totalList = new ArrayList<>();

		//做一个Map
		//设计返回结果Json
		//{"id":"dau","name":"新增日活","value":1200}
		Map<String, java.io.Serializable> dauMap = new HashMap<String, java.io.Serializable>();
		dauMap.put("id", "dau");
		dauMap.put("name", "新增日活");
		dauMap.put("value", dauTotal);
		totalList.add(dauMap);

		//"id":"new_mid","name":"新增设备","value":233}
		Map<String, java.io.Serializable> newMidMap = new HashMap<String, java.io.Serializable>();
		newMidMap.put("id", "new_mid");
		newMidMap.put("name", "新增设备");
		newMidMap.put("value", 23333);
		totalList.add(newMidMap);

		//"id":"order_amount","name":"新增交易额","value":1000.2 }
		Map<String, java.io.Serializable> orderAmountMap = new HashMap<>();
		orderAmountMap.put("id", "order_amount");
		orderAmountMap.put("name", "新增交易额");
		Double orderAmount = publisherService.getOrderAmount(date);
		orderAmountMap.put("value", orderAmount);
		totalList.add(orderAmountMap);

		//将Map转换成Json结果集返回
		return JSON.toJSONString(totalList);
	}

	@GetMapping("realtime-hour")
	public String getRealtimeHour(@RequestParam("id") String id, @RequestParam("date") String tdate) {
		if ("dau".equals(id)) { // dau的分时统计
			// http://logserver:8099/realtime-hour?id=dau&date=2020-06-09
			Map<String, Long> dauHourCountTodayMap = publisherService.getDauHourCount(tdate);

			String ydate = getYesterdayString(tdate);
			Map<String, Long> dauHourCountYDayMap = publisherService.getDauHourCount(ydate);

			Map<String, Map<String, Long>> dauMap = new HashMap<String, Map<String, Long>>();

			dauMap.put("today", dauHourCountTodayMap);
			dauMap.put("yesterday", dauHourCountYDayMap);

			return JSON.toJSONString(dauMap);
		} else if ("order_amount".equals(id)) { // 订单金额分时统计
			// http://logserver:8099/realtime-hour?id=order_amount&date=2020-06-09
			Map<String, Double> orderHourAmountTodayMap = publisherService.getOrderHourAmount(tdate);

			String yDate = getYesterdayString(tdate);
			Map<String, Double> orderHourAmountYDayMap = publisherService.getOrderHourAmount(yDate);

			Map<String, Map<String, Double>> orderAmountMap = new HashMap<String, Map<String, Double>>();

			orderAmountMap.put("today", orderHourAmountTodayMap);
			orderAmountMap.put("yesterday", orderHourAmountYDayMap);

			return JSON.toJSONString(orderAmountMap);
		}

		return null;

	}

	@GetMapping("sale_detail")
	// http://logserver:8070/sale_detail?date=2020-06-13&&startpage=1&size=5&keyword=手机小米
	public String getSaleDetail(@RequestParam("date") String date, @RequestParam("keyword") String keyword,
								@RequestParam("startpage") int startpage, @RequestParam("size") int size) {

		// 根据参数查询ES
		Map<String, Object> saleDetailMap = publisherService.getSaleDetailFromES(date, keyword, startpage, size);
		Long total = (Long) saleDetailMap.get("total");
		List saleList = (List) saleDetailMap.get("saleList");
		Map genderMap = (Map) saleDetailMap.get("genderMap");
		Map ageMap = (Map) saleDetailMap.get("ageMap");

		// 获取性别占比
		Long maleCount = (Long) genderMap.get("M");
		Long femaleCount = (Long) genderMap.get("F");
		Double maleRatio = Math.round(maleCount * 1000D / total) / 10D;
		Double femaleRatio = Math.round(femaleCount * 1000D / total) / 10D;
		// 构造选项
		ArrayList<Option> genderOptionList = new ArrayList<>();
		genderOptionList.add(new Option("男", maleRatio));
		genderOptionList.add(new Option("女", femaleRatio));

		// 饼图
		StatisticalGraph genderStatisticalGraph = new StatisticalGraph("性别占比", genderOptionList);

		// 年龄段
		Long age_20count = 0L;
		Long age20_30count = 0L;
		Long age30_count = 0L;

		for (Object o : ageMap.entrySet()) {
			Map.Entry entry = (Map.Entry) o;
			String ageString = (String) entry.getKey();
			Long ageCount = (Long) entry.getValue();
			if (Integer.parseInt(ageString) < 20) {
				age_20count += ageCount;
			} else if (Integer.parseInt(ageString) >= 20 && Integer.parseInt(ageString) < 30) {
				age20_30count += ageCount;
			} else {
				age30_count += ageCount;
			}
		}
		// 年龄分段占比
		Double age_20Ratio = Math.round(age_20count * 1000D / total) / 10D;
		Double age20_30Ratio = Math.round(age20_30count * 1000D / total) / 10D;
		Double age30_Ratio = Math.round(age30_count * 1000D / total) / 10D;

		ArrayList<Option> ageOptionList = new ArrayList<>();
		ageOptionList.add(new Option("20岁以下", age_20Ratio));
		ageOptionList.add(new Option("20到30岁", age20_30Ratio));
		ageOptionList.add(new Option("30岁以上", age30_Ratio));

		// 饼图
		StatisticalGraph ageGraph = new StatisticalGraph("年龄段占比", ageOptionList);

		ArrayList<Object> statisticList = new ArrayList<>();
		statisticList.add(genderStatisticalGraph);
		statisticList.add(ageGraph);

		HashMap<Object, Object> resultMap = new HashMap<>();
		resultMap.put("total", total);
		resultMap.put("stat", statisticList);
		resultMap.put("detail", saleList);

		return JSON.toJSONString(resultMap);
	}


	/**
	 * 根据传入日期获取前一天时间
	 *
	 * @param todayString 当天日期
	 * @return
	 */
	private String getYesterdayString(String todayString) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String yesterdayString = null;
		try {
			Date today = dateFormat.parse(todayString);
			Date yesterday = DateUtils.addDays(today, -1);
			yesterdayString = dateFormat.format(yesterday);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return yesterdayString;

	}

}
