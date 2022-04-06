package com.linzhenbin.xjxxxjjjkkkyyyyttt;



import com.linzhenbin.xjxxxjjjkkkyyyyttt.network.SocketServerTest;
import com.linzhenbin.xjxxxjjjkkkyyyyttt.utils.MyContext;
import ohos.aafwk.content.Intent;
import ohos.ace.ability.AceAbility;
import ohos.bundle.IBundleManager;


public class MainAbility extends AceAbility {
    @Override
    public void onStart(Intent intent) {
        System.out.println(this.getContext());
        MyContext.context = this.getContext();
        SocketServerTest server = new SocketServerTest(this);
        server.tcpServer();
        server.udpServer();

        System.out.println("服务器...");
        if (verifySelfPermission("ohos.permission.READ_MEDIA") != IBundleManager.PERMISSION_GRANTED &&
                verifySelfPermission("ohos.permission.WRITE_MEDIA") != IBundleManager.PERMISSION_GRANTED) {
            requestPermissionsFromUser(
                    new String[]{"ohos.permission.READ_MEDIA", "ohos.permission.WRITE_MEDIA"}, 1);
        }

        super.onStart(intent);
    }

    @Override
    public void onStop() {
        // 注销
        super.onStop();
    }
}
