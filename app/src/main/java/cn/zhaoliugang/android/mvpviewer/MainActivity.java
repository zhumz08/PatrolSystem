package cn.zhaoliugang.android.mvpviewer;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mvp.v2.LoginActivity;
import com.mvp.v2.fragment.LoginFragmentV2;
import com.mvp.v2.fragment.LoginSettingFragment;
import com.zxing.activity.CaptureActivity;

import java.util.ArrayList;
import java.util.List;

import cn.zhaoliugang.android.mvpviewer.protocol.MVPSimpleProtocol;

public class MainActivity  extends AppCompatActivity implements View.OnClickListener{

    private List<Fragment> fragmentList = null;
    private List<ImageView> imgViewList = null;
    private List<TextView>  txtViewList = null;

    private TextView titleTxtView;


    private int index;//新的fragment的index
    private int currentTabIndex;// 当前fragment的index


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById();
        initTabView();

        //////1. 登陆界面
       /* //跳转到第一个fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_frame_layout, getLoginFragment()).hide(getLoginFragment());
        transaction.add(R.id.main_frame_layout, getLoginSettingFragment()).hide(getLoginSettingFragment());

        //设置fragment
        showFragment(transaction,getLoginFragment());*/
    }

    private void initTabView() {
        getFragmentList().add(getLiveFragment());
        getFragmentList().add(getGuardFragment());
//        getFragmentList().add(getCaptureFragment());

        getImgViewList().add((ImageView) findViewById(R.id.tab_live_id));
        getImgViewList().add((ImageView) findViewById(R.id.tab_kwatch_id));
        getImgViewList().add((ImageView) findViewById(R.id.tab_scan_id));
        getImgViewList().add((ImageView) findViewById(R.id.tab_exit_id));
        /*for (ImageView  imgView:imgViewList){
            imgView.setOnClickListener(this);
        }*/

        getTxtViewList().add((TextView) findViewById(R.id.tab_txt_live_id));
        getTxtViewList().add((TextView) findViewById(R.id.tab_txt_kwatch_id));
        getTxtViewList().add((TextView) findViewById(R.id.tab_txt_scan_id));
        getTxtViewList().add((TextView) findViewById(R.id.tab_txt_exit_id));

        // 添加显示第一个fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, getLiveFragment()).hide(getLiveFragment());
        transaction.add(R.id.fragment_container, getGuardFragment()).hide(getGuardFragment());
//        transaction.add(R.id.fragment_container, getCaptureFragment()).hide(getCaptureFragment());

        playVideo();

        showFragment(transaction,getLiveFragment());
    }



    /*
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 取得返回信息
        if (resultCode == RESULT_OK) {
            if(requestCode == Activity.RESULT_FIRST_USER) {
                System.out.println("MainActivity onActivityResult");
                getFragment(0).onActivityResult(requestCode, resultCode, data);
            }
        }
    }
    */


    public void onTabClicked(View view) {
        int viewID = view.getId();

        try {
            switch (viewID) {
                case R.id.relayout_live_id:
                    index = 0;
                    showFragment(getLiveFragment());
                    break;
                case R.id.relayout_kwatch_id:
                    index = 1;
                    getGuardFragment().mWebView.reload();
                    showFragment(getGuardFragment());
                    break;
                case R.id.relayout_scan_id:
                    index = 2;
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, CaptureActivity.class);
                    startActivity(intent);

//                    showFragment(getCaptureFragment());
                    break;
                case R.id.relayout_exit_id:
                    index = 3;
                    MainActivity.this.finish();
                    break;
            }

            //TextView txtView = txtViewList.get(index);
            //titleTxtView.setText(txtView.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        rePlayVideo();

        showFragment(getLiveFragment());
    }

    private void playVideo() {
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            String rtspAdr = bundle.getString(CaptureActivity.RTSP_ADR);
            long cameraId = bundle.getLong(CaptureActivity.CAMERA_ID);

            String guardAddr = bundle.getString(CaptureActivity.M_GuardAddressId);
            String guardName = bundle.getString(CaptureActivity.M_GuardAddressName);

            getLiveFragment().setmStrPlayAddr(rtspAdr,cameraId);
            getLiveFragment().setmGuardAddressId(guardAddr);
            getLiveFragment().setmGuardAddressName(guardName);

        }

    }

    private void rePlayVideo() {
        playVideo();

        getLiveFragment().startPlay();
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    private void findViewById() {
        System.out.println(Thread.currentThread().getStackTrace()[2].getMethodName());
        titleTxtView = (TextView) findViewById(R.id.title_txt);
    }

    @Override
    public void onClick(View view) {

        FragmentTransaction transaction = null;
        switch (view.getId()){
            case R.id.login_server_button:
                transaction = getSupportFragmentManager().beginTransaction();
                break;
            case R.id.setting_ok_btn:
                transaction = getSupportFragmentManager().beginTransaction();
                //showFragment(transaction,getLoginFragment());
                break;
            case R.id.setting_cancel_btn:
                transaction = getSupportFragmentManager().beginTransaction();
                //showFragment(transaction,getLoginFragment());
                break;
            default:
                break;
        }
    }

    private void showFragment(FragmentTransaction transaction,Fragment fragment){
        //隐藏所有fragment
        hideFragment(transaction);

        //显示需要显示的fragment
        transaction.show(fragment);
        transaction.commitAllowingStateLoss();
    }

    public void showFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        showFragment(transaction,fragment);
    }


    //隐藏所有的fragment
    private void hideFragment(FragmentTransaction transaction){
        for (Fragment fragment:fragmentList){
            transaction.hide(fragment);
        }
    }

    private LiveFragment getLiveFragment(){
        if(livefragment==null){
            livefragment = new LiveFragment();
        }
        return livefragment;
    }

    private CameraListFragment getCameraListFragment(){
        if(cameralistfragment==null){
            cameralistfragment = new CameraListFragment();
        }
        return cameralistfragment;
    }


    private MapFragment getMapFragment(){
        if(mapfragment==null){
            mapfragment = MapFragment.newInstance("","");
        }
        return mapfragment;
    }

    private CameraListFragment cameralistfragment;
    private MapFragment mapfragment;

    private LiveFragment livefragment;
    private GuardFragment guardFragment;
    private CaptureActivity captureFragment;

    public List<Fragment> getFragmentList() {
        if(fragmentList == null){
            fragmentList = new ArrayList<>();
        }
        return fragmentList;
    }

    public List<ImageView> getImgViewList() {
        if(imgViewList==null){
            imgViewList = new ArrayList<>();
        }
        return imgViewList;
    }

    public List<TextView> getTxtViewList() {
        if(txtViewList==null){
            txtViewList = new ArrayList<>();
        }
        return txtViewList;
    }

    public GuardFragment getGuardFragment() {
        if (guardFragment==null){
            guardFragment = new GuardFragment();
        }
        return guardFragment;
    }

    public void setGuardFragment(GuardFragment guardFragment) {
        this.guardFragment = guardFragment;
    }

    public CaptureActivity getCaptureFragment() {
        if(captureFragment==null){
            captureFragment = new CaptureActivity();
        }
        return captureFragment;
    }
}
