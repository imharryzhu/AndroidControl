# AndroidControl
这是一款实时控制Android手机的工具

**欢迎提交`issue`，如果它对你有帮助，点个`Star`哟**

## What?

* 查看手机的实时屏幕
* 查看参数设置
  * 设置屏幕的缩放比例
  * 设置屏幕的旋转角度
* 操作手机
  * 单点触摸
  * 滑动
  * 键盘输入
  * 文字输入
* 上传文件到手机

![demo](docs/demo.gif)

## Run demo

1.  使用Gradle构建项目： `gradle jar`
2.  进入到生成的目录：`cd build/libs`
3.  运行服务器（这里使用本地服务器）：`java -jar AndroidControl.jar localserver 6655`
4.  打开`web/index.html`
5.  点击网页中的`设置`，配置IP和端口：`127.0.0.1` `6655`

注意：

* 测试的手机需要打开Debug模式
* 某些手机需要特别开启虚拟按键的权限

## For Developers

本项目的核心是: `LocalServer` `RemoteServer` `RemoteClient`，`web`文件夹内只是DEMO

你可以根据`docs/接口说明`封装一个操作的界面

## Features

* 帧数限制，用户可自定义帧数
* 群控功能，可以控制多台设备
* 模拟实体按键
* 操作映射，对一台机器的操作映射到多台机器
* 操作录制、运行(使用脚本语言，js|lua|python语言的支持，可能只实现js)
* 服务器支持
* 流压缩(h264)

### Thanks
* `minicap` [https://github.com/openstf/minicap](https://github.com/openstf/minicap)
* `minitouch` [https://github.com/openstf/minitouch](https://github.com/openstf/minitouch)