import storage from '@system.storage';
import {settings} from '../utils/settings_data.js'

/**
 * 本类主要用于将设置存入缓存中，再软件退出后，下次再启动软件时，
 * 上次的设置仍然会保留在缓存中。
 * */
const Storage = {
    set: (key, value) => {
        storage.set({
            key: key,
            value: value,
            success(data) {
                console.info(key);
                console.info("存储" + key + ":" + value + ", 成功");
            },
            fail(data, code) {
                console.info("fail");
                console.error("存储" + key + " : " + data + ", 失败！");
                console.info("存储失败， 返回数据 code ： " + code + ", data: "+ data);
            },
            complete(){
                console.info("存储数据完成")
            }
        })
    },

    /**
     * 从x从 1开始算起, 因为 0 是setting counts
     * x : 在第X行
     * y : 在第Y列
     * */
    get: (key, x, y) => {
        return new Promise((resolve, reject) => {
            storage.get({
                key: key,
                default: "empty",   // 当找不到key所对应的value时，data = default
                success(data) {
                    console.info("获取存储数据" + key + "成功, 值为" + data);
                    if (data != "empty") {
                        if (x == 0) {
                            settings[0].pos1 = parseInt(data);
                            while (settings.length < settings[0].pos1) {
                                settings.push({
                                    "pos1":"",
                                    "pos2":"",
                                    "pos3":""
                                });
                                console.info("setting 长度:" + JSON.stringify(settings));
                            }
                        } else {
                            switch(y) {
                                case 0 :
                                    settings[x].pos1 = data;
                                    break;
                                case 1 :
                                    settings[x].pos2 = data;
                                    break;
                                case 2 :
                                    settings[x].pos3 = data;
                                    break;
                                default: break;
                            }
                        }
                        console.info("修改了 settings");
                    }
                    resolve(data)
                },
                fail(data, code) {
                    console.error("获取存储数据" + key + "失败");
                    console.info("获取存储数据失败, 返回信息 code:" + code + ", data:"+ data);
                    reject();
                },
                complete(){
                    console.info("获取存储数据完成");
                }
            })
        });
    },


    clear: (x, y) => {
        return new Promise((resolve, reject) => {
            storage.delete({
                key: "pos" + x + y,
                success(data) {
                    console.info("删除存储数据" + this.key + "成功, 原始值为： " + data);
                    resolve(data)
                },
                fail(data, code) {
                    console.error("删除存储数据" + this.key + "失败");
                    console.info("删除存储数据失败 , 返回信息 code: " + code + ", data: "+ data);
                    reject();
                },
            })
        })
    },
    clearAll: () => {
        return new Promise((resolve, reject) => {
            storage.clear({
                success() {
                    console.info("删除所有储数据成功");
                    resolve()
                },
                fail() {
                    console.error("删除所有存储数据失败");
                    reject();
                },
            })
        })
    }
};

export default Storage;