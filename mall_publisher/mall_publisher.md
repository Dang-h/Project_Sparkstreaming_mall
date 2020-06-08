# 数据发布模块
### 日活查询接口
- 访问路径

|||
|:---|:---|
|总数|http://publisher:8070/realtime-total?date=2020-02-01|
|分时统计|http://publisher:8070/realtime-hour?id=dau&date=2020-02-01|
- 数据格式

|||
|:---|:---|
|总数|[{"id":"dau","name":"新增日活","value":1200},<br>{"id":"new_mid","name":"新增设备","value":233} ]|
|分时统计|{"yesterday":{"11":383,"12":123,"17":88,"19":200 },<br>"today":{"12":38,"13":1233,"17":123,"19":688 }}|

### 步骤
- 分层，分4层：controller(控制 发布Web接口或页面)、service(业务 处理业务逻辑)、dao(数据 查询后台数据)、mapper(myBatis查询数据库 映射成Java对象)
- 查询数据库（mapper），先定义数据查询的接口，接着编写编写配置文件（`xxxMapper.xml`)；在`MallPublisherApplication`中加`MapperScan`指定MyBatis用到的mapper
- 在`service`定义业务接口，实现接口
- 在`controller`定义接口
