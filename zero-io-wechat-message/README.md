# 微信小程序发送订阅消息工具<br>zero-io-wechat-message

---
> 注意！
> 该工具使用微信官方api，官方修改了api后可能会失效。
> 在 src/main/resource/application.yml 下存放着微信官方发送订阅消息的api路径。
> 别的项目引用该工具后会以别的项目中的application.yml为优先，所以请复制本工具中的yml中的内容添加到您的项目yml中即可

[微信官方说明文档](https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/subscribe-message.html#%E8%AE%A2%E9%98%85%E6%B6%88%E6%81%AF%E8%AF%AD%E9%9F%B3%E6%8F%90%E9%86%92)

---
## 配置说明

### 必需配置

在 `application.yml` 或 `application-dev.yml` 中添加以下配置：

```yaml
wechat:
  # 获取 access_token 的 API 地址
  getAccessToken:
    url: "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={appid}&secret={secret}"
  # 发送订阅消息的 API 地址
  sendMessage:
    url: "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token={access_token}"
```

### 微信小程序凭证

调用时需要传入以下参数（从微信公众平台获取）：

| 参数 | 说明 | 获取位置 |
|------|------|----------|
| `appid` | 小程序 AppID | 微信公众平台 - 开发 - 开发管理 - 开发设置 |
| `appSecret` | 小程序 AppSecret | 同上（需要管理员扫码确认） |

### Redis 配置（用于缓存 access_token）

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
```

> access_token 会被缓存到 Redis，有效期 7000 秒，避免频繁调用微信 API

---
## 使用说明


<br>

### 提供的操作对象：
`WeChatMiniProgramMessage`

<br>

### 提供的方法：
- `getAccessToken()` 获取官方凭证，已在`sendMessage()`中调用
- `sendMessage()` 发送微信小程序订阅消息,该方法提供了两组入参，
一个是提供全参数配置，更加灵活。一个是只需要必要的参数，其余的均使用默认配置

<br>

### sendMessage() 所需参数：

| 参数 | 说明 | 是否必填 | 默认值 | 示例 |
|------|------|----------|--------|------|
| `appAppid` | 小程序 AppID | 必填 | - | `"wx1234567890abcdef"` |
| `appSecret` | 小程序 AppSecret | 必填 | - | `"abc123def456..."` |
| `openid` | 接收消息的用户 openid | 必填 | - | `"oXYZ123..."` |
| `templateId` | 订阅消息模板 ID | 必填 | - | `"ABC123..."` |
| `data` | 模板内容 JSON | 必填 | - | `{"thing1": {"value": "订单内容"}}` |
| `page` | 跳转页面路径 | 可选 | 空 | `"pages/index/index"` |
| `miniProgramState` | 小程序类型 | 可选 | `formal` | `formal`(正式版) / `trial`(体验版) / `developer`(开发版) |
| `lang` | 进入小程序查看的语言类型 | 可选 | `zh_CN` | `zh_CN`(简体中文) / `en_US`(英文) / `zh_HK`(繁体中文) / `zh_TW`(繁体中文) |

> **注意事项：**
> - 需要在微信公众平台配置订阅消息模板
> - 用户需要先授权接收订阅消息
> - *参数获取方法请查询：[微信官方说明文档](https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/subscribe-message.html#%E8%AE%A2%E9%98%85%E6%B6%88%E6%81%AF%E8%AF%AD%E9%9F%B3%E6%8F%90%E9%86%92)*

<br>

### 例子：
现在以微信中的一次性订阅模版："订单进度提醒" 作为例子
获取微信通知模版位置：登录微信公众平台 - 订阅消息。

#### "订单进度提醒"要求的data格式:
开始时间 {{time3.DATA}}  
订单进度 {{thing4.DATA}}  
订单编号 {{character_string5.DATA}}  
产品名称 {{thing10.DATA}}  
```json
"data": {
      "name01": {
          "value": "某某"
      },
      "amount01": {
          "value": "￥100"
      },
      "thing01": {
          "value": "广州至北京"
      } ,
      "date01": {
          "value": "2018-01-01"
      }
  }
}
```

<br>

#### 代码例子：
模拟数据发送通知，正式使用时请使用真实数据
```java
    // 将操作对象注入
    @Resource
    WeChatMiniProgramMessage weChatMiniProgramMessage;

    // 调用 sendMessage()
    public void should_retuanHttpEntity_when_sendMessage() {
        
    // 封装消息内容 data
    JSONObject messageContent = new JSONObject();
    // 获取当前时间
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String dateTime = LocalDateTime.now().format(dateTimeFormatter);
    JSONObject time3 = new JSONObject();
    time3.put("value",dateTime);
    messageContent.put("time3",time3);
    // 订单进度
    JSONObject thing4 = new JSONObject();
    thing4.put("value","待确认");
    messageContent.put("thing4",thing4);
    // 订单编号
    JSONObject character_string5 = new JSONObject();
    character_string5.put("value","1111111111111");
    messageContent.put("character_string5",character_string5);
    // 产品名称
    JSONObject thing10 = new JSONObject();
    thing10.put("value","团购商品");
    messageContent.put("thing10",thing10);

    weChatMiniProgramMessage.sendMessage(appid,appSecret,openid,templateId,messageContent);
    }
```
