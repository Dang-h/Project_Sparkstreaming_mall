package mall_publisher.mapper;

import java.util.List;
import java.util.Map;

//MyBatis查询数据库映射成Java对象；通过SQL定义文件自动实现接口
public interface DAUMapper {
	/**按照日期查询总数*/
	public Long getDauTotal(String date);

	/**按照日期查询分时总数*/
	public List<Map> getDauHourCount(String date);
}
