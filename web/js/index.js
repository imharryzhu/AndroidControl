/**
 * Created by harry on 2017/3/30.
 */

var SERVER_IP = "server_ip";
var SERVER_PORT = "server_port";

var MINICAP_SCALE = "minicap_scale";
var MINICAP_ROTATE = "minicap_rotate";

String.prototype.startWith=function(str){
    var reg=new RegExp("^"+str);
    return reg.test(this);
};

String.prototype.endWith=function(str){
    var reg=new RegExp(str+"$");
    return reg.test(this);
};


function onMinicapConnect(success) {

    util.minicapConnected = success;
    if (success) {
        $("#minicap-statu").css("background-color", "#1de9b6");
    } else {
        $("#minicap-statu").css("background-color", "#e53935");
    }
}

function onMinitouchConnect(success) {
    util.minitouchConnected = success;
    if (success) {
        $("#minitouch-statu").css("background-color", "#1de9b6");
    } else {
        $("#minitouch-statu").css("background-color", "#e53935");
    }
}

function onConfigSave() {
    var ip = $("#server_ip").val();
    var port = $("#server_port").val();
    if (!ip || !port) {
        alert("please input fields");
        return;
    }

    localStorage.setItem(SERVER_IP, ip);
    localStorage.setItem(SERVER_PORT, port);


    alert("save success!");
    $(".config-header").click();
}

// 暂时测试
function onConfigChange() {
    var scale = localStorage.getItem(MINICAP_SCALE);
    var rotate = localStorage.getItem(MINICAP_ROTATE);
    // 重启minicap
    restartMinicap(scale, rotate);
}

function onMinicapScaleChange(a) {
    if (a) {
        var scale = parseFloat($(a).text());
        $('#dropdown-button-scale').text("scale:" + scale);
        // 保存设置到本地
        localStorage.setItem(MINICAP_SCALE, scale);
        setTimeout(function () {
            onConfigChange();
        }, 0);
    }
}

function onMinicapRotateChange(a) {
    if (a) {
        var scale = parseInt($(a).text());
        $('#dropdown-button-rotate').text("rotate:" + scale);
        // 保存设置到本地
        localStorage.setItem(MINICAP_ROTATE, scale);

        setTimeout(function () {
            onConfigChange();
        }, 0);
    }
}

function getUrlParams(name) {
    var reg = new RegExp("(^|\\?|&)"+ name +"=([^&]*)(\\s|&|$)", "i");
    if (reg.test(location.href)) return unescape(RegExp.$2.replace(/\+/g, " "));
    return "";
};

window.onload = function() {
    var ip = localStorage.getItem(SERVER_IP);
    var port = localStorage.getItem(SERVER_PORT);

    $("#server_ip").val(ip);
    $("#server_port").val(port);


    var scale = localStorage.getItem(MINICAP_SCALE);
    var rotate = localStorage.getItem(MINICAP_ROTATE);
    $("#minicap_scale").val(scale);
    $("#minicap_rotate").val(rotate);

    Materialize.updateTextFields();

    // 获取请求参数
    util.key = getUrlParams("key");
    util.ip = getUrlParams("ip");
    util.port = getUrlParams("port");

    // 连接服务器
    connectServer();
}
