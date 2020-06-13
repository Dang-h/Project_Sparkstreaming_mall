package o.dh.mall_publisher.service.impl;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.TermsAggregation;
import o.dh.mall_publisher.bean.OrderHourAmount;
import o.dh.mall_publisher.mapper.DAUMapper;
import o.dh.mall_publisher.mapper.OrderMapper;
import o.dh.mall_publisher.service.PublisherService;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PublisherServiceImpl implements PublisherService {

	@Autowired
	DAUMapper dauMapper;

	@Autowired
	OrderMapper orderMapper;

	@Autowired
	JestClient jestClient;

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

	@Override
	public Map<String, Object> getSaleDetailFromES(String date, String keyword, int pageNo, int pageSize) {

		/*
		GET mall_sale_detail/_search
		{
		  "query": {
		    "bool": {
		      "filter": {
		        "term": {# 按照字段搜索
		          "dt":"2020-06-13"
		        }
		      },
		      "must":{
		        "match":{
		          "sku_name":{
		            "query":"小米",
		            "operator":"and"
		          }
		        }
		      }
		    }
		  },
		  "aggs": {
		    "groupBy_gender": {
		      "terms": {# groupBy,送Count
		        "field": "user_gender",
		        "size": 2 #有几种可能
		      }
		    }
		  },
		  "from": 0, #从第几行开始（页码转行号：行号= (页码 -1) * 页行数）
		  "size": 20 #每页显示几行
		}
		 */

		// 创建searcherSource对象
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// 构造过滤匹配条件
		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
		boolQueryBuilder.filter(new TermQueryBuilder("dt", date));
		boolQueryBuilder.must(new MatchQueryBuilder("sku_name", keyword).operator(MatchQueryBuilder.Operator.AND));
		searchSourceBuilder.query(boolQueryBuilder);

		// 聚合
		TermsBuilder genderAgg = AggregationBuilders.terms("groupBy_gender").field("user_gender").size(2);
		TermsBuilder ageAgg = AggregationBuilders.terms("groupBy_age").field("user_age").size(100);
		searchSourceBuilder.aggregation(genderAgg);
		searchSourceBuilder.aggregation(ageAgg);

		// 分页
		searchSourceBuilder.from((pageNo - 1) * pageSize);
		searchSourceBuilder.size(pageSize);

		// 构造查询语句
		Search search = new Search.Builder(searchSourceBuilder.toString()).build();

		HashMap<String, Object> resultMap = new HashMap<>();
		try {
			SearchResult searchResult = jestClient.execute(search);

			// 获取总数"hits":{"total":14}
			resultMap.put("total", searchResult.getTotal());

			// 获取_source信息
			List<SearchResult.Hit<Map, Void>> hits = searchResult.getHits(Map.class);
			ArrayList<Map> saleList = new ArrayList<>();
			for (SearchResult.Hit<Map, Void> hit : hits) {
				saleList.add(hit.source);
			}
			// 明细数据
			resultMap.put("saleList", saleList);

			// 获取聚合数据
			// 性别聚合
			Map<String, Long> genderMap = new HashMap<>();
			List<TermsAggregation.Entry> genderBuckets = searchResult.getAggregations().getTermsAggregation("groupBy_gender").getBuckets();
			for (TermsAggregation.Entry bucket : genderBuckets) {
				genderMap.put(bucket.getKey(), bucket.getCount());
			}
			resultMap.put("genderMap", genderAgg);

			// 年龄聚合
			List<TermsAggregation.Entry> ageBuckets = searchResult.getAggregations().getTermsAggregation("groupBy_add").getBuckets();
			HashMap<Object, Object> ageMap = new HashMap<>();
			for (TermsAggregation.Entry ageBucket : ageBuckets) {
				ageMap.put(ageBucket.getKey(), ageBucket.getCount());
			}
			resultMap.put("ageMap", ageMap);


		} catch (IOException e) {
			e.printStackTrace();
		}

		return resultMap;
	}


}
