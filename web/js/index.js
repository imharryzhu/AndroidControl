/**
 * Created by harry on 2017/4/21.
 */

window.onload = function() {
    requestDevices();
};

window.onresize = function () {
  var dc = $(".device-container");
  dc.css("height", dc.width() * 2);
};

/**
 * 请求服务器获取
 *
 * return json
 */
function requestDevices() {
    var ws = new WebSocket("ws://127.0.0.1:6655");
    ws.onopen = function (p1) { ws.send("devices"); };
    ws.onmessage = function (d) {
        fillDevices(JSON.parse(d.data));
        window.onresize();
        ws.close();
    };
}

/**
 * 截图
 * @param deviceInfo
 *
 * return Blob;
 */
function takeScreenShot(sn, args) {
    var ws = new WebSocket("ws://127.0.0.1:6655");
    ws.onopen = function (p1) { ws.send("shot://" + sn); };
    ws.onmessage = function (d) {
        args.success(d.data);
        ws.close();
    };
}

function onDeviceClick(sn) {
    window.open("device.html?sn=" + sn);
};

/**
 *  填充设备列表
 * @param array
 */
var fillDevices = function(array) {

    for (var i in array) {
        var deviceInfo = array[i];
        var innerCanvas = "";
        var html = "<div class='col-lg-2 col-md-3 col-sm-3 col-xs-6  device-container'>" +
            "<img class='' src='" + "http://127.0.0.1:6655/shot/" + deviceInfo.sn + "' onclick='onDeviceClick(\"" + deviceInfo.sn + "\")' />" +
            "<div class='device-detail'><sapn>" + deviceInfo.sn + "</sapn></div>" +
            "</div>";
        var dom = $(html);
        $(".container-fluid .row").append(dom);
    }
};


