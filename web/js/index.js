/**
 * Created by harry on 2017/4/21.
 */

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
    var ws = new WebSocket("ws://" + server.getServerIp() + ":" + server.getServerPort());
    ws.onopen = function (p1) { ws.send("devices://"); };
    ws.onmessage = function (d) {
        fillDevices(JSON.parse(d.data));
        window.onresize();
        ws.close();
    };
}

function onDeviceClick(sn, w, h) {
    window.open("device.html?sn=" + sn + "&w=" + w + "&h=" + h);
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
            "<img class='' src='" + "http://" + server.getServerIp() + ":" + server.getServerPort() + "/shot/" + deviceInfo.sn + "' onclick='onDeviceClick(\"" + deviceInfo.sn + "\", " + deviceInfo.w + ","  + deviceInfo.h + ")' />" +
            "<div class='device-detail'><sapn>" + deviceInfo.sn + "</sapn><br><span>@" + deviceInfo.w + "x" + deviceInfo.h + "</span></div>" +
            "</div>";
        var dom = $(html);
        $(".container-fluid .row").append(dom);
    }
};


function onServerConfig() {
    var ip = $("#server-ip").val();
    var port = $("#server-port").val();

    server.setServerIp(ip);
    server.setServerPort(port);
    $("#settingModal").modal('toggle');
}

window.onload = function () {
    $("#settingModal").on('show.bs.modal', function (e) {
        $("#server-ip").val(server.getServerIp());
        $("#server-port").val(server.getServerPort());
    });

    requestDevices();
};



