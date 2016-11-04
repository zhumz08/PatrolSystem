package cn.zhaoliugang.android.mvpviewer;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;

public class MVPViewerApplication extends Application {
    private static MVPViewerApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;
    }

    public static Context getAppContext() {
        return sInstance;
    }

    private int result;
    private Intent intent;
    private MediaProjectionManager mMediaProjectionManager;

    public int getResult(){
        return result;
    }

    public Intent getIntent(){
        return intent;
    }

    public MediaProjectionManager getMediaProjectionManager(){
        return mMediaProjectionManager;
    }

    public void setResult(int result1){
        this.result = result1;
    }

    public void setIntent(Intent intent1){
        this.intent = intent1;
    }

    public void setMediaProjectionManager(MediaProjectionManager mMediaProjectionManager){
        this.mMediaProjectionManager = mMediaProjectionManager;
    }
}
