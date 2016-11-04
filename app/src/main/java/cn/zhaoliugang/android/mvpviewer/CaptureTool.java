package cn.zhaoliugang.android.mvpviewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.litesuits.http.LiteHttp;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.listener.HttpListener;
import com.litesuits.http.request.AbstractRequest;
import com.litesuits.http.request.BitmapRequest;
import com.litesuits.http.request.StringRequest;
import com.litesuits.http.request.content.HttpBody;
import com.litesuits.http.request.content.multi.BytesPart;
import com.litesuits.http.request.content.multi.FilePart;
import com.litesuits.http.request.content.multi.InputStreamPart;
import com.litesuits.http.request.content.multi.MultipartBody;
import com.litesuits.http.request.content.multi.StringPart;
import com.litesuits.http.request.param.HttpMethods;
import com.litesuits.http.response.Response;
import com.mvp.v2.http.LiteHttpClient;
import com.mvp.v2.listeners.LoginListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;

public class CaptureTool {
    private static final String TAG = "Service";
    private static String uploadUrl = "http://localhost:8080/guardtoursystem/action/GuardRecordServlet?action=save";
    private int userId;
    private String username;
    private String userRealName;

    CaptureTool(Context contex){
        mctx = contex;
        init();
    }
    private Context mctx;

    private WindowManager mWindowManager = null;

    private SimpleDateFormat dateFormat = null;
    private String strDate = null;
    private String pathImage = null;
    private String nameImage = null;

    private MediaProjection mMediaProjection = null;
    private VirtualDisplay mVirtualDisplay = null;

    public static int mResultCode = 0;
    public static Intent mResultData = null;
    public static MediaProjectionManager mMediaProjectionManager1 = null;

    private WindowManager mWindowManager1 = null;
    private int windowWidth = 0;
    private int windowHeight = 0;
    private ImageReader mImageReader = null;
    private DisplayMetrics metrics = null;
    private int mScreenDensity = 0;
    private String mCameraId = "0";
    private String mGuardAddressId = "0";



    public void init()
    {
        createVirtualEnvironment();
    }

