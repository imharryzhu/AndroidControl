/**
 * Created by harry on 2017/4/24.
 */


var util = {
    setMinicapScale:function (scale) {
        localStorage.setItem("minicap_scale", scale);
    },
    getMinicapScale:function () {
        return localStorage.getItem("minicap_scale");
    },
    setMinicapRotate:function (rotate) {
        localStorage.setItem("minicap_rotate", rotate);
    },
    getMinicapRotate:function () {
        return localStorage.getItem("minicap_rotate");
    },
    isRotate: false,
    screenW:400,
    screenH:400
};
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
    webSocket = new WebSocket("ws://" + server.getServerIp() + ":" + server.getServerPort());
    webSocket.onopen = function () {
        webSocket.send("wait://" + JSON.stringify({sn:util.sn, key:util.key}));
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
                requestStartMinicap();
                requestStartMinitouch();
            } else if(str.startWith("minicap://") && util.serverConnected) {
                var stat = str.substr(str.indexOf(":") + 3);
                if (stat === "open") {
                    onMinicapOpen();
                    waitingData();
                } else {
                    onMinicapClose();
                }
            } else if (str.startWith("minitouch://") && util.serverConnected) {
                var stat = str.substr(str.indexOf(":") + 3);
                if (stat === "open") {
                    onMinitouchOpen();
                } else {
                    onMinitouchClose();
                }
            } else if(str.startWith("message://")) {
                var msg = str.substr(str.indexOf(":") + 3);
                alert(msg);
            }
        } else {
            waitingData();
            setCanvasImageData(str);
        }
    }
}

window.onload = function() {
    // 获取请求参数
    util.sn = getUrlParams("sn");
    util.key = getUrlParams("key");

    util.ip = getUrlParams("ip") || "127.0.0.1";
    util.port = getUrlParams("port") || "6655";

    util.screenW = getUrlParams("w") || 1080;
    util.screenH = getUrlParams("h") || 1920;

    $("#minicapScaleText").val(util.getMinicapScale());

    var s = util.getMinicapRotate();

    connectServer();
};

document.onkeydown = function (event) {
    var e = event || window.event || arguments.callee.caller.arguments[0];
    console.log(e.keyCode);
    sendKeyEvent(e.keyCode);
};

