package com.linzhenbin.xjxxxjjjkkkyyyyttt.player.slice;




import com.linzhenbin.xjxxxjjjkkkyyyyttt.ResourceTable;
import com.linzhenbin.xjxxxjjjkkkyyyyttt.player.Constants.Constants;
import com.linzhenbin.xjxxxjjjkkkyyyyttt.player.ui.widget.media.VideoComponent;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.agp.colors.RgbColor;
import ohos.agp.components.*;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.graphics.SurfaceOps;
import ohos.agp.utils.Color;
import ohos.agp.window.service.Window;
import ohos.agp.window.service.WindowManager;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.multimodalinput.event.KeyEvent;

import static ohos.agp.components.ComponentContainer.LayoutConfig.MATCH_CONTENT;
import static ohos.agp.components.ComponentContainer.LayoutConfig.MATCH_PARENT;
//import static ohos.agp.components.ComponentContainer.LayoutConfig.WRAP_CONTENT;

/**
 * 全屏播放本地视频
 */
public class VideoPlayAbilitySlice extends AbilitySlice implements SurfaceOps.Callback {

    private VideoComponent videoComponent;

    ProgressBar roundProgressBar;
    Text timePlay;
    Image playBtn;
    public static final int MESSAGE_UPDATE_PLAY_TIME = 100;
    public static final int DELAY_TIME = 500;
    private SurfaceOps ops;
    private String videoPath;


