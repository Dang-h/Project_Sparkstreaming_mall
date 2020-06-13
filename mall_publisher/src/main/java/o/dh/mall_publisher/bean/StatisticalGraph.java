package o.dh.mall_publisher.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
/**
 * 统计图
 */
public class StatisticalGraph {

	String title ;

	List<Option> options;
}