import router from '@system.router'
import GESTURES  from '../../common/utils/store_cought_gesture.js';
import Storage from '../../common/store/storage.js';



// abilityType: 0-Ability; 1-Internal Ability
const ABILITY_TYPE_EXTERNAL = 0;
//const ABILITY_TYPE_INTERNAL = 1;
// syncOption(Optional, default sync): 0-Sync; 1-Async
const ACTION_SYNC = 0;
//const ACTION_ASYNC = 1;
const ACTION_MESSAGE_CODE_SUBSCRIBE = 1005;     // 订阅PA
const ACTION_MESSAGE_CODE_UNSUBSCRIBE = 1006;   // 取消订阅PA
const ACTION_MESSAGE_CODE_SERVER = 1007;        // 把数据传给服务器
const DISPLAY_GESTURES_NUMBER = 10;             // 设置显示手势的数量
const VIDEOHEIGHT = 200;
const VIDEOWIDTH = '100%';
const prefix = "HISTORY";       // 历史手势的前缀

const DISCONNECTED = {
    color: '#C82033',
    context: '未连接服务器...',
};
const CONNECTED = {
    color: '#72B340',
    context: '已连接服务器...',
};

const CONNECTING = {
    color : '#384C9D',
    context: '正在连接服务器...',
};

//const UNDEFINED = {
//    color : '#EF7D3C',
//};


export default {
    data: {
        title: "实时查看",
        color : DISCONNECTED.color,
        context: DISCONNECTED.context,
        img_setting:"/common/images/设置.png",
        img_poses:"/common/images/可识别手势.png",
        img_video:"/common/images/实时查看.png",

        focusImg: "",

//        videoSrc: "rtsp://192.168.31.2:554/h264",
//        videoSrc: "http://ivi.bupt.edu.cn/hls/cctv1hd.m3u8",
        videoSrc: "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov",
//        videoSrc: "rtsp://rtsp-v3-spbtv.msk.spbtv.com/spbtv_v3_1/214_110.sdp",

        message: "",
        gestures: [],
        tabsWidth: VIDEOWIDTH,
        tabsHeight: VIDEOHEIGHT,
        curImgWidth: VIDEOWIDTH,
        curImgHeight: VIDEOHEIGHT,
    },

    onInit() {
        console.log("Initing...");
        this.gestures = GESTURES;
        this.getSUBSCRIBE();
    },
    onShow(){
        const el = this.$refs.canvas1;
        const ctx = el.getContext('2d');
        ctx.fillRect(0,0, 1000, 1000);
    },
    onDestroy(){
        // 将收到的图片存进历史里面
        let history_count = GESTURES.length;
        Storage.set(prefix + "history_count", history_count);
        for (let i = 0; i < history_count; ++i) {
            // 将历史存起来
            Storage.set(prefix + i, GESTURES[i]);
        }
    },


    // =============== ================= =============
    videoOnline() {
        console.info("实时查看...");
        // 实时查看
        // socket.call(1, {type:1, deviceSerialId: this.$app.$def.store.state.deviceInfo.deviceSerialId, switch: 'on'});
        var action = {
            bundleName: "com.linzhenbin.xjxxxjjjkkkyyyyttt",
            abilityName: "com.linzhenbin.xjxxxjjjkkkyyyyttt.player.VideoAbility",
            deviceType: ABILITY_TYPE_EXTERNAL,
            data: {
                url: this.videoSrc,
            },
            flag: flag.fullToFull,
        };
        console.info("进入lookingClick...")
        StartAbility(action);
    },
    changeToSetting(){
        console.info("Change to Setting...");
        router.push({
            uri:"pages/setting/setting"
        });
    },
    changeToPoses(){
        console.info("Change to Poses...");
        router.push({
            uri:"pages/introduction/introduction"
        });
    },

    initAction: function(code) {
        var action = {};

        action.bundleName = 'com.linzhenbin.xjxxxjjjkkkyyyyttt';
        action.abilityName = 'com.linzhenbin.xjxxxjjjkkkyyyyttt.MessageAbility';
        action.messageCode = code;
        //1 为Internal Ability，与FA共进程，采用内部函数调用的方式和FA通信，
        //  适用于对PA响应时延要求较高的场景，不支持其他FA访问调用能力
        action.abilityType = ABILITY_TYPE_EXTERNAL;
        action.syncOption = ACTION_SYNC;  // 同步
        return action;
    },

    // 订阅PA --> callbackData 会传服务器数据回FA
    getSUBSCRIBE: async function() {
        var action = this.initAction(ACTION_MESSAGE_CODE_SUBSCRIBE);

        var result = await FeatureAbility.subscribeAbilityEvent(action, this.callback);
        var ret = JSON.parse(result);
        if (ret.code == 0) {
            console.info('subscribe success, result:' + JSON.stringify(ret));
            this.context = CONNECTING.context;
            this.color = CONNECTING.color;
        } else {
            console.error('subscribe error, result:' + JSON.stringify(ret));
            this.color = DISCONNECTED.color;
            this.context = DISCONNECTED.context;
        }
    },

    callback: function(callbackData) {
        // 从PA传来的数据
        this.context = CONNECTED.context;
        this.color = CONNECTED.color;
        var callbackJson = JSON.parse(callbackData);
        let board2phoneJson = JSON.parse(JSON.stringify(callbackJson.data.board2phone));
        console.info('board2phoneJson is: ' + JSON.stringify(board2phoneJson));
        board2phoneJson['time'] = getDateTime();
        GESTURES.unshift(board2phoneJson);
        console.info("GESTURES is: " + JSON.stringify(GESTURES));
        while (GESTURES.length > DISPLAY_GESTURES_NUMBER) {
            GESTURES.pop();
        }
        console.info("GESTURES length: " + GESTURES.length);
        CONNECTED.context = '从服务器接收到了数据...'

        // 接收到了图片，进行解析，然后传给服务器手势对应的功能
//        "phone2board":{
//        "order":"takephoto",
//        }
        let Gesture_name = board2phoneJson.gesture[0].name;   // 手势类别
        console.info("Gesture_class: " + Gesture_name);
        let response = {
            "phone2board": {
                    order: "function",
                }
        };
        this.sendMessage(JSON.stringify(response));  // 转成字符串给服务器
    },

    // 取消订阅PA
    getUNSUBSCRIBE: async function() {
        var action = this.initAction(ACTION_MESSAGE_CODE_UNSUBSCRIBE);
        var result = await FeatureAbility.unsubscribeAbilityEvent(action);
        var ret = JSON.parse(result);
        if (ret.code == 0) {
            console.info('unsubscribe success, result:' + result);
        } else {
            console.error('unsubscribe error, result:' + result);
        }
    },

    // 从FA主动将数据传给PA
    sendMessage: async function(data) {
        console.info('sendMessage....');
        var action = this.initAction(ACTION_MESSAGE_CODE_SERVER);
        action.data = data;

        CONNECTED.context = '发送数据给服务器...';
        var result = await FeatureAbility.callAbility(action);
        var ret = JSON.parse(result);
        if (ret.code == 0) {
            console.info('send to PA success, result:' + JSON.stringify(ret));
            CONNECTED.context = "发送数据给服务器成功!";
        } else {
            console.info('send to PA error, result:' + JSON.stringify(ret));
        }
    },

    // 选择场景 在识别场景处显示
    chooseScene: function(idx) {
//        this.$element("table").index = 1;
//        const tabs = this.$refs.table;
        this.focusImg = GESTURES[idx].image;
        console.info("DIV 被点击了...")
    },


    handleClick(idx) {

        const el = this.$refs.canvas1;
        const ctx = el.getContext('2d');
        var img = new Image();
        img.src = GESTURES[idx].image;


        ctx.fillStyle = 'rgb(0,0,0)'    // 黑色背景填充
        ctx.fillRect(0,0, 600, 600);


        let dWidth = GESTURES[idx].width;
        let dHeight = GESTURES[idx].height;
        // 图片绘制
        ctx.drawImage(img, 0, 0, dWidth, dHeight,
            0, 0, scalingDown(dWidth), scalingDown(dHeight));


        // 若识别到多个手势，从后往前画
        for (let i = GESTURES[idx].gesture.length - 1; i >= 0; --i) {
            let sx = GESTURES[idx].gesture[i].x;
            let sy = GESTURES[idx].gesture[i].y;
            let sWidth = GESTURES[idx].gesture[i].w;
            let sHeight = GESTURES[idx].gesture[i].h;
            this.curImgWidth = dWidth;
            this.curImgHeight = dHeight;
            drawRectangle(
                ctx,
                sx, sy, sWidth, sHeight,
                "准确度: " + GESTURES[idx].gesture[0].Confidence.toString().substring(0,6)
            );
        }


    },

    changeTabs(e) {
        if (e.index == 0) {
            this.tabsHeight = VIDEOHEIGHT;
            this.tabsWidth = VIDEOWIDTH;
        } else if (e.index == 1) {
            this.tabsWidth = this.curImgWidth;
            this.tabsHeight = this.curImgHeight;
        }
        console.info("******* id" + e.index);
    }

}

