package mock.utils;

/**
 *
 * @param <T>
 */
public class RanOpt<T>{
	T value ;
	int weight;

	/**
	 *
	 * @param value 业务类型
	 * @param weight 出现概率
	 */
	public RanOpt ( T value, int weight ){
		this.value=value ;
		this.weight=weight;
	}

	public T getValue() {
		return value;
	}

	public int getWeight() {
		return weight;
	}
}