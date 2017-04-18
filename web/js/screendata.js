/**
 * Created by harry on 2017/3/30.
 */
var webSocket = null;

var minicapts = 0;
var totalFPS = 0;
var totalImageCount = 0;
var totalData = 0;

var canvas = document.getElementById("phone-screen");
var g = canvas.getContext('2d');

var util = {
    ip:null,
    port:null,
    key:null,
    serverConnected:false,
    minicapConnected:false,
    minitouchConnected:false
};

Materialize.toast('Waiting For Client', 3000, 'rounded');

// 向服务器发送消息，表示想要接收数据
function waitingData() {
    webSocket.send("waiting");
}

/**
 *  连接服务器
 */
function connectServer() {
    var key = util.key;
    var ip = util.ip;
    var port = util.port;
    webSocket = new WebSocket("ws://" + ip + ":" + port);
    webSocket.onopen = function () {
        webSocket.send("wait://" + key);
    };
    webSocket.onclose = function () {
        onMinicapConnect(false);
        onMinitouchConnect(false);
        util.serverConnected = false;
        Materialize.toast('Lost Connection!!', 3000, 'rounded');
    };
    webSocket.onmessage = function(msg) {
        var str = msg.data;
        if (typeof(str) === "string") {
            if (str.startWith("open://")) {
                var checkKey = str.substr(str.indexOf(":") + 3);
                if (checkKey === key) {
                    util.serverConnected = true;
                    Materialize.toast('Client is OK, Waiting Minicap/touch Service', 3000, 'rounded');
                }
            } else if(str === "minicap" && util.serverConnected) {
                onMinicapConnect(true);
                waitingData();
                Materialize.toast('Minicap Successe', 3000, 'rounded');
            } else if (str === "minitouch" && util.serverConnected) {
                onMinitouchConnect(true);
                Materialize.toast('Minitouch Successe', 3000, 'rounded');
            }
        } else {
            waitingData();
            setCanvasImageData(str);
        }
    }
}

function  restartMinicap(scale, rotate) {
    onMinicapConnect(false);
    // 发送命令
    webSocket.send("config://" + scale + ":" + rotate);

}

function setCanvasImageData(data) {
    var blob = new Blob([data], {type: 'image/jpeg'});
    var URL = window.URL || window.webkitURL;
    var img = new Image();
    img.onload = function () {
        canvas.width = img.width;
        canvas.height = img.height;
        g.drawImage(img, 0, 0);
        img.onLoad = null;
        img = null;
        u = null;
        blob = null;
    };
    var u = URL.createObjectURL(blob);
    img.src = u;
    var nowTS = new Date().getTime();
    var usedTime = nowTS - minicapts;
    usedTime = usedTime === 0 ? 1 : usedTime;
    var fps = Math.round(1000.0 / usedTime);
    totalData += data.size;
    totalFPS += fps;
    totalImageCount += 1;

    var text = "fps: " + fps + "  avg:" + Math.round(totalFPS / totalImageCount) + " speed: " + Math.round(totalData / totalImageCount / 1024.0) + "KB/s";
    // fpsLabel.innerText = text;
    $("#minicap-fps").text(text);
    minicapts = nowTS;
}

// 获取鼠标在html中的绝对位置
function mouseCoords(event){
    if(event.pageX || event.pageY){
        return {x:event.pageX, y:event.pageY};
    }
    return{
        x:event.clientX + document.body.scrollLeft - document.body.clientLeft,
        y:event.clientY + document.body.scrollTop - document.body.clientTop
    };
}
// 获取鼠标在控件的相对位置
function getXAndY(control, event){
    //鼠标点击的绝对位置
    Ev= event || window.event;
    var mousePos = mouseCoords(event);
    var x = mousePos.x;
    var y = mousePos.y;
    //alert("鼠标点击的绝对位置坐标："+x+","+y);

    //获取div在body中的绝对位置
    var x1 = control.offsetLeft;
    var y1 = control.offsetTop;

    //鼠标点击位置相对于div的坐标
    var x2 = x - x1;
    var y2 = y - y1;
    return {x:x2,y:y2};
}

var isDown = false;

canvas.onmousedown = function (event) {
    if (!util.serverConnected) {
        return;
    }
    isDown = true;

    var scalex = 1080.0 / canvas.width;
    var scaley = 1920.0 / canvas.height;
    var pos = getXAndY(canvas, event);
    var x = Math.round(pos.x * scalex);
    var y = Math.round(pos.y * scaley);

    var command = "d 0 " + x + " " + y + " 50\n";
    command += "c\n";
    webSocket.send(command);
};

canvas.onmousemove = function (event) {

    if (!util.serverConnected || !isDown) {
        return;
    }
    var scalex = 1080.0 / canvas.width;
    var scaley = 1920.0 / canvas.height;
    var pos = getXAndY(canvas, event);
    var x = Math.round(pos.x * scalex);
    var y = Math.round(pos.y * scaley);

    var command = "m 0 " + x + " " + y + " 50\n";
    command += "c\n";
    webSocket.send(command);
};

canvas.onmouseover = function (event) {
    console.log("onmouseover");
};

canvas.onmouseout = function (event) {
    if (!util.serverConnected || !isDown) {
        return;
    }
    isDown = false;
    var scalex = 1080.0 / canvas.width;
    var scaley = 1920.0 / canvas.height;
    var pos = getXAndY(canvas, event);
    var x = Math.round(pos.x * scalex);
    var y = Math.round(pos.y * scaley);

    var command = "u 0\n";
    command += "c\n";
    webSocket.send(command);
};

canvas.onmouseup = function (event) {
    if (!util.serverConnected || !isDown) {
        return;
    }
    isDown = false;
    var scalex = 1080.0 / canvas.width;
    var scaley = 1920.0 / canvas.height;
    var pos = getXAndY(canvas, event);
    var x = Math.round(pos.x * scalex);
    var y = Math.round(pos.y * scaley);

    var command = "u 0\n";
    command += "c\n";
    webSocket.send(command);
};