function waitingData() {
    webSocket.send("waitting://");
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

function onMinitouchOpen() {
    $(canvas).toggleClass("minitouch");
    $.notify("Minitouch Ready!", {
        allow_dismiss: false,
        delay: 2000,
        placement: {
            align: "left"
        },
        type: "success"
    });
}

function onMinitouchClose() {
    $(canvas).toggleClass("minitouch");
    $.notify("Minitouch Close!", {
        allow_dismiss: false,
        delay: 2000,
        placement: {
            align: "left"
        },
        type: "danger"
    });
}

function onMinicapOpen() {
    $.notify("Minicap Ready!", {
        allow_dismiss: false,
        delay: 2000,
        placement: {
            align: "left"
        },
        type: "success"
    });
}

function onMinicapClose() {
    $.notify("Minicap Close!", {
        allow_dismiss: false,
        delay: 2000,
        placement: {
            align: "left"
        },
        type: "danger"
    });
}

function onMinicapScaleChange() {
    var inputScale = $("#minicapScaleText").val();
    if (util.getMinicapScale() === inputScale) {
        return;
    }
    util.setMinicapScale(inputScale);
    requestStartMinicap();
}

function onMinicapRotateChange(rotate) {
    var inputRotate = rotate;
    if (util.getMinicapRotate() ===inputRotate) {
        return;
    }
    util.setMinicapRotate(inputRotate);
    requestStartMinicap();
}

$("#fileToUpload").on("change", function () {
    var file = this.files[0];
    var total = file.size; // 文件总大小
    var curLoaded = 0; // 已读取文件字节数
    var step = 1024 * 1024 ; // 一次读取的最大长度
    var reader = new FileReader();
    reader.onload = function (e) {
        var loaded = e.loaded;

        // 发送
        console.log(loaded);
        sendFileData(reader.result, curLoaded, function() {
            curLoaded += loaded;
            if (curLoaded < total) {
                readBlob(curLoaded);
            } else {
                curLoaded = total;
            }
        });
    };

    function readBlob(start) {
        var blob = file.slice(start, start + step);
        reader.readAsArrayBuffer(blob);
    }

    function str2ab(str) {
        var buf = new ArrayBuffer(str.length*2); // 每个字符占用2个字节
        var bufView = new Uint16Array(buf);
        for (var i=0, strLen=str.length; i<strLen; i++) {
            bufView[i] = str.charCodeAt(i);
        }
        return buf;
    }

    function stringToUint(string) {
        var string = btoa(unescape(encodeURIComponent(string))),
            charList = string.split(''),
            uintArray = [];
        for (var i = 0; i < charList.length; i++) {
            uintArray.push(charList[i].charCodeAt(0));
        }
        return new Uint8Array(uintArray);
    }

    function sendFileData(data, chunkIndex, cb) {
        console.log(data.byteLength);
        var info = {};
        info.type = "file";
        info.name = file.name;
        info.filesize = total;
        info.packagesize = data.byteLength;
        info.offset = chunkIndex;
        var infostr = JSON.stringify(info);
        var infodata = new TextEncoder("utf-8").encode(infostr)
        var len = new Uint16Array(1);
        len[0] = infodata.byteLength;
        var blob = new Blob([len, infodata, data]);
        webSocket.send(blob);
        if (cb) {
            cb();
        }
    }

    readBlob(0);
});
$("#uploadtofile").on("click", function() {
    var path = $("#devicepath").val();
    if (!path) {
        path = $("#devicepath").attr("placeholder");
    }

    var file = $("#fileToUpload").get(0).files[0];
    var filename = file.name;

    webSocket.send("push://"+JSON.stringify({name:filename, path: path}));
});

function requestStartMinicap() {
    var rotate = parseInt(util.getMinicapRotate());
    var scale = parseFloat(util.getMinicapScale());
    util.isRotate = (rotate === 90 || rotate === 270);
    webSocket.send("start://" + JSON.stringify({type:"minicap", config:{'rotate':rotate, 'scale':scale}}));
}

function requestStartMinitouch() {
    webSocket.send("start://" + JSON.stringify({type: "minitouch"}));
}

function textInput(str) {
    webSocket.send("input://" + $("#text-input").val());
}

function sendTouchEvent(minitouchStr) {
    webSocket.send("touch://" + minitouchStr);
}

function sendKeyEvent(keyevent) {
    webSocket.send("keyevent://" + convertAndroidKeyCode(keyevent));
}

function sendDown(argx, argy, isRo) {
    var scalex = util.screenW / canvas.width;
    var scaley = util.screenH / canvas.height;
    var x = argx, y = argy;
    if (isRo) {
        x = (canvas.height - argy) * (canvas.width / canvas.height);
        y = argx * (canvas.height / canvas.width);
    }
    x = Math.round(x * scalex);
    y = Math.round(y * scaley);
    var command = "d 0 " + x + " " + y + " 50\n";
    command += "c\n";
    sendTouchEvent(command);
}

function sendMove(argx, argy, isRo) {
    var scalex = util.screenW / canvas.width;
    var scaley = util.screenH / canvas.height;
    var x = argx, y = argy;
    if (isRo) {
        x = (canvas.height - argy) * (canvas.width / canvas.height);
        y = argx * (canvas.height / canvas.width);
    }
    x = Math.round(x * scalex);
    y = Math.round(y * scaley);

    var command = "m 0 " + x + " " + y + " 50\n";
    command += "c\n";
    sendTouchEvent(command);
}

function sendUp() {
    var command = "u 0\n";
    command += "c\n";
    sendTouchEvent(command);
}

canvas.onmousedown = function (event) {
    if (!util.serverConnected) {
        return;
    }
    isDown = true;
    var pos = getXAndY(canvas, event);
    sendDown(pos.x, pos.y, util.isRotate);
};

canvas.onmousemove = function (event) {
    if (!util.serverConnected || !isDown) {
        return;
    }
    var pos = getXAndY(canvas, event);

    sendMove(pos.x, pos.y, util.isRotate);
};

canvas.onmouseover = function (event) {
    console.log("onmouseover");
};

canvas.onmouseout = function (event) {
    if (!util.serverConnected || !isDown) {
        return;
    }
    isDown = false;
    sendUp();
};

canvas.onmouseup = function (event) {
    if (!util.serverConnected || !isDown) {
        return;
    }
    isDown = false;
    sendUp();
};
