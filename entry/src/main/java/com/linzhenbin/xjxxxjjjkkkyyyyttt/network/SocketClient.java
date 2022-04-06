
/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package com.linzhenbin.xjxxxjjjkkkyyyyttt.network;


import com.linzhenbin.xjxxxjjjkkkyyyyttt.utils.MyContext;
import com.linzhenbin.xjxxxjjjkkkyyyyttt.utils.ThreadPoolUtil;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.net.NetHandle;
import ohos.net.NetManager;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.utils.zson.ZSONObject;
import ohos.wifi.WifiDevice;
import ohos.wifi.WifiLinkedInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


        // base64编码的图片

/**
 * Socket Client Slice
 */
public class SocketClient {
    private static final String TAG = SocketClient.class.getSimpleName();

    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 0xD000F00, TAG);

    private static final int PORT = 8899;

    private static final String board2phone = "board2phone";
    private static final String gesture = "gesture";
    private static final String name = "name";
    private static final String image = "image";


    private InetAddress ipaddress;

    public SocketClient() {
        try {
            ipaddress = InetAddress.getByName(this.getLocationIpAddress(MyContext.context));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private final Lock lock = new ReentrantLock();
    private Socket server;

    // 建立与服务器的连接
    public void buildConnect() {
        ThreadPoolUtil.submit(() -> {

            boolean recFlag = false;    // 接收标志
            // 接收服务器传来的数据包
            DatagramPacket result = new DatagramPacket(new byte[255], 255);


            NetManager netManager = NetManager.getInstance(null);
            if (!netManager.hasDefaultNet()) {
                // 数据网络不可用
                return;
            }
            // 指定发送方的IP和Port
            try (DatagramSocket socket =
                         new DatagramSocket(7788, ipaddress)) {
                // 客户端IP和Port
                ZSONObject json = new ZSONObject();
                json.put("GESTURE_CLIENT", ipaddress);
                json.put("PORT", 7788);
                NetHandle netHandle = netManager.getDefaultNet();
                netHandle.bindSocket(socket);
                byte[] buffer = json.toString().getBytes();

                // 和板端的测试开启
//                DatagramPacket request = new DatagramPacket(buffer, buffer.length,
//                        InetAddress.getByName("192.168.31.255"), PORT);
                // 本机测试开启
                DatagramPacket request = new DatagramPacket(buffer, buffer.length,
                        InetAddress.getByName("255.255.255.255"), PORT);


                socket.setSoTimeout(5000);  // 5秒
                for (int i = 0; i < 3 && !recFlag; ++i) {   // 重复三次
                    socket.setBroadcast(true);  // 开启广播(本机测试开启)
                    socket.send(request);       // 发送数据
                    System.out.println("UDP客户端: 客户端发送了...");
                    try {
                        socket.setBroadcast(false); // 关闭广播(本机测试开启)
                        socket.receive(result);     // 阻塞监听
                        System.out.println("UDP客户端: 收到了服务器的回应...");
                    } catch (SocketTimeoutException ignored) {
                        recFlag = false;
                        continue;
                    }
                    recFlag = true;
                }
                socket.close();     // 关闭UDP套接字
            } catch (IOException e) {
                HiLog.error(LABEL_LOG, "%{public}s", "netRequest IOException");
            }

            System.out.println("UDP服务器: 收到的包内容为..." + result.toString());
            if (recFlag) {
                // 收到了
                byte[] Rdata = result.getData();
                ZSONObject json = ZSONObject.stringToZSON(new String(Rdata));

//                HiLog.info(LABEL_LOG, "UDP客户端: 服务器的IP地址"+ json.get("GESTURE_SERVER")
//                        + " 服务器的端口号"+ json.get("GESTURE_SERVER_PORT"));
                System.out.println("UDP客户端: 服务器的IP地址"+ json.get("GESTURE_SERVER"));

                try {
                    // 打开Tcp连接

                    // JSON中服务器的IP地址为String类型
                    InetAddress serverIP = InetAddress.getByName((String) json.get("GESTURE_SERVER"));
                    // port为整型
//                    Integer serverPort = (Integer) json.get("GESTURE_SERVER_PORT");
//                    HiLog.info(LABEL_LOG, "TCP客户端: 服务器地址" +
//                            serverIP.getHostAddress() + " 服务器端口号" + serverPort);
                    System.out.println("TCP客户端: 服务器地址" +
                            serverIP.getHostAddress());
//                    lock.lock();    // 给server上锁
                    server = new Socket();
//                    server.bind(new InetSocketAddress(ipaddress, 0));
                    server.bind(new InetSocketAddress(ipaddress, 0));
//                    server.connect(new InetSocketAddress(serverIP, serverPort));
                    server.connect(new InetSocketAddress(serverIP, 9988));
//                    lock.unlock();  // 给server解锁
                    System.out.println("TCP客户端: TCP连接完成...");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // 没收到
                HiLog.info(LABEL_LOG, "UDP客户端: 没收到服务器的回应...");
            }
        });
    }

    private static final String GESTURE_MSG_HEAD = "gesture_msg_head"; // 头部标识
    private static final int GESTURE_HEAD_FLAG_LENGTH = GESTURE_MSG_HEAD.length();  // 头部标识长度
    private static final int GESTURE_HEAD_LENGTH = GESTURE_HEAD_FLAG_LENGTH + 4;    // 头部长度
    /**
     * 接受服务器的数据，传给FA界面
     * @param remoteObjectHandler 从PA写给FA的MessageParcel
     * */
    public void read(IRemoteObject remoteObjectHandler) {
        if (remoteObjectHandler == null) {
            return;
        }
        try {
            Map<String, Object> event = new HashMap<String, Object>();
            byte[] buffer = new byte[2048];
            StringBuilder image = new StringBuilder();
            while (server == null || !server.isConnected()) { // 服务器还未连接 循环等待
                Thread.sleep(1000);
            }
            InputStream in = server.getInputStream();
            int i = 0;
            int count = 0;

            // 初始化TCP包
            int bodyLength = 0;
            byte[] headBuf = new byte[GESTURE_HEAD_LENGTH];
            byte[] tmp = new byte[0];
            int counter = 0;        // 计数器
            int need = GESTURE_HEAD_LENGTH;  // 还需要的字节数
            // 设置接受缓冲区大小，用于测试
//            server.setReceiveBufferSize(1024);
            while (server.isConnected()) {
                // 接收数据包并解析
                while ((counter = in.read(headBuf)) > 0) {
                    // 提取出头部 = body length + 标志
                    System.out.println(
                            "TCP客户端: " + "读取到了head数据包....长度 " + counter + " need: " + need);
                    if (counter < need) {
                        // 头部还没读完
                        tmp = mergeByte(tmp, headBuf, 0, counter);
                        need = GESTURE_HEAD_LENGTH - tmp.length;
                        headBuf = new byte[need];    // 重新指定接受缓冲区大小
                        continue;
                    } else {
                        // 1. 前面出现断包,此时已经接受了全部的包,合包操作
                        // 2. 前面没有出现断包,直接把headBuf给自己
                        headBuf = mergeByte(tmp, headBuf, 0, counter);
                        need = GESTURE_HEAD_LENGTH - headBuf.length;
                    }
                    // 头部读完了,开始解析头部
                    // +--------------------+
                    // |  gesture_msg_head  |
                    // +--------------------+
                    // | bodyLength = 4byte |
                    // +--------------------+
                    System.out.println("TCP客户端: " + "开始解析头部...");
                    byte[] gesture_msg_head_bytes = splitBytes(headBuf, 0, GESTURE_HEAD_FLAG_LENGTH);
                    String gesture_msg_head = new String(gesture_msg_head_bytes);   // 获取头部标签
                    // 数据包body长度
                    bodyLength = byteArrayToInt(splitBytes(headBuf, GESTURE_HEAD_FLAG_LENGTH, headBuf.length));
                    if (!gesture_msg_head.equals(GESTURE_MSG_HEAD)) { // 头部标识不对!
                        System.out.println("TCP客户端: " + "接受到错误的包...");
                        break;
                    }
                    System.out.println("TCP客户端: " + "头部解析完毕...头部标签内容为'" + gesture_msg_head + "' " +
                            "头部包含的数据包body的长度为 " + bodyLength);


                    // 开始读取body
                    byte[] bodyBuf = new byte[bodyLength];
                    tmp = new byte[0];  // 重置tmp
//                    counter = 0;      // 清零count计数器
                    need = bodyLength;  // 还需要读取的字节数
                    while (counter < need) {
                        while ((counter = in.read(bodyBuf)) > 0) { // 又读一次!!!
                            System.out.println(
                                    "TCP客户端: " + "读取到了body数据包....长度 " + counter + " need: " + need);
                            if (counter < need) {
                                // body还没读完
                                tmp = mergeByte(tmp, bodyBuf, 0, counter);
                                need = bodyLength - tmp.length;
                                bodyBuf = new byte[need];    // 重新指定接受缓冲区大小
                            } else {
                                // 1. 前面出现断包,此时已经接受了全部的包,合包操作
                                // 2. 前面没有出现断包,直接把headBuf给自己
                                bodyBuf = mergeByte(tmp, bodyBuf, 0, bodyBuf.length);
                                need = bodyLength - bodyBuf.length;
                                break;
                            }
                        }
                    }

                    //开始解析body
                    System.out.println("TCP客户端: " + "开始解析body...");
                    ZSONObject jsonBody = ZSONObject.stringToZSON(new String(bodyBuf));
                    System.out.println("TCP客户端: " + "body数据包解析完毕...");
                    MessageParcel data = MessageParcel.obtain();
                    MessageParcel reply = MessageParcel.obtain();
                    data.writeString(jsonBody.toString());
                    System.out.println("TCP客户端: " + jsonBody.toString());
//                    System.out.println("TCP客户端: body长度为" + );
                    jsonBody.clear();
                    // 传给FA
                    remoteObjectHandler.sendRequest(100, data, reply, new MessageOption());
                    reply.reclaim();
                    data.reclaim();

                    // 重新初始化，接收下一个TCP包
                    bodyLength = 0;
                    headBuf = new byte[GESTURE_HEAD_LENGTH];
                    tmp = new byte[0];
                    counter = 0;        // 计数器
                    need = GESTURE_HEAD_LENGTH;  // 还需要的字节数
                    break;  // 结束本TCP包,接收下一个TCP包
                }

            }
        } catch (IOException | RemoteException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    //==================== 发给服务器 ===============================

    public void write(String message, MessageParcel reply) {
        if (message == null) {
            return;
        }
        try {
            OutputStream out = server.getOutputStream();
            byte[] send = mergeByte(("gesture_msg_head").getBytes(StandardCharsets.US_ASCII),
                    intToByteArray(message.length()), 0, intToByteArray(message.length()).length);
            send = mergeByte(send, message.getBytes(), 0, message.getBytes().length);
            out.write(send);


            Map<String, Object> result = new HashMap<String, Object>();
            result.put("code", 0);      // 0为成功
            System.out.println(ZSONObject.toZSONString(result));
            reply.writeString(ZSONObject.toZSONString(result));
            System.out.println("TCP客户端: 发送了数据给服务器...内容为" + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
    }
    // =============================================================


    private String getLocationIpAddress(Context context) {
        WifiDevice wifiDevice = WifiDevice.getInstance(context);
        Optional<WifiLinkedInfo> linkedInfo = wifiDevice.getLinkedInfo();
        int ip = linkedInfo.get().getIpAddress();
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + (ip >> 24 & 0xFF);
    }

    private byte[] mergeByte(byte[] a, byte[] b, int begin, int end) {
        byte[] add = new byte[a.length + end - begin];
        int i = 0;
        for (i = 0; i < a.length; i++) {
            add[i] = a[i];
        }
        for (int k = begin; k < end; k++, i++) {
            add[i] = b[k];
        }
        return add;
    }

    private byte[] splitBytes(byte[] bytes, int begin, int end) {
        byte[] ret = new byte[end - begin];
        int j = 0;
        for (int i = begin; i < end; ++i) {
            ret[j++] = bytes[i];
        }
        return ret;
    }

    private int byteArrayToInt(byte[] bytes) {
        return   bytes[3] & 0xFF |
                (bytes[2] & 0xFF) << 8 |
                (bytes[1] & 0xFF) << 16 |
                (bytes[0] & 0xFF) << 24;
    }

}