    private EventHandler handler = new EventHandler(EventRunner.current()) {
        @Override
        protected void processEvent(InnerEvent event) {
            super.processEvent(event);
//            Log.hiLog("processEvent event:" + event.eventId);
            switch (event.eventId) {
                case MESSAGE_UPDATE_PLAY_TIME:
                    int time = videoComponent.getPlayTime();
                    int h = time / (60 * 60 * 1000);
                    int m = (time - h * 60 * 60 * 1000) / (60 * 1000);
                    int s = (time - h * 60 * 60 * 1000 - m * 60 * 1000) / 1000;
                    time = videoComponent.getAllTime();
                    int h1 = time / (60 * 60 * 1000);
                    int m1 = (time - h * 60 * 60 * 1000) / (60 * 1000);
                    int s1 = (time - h * 60 * 60 * 1000 - m * 60 * 1000) / 1000;
                    if (event.eventId == MESSAGE_UPDATE_PLAY_TIME) {
                        roundProgressBar.setProgressValue(100 * videoComponent.getPlayTime() / videoComponent.getAllTime());
                        timePlay.setText(h + ":" + m + ":" + s + "/" + h1 + ":" + m1 + ":" + s1);
                        if (videoComponent.getPlayState() == VideoComponent.STATE_PLAYING) {
                            handler.sendEvent(InnerEvent.get(MESSAGE_UPDATE_PLAY_TIME), DELAY_TIME);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private Element getBackGroundFocus() {
        ShapeElement drawable = new ShapeElement();
        drawable.setShape(ShapeElement.RECTANGLE);
        RgbColor rgbColor = new RgbColor();
        rgbColor.setAlpha(0xaa);
        rgbColor.setRed(0x3E);
        rgbColor.setGreen(0x43);
        rgbColor.setBlue(0x49);
        drawable.setRgbColor(rgbColor);
        drawable.setCornerRadius(12);
        return drawable;
    }

    private Element getBackGroundNormal() {
        ShapeElement drawable = new ShapeElement();
        drawable.setShape(ShapeElement.RECTANGLE);
        RgbColor rgbColor = new RgbColor();
        rgbColor.setAlpha(0x00);
        rgbColor.setRed(0x00);
        rgbColor.setGreen(0x00);
        rgbColor.setBlue(0x00);
        drawable.setRgbColor(rgbColor);
        drawable.setCornerRadius(12);
        return drawable;
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent keyEvent) {
        switch (keyCode) {
            case KeyEvent.KEY_DPAD_CENTER:
            case KeyEvent.KEY_ENTER:
                if (playBtn.hasFocus()) {
                    playBtn.callOnClick();
                }
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        IntentParams ip = intent.getParams();
        videoPath = (String) ip.getParam(Constants.VIDEO_PATH);
        WindowManager windowManager = WindowManager.getInstance();
        Window window = windowManager.getTopWindow().get();
        window.setTransparent(true);
        getWindow().addFlags(WindowManager.LayoutConfig.MARK_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutConfig.MARK_TRANSLUCENT_NAVIGATION);
        getWindow().addFlags(WindowManager.LayoutConfig.MARK_FULL_SCREEN);

        DependentLayout myLayout = new DependentLayout(this);
        DependentLayout.LayoutConfig params = new DependentLayout.LayoutConfig(MATCH_PARENT, MATCH_PARENT);
        myLayout.setLayoutConfig(params);

        DependentLayout.LayoutConfig lpVideo = new DependentLayout.LayoutConfig(MATCH_PARENT, MATCH_PARENT);
        videoComponent = new VideoComponent(this, this);
        videoComponent.setHandler(handler);
        myLayout.addComponent(videoComponent, lpVideo);

        DependentLayout rlParent = new DependentLayout(this);
        DependentLayout.LayoutConfig lpParent = new DependentLayout.LayoutConfig(MATCH_PARENT, MATCH_CONTENT);
        lpParent.addRule(DependentLayout.LayoutConfig.ALIGN_PARENT_BOTTOM);
        lpParent.setMarginLeft(40);
        lpParent.setMarginRight(40);
        lpParent.setMarginBottom(40);
        myLayout.addComponent(rlParent, lpParent);


        //显示播放暂停按钮
        playBtn = new Image(this);
        DependentLayout.LayoutConfig lpPlayBtn = new DependentLayout.LayoutConfig(80, 80);
        lpPlayBtn.addRule(DependentLayout.LayoutConfig.ALIGN_PARENT_RIGHT);
        playBtn.setBackground(getBackGroundFocus());
        playBtn.setLayoutConfig(lpPlayBtn);
        playBtn.setId(1112);
        playBtn.setPixelMap(ResourceTable.Media_ic_media_pause);
        playBtn.setScaleMode(Image.ScaleMode.STRETCH);
        playBtn.requestFocus();
        playBtn.setFocusChangedListener(new Component.FocusChangedListener() {
            @Override
            public void onFocusChange(Component component, boolean b) {
                if (b) {
                    playBtn.setBackground(getBackGroundFocus());
                } else {
                    playBtn.setBackground(getBackGroundNormal());
                }
            }
        });
        playBtn.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
//                Log.hiLog("playBtn onClick");
                if (videoComponent.getPlayState() == VideoComponent.STATE_PLAYING) {
                    videoComponent.pause();
                    playBtn.setPixelMap(ResourceTable.Media_ic_media_play);
                } else {
                    videoComponent.start();
                    playBtn.setPixelMap(ResourceTable.Media_ic_media_pause);
                }
            }
        });
        rlParent.addComponent(playBtn);

        //显示当前视频播放时间
        timePlay = new Text(this);
        DependentLayout.LayoutConfig lpTimePlay = new DependentLayout.LayoutConfig(MATCH_CONTENT, MATCH_CONTENT);
        lpTimePlay.addRule(DependentLayout.LayoutConfig.VERTICAL_CENTER);
        timePlay.setLayoutConfig(lpTimePlay);
        timePlay.setId(1111);
        timePlay.setText("00:00/00:00");
        timePlay.setTextSize(60);
        timePlay.setTextColor(Color.WHITE);
        rlParent.addComponent(timePlay);

        // 显示播放进度条
        roundProgressBar = new ProgressBar(this);
        roundProgressBar.setProgressWidth(10);
        roundProgressBar.setProgressColor(Color.RED);
        roundProgressBar.setMaxValue(100);
        roundProgressBar.setProgressValue(0);
        DependentLayout.LayoutConfig lpProgressBar = new DependentLayout.LayoutConfig(MATCH_PARENT, 80);
        lpProgressBar.addRule(DependentLayout.LayoutConfig.RIGHT_OF, timePlay.getId());
        lpProgressBar.setMarginLeft(20);
        lpProgressBar.setMarginRight(100);
        rlParent.addComponent(roundProgressBar, lpProgressBar);
        super.setUIContent(myLayout);
    }



    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (videoComponent != null) {
            videoComponent.destroy();
            videoComponent.removeFromWindow();
        }
    }


    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
    }

    @Override
    protected void onBackground() {
        super.onBackground();
    }


    @Override
    public void surfaceCreated(SurfaceOps surfaceOps) {
//        if(TextUtils.isEmpty(videoPath)){
//            return;
//        }
//        videoComponent.playOnlineVideo(videoPath, true, surfaceOps);
        videoComponent.playLocalVideo("resources/rawfile/aaa_mov_bbb.mp4", true, surfaceOps);
    }

    @Override
    public void surfaceChanged(SurfaceOps surfaceOps, int i, int i1, int i2) {
    }

    @Override
    public void surfaceDestroyed(SurfaceOps surfaceOps) {
        videoComponent.stop();
    }
}
