package mock.application;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import mock.utils.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class JsonMocker {

	int startupNum = 100000;
	int eventNum = 200000;

	RandomDate logDateUtil = null;


	RanOpt[] osOpts = {new RanOpt("ios", 3), new RanOpt("andriod", 7)};
	RandomOptionGroup<String> osOptionGroup = new RandomOptionGroup(osOpts);
	Date startTime = null;
	Date endTime = null;

	RanOpt[] areaOpts = {
			new RanOpt("beijing", 10),
			new RanOpt("shanghai", 10), new RanOpt("guangdong", 20), new RanOpt("hebei", 5),
			new RanOpt("heilongjiang", 5), new RanOpt("shandong", 5), new RanOpt("tianjin", 5),
			new RanOpt("shan3xi", 5), new RanOpt("shan1xi", 5), new RanOpt("sichuan", 5)
	};
	RandomOptionGroup<String> areaOptionGroup = new RandomOptionGroup(areaOpts);

	String appId = "mall2020";

	RanOpt[] vsOpts = {
			new RanOpt("1.2.0", 50), new RanOpt("1.1.2", 15),
			new RanOpt("1.1.3", 30),
			new RanOpt("1.1.1", 5)
	};

	RandomOptionGroup<String> vsOptionGroup = new RandomOptionGroup(vsOpts);

	//事件操作
    /*
    业务类型
    addFavor:加入收藏
    addComment：加评论
    addCart：加入购物车
    clickItem：点击查看
     */
	RanOpt[] eventOpts = {
			new RanOpt("addFavor", 10), new RanOpt("addComment", 15),
			new RanOpt("addCart", 20), new RanOpt("clickItem", 1),
			new RanOpt("coupon", 45)
	};

	RandomOptionGroup<String> eventOptionGroup = new RandomOptionGroup(eventOpts);

	RanOpt[] channelOpts = {
			new RanOpt("xiaomi", 10), new RanOpt("huawei", 20),
			new RanOpt("wandoujia", 30), new RanOpt("360", 20),
			new RanOpt("tencent", 20), new RanOpt("baidu", 10),
			new RanOpt("website", 10)
	};

	RandomOptionGroup<String> channelOptionGroup = new RandomOptionGroup(channelOpts);

	RanOpt[] quitOpts = {new RanOpt(true, 20), new RanOpt(false, 80)};

	RandomOptionGroup<Boolean> isQuitGroup = new RandomOptionGroup(quitOpts);


	public JsonMocker() {

	}

	/**
	 * 自定义日志生成时间
	 *
	 * @param startTimeString 开始时间
	 * @param endTimeString 结束时间
	 * @param startupNum 启动日志数量
	 * @param eventNum 事件日志数量
	 */
	public JsonMocker(String startTimeString, String endTimeString, int startupNum, int eventNum) {
		try {
			startTime = new SimpleDateFormat("yyyy-MM-dd").parse(startTimeString);
			endTime = new SimpleDateFormat("yyyy-MM-dd").parse(endTimeString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		logDateUtil = new RandomDate(startTime, endTime, startupNum + eventNum);
	}

	/**
	 * 生成事件日志<br>
	 * `type` 	 日志类型,		<br>
	 * `mid` 	 设备唯一 表示,	<br>
	 * `uid` 	 用户标识,		<br>
	 * `os` 	 操作系统,		<br>
	 * `appid` 	 应用id,		<br>
	 * `area` 	 地区 ,			<br>
	 * `evid` 	 事件id,		<br>
	 * `pgid` 	 当前页,		<br>
	 * `npgid` 	 跳转页,		<br>
	 * `itemid`  商品编号,		<br>
	 * `ts` 	 时间,
	 *
	 * @param startLogJson
	 * @return
	 */
	String initEventLog(String startLogJson) {

		JSONObject startLog = JSON.parseObject(startLogJson);
		String mid = startLog.getString("mid");
		String uid = startLog.getString("uid");
		String os = startLog.getString("os");
		String appid = this.appId;
		String area = startLog.getString("area");
		String evid = eventOptionGroup.getRandomOpt().getValue();
		int pgid = new Random().nextInt(50) + 1;
		int npgid = new Random().nextInt(50) + 1;
		int itemid = new Random().nextInt(50);
		//  long ts= logDateUtil.getRandomDate().getTime();

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("type", "event");
		jsonObject.put("mid", mid);
		jsonObject.put("uid", uid);
		jsonObject.put("os", os);
		jsonObject.put("appid", appid);
		jsonObject.put("area", area);
		jsonObject.put("evid", evid);
		jsonObject.put("pgid", pgid);
		jsonObject.put("npgid", npgid);
		jsonObject.put("itemid", itemid);
		return jsonObject.toJSONString();
	}


	/**
	 * 启动日志<br>
	 * `type` string   COMMENT '日志类型',<br>
	 * `mid` string COMMENT '设备唯一标识',<br>
	 * `uid` string COMMENT '用户标识',<br>
	 * `os` string COMMENT '操作系统', <br>
	 * `appId` string COMMENT '应用id', <br>
	 * `vs` string COMMENT '版本号',<br>
	 * `ts` bigint COMMENT '启动时间', <br>
	 * `area` string COMMENT '城市'
	 *
	 * @return
	 */
		String initStartupLog () {

			//mid编号为1-500随机数
			String mid = "mid_" + RandomNum.getRandInt(1, 10);
			String uid = "uid_" + RandomNum.getRandInt(1, 500);//为了出发预警，适当调高
			String os = osOptionGroup.getRandomOpt().getValue();
			String appid = this.appId;
			String area = areaOptionGroup.getRandomOpt().getValue();
			String vs = vsOptionGroup.getRandomOpt().getValue();
//        long ts= logDateUtil.getRandomDate().getTime();
			String ch = os.equals("ios") ? "appstore" : channelOptionGroup.getRandomOpt().getValue();


			JSONObject jsonObject = new JSONObject();
			jsonObject.put("type", "startup");
			jsonObject.put("mid", mid);
			jsonObject.put("uid", uid);
			jsonObject.put("os", os);
			jsonObject.put("appid", appid);
			jsonObject.put("area", area);
			jsonObject.put("ch", ch);
			jsonObject.put("vs", vs);
			return jsonObject.toJSONString();
		}

		public static void genLog () {
			JsonMocker jsonMocker = new JsonMocker();

			//每次生成100W条模拟日志
			jsonMocker.startupNum = 1000000;
			for (int i = 0; i < jsonMocker.startupNum; i++) {
				//初始化一条启动日志JSON串
				String startupLog = jsonMocker.initStartupLog();
				jsonMocker.sendLog(startupLog);
				// 随机附加几个事件日志
				while (!jsonMocker.isQuitGroup.getRandomOpt().getValue()) {
					String eventLog = jsonMocker.initEventLog(startupLog);
					jsonMocker.sendLog(eventLog);
				}
				try {
					Thread.sleep(70);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}


		}

		public void sendLog (String log){
			LogUploader.sendLogStream(log);
		}

		public static void main (String[]args){
			genLog();
		}

	}
