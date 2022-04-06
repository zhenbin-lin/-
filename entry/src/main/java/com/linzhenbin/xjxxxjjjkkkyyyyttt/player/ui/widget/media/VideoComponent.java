package com.linzhenbin.xjxxxjjjkkkyyyyttt.player.ui.widget.media;

import ohos.agp.components.surfaceprovider.SurfaceProvider;
import ohos.agp.graphics.SurfaceOps;
import ohos.agp.utils.Point;
import ohos.agp.window.service.DisplayManager;
import ohos.app.Context;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.InnerEvent;
import ohos.media.common.Source;
import ohos.media.player.Player;

import java.io.IOException;
import java.util.Optional;

public class VideoComponent extends SurfaceProvider implements Player.IPlayerCallback {

    public static final int STATE_FIRST = 0x101;
    public static final int STATE_INIT = 0x102;
    public static final int STATE_PLAYING = 0x103;
    public static final int STATE_PAUSE = 0x104;
    public static final int STATE_STOP = 0X105;
    public static final int STATE_DESTROY = 0x106;

    public static final int MESSAGE_UPDATE_PLAY_TIME = 100;
    private int  screenHeight, screenWidth;

    public void setHandler(EventHandler handler) {
        this.handler = handler;
    }

    private EventHandler handler;


    private final Player player;

    private int state = STATE_FIRST;

    public VideoComponent(Context context, SurfaceOps.Callback callback) {
        super(context);
//        screenWidth = ConvertUtils.dp2px(getContext(), getContext().getResourceManager().getDeviceCapability().width);
//        screenHeight = ConvertUtils.dp2px(getContext(), getContext().getResourceManager().getDeviceCapability().height);
        DisplayManager dm = DisplayManager.getInstance();
        Point point = new Point();
        dm.getDefaultDisplay(getContext()).get().getRealSize(point);
        screenWidth = (int) point.position[0];
        screenHeight = (int) point.position[1];
        player = new Player(getContext());
        player.setPlayerCallback(this);
        Optional<SurfaceOps> surfaceHolderOptional = getSurfaceOps();
        SurfaceOps surfaceHolder = surfaceHolderOptional.get();
        surfaceHolder.addCallback(callback);
//        setZOrderOnTop(false);
        state = STATE_INIT;
    }


    public int getAllTime() {
        return player.getDuration();
    }

    public int getPlayTime() {
        return player.getCurrentTime();
    }

    public int getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
    }

    int currentTime = 0;

    public void playOnlineVideo(String videoUrl, boolean isLooping, SurfaceOps holder) {
        // 设置播放的视频URL
        player.setSource(new Source(videoUrl));
        player.setVideoSurface(holder.getSurface());
        player.enableSingleLooping(isLooping);
        player.enableScreenOn(true);
        initPlayViewSize();
        // 在线视频播放是耗时操作，需要放在子线程播放
        getContext().getGlobalTaskDispatcher(TaskPriority.HIGH).asyncDispatch(new Runnable() {
            @Override
            public void run() {
                player.prepare();
                player.play();
                player.rewindTo(getCurrentTime() * 1000);
                state = STATE_PLAYING;
                handler.sendEvent(InnerEvent.get(MESSAGE_UPDATE_PLAY_TIME));
            }
        });
    }

    public void playLocalVideo(String fileName, boolean isLooping, SurfaceOps holder) {
        try {
            player.setSource(getContext().getResourceManager().getRawFileEntry(fileName).openRawFileDescriptor());
            player.setVideoSurface(holder.getSurface());
            player.enableSingleLooping(isLooping);
            player.enableScreenOn(true);
            initPlayViewSize();
            // 在线视频播放是耗时操作，需要放在子线程播放
            getContext().getGlobalTaskDispatcher(TaskPriority.HIGH).asyncDispatch(new Runnable() {
                @Override
                public void run() {
                    player.prepare();
                    player.play();
                    player.rewindTo(getCurrentTime() * 1000);
                    state = STATE_PLAYING;
                    handler.sendEvent(InnerEvent.get(MESSAGE_UPDATE_PLAY_TIME));
                }
            });
        } catch (IOException e) {
        }
    }

    public void playDistributedVideo(Source source, boolean isLooping, SurfaceOps holder) {
        player.setSource(source);
        player.setVideoSurface(holder.getSurface());
        player.enableSingleLooping(isLooping);
        player.enableScreenOn(true);
        initPlayViewSize();
        // 在线视频播放是耗时操作，需要放在子线程播放
        getContext().getGlobalTaskDispatcher(TaskPriority.HIGH).asyncDispatch(new Runnable() {
            @Override
            public void run() {
                player.prepare();
                player.play();
                player.rewindTo(getCurrentTime() * 1000);
                state = STATE_PLAYING;
                handler.sendEvent(InnerEvent.get(MESSAGE_UPDATE_PLAY_TIME));
            }
        });
    }

    private void initPlayViewSize() {
        int videoWidth = player.getVideoWidth();
        int videoHeight = player.getVideoHeight();
        if (videoWidth < videoHeight) {
            double scale = screenHeight * 1.f / videoHeight;
            double currHeight = videoHeight * scale;
            double currWidth = videoWidth * scale;
            setHeight(((int) currHeight));
            setWidth(((int) currWidth));
        } else {
            double scale = screenWidth * 1.f / videoWidth;
            double currHeight = videoHeight * scale;
            double currWidth = videoWidth * scale;
            setHeight(((int) currHeight));
            setWidth(((int) currWidth));
        }
    }

    public void start() {
        if (state == STATE_PAUSE || state == STATE_INIT) {
            player.play();
            state = STATE_PLAYING;
            handler.sendEvent(InnerEvent.get(MESSAGE_UPDATE_PLAY_TIME), 500);
        }
    }

    public void pause() {
        if (state == STATE_PLAYING) {
            player.pause();
            handler.removeAllEvent();
            state = STATE_PAUSE;
        }
    }

    public void stop() {
        if (state == STATE_PLAYING || state == STATE_PAUSE) {
//            currentTime = player.getCurrentTime();
            setCurrentTime(player.getCurrentTime());
            player.pause();
            handler.removeAllEvent();
            state = STATE_STOP;
        }
    }

    public int getPlayState() {
        return state;
    }

    public void destroy() {
        player.stop();
        player.release();
        handler.removeAllEvent();
        state = STATE_DESTROY;
    }

    @Override
    public void onPlayBackComplete() {
        handler.sendEvent(InnerEvent.get(MESSAGE_UPDATE_PLAY_TIME), EventHandler.Priority.HIGH);
    }

    @Override
    public void onRewindToComplete() {
    }

    @Override
    public void onBufferingChange(int i) {

    }

    @Override
    public void onNewTimedMetaData(Player.MediaTimedMetaData mediaTimedMetaData) {
    }

    @Override
    public void onMediaTimeIncontinuity(Player.MediaTimeInfo mediaTimeInfo) {
    }

    @Override
    public void onPrepared() {

    }

    @Override
    public void onMessage(int i, int i1){
    }

    @Override
    public void onError(int i, int i1) {
        state = STATE_INIT;
        player.stop();
    }

    @Override
    public void onResolutionChanged(int i, int i1) {

    }


}

