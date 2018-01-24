
// let ip = "127.0.0.1"
// let port = 6655

let scale = 0.15

let device_list = new Vue({
    el: '#phone-list',
    data: {
        devices: [],
        name: "zhuhui"
    },
    methods: {
        /**
         * 删除相同服务器中的设备列表
         */
        clearServerDevices: function(server) {
            for(let i = 0; i < this.devices.length;) {
                if (this.devices[i].server == server) {
                    this.devices.splice(i, 1)
                    continue
                }
                i++
            }
        }
    }
})

class NetWork {
    constructor(ip, port) {
        this.ip = ip
        this.port = port
    }

    connect(config) {
        let webSocket = new WebSocket("ws://" + this.ip + ":" + this.port)
        webSocket.onopen = function() {
            config.onopen()
        }
        webSocket.onclose = function() {
            config.onclose()
        }
        webSocket.onmessage = function(data) { 
            config.onmessage(data)
        }
        this.webSocket = webSocket
    }

    request(name, argobj) {
        let ss = name + "://" + (argobj ? JSON.stringify(argobj) : "{}");
        this.webSocket.send(ss);
    }
}

class Device {
    constructor(configObject, server) {
        this.w = configObject.w
        this.h = configObject.h
        this.sn = configObject.sn
        this.server = server
    }
}

class Server {
    constructor(ip, port) {
        this.ip = ip
        this.port = port
        this.connected = false // 连接状态
        this.devices = []
    }

    connect() {
        let net = new NetWork(this.ip, this.port)
        let self = this
        net.connect({
            onopen() {
                self.connected = true
                // 请求获取设备列表
                net.request("M_DEVICES", null)
            },
            onclose() {
                self.connected = false
            },
            onmessage(msg) {
                let data = msg.data
                if (typeof(data) == 'string') {
                    this.ontext(data)
                } else {
                    this.onbinary(data)
                }
            },
            ontext(text) {
                let sp = text.indexOf('://')
                if (sp == -1) {
                    console.log("无效的协议")
                    this.onclose()
                }

                let head = text.substr(0, sp)
                let body = text.substring(sp + 3)

                let func = this[head]
                func.call(this, body)
            },
            onbinary(data) {
            },
            SM_DEVICES(body) {
                let devicesConf = JSON.parse(body);
                self.devices = []
                for (let conf of devicesConf) {
                    let device = new Device(conf, self)
                    self.devices.push(device)
                }
                device_list.clearServerDevices(self)
                self.devices.map(d => {
                    device_list.devices.push(d)
                })
            }
        })
    }
}


let serverList = new Vue({
    data: {
        serverList: [
            /*
             {ip: 'localhost', port: 6655, connected: false}
             */
        ],
    },
    methods: {
        addServer: function (ip, port) {
            let server = new Server(ip, port)
            this.serverList.push(server)
            server.connect()
        }
    }
})

/**
 * js 对象转 url参数表
 */
function object2urlParam(obj) {
    let result = ""
    for (let k in obj) {
        result += k + "=" + obj[k] + "&"
    }
    result = result.slice(0, -1)
    return result
}

var phoneClick = function(sn) {
    let w = 0, h = 0
    for (device of device_list.devices) {
        if (device.sn == sn) {
            w = device.w
            h = device.h
            break;
        }
    }
    window.open("device.html?sn=" + sn + "&w=" + w + "&h=" + h);
}

function phoneClick(sn) {
    
}

window.onload = function() {
    
    /** html 连接方式 */
    // $.ajax({
    //     url: "http://127.0.0.1:6655/devices",
    //     success: function(data) {
    //         device_list.devices = data;
    //     }
    // });

    serverList.addServer('localhost', 6655)
}

/**
 * 添加服务器按钮点击
 */
$('#btn-addserver').on('click', function() {
    let val = $('#server-input').val()
    let ip = val
    let port = 6655
    if (val.indexOf(':') != -1) {
        [ip, port] = val.split(':')
    }
    // 添加数据到服务器列表
    serverList.addServer(ip, port)
})

/**
 * 群控机点击
 */
$('#control-all-phone').on('click', function() {
    
     if (!isBrowser()) {
        (function() {
            const remote = require('electron').remote
            const BrowserWindow = remote.BrowserWindow
            const path = require('path')
            const url = require('url')
            
            let w = 0, h = 0

            // 创建新窗口
            var win = new BrowserWindow({ 
                width: 400, 
                height: 800,
                resizable: false,
                title: "fuck"
                // titleBarStyle: "hidden", // MAC隐藏菜单栏
            })
            
            // 生成serverlist
            let svrlst = []
            for (svr of serverList.serverList) {
                svrlst.push({
                    ip: svr.ip,
                    port:svr.port
                })
            }

            win.loadURL(url.format({
                pathname: path.join(__dirname, 'control.html'),
                protocol: 'file:',
                slashes: true
            }))

            window.localStorage.setItem('svrlst', JSON.stringify(svrlst))

            // win.show()
            win.once('ready-to-show', () => {
                win.show()
            })
            // win.openDevTools();
        })()
    }
})