    public void Capture(String cameraId,String guardAddressId,String guardAddressName)
    {
        mCameraId = cameraId;
        mGuardAddressId = guardAddressId;
        if(guardAddressId.equals("0")||guardAddressName.equals("0")){
            Toast.makeText(mctx, "您还没有扫描巡更二维码，请扫描后再操作！", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(mctx, "开始截图:"+"["+guardAddressId+"]"+guardAddressName+"！", Toast.LENGTH_SHORT).show();
        Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            public void run() {
                //start virtual
                startVirtual();
            }
        }, 500);

        Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            public void run() {
                //capture the screen
                startCapture();
            }
        }, 1500);
    }

    private void createVirtualEnvironment(){
        mMediaProjectionManager1 = (MediaProjectionManager)((MainActivity)(mctx)).getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mWindowManager1 = (WindowManager)((MainActivity)(mctx)).getApplication().getSystemService(Context.WINDOW_SERVICE);
        windowWidth = mWindowManager1.getDefaultDisplay().getWidth();
        windowHeight = mWindowManager1.getDefaultDisplay().getHeight();
        metrics = new DisplayMetrics();
        mWindowManager1.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mImageReader = ImageReader.newInstance(windowWidth, windowHeight, 0x1, 2); //ImageFormat.RGB_565


        SharedPreferences shared = mctx.getSharedPreferences(LoginListener.Persistent_NAME, Context.MODE_PRIVATE);
        userId = shared.getInt("user.userid",0);
        username = shared.getString("user.username","none");
        userRealName = shared.getString("user.name","none");
        String serverIp = shared.getString("serverIp","localhost");

        uploadUrl = uploadUrl.replace("localhost",serverIp);

        Log.i(TAG, "prepared the virtual environment");
    }

    public void startVirtual(){
        if (mMediaProjection != null) {
            Log.i(TAG, "want to display virtual");
            virtualDisplay();
        } else {
            Log.i(TAG, "start screen capture intent");
            Log.i(TAG, "want to build mediaprojection and display virtual");
            setUpMediaProjection();
            virtualDisplay();
        }
    }

    public void setUpMediaProjection(){
        mResultData = ((MVPViewerApplication)((MainActivity)(mctx)).getApplication()).getIntent();
        mResultCode = ((MVPViewerApplication)((MainActivity)(mctx)).getApplication()).getResult();
        mMediaProjectionManager1 = ((MVPViewerApplication)((MainActivity)(mctx)).getApplication()).getMediaProjectionManager();
        mMediaProjection = mMediaProjectionManager1.getMediaProjection(mResultCode, mResultData);
        Log.i(TAG, "mMediaProjection defined");
    }

    private void virtualDisplay() {
        int i = 0;
        while (mMediaProjection==null){
            if(i>3){
                Toast.makeText(mctx, "视频截图不成功,请稍候重试。", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
            mMediaProjection = mMediaProjectionManager1.getMediaProjection(mResultCode, mResultData);
        }
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                windowWidth, windowHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
        Log.i(TAG, "virtual displayed");
    }

    private void startCapture(){
        Toast.makeText(mctx, "正在处理中！", Toast.LENGTH_SHORT).show();
        pathImage = Environment.getExternalStorageDirectory().getPath()+"/Pictures/";
        dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        strDate = dateFormat.format(new java.util.Date());
        nameImage = pathImage+strDate+"_"+mCameraId+ "_"+mGuardAddressId+".jpg";

        Image image = mImageReader.acquireLatestImage();
        if(image==null){
            Toast.makeText(mctx, "视频截图不成功,请重试。", Toast.LENGTH_SHORT).show();
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width+rowPadding/pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0,width, height);
        image.close();
        Log.i(TAG, "image data captured");

        if(bitmap != null) {
            try{
               /* File fileImage = new File(nameImage);
                if(!fileImage.exists()){
                    fileImage.createNewFile();
                    Log.i(TAG, "image file created");
                }*/

                Toast.makeText(mctx, "开始上传至巡更系统！！！", Toast.LENGTH_SHORT).show();

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                if(outputStream != null){
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();

                    byte[] imgArr = outputStream.toByteArray();

                    MultipartBody body = new MultipartBody();
                    body.setContentEncoding("UTF-8");

                    MultipartBody multipartBody = body.addPart(new BytesPart("imgFile", imgArr))
                            .addPart(new StringPart("uid",String.valueOf(userId)))
                            .addPart(new StringPart("username",username))
                            .addPart(new StringPart("name",userRealName))
                            .addPart(new StringPart("guardaddress",mGuardAddressId))
                            ;


                    HttpListener uploadListener = new HttpListener<String>(true, false, true) {
                        @Override
                        public void onSuccess(String s, Response<String> response) {
                            response.printInfo();
                            Toast.makeText(mctx, "上传至巡更系统成功！！！", Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onFailure(HttpException e, Response<String> response) {
                            response.printInfo();
                            Toast.makeText(mctx, "上传至巡更系统失败！！！", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onUploading(AbstractRequest<String> request, long total, long len) {
                        }
                    };

                    AbstractRequest upload = new StringRequest(uploadUrl)
                            .setMethod(HttpMethods.Post)
                            .setHttpListener(uploadListener)
                            .setHttpBody(body);


                    LiteHttp client = LiteHttpClient.getLiteHttp(mctx,uploadUrl);
                    client.executeAsync(upload);


                    Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    //Uri contentUri = Uri.fromFile(fileImage);
                    //media.setData(contentUri);
                    ((MainActivity)(mctx)).sendBroadcast(media);
                    Log.i(TAG, "screen image saved");
                    //Toast.makeText(mctx, "保存截图成功，请上传至巡更系统", Toast.LENGTH_SHORT).show();
                    return ;
                }
            }catch(FileNotFoundException e) {
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        Toast.makeText(mctx, "视频截图不成功,请重试。", Toast.LENGTH_SHORT).show();
        return ;
    }

    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG,"mMediaProjection undefined");
    }

    private void stopVirtual() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
        Log.i(TAG,"virtual display stopped");
    }
}
