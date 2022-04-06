package tv.danmaku.ijk.utils;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class LogUtil {
    public static void e(String tag,String message){
        HiLogLabel label=new HiLogLabel(HiLog.LOG_APP,0,tag);
        HiLog.error(label,message);
    }

    public static void d(String tag,String message){
        HiLogLabel label=new HiLogLabel(HiLog.LOG_APP,0,tag);
        HiLog.info(label,message);
    }

    public static void v(String tag,String message){
        HiLogLabel label=new HiLogLabel(HiLog.LOG_APP,0,tag);
        HiLog.info(label,message);
    }

    public static void i(String message){
        HiLogLabel label=new HiLogLabel(HiLog.LOG_APP,0,"logUtils");
        HiLog.info(label,message);
    }

    public static void i(String tag,String message){
        HiLogLabel label=new HiLogLabel(HiLog.LOG_APP,0,tag);
        HiLog.info(label,message);
    }

    public static void w(String tag,String message){
        HiLogLabel label=new HiLogLabel(HiLog.LOG_APP,0,tag);
        HiLog.warn(label,message);
    }

    public static void w(String message){
        HiLogLabel label=new HiLogLabel(HiLog.LOG_APP,0,"logUtils");
        HiLog.warn(label,message);
    }

    public static void wtf(String tag,String message){
        HiLogLabel label=new HiLogLabel(HiLog.LOG_APP,0,tag);
        HiLog.fatal(label,message);
    }
}
