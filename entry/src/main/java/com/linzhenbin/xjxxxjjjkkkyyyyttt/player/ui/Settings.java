package com.linzhenbin.xjxxxjjjkkkyyyyttt.player.ui;///*

import ohos.app.Context;
import ohos.data.DatabaseHelper;

public class Settings {
    private Context mAppContext;
    private DatabaseHelper mSharedPreferences;//DatabaseHelper

    public static final int PV_PLAYER__Auto = 0;
    public static final int PV_PLAYER__AndroidMediaPlayer = 1;
    public static final int PV_PLAYER__IjkMediaPlayer = 2;
    public static final int PV_PLAYER__IjkExoMediaPlayer = 3;

    public Settings(Context context) {
        mAppContext = context.getApplicationContext();
//        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mAppContext);
        mSharedPreferences = new DatabaseHelper(mAppContext);
    }

    public boolean getEnableBackgroundPlay() {
        String key = "";
//        return mSharedPreferences.getBoolean(key, false);
        return mSharedPreferences.getPreferences(mAppContext.getPreferencesDir().getName()).getBoolean(key, false);
    }

    public int getPlayer() {
        String key = "";
        String value = mSharedPreferences.getPreferences(mAppContext.getPreferencesDir().getName()).getString(key, "");
        try {
            return Integer.valueOf(value).intValue();
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean getUsingMediaCodec() {
//        String key = mAppContext.getString(R.string.pref_key_using_media_codec);
        String key = "";
        return mSharedPreferences.getPreferences(mAppContext.getPreferencesDir().getName()).getBoolean(key, false);
    }

    public boolean getUsingMediaCodecAutoRotate() {
        String key = "";
        return mSharedPreferences.getPreferences(mAppContext.getPreferencesDir().getName()).getBoolean(key, false);
    }

    public boolean getMediaCodecHandleResolutionChange() {
        String key = "";
        return mSharedPreferences.getPreferences(mAppContext.getPreferencesDir().getName()).getBoolean(key, false);
    }

    public boolean getUsingOpenSLES() {
        String key = "";
        return mSharedPreferences.getPreferences(mAppContext.getPreferencesDir().getName()).getBoolean(key, false);
    }

    public String getPixelFormat() {
        String key = "";
        return mSharedPreferences.getPreferences(mAppContext.getPreferencesDir().getName()).getString(key, "");
    }

    public boolean getEnableNoView() {
        String key = "";
        return mSharedPreferences.getPreferences(mAppContext.getPreferencesDir().getName()).getBoolean(key, false);
    }

    public boolean getEnableSurfaceView() {
        String key = "";
        return mSharedPreferences.getPreferences(mAppContext.getPreferencesDir().getName()).getBoolean(key, false);
    }

    public boolean getEnableTextureView() {
        String key = "";
        return mSharedPreferences.getPreferences(mAppContext.getPreferencesDir().getName()).getBoolean(key, false);
    }

    public boolean getEnableDetachedSurfaceTextureView() {
        String key = "";
        return mSharedPreferences.getPreferences(mAppContext.getPreferencesDir().getName()).getBoolean(key, false);
    }

    public boolean getUsingMediaDataSource() {
        String key = "";
        return mSharedPreferences.getPreferences(mAppContext.getPreferencesDir().getName()).getBoolean(key, false);
    }

    public String getLastDirectory() {
        String key = "";
        return mSharedPreferences.getPreferences(mAppContext.getPreferencesDir().getName()).getString(key, "/");
    }

    public void setLastDirectory(String path) {
        String key = "";
//        mSharedPreferences.edit().putString(key, path).apply();
        mSharedPreferences.getPreferences(mAppContext.getPreferencesDir().getName()).putString(key, path).flush();
    }
}
