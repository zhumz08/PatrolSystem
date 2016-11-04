package com.zxing.activity;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import cn.zhaoliugang.android.mvpviewer.AesEnc;
import cn.zhaoliugang.android.mvpviewer.MainActivity;
import cn.zhaoliugang.android.mvpviewer.R;
import cn.zhaoliugang.android.mvpviewer.protocol.MVPSimpleProtocol;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.mvp.v2.LoginActivity;
import com.zxing.camera.CameraManager;
import com.zxing.decoding.CaptureActivityHandler;
import com.zxing.decoding.InactivityTimer;
import com.zxing.decoding.RGBLuminanceSource;
import com.zxing.view.ViewfinderView;

import org.apache.commons.lang3.StringUtils;

/**
 * Initial the camera
 */
public class CaptureActivity extends Activity implements Callback {

    public final static String RTSP_ADR = "RTSP_ADR";
    public final static String CAMERA_ID = "CAMERA_ID";
    public final static String PRESENT_ID = "PRESENT_ID";
    public final static String M_GuardAddressId = "mGuardAddressId";
    public final static String M_GuardAddressName = "mGuardAddressName";

    private static final String TAG = "CaptureActivity";

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private Button cancelScanButton;

    int ifOpenLight = 0; // 判断是否开启闪光灯

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);
        // ViewUtil.addTopView(getApplicationContext(), this,
        // R.string.scan_card);
        CameraManager.init(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        cancelScanButton = (Button) this.findViewById(R.id.btn_cancel_scan);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;

        // quit the scan view
        cancelScanButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                CaptureActivity.this.finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    /**
     * Handler scan result
     *
     * @param result
     * @param barcode 获取结果
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        //判断有没有扫码成功
        String resultString = result.getText();
        if(StringUtils.isBlank(resultString)){
            Toast.makeText(CaptureActivity.this, "扫描失败!", Toast.LENGTH_SHORT).show();
            return;
        }
        //解析二维码，获取rtsp地址
        Bundle bundle = doScanParse(resultString);
        if(bundle==null){
            Toast.makeText(CaptureActivity.this, "解析二维码失败!", Toast.LENGTH_SHORT).show();
            return;
        }
        //解析成功跳转到主页面
        Intent intent = new Intent();
        intent.putExtras(bundle);
        intent.setClass(CaptureActivity.this, MainActivity.class);
        startActivity(intent);

        this.finish();

        //SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        //SurfaceHolder surfaceHolder = surfaceView.getHolder();
        //CaptureActivity.this.recreate();
    }


    /*
     * 获取带二维码的相片进行扫描
     */
    public void pickPictureFromAblum(View v) {
        // 打开手机中的相册
        //Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); // "android.intent.action.GET_CONTENT"
        //innerIntent.setType("image/*");
        //Intent wrapperIntent = Intent.createChooser(innerIntent, "选择二维码图片");
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        this.startActivityForResult(intent, 1);
    }

    String photo_path;
    ProgressDialog mProgress;
    Bitmap scanBitmap;

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onActivityResult(int, int,
     * android.content.Intent) 对相册获取的结果进行分析
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    // 获取选中图片的路径
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Uri selectedImage = data.getData();
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int column_index = cursor.getColumnIndex(filePathColumn[0]);
                        cursor.moveToFirst();
                        photo_path = cursor.getString(column_index);

                        Log.i("路径", photo_path);
                        cursor.close();
                    }

                    mProgress = new ProgressDialog(CaptureActivity.this);
                    mProgress.setMessage("正在扫描...");
                    mProgress.setCancelable(false);
                    mProgress.show();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Result result = scanningImage(photo_path);
                            if (result != null) {
                                Message m = mHandler.obtainMessage();
                                m.what = 1;
                                m.obj = result.getText();
                                mHandler.sendMessage(m);
                            } else {
                                Message m = mHandler.obtainMessage();
                                m.what = 2;
                                m.obj = "Scan failed!";
                                mHandler.sendMessage(m);
                            }

                        }
                    }).start();
                    break;

                default:
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub

            switch (msg.what) {
                case 1:
                    mProgress.dismiss();
                    String resultString = msg.obj.toString();
                    if (resultString.equals("")) {
                        Toast.makeText(CaptureActivity.this, "扫描失败!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // System.out.println("Result:"+resultString);
                        Intent resultIntent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("result", resultString);
                        resultIntent.putExtras(bundle);
                        CaptureActivity.this.setResult(RESULT_OK, resultIntent);
                    }
                    CaptureActivity.this.finish();
                    break;

                case 2:
                    mProgress.dismiss();
                    Toast.makeText(CaptureActivity.this, "解析错误！", Toast.LENGTH_LONG).show();

                    break;
                default:
                    break;
            }

            super.handleMessage(msg);
        }

    };

    /**
     * 扫描二维码图片的方法
     * <p/>
     * 目前识别度不高，有待改进
     *
     * @param path
     * @return
     */
    public Result scanningImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); // 设置二维码内容的编码

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
        scanBitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false; // 获取新的大小
        int sampleSize = (int) (options.outHeight / (float) 100);
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        try {
            scanBitmap = BitmapFactory.decodeFile(path, options);
            if (scanBitmap==null){
                return null;
            }
            RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
            BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
            QRCodeReader reader = new QRCodeReader();
            return reader.decode(bitmap1, hints);

        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 是否开启闪光灯
    public void IfOpenLight(View v) {
        ifOpenLight++;

        switch (ifOpenLight % 2) {
            case 0:
                // 关闭
                CameraManager.get().closeLight();
                break;

            case 1:
                // 打开
                CameraManager.get().openLight(); // 开闪光灯
                break;
            default:
                break;
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats,
                    characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };



    /**
     * 解析二维码
     * @param data
     */
    public Bundle doScanParse(String data) {
        try {
            byte[] enc = data.getBytes();
            if (enc.length<=1){
                Toast.makeText(this, "请扫描正确的巡更二维码。", Toast.LENGTH_SHORT).show();
                return null;
            }

            String timestamp="";
            String cameraId="";
            String cameraName="";
            String presentId="";
            String guardAddrId="";
            String guardAddrName="";
            String mGuardAddressId = "0";
            String mGuardAddressName = "0";

            if (enc[0] == '#') {
                cameraId = data.substring(1);
            }else if (enc.length>=16 && enc.length%16==0){
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

            if (StringUtils.isBlank(cameraId)){
                Toast.makeText(this, "请扫描正确的巡更二维码。", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Can't find cameraId!");
                return null;
            }else {
                Log.i(TAG,"parse cameraId:"+cameraId);
            }

            long cId = Integer.parseInt(cameraId);
            Log.i(TAG, "find cameraId:" + cId);

            if(presentId.equals("")){
                presentId = "9";
            }
            long pId = Integer.parseInt(presentId);
            Log.i(TAG, "find presentId:" + pId);

            String rtspAdr = MVPSimpleProtocol.getInstance().startlive(cId);
            if(StringUtils.isBlank(rtspAdr)){
                return null;
            }

            //设置预置位
            boolean flag = MVPSimpleProtocol.getInstance().ptzcontrol(cId, 16, (int) pId);
            if(!flag){
                Toast.makeText(this, "设置预置位失败! PID=" + pId, Toast.LENGTH_SHORT).show();
            }

            Bundle bundle = new Bundle();
            bundle.putString(RTSP_ADR,rtspAdr);
            bundle.putLong(CAMERA_ID,cId);
            bundle.putString(M_GuardAddressId,mGuardAddressId);
            bundle.putString(M_GuardAddressName,mGuardAddressName);

            return bundle;
        }catch (Exception e) {
            Toast.makeText(this, "请扫描正确的巡更二维码。", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Error info:" + e.toString());
        }
        return null;
    }

}