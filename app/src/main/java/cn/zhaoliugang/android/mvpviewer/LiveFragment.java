package cn.zhaoliugang.android.mvpviewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.mvp.v2.listeners.LoginListener;
import com.zxing.activity.CaptureActivity;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.vlc.util.VLCInstance;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.zhaoliugang.android.mvpviewer.protocol.MVPSimpleProtocol;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class LiveFragment extends Fragment implements View.OnClickListener, View.OnTouchListener, SurfaceHolder.Callback, IVideoPlayer {
    public static final String TAG = "LiveFragment";

    private SurfaceView mSurfaceView;

    private LibVLC mMediaPlayer;
    private SurfaceHolder mSurfaceHolder;

    private int result = 0;
    private Intent intent = null;
    private int REQUEST_MEDIA_PROJECTION = 2;
    private MediaProjectionManager mMediaProjectionManager;
    CaptureTool mCaptureTool = null;

    private int mVideoHeight;
    private int mVideoWidth;
    private int mVideoVisibleHeight;
    private int mVideoVisibleWidth;
    private int mSarNum;
    private int mSarDen;

    Button mBtnUp,mBtnDown,mBtnLeft,mBtnRight;
    Button mBtnSpeed1,mBtnSpeed2,mBtnSpeed3,mBtnSpeed4,mBtnSpeed5;
    Button mBtnPreset0H,mBtnPreset1H,mBtnPreset2H,mBtnPreset3H,mBtnPreset4H,mBtnPreset5H,mBtnPreset6H,mBtnPreset7H,mBtnPreset8H,mBtnPreset9H;
    Button mBtnPreset0L,mBtnPreset1L,mBtnPreset2L,mBtnPreset3L,mBtnPreset4L,mBtnPreset5L,mBtnPreset6L,mBtnPreset7L,mBtnPreset8L,mBtnPreset9L;
    Button mBtnPtzGqd,mBtnPtzGqx,mBtnPtzFd,mBtnPtzSx,mBtnPtzJjy,mBtnPtzJjj,mBtnPtzDyzw,mBtnPtzSyzw,mBtnPtzDkg,mBtnPtzYs;

    private Button scanBarCodeButton,submitGuardButton,screenshotButton;
    protected int mScreenWidth ;

    // ptz速度
    int mSpeed = 3;
    // 预置位高位
    int mPresetH = 0;
    // 预置位低位
    int mPresetL = 0;

    public String getmStrPlayAddr() {
        return mStrPlayAddr;
    }

    public void setmStrPlayAddr(String PlayAddr, long CameraId) {
        this.mStrPlayAddr = PlayAddr;
        //this.mStrPlayAddr = "rtsp://218.204.223.237:554/live/1/66251FC11353191F/e7ooqwcfbqjoo80j.sdp";
        //this.mStrPlayAddr = "rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov";
        //this.mStrPlayAddr = "rtsp://192.168.199.100:8554/1";//guard system test
        this.mCameraId = CameraId;
    }

    private String mStrPlayAddr=""; //rtsp://192.168.1.232:40001/1/264
    private long mCameraId = 0;
    private String mGuardAddressId = "0";
    private String mGuardAddressName = "0";

    public LiveFragment() {
        // Required empty public constructor
    }

    protected void initButton(View view) {
        scanBarCodeButton = (Button) view.findViewById(R.id.btn_qr_scan);
        scanBarCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 调用ZXIng开源项目源码  扫描二维码
                Intent openCameraIntent = new Intent(getActivity(), CaptureActivity.class);
                startActivityForResult(openCameraIntent, Activity.RESULT_FIRST_USER);
            }
        });

        submitGuardButton = (Button) view.findViewById(R.id.btn_guard_record);
        submitGuardButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //调用巡更系统
                Intent intent = new Intent();
                intent.setClass(getActivity(), GuardActivity.class);
                startActivity(intent);
            }
        });
        screenshotButton = (Button)view.findViewById(R.id.btn_screenshot);
        screenshotButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //视频截图
                //screenshotvideo();
                //screenshot(v);
                capturescreen();
            }
        });
    }

    public void capturescreen(){
        if(mCaptureTool!=null) {
            mCaptureTool.Capture(String.valueOf(mCameraId),mGuardAddressId,mGuardAddressName);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }else if(data != null && resultCode != 0){
                Log.i(TAG, "user agree the application to capture screen");
                result = resultCode;
                intent = data;
                ((MVPViewerApplication)getActivity().getApplication()).setResult(resultCode);
                ((MVPViewerApplication)getActivity().getApplication()).setIntent(data);
                Log.i(TAG, "start service CaptureTool");
            }
        }
        // 取得返回信息
        if (requestCode == Activity.RESULT_FIRST_USER && resultCode == getActivity().RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("result");
            if (scanResult != null && scanResult.length() > 0) {
                System.out.println("LiveFragment onActivityResult");
                Process(scanResult);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        //String testAddr = "rtsp://192.168.199.100:8554/1";//guard system test
        //setmStrPlayAddr(testAddr,1);//test

        mCaptureTool = new CaptureTool(getContext());
    }

    private void startIntent(){
        if(intent != null && result != 0){
            Log.i(TAG, "user agree the application to capture screen");
            ((MVPViewerApplication)getActivity().getApplication()).setResult(result);
            ((MVPViewerApplication)getActivity().getApplication()).setIntent(intent);
            Log.i(TAG, "start service CaptureTool");
        }else{
            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
            ((MVPViewerApplication)getActivity().getApplication()).setMediaProjectionManager(mMediaProjectionManager);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_live, container, false);
        mSurfaceView = (SurfaceView) view.findViewById(R.id.surface_view);
        try {
            mMediaPlayer = VLCInstance.getLibVlcInstance();
        } catch (LibVlcException e) {
            e.printStackTrace();
        }

        mMediaProjectionManager = (MediaProjectionManager)getActivity().getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startIntent();
        initButton(view);

        mBtnUp = (Button)view.findViewById(R.id.btn_ptz_up);
        mBtnUp.setOnClickListener(this);
        mBtnUp.setOnTouchListener(this);
        mBtnDown = (Button)view.findViewById(R.id.btn_ptz_down);
        mBtnDown.setOnClickListener(this);
        mBtnDown.setOnTouchListener(this);
        mBtnLeft = (Button)view.findViewById(R.id.btn_ptz_left);
        mBtnLeft.setOnClickListener(this);
        mBtnLeft.setOnTouchListener(this);
        mBtnRight = (Button)view.findViewById(R.id.btn_ptz_right);
        mBtnRight.setOnClickListener(this);
        mBtnRight.setOnTouchListener(this);

        mBtnSpeed1 = (Button)view.findViewById(R.id.btn_speed_1);
        mBtnSpeed1.setOnClickListener(this);
        mBtnSpeed2 = (Button)view.findViewById(R.id.btn_speed_2);
        mBtnSpeed2.setOnClickListener(this);
        mBtnSpeed3 = (Button)view.findViewById(R.id.btn_speed_3);
        mBtnSpeed3.setOnClickListener(this);
        mBtnSpeed4 = (Button)view.findViewById(R.id.btn_speed_4);
        mBtnSpeed4.setOnClickListener(this);
        mBtnSpeed5 = (Button)view.findViewById(R.id.btn_speed_5);
        mBtnSpeed5.setOnClickListener(this);

        mBtnPtzGqd = (Button)view.findViewById(R.id.btn_ptz_gqd);
        mBtnPtzGqd.setOnClickListener(this);
        mBtnPtzGqd.setOnTouchListener(this);
        mBtnPtzGqx = (Button)view.findViewById(R.id.btn_ptz_gqx);
        mBtnPtzGqx.setOnClickListener(this);
        mBtnPtzGqx.setOnTouchListener(this);
        mBtnPtzFd = (Button)view.findViewById(R.id.btn_ptz_fd);
        mBtnPtzFd.setOnClickListener(this);
        mBtnPtzFd.setOnTouchListener(this);
        mBtnPtzSx = (Button)view.findViewById(R.id.btn_ptz_sx);
        mBtnPtzSx.setOnClickListener(this);
        mBtnPtzSx.setOnTouchListener(this);
        mBtnPtzJjy = (Button)view.findViewById(R.id.btn_ptz_jjy);
        mBtnPtzJjy.setOnClickListener(this);
        mBtnPtzJjy.setOnTouchListener(this);
        mBtnPtzJjj = (Button)view.findViewById(R.id.btn_ptz_jjj);
        mBtnPtzJjj.setOnClickListener(this);
        mBtnPtzJjj.setOnTouchListener(this);
        mBtnPtzDyzw = (Button)view.findViewById(R.id.btn_ptz_dyzw);
        mBtnPtzDyzw.setOnClickListener(this);
        mBtnPtzDyzw.setOnTouchListener(this);
        mBtnPtzSyzw = (Button)view.findViewById(R.id.btn_ptz_syzw);
        mBtnPtzSyzw.setOnClickListener(this);
        mBtnPtzSyzw.setOnTouchListener(this);
        mBtnPtzDkg = (Button)view.findViewById(R.id.btn_ptz_dkg);
        mBtnPtzDkg.setOnClickListener(this);
        mBtnPtzDkg.setOnTouchListener(this);
        mBtnPtzYs = (Button)view.findViewById(R.id.btn_ptz_ys);
        mBtnPtzYs.setOnClickListener(this);
        mBtnPtzYs.setOnTouchListener(this);

        mBtnPreset0H = (Button)view.findViewById(R.id.btn_yzw_h0);
        mBtnPreset0H.setOnClickListener(this);
        mBtnPreset1H = (Button)view.findViewById(R.id.btn_yzw_h1);
        mBtnPreset1H.setOnClickListener(this);
        mBtnPreset2H = (Button)view.findViewById(R.id.btn_yzw_h2);
        mBtnPreset2H.setOnClickListener(this);
        mBtnPreset3H = (Button)view.findViewById(R.id.btn_yzw_h3);
        mBtnPreset3H.setOnClickListener(this);
        mBtnPreset4H = (Button)view.findViewById(R.id.btn_yzw_h4);
        mBtnPreset4H.setOnClickListener(this);
        mBtnPreset5H = (Button)view.findViewById(R.id.btn_yzw_h5);
        mBtnPreset5H.setOnClickListener(this);
        mBtnPreset6H = (Button)view.findViewById(R.id.btn_yzw_h6);
        mBtnPreset6H.setOnClickListener(this);
        mBtnPreset7H = (Button)view.findViewById(R.id.btn_yzw_h7);
        mBtnPreset7H.setOnClickListener(this);
        mBtnPreset8H = (Button)view.findViewById(R.id.btn_yzw_h8);
        mBtnPreset8H.setOnClickListener(this);
        mBtnPreset9H = (Button)view.findViewById(R.id.btn_yzw_h9);
        mBtnPreset9H.setOnClickListener(this);

        mBtnPreset0L = (Button)view.findViewById(R.id.btn_yzw_l0);
        mBtnPreset0L.setOnClickListener(this);
        mBtnPreset1L = (Button)view.findViewById(R.id.btn_yzw_l1);
        mBtnPreset1L.setOnClickListener(this);
        mBtnPreset2L = (Button)view.findViewById(R.id.btn_yzw_l2);
        mBtnPreset2L.setOnClickListener(this);
        mBtnPreset3L = (Button)view.findViewById(R.id.btn_yzw_l3);
        mBtnPreset3L.setOnClickListener(this);
        mBtnPreset4L = (Button)view.findViewById(R.id.btn_yzw_l4);
        mBtnPreset4L.setOnClickListener(this);
        mBtnPreset5L = (Button)view.findViewById(R.id.btn_yzw_l5);
        mBtnPreset5L.setOnClickListener(this);
        mBtnPreset6L = (Button)view.findViewById(R.id.btn_yzw_l6);
        mBtnPreset6L.setOnClickListener(this);
        mBtnPreset7L = (Button)view.findViewById(R.id.btn_yzw_l7);
        mBtnPreset7L.setOnClickListener(this);
        mBtnPreset8L = (Button)view.findViewById(R.id.btn_yzw_l8);
        mBtnPreset8L.setOnClickListener(this);
        mBtnPreset9L = (Button)view.findViewById(R.id.btn_yzw_l9);
        mBtnPreset9L.setOnClickListener(this);

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
        mSurfaceHolder.addCallback(this);

        scanBarCodeButton=(Button)view.findViewById(R.id.btn_qr_scan);
        submitGuardButton=(Button)view.findViewById(R.id.btn_guard_record);

        mMediaPlayer.eventVideoPlayerActivityCreated(true);

        EventHandler em = EventHandler.getInstance();
        em.addHandler(mVlcHandler);

        //this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        //mSurfaceView.setKeepScreenOn(true);
        //		mMediaPlayer.setMediaList();
        //		mMediaPlayer.getMediaList().add(new Media(mMediaPlayer, "http://live.3gv.ifeng.com/zixun.m3u8"), false);
        //		mMediaPlayer.playIndex(0);
        //mMediaPlayer.playMRL("http://live.3gv.ifeng.com/zixun.m3u8");
        //mMediaPlayer.playMRL("rtsp://192.168.1.22:554/HD");

        startPlay();
        return view;
    }

    private void resetSpeedBackground(){
        mBtnSpeed1.setBackground(getResources().getDrawable(R.drawable.b1));
        mBtnSpeed2.setBackground(getResources().getDrawable(R.drawable.b2));
        mBtnSpeed3.setBackground(getResources().getDrawable(R.drawable.b3));
        mBtnSpeed4.setBackground(getResources().getDrawable(R.drawable.b4));
        mBtnSpeed5.setBackground(getResources().getDrawable(R.drawable.b5));
    }
    private void resetPresetHBackground(){
        mBtnPreset0H.setBackground(getResources().getDrawable(R.drawable.b0));
        mBtnPreset1H.setBackground(getResources().getDrawable(R.drawable.b1));
        mBtnPreset2H.setBackground(getResources().getDrawable(R.drawable.b2));
        mBtnPreset3H.setBackground(getResources().getDrawable(R.drawable.b3));
        mBtnPreset4H.setBackground(getResources().getDrawable(R.drawable.b4));
        mBtnPreset5H.setBackground(getResources().getDrawable(R.drawable.b5));
        mBtnPreset6H.setBackground(getResources().getDrawable(R.drawable.b6));
        mBtnPreset7H.setBackground(getResources().getDrawable(R.drawable.b7));
        mBtnPreset8H.setBackground(getResources().getDrawable(R.drawable.b8));
        mBtnPreset9H.setBackground(getResources().getDrawable(R.drawable.b9));
    }
    private void resetPresetLBackground(){
        mBtnPreset0L.setBackground(getResources().getDrawable(R.drawable.b0));
        mBtnPreset1L.setBackground(getResources().getDrawable(R.drawable.b1));
        mBtnPreset2L.setBackground(getResources().getDrawable(R.drawable.b2));
        mBtnPreset3L.setBackground(getResources().getDrawable(R.drawable.b3));
        mBtnPreset4L.setBackground(getResources().getDrawable(R.drawable.b4));
        mBtnPreset5L.setBackground(getResources().getDrawable(R.drawable.b5));
        mBtnPreset6L.setBackground(getResources().getDrawable(R.drawable.b6));
        mBtnPreset7L.setBackground(getResources().getDrawable(R.drawable.b7));
        mBtnPreset8L.setBackground(getResources().getDrawable(R.drawable.b8));
        mBtnPreset9L.setBackground(getResources().getDrawable(R.drawable.b9));
    }

    public void onClick(View v) {
//        if (v == mBtnUp) {
//            MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,3,mSpeed);
//        }
//        else if (v == mBtnDown) {
//            MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,4,mSpeed);
//        }
//        else if (v == mBtnLeft) {
//            MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,2,mSpeed);
//        }
//        else if (v == mBtnRight) {
//            MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,1,mSpeed);
//        }

        if (v == mBtnSpeed1) {
            mSpeed = 1;
            resetSpeedBackground();
            mBtnSpeed1.setBackground(getResources().getDrawable(R.drawable.b1_1));
        }
        else if (v == mBtnSpeed2) {
            mSpeed = 2;
            resetSpeedBackground();
            mBtnSpeed2.setBackground(getResources().getDrawable(R.drawable.b2_1));
        }
        else if (v == mBtnSpeed3) {
            mSpeed = 3;
            resetSpeedBackground();
            mBtnSpeed3.setBackground(getResources().getDrawable(R.drawable.b3_1));
        }
        else if (v == mBtnSpeed4) {
            mSpeed = 4;
            resetSpeedBackground();
            mBtnSpeed4.setBackground(getResources().getDrawable(R.drawable.b4_1));
        }
        else if (v == mBtnSpeed5) {
            mSpeed = 5;
            resetSpeedBackground();
            mBtnSpeed5.setBackground(getResources().getDrawable(R.drawable.b5_1));
        }

//        else if (v == mBtnPtzGqd) {
//            MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,13,mSpeed);
//        }
//        else if (v == mBtnPtzGqx) {
//            MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,14,mSpeed);
//        }
//        else if (v == mBtnPtzFd) {
//            MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,9,mSpeed);
//        }
//        else if (v == mBtnPtzSx) {
//            MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,10,mSpeed);
//        }
//        else if (v == mBtnPtzJjy) {
//            MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,12,mSpeed);
//        }
//        else if (v == mBtnPtzJjj) {
//            MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,11,mSpeed);
//        }
//        else if (v == mBtnPtzDyzw) {
//            MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,16,mPresetH*10 + mPresetL);
//        }
//        else if (v == mBtnPtzSyzw) {
//            MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,15,mPresetH*10 + mPresetL);
//        }
//        else if (v == mBtnPtzDkg) {
//            MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,17,0);
//        }
//        else if (v == mBtnPtzYs) {
//            MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,19,0);
//        }

        else if (v == mBtnPreset0H) {
            mPresetH = 0;
            resetPresetHBackground();
            mBtnPreset0H.setBackground(getResources().getDrawable(R.drawable.b0_1));
        }
        else if (v == mBtnPreset1H) {
            mPresetH = 1;
            resetPresetHBackground();
            mBtnPreset1H.setBackground(getResources().getDrawable(R.drawable.b1_1));
        }
        else if (v == mBtnPreset2H) {
            mPresetH = 2;
            resetPresetHBackground();
            mBtnPreset2H.setBackground(getResources().getDrawable(R.drawable.b2_1));
        }
        else if (v == mBtnPreset3H) {
            mPresetH = 3;
            resetPresetHBackground();
            mBtnPreset3H.setBackground(getResources().getDrawable(R.drawable.b3_1));
        }
        else if (v == mBtnPreset4H) {
            mPresetH = 4;
            resetPresetHBackground();
            mBtnPreset4H.setBackground(getResources().getDrawable(R.drawable.b4_1));
        }
        else if (v == mBtnPreset5H) {
            mPresetH = 5;
            resetPresetHBackground();
            mBtnPreset5H.setBackground(getResources().getDrawable(R.drawable.b5_1));
        }
        else if (v == mBtnPreset6H) {
            mPresetH = 6;
            resetPresetHBackground();
            mBtnPreset6H.setBackground(getResources().getDrawable(R.drawable.b6_1));
        }
        else if (v == mBtnPreset7H) {
            mPresetH = 7;
            resetPresetHBackground();
            mBtnPreset7H.setBackground(getResources().getDrawable(R.drawable.b7_1));
        }
        else if (v == mBtnPreset8H) {
            mPresetH = 8;
            resetPresetHBackground();
            mBtnPreset8H.setBackground(getResources().getDrawable(R.drawable.b8_1));
        }
        else if (v == mBtnPreset9H) {
            mPresetH = 9;
            resetPresetHBackground();
            mBtnPreset9H.setBackground(getResources().getDrawable(R.drawable.b9_1));
        }

        else if (v == mBtnPreset0L) {
            mPresetL = 0;
            resetPresetLBackground();
            mBtnPreset0L.setBackground(getResources().getDrawable(R.drawable.b0_1));
        }
        else if (v == mBtnPreset1L) {
            mPresetL = 1;
            resetPresetLBackground();
            mBtnPreset1L.setBackground(getResources().getDrawable(R.drawable.b1_1));
        }
        else if (v == mBtnPreset2L) {
            mPresetL = 2;
            resetPresetLBackground();
            mBtnPreset2L.setBackground(getResources().getDrawable(R.drawable.b2_1));
        }
        else if (v == mBtnPreset3L) {
            mPresetL = 3;
            resetPresetLBackground();
            mBtnPreset3L.setBackground(getResources().getDrawable(R.drawable.b3_1));
        }
        else if (v == mBtnPreset4L) {
            mPresetL = 4;
            resetPresetLBackground();
            mBtnPreset4L.setBackground(getResources().getDrawable(R.drawable.b4_1));
        }
        else if (v == mBtnPreset5L) {
            mPresetL = 5;
            resetPresetLBackground();
            mBtnPreset5L.setBackground(getResources().getDrawable(R.drawable.b5_1));
        }
        else if (v == mBtnPreset6L) {
            mPresetL = 6;
            resetPresetLBackground();
            mBtnPreset6L.setBackground(getResources().getDrawable(R.drawable.b6_1));
        }
        else if (v == mBtnPreset7L) {
            mPresetL = 7;
            resetPresetLBackground();
            mBtnPreset7L.setBackground(getResources().getDrawable(R.drawable.b7_1));
        }
        else if (v == mBtnPreset8L) {
            mPresetL = 8;
            resetPresetLBackground();
            mBtnPreset8L.setBackground(getResources().getDrawable(R.drawable.b8_1));
        }
        else if (v == mBtnPreset9L) {
            mPresetL = 9;
            resetPresetLBackground();
            mBtnPreset9L.setBackground(getResources().getDrawable(R.drawable.b9_1));
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (mCameraId<0){
            return false;
        }

        if(v==mBtnUp){
            if(event.getAction() == MotionEvent.ACTION_UP){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,0,0);
            }
            else if(event.getAction() == MotionEvent.ACTION_DOWN){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,3,mSpeed*30);
            }
        }
        else if(v==mBtnDown){
            if(event.getAction() == MotionEvent.ACTION_UP){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,0,0);
            }
            else if(event.getAction() == MotionEvent.ACTION_DOWN){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,4,mSpeed*30);
            }
        }
        else if(v==mBtnLeft){
            if(event.getAction() == MotionEvent.ACTION_UP){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,0,0);
            }
            else if(event.getAction() == MotionEvent.ACTION_DOWN){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,2,mSpeed*30);
            }
        }
        else if(v==mBtnRight){
            if(event.getAction() == MotionEvent.ACTION_UP){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,0,0);
            }
            else if(event.getAction() == MotionEvent.ACTION_DOWN){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,1,mSpeed*30);
            }
        }

        else if(v==mBtnPtzGqd){
            if(event.getAction() == MotionEvent.ACTION_UP){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,0,0);
            }
            else if(event.getAction() == MotionEvent.ACTION_DOWN){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,13,mSpeed);
            }
        }
        else if(v==mBtnPtzGqx){
            if(event.getAction() == MotionEvent.ACTION_UP){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,0,0);
            }
            else if(event.getAction() == MotionEvent.ACTION_DOWN){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,14,mSpeed);
            }
        }
        else if(v==mBtnPtzFd){
            if(event.getAction() == MotionEvent.ACTION_UP){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,0,0);
            }
            else if(event.getAction() == MotionEvent.ACTION_DOWN){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,9,mSpeed);
            }
        }
        else if(v==mBtnPtzSx){
            if(event.getAction() == MotionEvent.ACTION_UP){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,0,0);
            }
            else if(event.getAction() == MotionEvent.ACTION_DOWN){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,10,mSpeed);
            }
        }
        else if(v==mBtnPtzJjy){
            if(event.getAction() == MotionEvent.ACTION_UP){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,0,0);
            }
            else if(event.getAction() == MotionEvent.ACTION_DOWN){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,12,mSpeed);
            }
        }
        else if(v==mBtnPtzJjj){
            if(event.getAction() == MotionEvent.ACTION_UP){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,0,0);
            }
            else if(event.getAction() == MotionEvent.ACTION_DOWN){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,11,mSpeed);
            }
        }
        else if(v==mBtnPtzDyzw){
            if(event.getAction() == MotionEvent.ACTION_UP){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,16,mPresetH*10 + mPresetL);
            }
            else if(event.getAction() == MotionEvent.ACTION_DOWN){
            }
        }
        else if(v==mBtnPtzSyzw){
            if(event.getAction() == MotionEvent.ACTION_UP){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,15,mPresetH*10 + mPresetL);
            }
            else if(event.getAction() == MotionEvent.ACTION_DOWN){
            }
        }
        else if(v==mBtnPtzDkg){
            if(event.getAction() == MotionEvent.ACTION_UP){
            }
            else if(event.getAction() == MotionEvent.ACTION_DOWN){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,17,0);
            }
        }
        else if(v==mBtnPtzYs){
            if(event.getAction() == MotionEvent.ACTION_UP){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,0,0);
            }
            else if(event.getAction() == MotionEvent.ACTION_DOWN){
                MVPSimpleProtocol.getInstance().ptzcontrol(mCameraId,19,0);
            }
        }
        return true;
    }

    public void startPlay()
    {
        if (mStrPlayAddr!="") {
            mSurfaceView.setVisibility(View.VISIBLE);
            mSurfaceView.setKeepScreenOn(true);
            mMediaPlayer.playMRL(mStrPlayAddr);
        }
        else {
            mSurfaceView.setVisibility(View.INVISIBLE);
        }
    }
    private String getpathfile(String id)
    {
        if(id == null || id.equals("")){
            return "";
        }
        File dir = null;
        dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //dir = Environment.getExternalStorageDirectory();
        String path = dir.getPath() +"/Screenshots/";
        String pathfile = path + id + getDataTimeString(true,0)+".jpg";

        try
        {
            File file = new File(path);
            if (!file.exists())
            {
                if (!file.mkdirs())
                {
                    return "";
                }
            }
            return pathfile;
        }
        catch (Exception ex)
        {
            return "";
        }
        finally
        {

        }
    }

    private static String getDataTimeString(Boolean isfilename, int second)
    {
        try
        {
            SimpleDateFormat formatter = null;
            if (!isfilename)
            {
                formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            }
            else
            {
                formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            }
            Date curDate = new Date(System.currentTimeMillis() + second*1000);// 获取当前时间
            return formatter.format(curDate);
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
            return "";
        }
    }
    public int screenshotvideo()
    {
        Toast.makeText(getActivity(), "准备视频截图", Toast.LENGTH_SHORT).show();
        if (mStrPlayAddr!=""&&mMediaPlayer.isPlaying()) {
            Toast.makeText(getActivity(), "视频截图", Toast.LENGTH_SHORT).show();
            byte[] pic = mMediaPlayer.getThumbnail(mStrPlayAddr, 100, 100);
            if(pic!=null&&pic.length!=0){
                Bitmap bmp = BitmapFactory.decodeByteArray(pic, 0, pic.length);
                File file = new File(getpathfile(String.valueOf(mCameraId)));
                if (file.equals("")){
                    Toast.makeText(getActivity(), "路径错误，截图保存失败", Toast.LENGTH_SHORT).show();
                    return -1;
                }

                try{
                    if(file.exists()){
                        file.delete();
                    }
                    file.createNewFile();
                    FileOutputStream fOut = null;
                    fOut = new FileOutputStream(file);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100,fOut);
                    fOut.flush();
                    fOut.close();
                    Toast.makeText(getActivity(), "保存截图成功，请上传至巡更系统", Toast.LENGTH_SHORT).show();
                    return 0;

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        }
        Toast.makeText(getActivity(), "视频截图不成功", Toast.LENGTH_SHORT).show();
        return -1;
    }

    public void screenshot(View v)
    {   //此方法只支持普通view，不支持SurfaceView
        Toast.makeText(getActivity(), "准备手机视频截图", Toast.LENGTH_SHORT).show();
        View view = v.getRootView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();

        if (bmp != null) {
            Toast.makeText(getActivity(), "视频截图", Toast.LENGTH_SHORT).show();
                File file = new File(getpathfile(String.valueOf(mCameraId)));
                if (file.equals("")){
                    Toast.makeText(getActivity(), "路径错误，截图保存失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                try{
                    if(file.exists()){
                        file.delete();
                    }
                    file.createNewFile();
                    FileOutputStream fOut = null;
                    fOut = new FileOutputStream(file);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100,fOut);
                    fOut.flush();
                    fOut.close();
                    Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(file);
                    media.setData(contentUri);
                    getActivity().sendBroadcast(media);
                    Toast.makeText(getActivity(), "保存截图成功，请上传至巡更系统", Toast.LENGTH_SHORT).show();
                    return;

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        Toast.makeText(getActivity(), "视频截图不成功", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onStart() {
        super.onStart();
        startPlay();
    }

    @Override
    public void onResume() {
        super.onResume();
        startPlay();
    }

    public void hideView()
    {
        mSurfaceView.setVisibility(View.INVISIBLE);
    }
    public void showView()
    {
        mSurfaceView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mSurfaceView.setKeepScreenOn(false);
            mSurfaceView.setVisibility(View.INVISIBLE);
        }
        else {
            mSurfaceView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.eventVideoPlayerActivityCreated(false);

            EventHandler em = EventHandler.getInstance();
            em.removeHandler(mVlcHandler);
        }
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        setSurfaceSize(mVideoWidth, mVideoHeight, mVideoVisibleWidth, mVideoVisibleHeight, mSarNum, mSarDen);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mMediaPlayer != null) {
            mSurfaceHolder = holder;
            mMediaPlayer.attachSurface(holder.getSurface(), this);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceHolder = holder;
        if (mMediaPlayer != null) {
            mMediaPlayer.attachSurface(holder.getSurface(), this);//, width, height
        }
        if (width > 0) {
            mVideoHeight = height;
            mVideoWidth = width;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mMediaPlayer != null) {
            mMediaPlayer.detachSurface();
        }
    }

    @Override
    public void setSurfaceSize(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den) {
        mVideoHeight = height;
        mVideoWidth = width;
        mVideoVisibleHeight = visible_height;
        mVideoVisibleWidth = visible_width;
        mSarNum = sar_num;
        mSarDen = sar_den;
        mHandler.removeMessages(HANDLER_SURFACE_SIZE);
        mHandler.sendEmptyMessage(HANDLER_SURFACE_SIZE);
    }

    private static final int HANDLER_BUFFER_START = 1;
    private static final int HANDLER_BUFFER_END = 2;
    private static final int HANDLER_SURFACE_SIZE = 3;

    private static final int SURFACE_BEST_FIT = 0;
    private static final int SURFACE_FIT_HORIZONTAL = 1;
    private static final int SURFACE_FIT_VERTICAL = 2;
    private static final int SURFACE_FILL = 3;
    private static final int SURFACE_16_9 = 4;
    private static final int SURFACE_4_3 = 5;
    private static final int SURFACE_ORIGINAL = 6;
    private int mCurrentSize = SURFACE_BEST_FIT;

//    public void init(){
//        FrameLayout.LayoutParams cameraFL = new FrameLayout.LayoutParams(320, 240, Gravity.TOP); // set size
//        cameraFL.setMargins(900, 50, 0, 0);  // set position
//
//    }
    private Handler mVlcHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg == null || msg.getData() == null)
                return;

            switch (msg.getData().getInt("event")) {
                case EventHandler.MediaPlayerTimeChanged:
                    break;
                case EventHandler.MediaPlayerPositionChanged:
                    break;
                case EventHandler.MediaPlayerPlaying:
                    mHandler.removeMessages(HANDLER_BUFFER_END);
                    mHandler.sendEmptyMessage(HANDLER_BUFFER_END);
                    break;
                case EventHandler.MediaPlayerBuffering:
                    break;
                case EventHandler.MediaPlayerLengthChanged:
                    break;
                case EventHandler.MediaPlayerEndReached:
                    //播放完成
                    break;
            }

        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_BUFFER_START:
                    showLoading();
                    break;
                case HANDLER_BUFFER_END:
                    hideLoading();
                    break;
                case HANDLER_SURFACE_SIZE:
                    changeSurfaceSize();
                    break;
            }
        }
    };

    private void showLoading() {
    }

    private void hideLoading() {
    }

    private void changeSurfaceSize() {
        // get screen size
        int dw = mSurfaceView.getWidth();
        int dh = mSurfaceView.getHeight();

        // calculate aspect ratio
        double ar = (double) mVideoWidth / (double) mVideoHeight;
        // calculate display aspect ratio
        double dar = (double) dw / (double) dh;

        switch (mCurrentSize) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = (int) (dw / ar);
                else
                    dw = (int) (dh * ar);
                break;
            case SURFACE_FIT_HORIZONTAL:
                dh = (int) (dw / ar);
                break;
            case SURFACE_FIT_VERTICAL:
                dw = (int) (dh * ar);
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = (int) (dw / ar);
                else
                    dw = (int) (dh * ar);
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = (int) (dw / ar);
                else
                    dw = (int) (dh * ar);
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoHeight;
                dw = mVideoWidth;
                break;
        }
        mSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
        ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
        lp.width = dw;
        lp.height = dh;
        mSurfaceView.setLayoutParams(lp);
        mSurfaceView.invalidate();
    }

    @Deprecated
    public  void Process(String data) {
        try {
            byte[] enc = data.getBytes();
            if (enc.length<=1){
                Toast.makeText(getActivity(), "请扫描正确的巡更二维码。", Toast.LENGTH_SHORT).show();
                return;
            }

            String timestamp="";
            String cameraId="";
            String cameraName="";
            String presentId="";
            String guardAddrId="";
            String guardAddrName="";

            if (enc[0] == '#') {
                cameraId = data.substring(1);
            }
            else if (enc.length>=16 && enc.length%16==0)
            {
                byte rawKeyData[] = "zhaoliugang88888".getBytes();
                byte[] encByte = AesEnc.HexStringToByteArray(data);
                byte[] dec = AesEnc.aesDecrypt(encByte, rawKeyData);
                String decStr = new String(dec);

                String[] words = decStr.split("\\s+");
                for(String word : words){
                    System.out.println(word);
                    if (word.startsWith("T=")) {
                        timestamp = word.substring(2).trim();
                    }else if (word.startsWith("I=")) {
                        cameraId = word.substring(2).trim();
                    }else if(word.startsWith("N=")){
                        cameraName = word.substring(2).trim();
                    }else if(word.startsWith("P=")){
                        presentId = word.substring(2).trim();
                    }else if(word.startsWith("A=")){
                        guardAddrId = word.substring(2).trim();
                        mGuardAddressId = guardAddrId;
                    }else if(word.startsWith("G=")){
                        guardAddrName = word.substring(2).trim();
                        mGuardAddressName = guardAddrName;
                    }
                }
            }

            if (cameraId.equals(""))
            {
                Toast.makeText(getActivity(), "请扫描正确的巡更二维码。", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Can't find cameraId!");
                return;
            }
            else {
                Log.i(TAG,"parse cameraId:"+cameraId);
            }

            long cId = Integer.parseInt(cameraId);
            Log.i(TAG, "find cameraId:" + cId);

            if(presentId.equals("")){
                presentId = "9";
            }
            long pId = Integer.parseInt(presentId);
            Log.i(TAG, "find presentId:" + pId);

            try {
                //control
                Toast.makeText(getActivity(),"视频直播摄像机:"+cameraName+" 编号:" + cId, Toast.LENGTH_SHORT).show();
                String playaddr= MVPSimpleProtocol.getInstance().startlive(cId);
                //playaddr="rtsp://192.168.199.100:8554/1";//guard system test
                //playaddr="rtsp://159.99.249.110:8554/1";
                if (playaddr!="")
                {
                    setmStrPlayAddr(playaddr, cId);
                    startPlay();

                    Toast.makeText(getActivity(), "摄像机"+cameraName+"启动自动预置位" + pId, Toast.LENGTH_SHORT).show();
                    MVPSimpleProtocol.getInstance().ptzcontrol(cId, 16, (int) pId);
                    Toast.makeText(getActivity(), "请面对摄像机保存截图并提交巡更记录凭证!", Toast.LENGTH_LONG).show();
                    Toast.makeText(getActivity(), "当前巡更站点号:"+guardAddrId+" 地点为:"+guardAddrName, Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(getActivity(), "摄像机"+cameraName+"视频直播异常，请重新尝试！", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.i(TAG, "Control camera Error!");
                e.printStackTrace();
            }
        }catch (Exception e) {
            Toast.makeText(getActivity(), "请扫描正确的巡更二维码。", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Error info:" + e.toString());
        }
    }

    public void playVideo(int cId){
        startPlay();
    }

    public String getmGuardAddressName() {
        return mGuardAddressName;
    }

    public void setmGuardAddressName(String mGuardAddressName) {
        this.mGuardAddressName = mGuardAddressName;
    }

    public String getmGuardAddressId() {
        return mGuardAddressId;
    }

    public void setmGuardAddressId(String mGuardAddressId) {
        this.mGuardAddressId = mGuardAddressId;
    }
}
