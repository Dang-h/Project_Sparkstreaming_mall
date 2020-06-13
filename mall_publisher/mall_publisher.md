
## 接口定义
### 
- 访问路径：

| 总数     | http://publisher:8070/realtime-total?date=2019-02-01         |
| -------- | ------------------------------------------------------------ |
| 分时统计 | http://publisher:8070/realtime-hour?id=order_amount&date=2019-02-01 |

- 要求数据格式：

| | |
|:-------- |:------------------------------------------------------------ |
| 总数     | [{"id":"dau","name":"新增日活","value":1200},  {"id":"new_mid","name":"新增设备","value":233  },  {"id":"order_amount","name":"新增交易额","value":1000.2  }] |
| 分时统计 | {"yesterday":{"11":383,"12":123,"17":88,"19":200  },  "today":{"12":38,"13":1233,"17":123,"19":688  }} |

### 灵活分析
需求详细：

输入参数：  

|日期|查询数据的日期|
|----|----|
|关键字|根据商品名称涉及到的词进行搜索|

返回结果 :

饼图	:
男女比例占比:男 ,女

年龄比例占比:20岁以下，20-30岁 ，30岁以上

购买行为数据明细:包括，用户id,性别年龄，级别，购买的时间，商品价格，订单状态，等信息。


- 传入路径及参数
```html
http://localhost:8070/sale_detail?date=2019-04-01&&startpage=1&size=5&keyword=手机小米
```

- 返回值
```json
{
  "total": 62,
  "stat": [
    {
      "options": [
        {
          "name": "20 岁以下",
          "value": 0.0
        },
        {
          "name": "20 岁到 30 岁 ",
          "value": 25.8
        },
        {
          "name": "30 岁及 30 岁以上",
          "value": 74.2
        }
      ],
      "title": "用户年龄占比 "
    },
    {
      "options": [
        {
          "name": "男",
          "value": 38.7
        },
        {
          "name": "女",
          "value": 61.3
        }
      ],
      "title": "用户性别占 比 "
    }
  ],
  "detail": [
    {
      "user_id": "9",
      "sku_id": "8",
      "user_gender": "M",
      "user_age": 49.0,
      "user_level": " 1",
      "sku_price": 8900.0,
      "sku_name": "Apple iPhone XS Max (A2104) 256GB 深空灰色 移动 联 通 电 信 4G 手 机 双 卡 双 待 ",
      "sku_tm_id": "86",
      "sku_category1_id": "2",
      "sku_category2_id": "13",
      "sku_category3_id": " 61",
      "sku_category1_name": " 手 机 ",
      "sku_category2_name": " 手 机 通 讯 ",
      "sku_category3_name": " 手 机 ",
      "spu_id": "1",
      "sku_num": 6.0,
      "order_count": 2.0,
      "order_amount": 53400.0,
      "dt": "2019-02- 14",
      "es_metadata_id": "wPdM7GgBQMmfy2BJr4YT"
    },
    {
      "user_id": "5",
      "sku_id": "8",
      "user_ge nder": "F",
      "user_age": 36.0,
      "user_level": "4",
      "sku_price": 8900.0,
      "sku_name": "Apple iPhone XS Max (A2104) 256GB 深 空 灰 色 移 动 联 通 电 信 4G 手 机 双 卡 双 待 ",
      "sku_tm_id": "86",
      "sku_category1_id": "2",
      "sku_category2_id": "13",
      "sku_category3_id": " 61",
      "sku_category1_name": " 手 机 ",
      "sku_category2_name": " 手 机 通 讯 ",
      "sku_category3_name": " 手 机 ",
      "spu_id": "1",
      "sku_num": 5.0,
      "order_count": 1.0,
      "order_amount": 44500.0,
      "dt": "2019-02- 14",
      "es_metadata_id": "wvdM7GgBQMmfy2BJr4YT"
    },
    {
      "user_id": "19",
      "sku_id": "8",
      "user_g ender": "F",
      "user_age": 43.0,
      "user_level": "5",
      "sku_price": 8900.0,
      "sku_name": "Apple iPhone XS Max (A2104) 256GB 深空灰色 移动联通电信 4G 手机 双卡双待 ",
      "sku_tm_id": "86",
      "sku_category1_id": "2",
      "sku_category2_id": "13",
      "sku_category3_id": " 61",
      "sku_category1_name": " 手 机 ",
      "sku_category2_name": " 手 机 通 讯 ",
      "sku_category3_name": " 手 机 ",
      "spu_id": "1",
      "sku_num": 7.0,
      "order_count": 2.0,
      "order_amount": 62300.0,
      "dt": "2019-02- 14",
      "es_metadata_id": "xvdM7GgBQMmfy2BJr4YU"
    },
    {
      "user_id": "15",
      "sku_id": "8",
      "user_g ender": "M",
      "user_age": 66.0,
      "user_level": "4",
      "sku_price": 8900.0,
      "sku_name": "Apple iPhone XS Max (A2104) 256GB 深空灰色 移动联通电信 4G 手机 双卡双待 ",
      "sku_tm_id": "86",
      "sku_category1_id": "2",
      "sku_category2_id": "13",
      "sku_category3_id": " 61",
      "sku_category1_name": " 手 机 ",
      "sku_category2_name": " 手 机 通 讯 ",
      "sku_category3_name": " 手 机 ",
      "spu_id": "1",
      "sku_num": 3.0,
      "order_count": 1.0,
      "order_amount": 26700.0,
      "dt": "2019-02- 14",
      "es_metadata_id": "xvdM7GgBQMmfy2BJr4YU"
    }
  ]
}
```
