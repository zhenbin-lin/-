import {choice2char} from '../../common/utils/static_gesture.js';
import {allgesture_zh} from '../../common/utils/static_gesture.js'; // 全部手势
import settingStorage from '../../common/store/settingStorage.js';
import {settings} from '../../common/utils/settings_data.js'; // 默认的设置列表
import {effect_choice_zh, effect_choice_en} from '../../common/utils/all_function.js';


export default {

    data: {
    /**
         * 功能组合中的手势或功能的位置
         * */
        GESTURE1: 0,
        GESTURE2: 1,
        EFFECT: 2,
        storage: settingStorage,

        title: "",
        deviceInfo: {
            title: "手势设置",
            status: "0",
        },
        settings: settings,
        isUnOnLine: true,
        showing_select: allgesture_zh,
    /**
         * 用于定位要修改的手势位置（postx, posty）
         * 例如第一个功能的组合的 手势一为:(0,0) 手势二为:(0,1) 功能为:(0,3)
         * */
        postx: "-1",
        posty: "-1",
        selected: "null",    // 被选择的手势或功能
    },

    featureClick(idx, uid) {
        debounce(this.modify(idx, uid), 3000);
    },

/**
     * 添加新功能列表
     * gs1: 手势1
     * gs2: 手势2
     * effect: 手势对应的功能
     * */
    addNewItem() {
        settings[0].pos1 = (parseInt(settings[0].pos1) + 1);
        console.info("settings: 长度" + settings[0].pos1);
        settings.push({
            "pos1" : "无", // 手势1
            "pos2" : "无", // 手势2
            "pos3" : "无"   // 功能
        });
        this.storeAllSettings();
    },

/**
     * 修改现存手势功能
     * idx: 是选择功能的行号，比如组合一的 idx = 0, 组合二的 idx = 1...
     * choice: 是功能里面的选项其中GESTURE1: 手势1; GESTURE2: 手势2; EFFECT: 功能
     * */
    modify(idx, choice) {    // 接口
        debounce(this.modifyImp(idx, choice), 3000);
    },
    modifyImp(idx, choice) {    // 实现
        // 标记被按到的组件位置
        console.info("点击了坐标("+ idx + "," + choice +")");
        this.postx = idx;
        this.posty = choice;
        this.selected = this.settings[this.postx][choice2char[this.posty]];
        if (choice == 2) {
            // 点击的是功能按钮
            this.showing_select = effect_choice_zh;
        } else {
            this.showing_select = allgesture_zh;
        }
        this.$element("loginDialog").show();    // 显示菜单
    },

    handleChange(data) {
        this.selected = data.newValue;
    },

    /**
     * 被选的手势 selectGS (selected gesture)
     * */
    textonchange(e) {
        this.$element("loginDialog").close();
        // choice2char 将 this.posty转成
        // gesture1、gesture2、或者 effect 用于在setting中访问对象
        console.info("显示坐标("+ this.postx + "," + this.posty +")");
        var ty = choice2char[this.posty];
        this.settings[this.postx][ty] = this.selected;
        // 还原
        this.postx = -1;
        this.posty = -1;
        this.storeAllSettings();
    },

    /**
     * 取消按钮
     * */
    textoncancel(e) {
        this.postx = -1;
        this.posty = -1;
        this.selected = null;
        this.$element("loginDialog").close();
    },


// 生命周期开始
    onInit() {
        this.LogSettings(); // 加载手势设置
    },

    LogSettings: async function() {
        this.storage.get("pos00", 0, 0).then(()=>{
            console.info(settings[0].pos1);
            for (let i = 1; i < settings[0].pos1; ++i) {
                this.storage.get("pos" + i + 0, i, 0);
                this.storage.get("pos" + i + 1, i, 1);
                this.storage.get("pos" + i + 2, i, 2);
            }
        });
    },

// 生命周期结束，将设置存入缓存
    onDestroy() {
    },

    // 将全部手势存入缓存
    storeAllSettings : async function() {
        // settings[0].pos1存的是settings列表的长度  pos为position
        var setLength = parseInt(settings[0].pos1);
        for (var i = 1; i < setLength; ++i) {
            // this.settings 和 settings 可以混用
            // this.settings 本质是 settings
            this.storage.set("pos" + i + 0 , this.settings[i]["pos1"]);
            this.storage.set("pos" + i + 1 , this.settings[i]["pos2"]);
            this.storage.set("pos" + i + 2 , this.settings[i]["pos3"]);
            console.info("onDestroy "+ "set 调用");
        }
        this.storage.set("pos00", settings[0].pos1.toString());
    },
    deleteSetting(idx) {
        console.info("长按...");
        settings.splice(idx,1);
    },

}




export const debounce = (fn, delay) => {
    let timer = null;
    return function() {
        if(timer) {
            clearTimeout(timer);
        }
        timer = setTimeout(fn, delay);
    }
}
