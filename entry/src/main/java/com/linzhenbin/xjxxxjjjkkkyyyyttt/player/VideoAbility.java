package com.linzhenbin.xjxxxjjjkkkyyyyttt.player;



import com.linzhenbin.xjxxxjjjkkkyyyyttt.ResourceTable;
import com.linzhenbin.xjxxxjjjkkkyyyyttt.player.ui.adapter.HudListItemProvider;
import com.linzhenbin.xjxxxjjjkkkyyyyttt.player.ui.adapter.SampleItem;
import com.linzhenbin.xjxxxjjjkkkyyyyttt.player.ui.adapter.TestListItemProvider;
import com.linzhenbin.xjxxxjjjkkkyyyyttt.player.ui.widget.media.IjkVideoView;
import com.linzhenbin.xjxxxjjjkkkyyyyttt.player.utils.LogUtil;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.agp.components.*;
import ohos.agp.window.dialog.PopupDialog;
import ohos.agp.window.dialog.ToastDialog;
import ohos.bundle.IBundleManager;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import tv.danmaku.ijk.media.player.IMediaPlayer;


import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;


/**
 * 视频播放器：播放网络视频和本地视频
 * 默认播放传过来的网络视频，当切换视频时是本地视频
 *
 * 视频播放器页面
 */
