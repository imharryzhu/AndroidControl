|  版本   |    修订时间     | 修订内容 |  作者   |
| :---: | :---------: | :--: | :---: |
| 1.0.0 | 2017年07月14日 |  创建  | harry |
|       |             |      |       |
|       |             |      |       |

# 介绍

协议分为`文本协议`和`二进制协议`

文本协议：

* 属于二进制协议的一种，所有文本协议均可用二进制协议发送
* 文本协议只能用来传输文本信息

二进制协议：

* 可以传输任何信息，包括文本信息

# 文本协议 TextProtocol

文本协议格式：

`MessageHeader://Body`

MessageHeader：由普通字符串组成，标识该消息的类型

Body：由字符串组成，可以使普通字符串可也以是JSON数据，具体看消息类型

### HTPP接口

由于服务端是基于TCP的，所以接受HTTP协议传输

| URL                 | Return                              | Description      |
| ------------------- | ----------------------------------- | ---------------- |
| devices             | [{w:1920, h: 1080, sn:'xxxx'}, ...] | 返回已连接设备列表的JSON格式 |
| shot/{SerialNumber} | 手机截图的二进制内容                          | 获取手机当前屏幕截图       |

### C->S

| Type       | Data                                     | Description                |
| ---------- | ---------------------------------------- | -------------------------- |
| M_WAIT     | JSON: {sn:SerialNumber[, key: [P2PKey]()]} | 请求连接手机，客户端已准备接受消息          |
| M_START    | JSON: {type: xx, config:object} tpye:'cap'\|'event' config: [CapConfig]() | 通知服务端启动屏幕\|事件监听服务          |
| M_WAITTING | None                                     | 等待服务端传来新的图像数据              |
| M_TOUCH    | minitouch格式的输入数据                         | 触摸事件                       |
| M_KEYEVENT | 数值类型，关于[KeyEvent]()                      | 事件输入（目前为adb操作，速度较慢）        |
| M_INPUT    | 字符串                                      | 字符串输入（目前为adb操作，速度较慢切不支持中文） |
| M_PUSH     | None                                     | 保留                         |
| M_SHOT     | JSON: {sn:SerialNumber}                  | 请求目标设备的屏幕截图                |
| M_DEVICES  | None                                     | 请求已连接设备列表的JSON格式           |

### S->C

| Type             | Data                                     | Description                    |
| ---------------- | ---------------------------------------- | ------------------------------ |
| SM_OPENED        | None                                     | 接收到M_WAIT后，服务端与客户端连接建立成功后发送改消息 |
| SM_SERVICE_STATE | JSON: {type: xx, stat: "open"\|"close"} tpye:'cap'\|'event' | 服务的状态改变时，通知客户端                 |
| SM_MESSAGE       | text                                     | 服务端主动给客户端发送的文本消息               |
| SM_DISCONNECT    | None                                     | 服务端主动关闭连接之前会发送                 |
| SM_DEVICES       | JSON：[{w:1920, h: 1080, sn:'xxxx'}, ...] | 返回已连接设备列表的JSON格式               |
| SM_SHOT          | 详情请看BinaryProtocol                       |                                |
| SM_JPG           | 详情请看BinaryProtocol                       |                                |



# 二进制协议 BinaryProtocol

二进制协议格式：

| MessageHeader | MessageBody |
| :-----------: | :---------: |
|     short     |      *      |
|     2byte     |      *      |

### C->S

### S->C

* SM_SHOT `0x0010`

  客户端发送M_SHOT后，服务端获取对应设备的屏幕截图，而后将截图返回给客户端

  | Header               | CType  | Bytes | Description |
  | -------------------- | ------ | ----- | ----------- |
  | SM_SHOT              | Int16  | 2     | 消息头         |
  | SERIAL_NUMBER_LENGTH | UInt16 | 2     | 该图像所对应的设备序号 |
  | DATA_LENGTH          | UInt32 | 4     | 图像数据长度      |
  | SERIAL_NUMBER_DATA   | char[] | ...   | 序列号         |
  | DATA                 | char[] | ...   | 图像数据        |

* SM_JPG `0x0011`

  接受SM_SHOT消息，说明客户端已经打开了与设备的图像通路，所以该消息绝对只会和对应设备相关

  | Header      | CType  | Bytes | Description |
  | ----------- | ------ | ----- | ----------- |
  | SM_JPG      | Int16  | 2     | 消息头         |
  | DATA_LENGTH | UInt32 | 4     | 图像数据长度      |
  | DATA        | char[] | ...   | 图像数据        |