const getDateTime = function (){
    var now = new Date();

    var year = now.getFullYear();   //得到年份

    var month = now.getMonth()+1;   //得到月份

    var date = now.getDate();       //得到日期

    // var day = now.getDay();      //得到周几

    var hour= now.getHours();       //得到小时数

    var minute= now.getMinutes();   //得到分钟数

    var second= now.getSeconds();   //得到秒数

    return year + "-" + month + "-" + date + "  " + hour + ":" + minute + ":" + second;
}

const scalingDown = function (input) {
    return input * 0.75;
}

// 绘制矩形
const drawRectangle = function(
        ctx,                    // 绘制的画布
        sx, sy, sWidth, sHeight,// 矩形位置 + 矩形大小
        text                    // 显示文字
) {
    // 矩形绘制
    ctx.lineWidth = 1.5;
    ctx.strokeStyle = 'rgb(248,194,235)';
    ctx.strokeRect(scalingDown(sx), scalingDown(sy),
        scalingDown(sWidth), scalingDown(sHeight));

    // 字体背景绘制
    ctx.fillStyle = 'rgb(248,194,235)';
    ctx.fillRect(scalingDown(sx), scalingDown(sy) - 15, ctx.measureText(text).width, 15);    // 15为字体高度

    // 字体绘制
    ctx.fillStyle = 'rgb(168,194,238)';
    ctx.font = '12px sans-serif bold';
    ctx.fillText(text,
        scalingDown(sx), scalingDown(sy) - ctx.lineWidth - 2);
}


export const StartAbility = (action) => {
    return new Promise(resolve => {
        FeatureAbility.startAbility(action).then(result => {
            var ret = JSON.parse(result);
            if (ret.code == 0) {
                console.info(' 启动FA成功， 返回结果为： ' + JSON.stringify(ret.message));
            } else {
                console.error(' 启动FA失败， 返回结果为： ' + JSON.stringify(ret.message));
            }
            resolve(result);
        })
    });
}

export const flag = {
    halfToFull: 286435456,
    fullToFull: 276826112
}

export const getHistoryGesture = ()=> {
    let history_count = parseInt(Storage.get(prefix + "history_count"));
    for (let i = 0; i < history_count; ++i) {
        let tmp = Storage.get(prefix+i);
        GESTURES.unshift(tmp);
    }
}