public class VideoAbility extends Ability
        implements Component.ClickedListener, ListContainer.ItemClickedListener, Slider.ValueChangedListener {

    private static final String TAG = "VideoAilityDemo";
    private MyHandler handler = new MyHandler(EventRunner.getMainEventRunner());
    private String videoTitle;
    private String videoPath;
    private boolean mDragging;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    private HudListItemProvider listItemProvider;
    private TestListItemProvider testListItemProvider;
    private ListContainer hudListContainer;
    private ListContainer popListContainer;
    private List<SampleItem> itemHudViewList;
    private List<String> itemPopList;

    private Image img_action_pop_dialog;
    private IjkVideoView ijkVideoView;

    private Image mPauseButton;
    private Image mFfwdButton;
    private Image mRewButton;
    private Image mSwitchImag;

    private Slider mProgress;   // 进度条 ?
    private Text mEndTime;      // 结束时间
    private Text mCurrentTime;  // 现在时间

    //单位ms
    private static final long DELAYTIME = 500;
    private List<String> playList = new ArrayList<>();
    private int curIndex = -1;

    /**
     * 本地视频
     */
    private void intPlayList() {
        playList.add(getFilesDir() + File.separator + "aaa_mov_bbb.mp4");
        playList.add(getFilesDir() + File.separator + "aaa_mov_bbb.mp4");
        playList.add(getFilesDir() + File.separator + "aaa_mov_bbb.mp4");
        playList.add(getFilesDir() + File.separator + "aaa_mov_bbb.mp4");
    }

    @Override
    protected void onStart(Intent intent) {
        addActionRoute("action.fullscreenplay.slice", this.getClass().getName());
        System.out.println("进入onStart...");
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_video);
        IntentParams ip = intent.getParams();
        String data = intent.getStringParam("data");
        String url = intent.getStringParam("url");
        System.out.println("data: " + data + " url: " + url);
        videoTitle = "无";   // 设置视频标题
//        videoPath = "rtsp://192.168.43.240:8554/h264";
//        videoPath = "rtsp://192.168.31.2:554/h264";
//        videoPath = "http://219.151.31.43/liveplay-kk.rtxapp.com/live/program/live/cctv1hd/2300000/mnf.m3u8";     // 设置视频路径
        videoPath = "http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8";
        intPlayList();
        initComponent();
    }


    @Override
    protected void onActive() {
        super.onActive();
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeTask(mShowProgress);
        handler.removeAllEvent();
        if (ijkVideoView != null) {
            ijkVideoView.stopPlayback();
            ijkVideoView.release(true);
        }
    }

    //============================================================================================
    private void initComponent() {
        initHudView();              // 下方的信息流
        initIjkVideoView();         // 视频播放器
        initMediaControllerView();  // 控制条
    }

    private void initMediaControllerView() {
        mPauseButton = (Image) findComponentById(ResourceTable.Id_pause);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setClickedListener(this);
        }

        mProgress = (Slider) findComponentById(ResourceTable.Id_mediacontroller_progress);
        mCurrentTime = (Text) findComponentById(ResourceTable.Id_time_current);
        mEndTime = (Text) findComponentById(ResourceTable.Id_time);
        mFfwdButton = (Image) findComponentById(ResourceTable.Id_ffwd);
        mRewButton = (Image) findComponentById(ResourceTable.Id_rew);
        mSwitchImag = (Image) findComponentById(ResourceTable.Id_next);


        mRewButton.setClickedListener(this);
        mFfwdButton.setClickedListener(this);
        mProgress.setValueChangedListener(this);
        mSwitchImag.setClickedListener(this);

        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    private void initIjkVideoView() {
        ijkVideoView = (IjkVideoView) findComponentById(ResourceTable.Id_video_view);
        ijkVideoView.setHudView();
        ijkVideoView.setVideoPath(videoPath); // 网络视频播放测试
        //ijkVideoView.setVideoPath(playList.get(0)); // cjw 本地视频播放测试
        ijkVideoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                // 当prepared后调用该方法!!!
                // 然后handler放送通知提醒
                // note: onPrepared回调之后才能获取数据，例如播放状态，总时长等
                handler.sendEvent(MSG_UPDATE_HUD, DELAYTIME);
                handler.sendEvent(MSG_PLAYER_DEFAULT_STATUS, DELAYTIME);
            }
        });
        ijkVideoView.start();
    }

    private void initHudView() {
        hudListContainer = (ListContainer) findComponentById(ResourceTable.Id_hud_view);
    }

    private void initTitleBarView() {
        Component tv_main = findComponentById(ResourceTable.Id_tv_action_main);
        tv_main.setClickedListener(this);
        img_action_pop_dialog.requestFocus();
        img_action_pop_dialog.setClickedListener(this);
    }

    private void initData() {
        itemPopList = new ArrayList<>();
        itemPopList.add("Scale");
        itemPopList.add("Info");
        itemPopList.add("Tracks");
    }


    @Override
    public void onClick(Component component) {
        switch (component.getId()) {
            case ResourceTable.Id_pause:
                doPauseResume();
                break;
            case ResourceTable.Id_ffwd:
                int pos = (int) ijkVideoView.getCurrentPosition();
                pos += 5000; // milliseconds
                ijkVideoView.seekTo(pos);
                setProgress();
                break;
            case ResourceTable.Id_rew:
                int posRew = (int) ijkVideoView.getCurrentPosition();
                posRew -= 3000; // milliseconds
                ijkVideoView.seekTo(posRew);
                setProgress();
                break;
            case ResourceTable.Id_next:
                //切换视频
                playNextVideo();
                break;
        }
    }



    private void playNextVideo() {
        try {
            if (curIndex == playList.size() - 1) {
                curIndex = 0;
            } else {
                curIndex++;
            }
            if (ijkVideoView != null) {
                ijkVideoView.stopPlayback();
                ijkVideoView.release(true);
            }
            LogUtil.e("ijkplayer", "1=======setVideoPath===" + playList.get(curIndex));
            ijkVideoView.setVideoPath(playList.get(curIndex));
            ijkVideoView.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createPopupDialog() {
        PopupDialog popupDialog = new PopupDialog(getContext(), img_action_pop_dialog, ComponentContainer.LayoutConfig.MATCH_PARENT, ComponentContainer.LayoutConfig.MATCH_CONTENT);
        Component customComponent = LayoutScatter.getInstance(getContext()).parse(ResourceTable.Layout_custom_pop_dialog, null, false);

        popListContainer = (ListContainer) customComponent.findComponentById(ResourceTable.Id_pop_dialog_list_view);
        testListItemProvider = new TestListItemProvider(getContext(), itemPopList);
        popListContainer.setItemProvider(testListItemProvider);
        popListContainer.setItemClickedListener(this);
        popupDialog.setCustomComponent(customComponent).
                setAutoClosable(true).show();
    }

    @Override
    public void onItemClicked(ListContainer listContainer, Component component, int i, long l) {
        ToastDialog toastDialog = new ToastDialog(getContext());
        toastDialog.setText(testListItemProvider.getItem(i).toString()).show();
        switch (i) {
            case 0:
                //todo 设置视频缩放模式
                break;
            case 1:
                //todo 显示Info相关信息
//                ijkVideoView.showMediaInfo();
                break;
            case 2:
                //显示tracks信息
                break;
        }

    }

    //=========================================音视频界面控制按钮==================================================================
    private void updatePausePlay() {
        if (mPauseButton == null) {
            return;
        }
        if (ijkVideoView.isPlaying()) {
            mPauseButton.setPixelMap(ResourceTable.Media_ic_media_pause);
        } else {
            mPauseButton.setPixelMap(ResourceTable.Media_ic_media_play);
        }
    }

    private void doPauseResume() {
        if (ijkVideoView.isPlaying()) {
            ijkVideoView.pause();
        } else {
            ijkVideoView.start();
        }
        //执行暂停、播放之后再更新Ui
        updatePausePlay();
    }

    private long setProgress() {
        if (ijkVideoView == null || mDragging) {
            return 0;
        }
        long position = ijkVideoView.getCurrentPosition();  // 获取当前播放位置。
        long duration = ijkVideoView.getDuration();         // 获取总长度
//        LogUtil.i(getClass().getSimpleName() + " setProgress currentPosition:" + position + " duration:" + duration);

        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgressValue((int) pos);
            }
            int percent = ijkVideoView.getBufferPercentage();
            mProgress.setViceProgress(percent * 10);
        }
        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null) {
            mCurrentTime.setText(stringForTime(position));
        }
        return position;
    }

    private String stringForTime(long timeMs) {
        int totalSeconds = (int) (timeMs / 1000);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }


    @Override
    public void onProgressUpdated(Slider slider, int i, boolean b) {
        LogUtil.w(TAG, "onProgressUpdated  " + b);
        if (!b) {
            return;
        }
        long duration = ijkVideoView.getDuration();
        long newposition = (duration * i) / 1000L;
        ijkVideoView.seekTo((int) newposition);
        if (mCurrentTime != null) {
            mCurrentTime.setText(stringForTime((int) newposition));
        }
    }

    @Override
    public void onTouchStart(Slider slider) {
//        LogUtil.i(getClass().getSimpleName() + "onTouchStart");
        mDragging = true;
        handler.removeTask(mShowProgress);
    }

    @Override
    public void onTouchEnd(Slider slider) {
//        LogUtil.i(getClass().getSimpleName() + "onTouchEnd");
        mDragging = false;
        setProgress();
        updatePausePlay();
        handler.postTask(mShowProgress);
    }

    //===========================================handler刷新hudview===================================================
    private static final int MSG_UPDATE_HUD = 1;
    private static final int MSG_PLAYER_DEFAULT_STATUS = 2;
    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            long pos = setProgress();
            // 正在播放，并且没有拖动进度条，就一直循环改变进度条长度...
            if (!mDragging && ijkVideoView.isPlaying()) {
                handler.postTask(mShowProgress, 1000 - (pos % 1000));
            }
        }
    };

    //todo  点击暂停后，MSG_UPDATE_HUD事件循环停止
    private class MyHandler extends EventHandler {
        public MyHandler(EventRunner runner) throws IllegalArgumentException {
            super(runner);
        }

        @Override
        protected void processEvent(InnerEvent event) {
            super.processEvent(event);
            switch (event.eventId) {
                case MSG_PLAYER_DEFAULT_STATUS:
                    // 暂停 ???
//                    LogUtil.w("MSG_PLAYER_DEFAULT_STATUS  " + ijkVideoView.isPlaying());
                    setProgress();
                    updatePausePlay();
                    handler.postTask(mShowProgress);    // 推动进度条向前....
                    handler.removeEvent(MSG_PLAYER_DEFAULT_STATUS);
                    break;
                case MSG_UPDATE_HUD: {
                    //每500ms刷新一次数据
                    if (ijkVideoView != null) {
                        itemHudViewList = ijkVideoView.getHudViewDataLists();
                        if (itemHudViewList != null && itemHudViewList.size() > 0) {
                            if (listItemProvider == null) {
                                listItemProvider = new HudListItemProvider(VideoAbility.this,
                                        itemHudViewList);
                                hudListContainer.setItemProvider(listItemProvider);
                            } else {
                                listItemProvider.setData(itemHudViewList);
                            }
                        }
                    }
                    handler.removeEvent(MSG_UPDATE_HUD);
                    handler.sendEvent(MSG_UPDATE_HUD, DELAYTIME);
                }
                break;
            }
        }
    }
}
