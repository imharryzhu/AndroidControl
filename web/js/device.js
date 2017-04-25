/**
 * Created by harry on 2017/4/24.
 */

var util = {};
var webSocket = null;

String.prototype.startWith=function(str){
    var reg=new RegExp("^"+str);
    return reg.test(this);
};

String.prototype.endWith=function(str){
    var reg=new RegExp(str+"$");
    return reg.test(this);
};

var canvas = document.getElementById("phone-screen");
var g = canvas.getContext('2d');

function getUrlParams(name) {
    var reg = new RegExp("(^|\\?|&)"+ name +"=([^&]*)(\\s|&|$)", "i");
    if (reg.test(location.href)) return unescape(RegExp.$2.replace(/\+/g, " "));
    return "";
}

function connectServer() {
    var key = util.key;
    var ip = util.ip;
    var port = util.port;
    webSocket = new WebSocket("ws://" + ip + ":" + port);
    webSocket.onopen = function () {
        webSocket.send("wait://" + getUrlParams("sn"));
    };
    webSocket.onclose = function () {
        util.serverConnected = false;
    };
    webSocket.onmessage = function(msg) {
        var str = msg.data;
        if (typeof(str) === "string") {
            if (str.startWith("open://")) {
                var checkKey = str.substr(str.indexOf(":") + 3);
                util.serverConnected = true;
            } else if(str === "minicap" && util.serverConnected) {
                waitingData();
            } else if (str === "minitouch" && util.serverConnected) {
                onMinitouchReady();
            }
        } else {
            waitingData();
            setCanvasImageData(str);
        }
    }
}

window.onload = function() {
    // 获取请求参数
    util.key = getUrlParams("sn");

    util.ip = "127.0.0.1";
    util.port = 6655;

    connectServer();
};

function waitingData() {
    webSocket.send("waiting");
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

function onMinitouchReady() {
    $(canvas).toggleClass("minitouch");
}

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

