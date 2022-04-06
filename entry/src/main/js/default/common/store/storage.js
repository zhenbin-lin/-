import storage from '@system.storage';

const Storage = {
    set: (key, data) => {
        storage.set({
            key: key,
            value: data,
            success(data) {
                console.info("存储" + key + " : " + data + ", 成功");
            },
            fail(data, code) {
                console.error("存储" + key + " : " + data + ", 失败！");
                console.info("存储失败， 返回数据 code ： " + code + ", data: "+ data);
            },
            complete(){
                console.info("存储数据完成")
            }
        })
    },
    get: (key) => {
        return new Promise((resolve, reject) => {
            storage.get({
                key: key,
                success(data) {
                    console.info("获取存储数据" + key + "成功, 值为： " + data);
                    resolve(data)
                },
                fail(data, code) {
                    console.error("获取存储数据" + key + "失败");
                    console.info("获取存储数据失败 , 返回信息 code: " + code + ", data: "+ data);
                    reject();
                },
                complete(){
                    console.info("获取存储数据完成")
                }
            })
        })
    },
    clear: (key) => {
        return new Promise((resolve, reject) => {
            storage.delete({
                key: key,
                success(data) {
                    console.info("删除存储数据" + key + "成功, 原始值为： " + data);
                    resolve(data)
                },
                fail(data, code) {
                    console.error("删除存储数据" + key + "失败");
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