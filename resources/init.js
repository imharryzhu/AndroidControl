/**
 * Common functions
 */

/**
 * Sleep In Java Thread
 * @param millisecond
 */
function sleep(millisecond) {
    java.lang.Thread.sleep(millisecond);
}

/**
 * 获取已安装包名
 * @param device
 * @returns {*}
 */
function getPKGList(device) {
    return device.executeShellAndGetString("pm list packages");
}

var device = com.yeetor.engine.EngineDevice.getDevice("3HX5T16C17040892");


function 画圆() {
    device.touchDown(540, 0);
    var r = 450;
    for (var i = 0; i < 360; i++) {
        var y = Math.cos(i) * r;
        var x = Math.sin(i) * r;

        x += 540;
        y += 960;

        print(x + ":" + y);

        device.touchMove(x, y);
    }
    device.touchUp();
}
画圆();



