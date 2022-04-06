package com.linzhenbin.xjxxxjjjkkkyyyyttt;
/*
 * 用于FA与PA之间进行通信使用，
 * 本类包的作用是将后台网络获取的信息，传递给前端FA
 * 并且将FA的消息传递给后台网络线程
 * */



import com.linzhenbin.xjxxxjjjkkkyyyyttt.network.SocketClient;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.*;
import ohos.utils.zson.ZSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MessageAbility extends Ability {
    private static final String BUNDLE_NAME = "com.linzhenbin.xjxxxjjjkkkyyyyttt";
    private static final String ABILITY_NAME = "com.linzhenbin.xjxxxjjjkkkyyyyttt.MessageAbility";

    // code的类型
    private static final int VIDEO = 1001;
    // 定义日志标签
    private static final HiLogLabel LABEL = new HiLogLabel(HiLog.LOG_APP, 0, "MY_TAG");


    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);

    }

    @Override
    protected IRemoteObject onConnect(Intent intent) {
        super.onConnect(intent);
        PlayRemote remote = new PlayRemote();
        return remote.asObject();
    }


    static class PlayRemote extends RemoteObject implements IRemoteBroker {

        private List<String> message;

        private Thread thread;

        private SocketClient client;


        public PlayRemote() {
            super("PlayRemote");
            client = new SocketClient();
            client.buildConnect();
        }

        private static final int SUCCESS = 0;
        private static final int ERROR = 1;
        private static final int SUBSCRIBE = 1005;
        private static final int UNSUBSCRIBE = 1006;
        private static final int SERVER = 1007;
        private IRemoteObject remoteObjectHandler = null;



        // PA send to FA
        @Override
        public boolean onRemoteRequest(int code, MessageParcel data, MessageParcel reply, MessageOption option) {
            switch (code) {
                case SUBSCRIBE: {
                    System.out.println("onRemoteRequest...");
                    remoteObjectHandler = data.readRemoteObject();
                    startNotify();
                    Map<String, Object> result = new HashMap<String, Object>();
                    result.put("code", SUCCESS);
                    reply.writeString(ZSONObject.toZSONString(result));
                    break;
                }
                case SERVER: {
                    // 从FA传来的数据 发给服务器
                    System.out.println("PA调用成功..");
                    String dataStr = data.readString();
                    ZSONObject param = null;
                    try {
                        param = ZSONObject.stringToZSON(dataStr);
                    } catch (RuntimeException e) {
                        HiLog.error(LABEL, "convert failed.");
                    }
                    // SYNC
                    System.out.println("PA: 接受到了FA的数据" + param.toString());
                    // 调用客户端发送消息给服务器
                    client.write(param.toString(), reply);
                    break;
                }
                case UNSUBSCRIBE: {
                    // 如果仅支持单FA订阅，可直接置空：remoteObjectHandler = null;
                    remoteObjectHandler = null;
                    thread.stop();
                    Map<String, Object> result = new HashMap<String, Object>();
                    result.put("code", SUCCESS);
                    reply.writeString(ZSONObject.toZSONString(result));
                    break;
                }
            }
            return true;
        }

        /**
         * 从服务器接数据 然后发往FA
         * */
        private void startNotify() {
            thread = new Thread(() -> {
                client.read(remoteObjectHandler);
            });
            thread.start();
        }


        @Override
        public IRemoteObject asObject() {
            return this;
        }
    }
}