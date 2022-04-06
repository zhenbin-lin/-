package com.linzhenbin.videoplaydemo.utils;



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class RtspClient extends Thread  {
    //服务端ip
    private static String localIp = "184.72.239.149";//本机测试ip就填127.0.0.1 不填自己的ip号
    //服务端端口号
    private static final String port="8554";
    //rtp和rtcp端口号
    private static final int rtp_port=10086;
    private static final int rtcp_port=rtp_port+1;
    //rtsp版本 默认写法
    private static final String rtsp_version=" RTSP/1.0";
    //端口号后面的索引
    private static final String index="/vod/mp4:BigBuckBunny_115k.mov";
    //默认这5种状态
    private static final String OPTIONS="OPTIONS";
    private static final String DESCRIBE="DESCRIBE";
    private static final String SETUP="SETUP";
    private static final String TEARDOWN="TEARDOWN";
    private static final String PLAY="PLAY";
    //当前状态，初始为OPTIONS
    private static String currentStatue=OPTIONS;
    //用户信息（可有可无）
    private static final String user="syf";
    //序列号
    private static int CSeq=1;
    //检查是否接收正确字符串（回复正确时，会接收到以下字符串）
    private static final String OK="RTSP/1.0 200 OK";
    //trackID，本项目默认最多两个
    private static String[] trackInfo=new String[2];
    //表示track的数量，后面方便显示
    private static int num=0;
    //session
    private static String session="";
//    /**
//     * 测试
//     */
//    public static void main(String[] args) {
//        RTSP_Client(ip,port);
//    }

    /**
     * 建立TCP和服务器进行内容交换，然后开启UDP进行视频数据接收
     */
    public static void RTSP_Client(String ip,String port){
        localIp=ip;
        Socket client =null;
        BufferedInputStream in=null;
        BufferedOutputStream out=null;
        StringBuilder data;
        int len;
        byte[] bytes=new byte[1024*10];
        boolean stop=false;
        int sum;
        boolean count;
        currentStatue=OPTIONS;
        try {
            //建立连接
            System.out.println("正在建立连接。。。。");
            client=new Socket(ip,Integer.parseInt(port));
            System.out.println("成功建立连接！！！！");
            //设定超时时间10s,10s无动作就重启
            client.setSoTimeout(10000);
            //获取输入输出流
            in=new BufferedInputStream(client.getInputStream());
            out=new BufferedOutputStream(client.getOutputStream());
            byte[] tmp =new byte[1024*10];
            //得到需要写的数据
            while (true) {
                Arrays.fill(bytes,(byte)0);
                sum=0;
                count=false;
                //不睡眠有时候会接收出错
                Thread.sleep(50);
                //得到发送的数据
                data = getWriteData();
                System.out.println("本次发送的请求\n"+ data);
                //向服务器写数据
                out.write(data.toString().getBytes());
                out.flush();
                //清空发送内容字符串
                data.delete(0, data.length());
                //读服务器发过来的数据，阻塞方法
                while ((len=in.read(tmp))!=-1){
                    if (!currentStatue.equals(SETUP)) {//除DESCRIBE方法外，其他方法都只复制一次
                        System.arraycopy(tmp,0,bytes,0,len);
                        sum+=len;
                        Arrays.fill(tmp,(byte)0);
                        break;
                    } else{//为Describe时需复制两次
                        System.arraycopy(tmp,0,bytes,sum,len);
                        Arrays.fill(tmp,(byte)0);
                        sum+=len;
                        if (count){
                            break;
                        }
                        count=true;
                    }
                }
                if (len==-1){//断开连接了
                    System.out.println("连接断开了");
                    break;
                }
                //判断本次反应是否正确
                if (isRight(bytes)){//本次请求回应正确
                    //根据返回的方法名进一步处理
                    getMethod(bytes,sum);
                }else {
                    System.out.println("接收错误，重新发送接收请求");
                    currentStatue=SETUP;
                }
                System.out.println("接收到的数据\n"+new String(bytes,0,sum));
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }finally {
            closeAll(client,in,out);
            if (!stop) {
                System.out.println("重启TCP");
                RTSP_Client(ip, port);
            }
        }
    }

    /**
     * 功能：根据方法名得到需要向服务器写的数据
     * @return 返回拼接的数据
     */
    public static StringBuilder getWriteData(){
        StringBuilder command=new StringBuilder();
        switch(currentStatue){
            case(OPTIONS):{//客户端向服务器请求可用方法
                command.append("OPTIONS ");
                command.append(addHeader()).append("\r\n");
                currentStatue=DESCRIBE;
                break;
            }
            case(DESCRIBE):{//客户端向服务器请求媒体描述文件，格式为sdp
                command.append("DESCRIBE ");
                command.append(addHeader());
                command.append("Accept: application/sdp").append("\r\n").append("\r\n");
                currentStatue=SETUP;
                break;
            }
            case(SETUP):{//客户端发送建立请求，请求建立连接会话，准备接收音视频数据
                if (num!=0) {
                    num--;
                    command.append("SETUP ");
                    command.append(addHeader());
                    command.append("Transport: RTP/AVP;unicast;client_port=" + rtp_port + "-" + rtcp_port).append("\r\n").append("\r\n");
                    if (num==0)
                        currentStatue=PLAY;
                    break;
                }else{
                    command.append("SETUP ");//发送UDP端口号，准备接收rtp和rtcp文件
                    command.append(addHeader());
                    command.append("Transport: RTP/AVP;unicast;client_port=" + rtp_port + "-" + rtcp_port).append("\r\n").append("\r\n");
                    currentStatue = PLAY;
                }
            }
            case(PLAY):{//客户端请求播放媒体
                command.append("PLAY ");
                command.append(addHeader());
                command.append("Range: npt=0.000-").append("\r\n").append("\r\n");
                currentStatue=TEARDOWN;
                break;
            }
            case(TEARDOWN): {
                command.append("TEARDOWN ");
                command.append(addHeader());
                command.append("Session: 66334873").append("\r\n").append("\r\n");
                currentStatue=OPTIONS;
                while (true){
                    //不关闭一直播放
                }
//                break;
            }
            default: {
                System.out.println("错误信息");
                currentStatue=OPTIONS;
                break;
            }
        }
        //返回发送的数据
        return command;
    }

    /**
     * 写给服务端中的重复信息
     * @return 拼接好了的信息字符串
     */
    //重复字段
    public static String addHeader(){
        return "rtsp://"+localIp+":"+port+index+((currentStatue.equals(SETUP))?"/trackID="+trackInfo[num]:"")
                +rtsp_version+"\r\n"
                +"CSeq: "+(CSeq++)+"\r\n"
                +((currentStatue.equals(PLAY))?"Session: "+session+"\r\n":"")
                +"User-Agent: "+user+"\r\n";

    }

    /**
     * 功能：根据方法名进行下一步
     * @param bytes 获取到的数据字节
     * @param len 字节长度
     */
    public static void getMethod(byte[] bytes,int len){
        //将字节转换成字符串
        String all = new String(bytes, 0, len);
        if (currentStatue.equals(PLAY)){//收到服务器返回的数据后建立UDP或者TCP连接接收数据
            String[] lines=all.split("\r\n");
            for (int i=2;i<lines.length;i++){
                if (lines[i].contains("Session")){
                    session=lines[i].split(" ")[1].split(";")[0];
                }
            }
            boolean judgeConnect=all.contains("RTP/AVP/TCP");//获取使用什么通信方式
            if (!judgeConnect){//judgeConnect为真采用TCP,反之UDP
                //开启Udp
//                UdpReceiveData.openAllUdp(rtp_port,rtcp_port);
            }else {
                System.out.println("开启Tcp");//本项目采用UDP就不写TCP了
            }
        }else if (currentStatue.equals(SETUP)){
            //得到trackID号
            String[] lines=all.split("\r\n");
            for (String line : lines) {
                if (line.contains("trackID=")) {
                    trackInfo[num++] = line.substring(line.indexOf("trackID=") + 8);
                }
            }
        }
    }

    /**
     * 检验服务端回复的信息是否正确
     * @param bytes 客户端返回的信息
     * @return 正确返回true，反之返回false
     */
    public static boolean isRight(byte[] bytes){
        return new String(bytes,0,15).contains(OK);
    }
    /**
     * 关闭输入输出流以及客户端
     * @param socket 客服端
     * @param inputStream 输入流
     * @param outputStream 输出流
     */
    public static void closeAll(Socket socket, BufferedInputStream inputStream, BufferedOutputStream outputStream){
        if (socket!=null){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (inputStream!=null){
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (outputStream!=null){
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}