/**
 * Created by harry on 2017/4/26.
 */

function convertAndroidKeyCode(keycode) {
    var map = {
        //backspace/del
        8:67,
        // sapce
        32:62,
        // shift
        16:59,
        // enter
        13:66,
        // 0-9
        48:7,
        49:8,
        50:9,
        51:10,
        52:11,
        53:12,
        54:13,
        55:14,
        56:15,
        57:16,
        96:7,
        97:8,
        98:9,
        99:10,
        100:11,
        101:12,
        102:13,
        103:14,
        104:15,
        105:16,
        // a-z
        65:29,
        66:30,
        67:31,
        68:32,
        69:33,
        70:34,
        71:35,
        72:36,
        73:37,
        74:38,
        75:39,
        76:40,
        77:41,
        78:42,
        79:43,
        80:44,
        81:45,
        82:46,
        83:47,
        84:48,
        85:49,
        86:50,
        87:51,
        88:52,
        89:53,
        90:54
    };
    console.log("keycode" + keycode);
    if (map[keycode]) {
        return map[keycode];
    }else {
        return keycode;
    }
}
