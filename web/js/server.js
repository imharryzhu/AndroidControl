var server = {
    getServerIp:function() {
        return localStorage.getItem("server-ip") || "127.0.0.1";
    },
    getServerPort:function () {
        return localStorage.getItem("server-port") || "6655";
    },
    setServerIp:function (ip) {
        localStorage.setItem("server-ip", ip);
    },
    setServerPort:function (port) {
        localStorage.setItem("server-port", port);
    }
};