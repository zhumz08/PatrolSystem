package com.mvp.v2;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.baidu.mapapi.SDKInitializer;
import com.mvp.v2.fragment.LoginFragmentV2;
import com.mvp.v2.fragment.LoginSettingFragment;
import com.mvp.v2.listeners.LoginListener;
import com.zxing.activity.CaptureActivity;

import cn.zhaoliugang.android.mvpviewer.LoginFragment;
import cn.zhaoliugang.android.mvpviewer.MainActivity;
import cn.zhaoliugang.android.mvpviewer.R;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private LoginFragmentV2 loginFragment_1;
    private LoginSettingFragment loginSettingFragment_2;

    private LoginListener loginListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_layout);
        loginListener = new LoginListener(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //跳转到第一个fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_frame_layout, getLoginFragment()).hide(getLoginFragment());
        transaction.add(R.id.main_frame_layout, getLoginSettingFragment()).hide(getLoginSettingFragment());

        //设置fragment
        showFragment(transaction,getLoginFragment());

        SDKInitializer.initialize(getApplicationContext());

    }

    @Override
    public void onClick(View view) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (view.getId()){
            case R.id.login_server_button:
                showFragment(transaction,getLoginSettingFragment());
                break;
            case R.id.setting_ok_btn:
                showFragment(transaction,getLoginFragment());
                break;
            case R.id.setting_cancel_btn:
                showFragment(transaction,getLoginFragment());
                break;
            case R.id.login_v2_btn:
                this.finish();
                break;
            default:
                break;
        }
    }


    /**
     * 跳转到扫码的页面
     */
    public void intentCapture(){
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, MainActivity.class);
        intent.setClass(LoginActivity.this, CaptureActivity.class);
        startActivity(intent);

        //this.finish();
    }

    private void showFragment(FragmentTransaction transaction,Fragment fragment){
        //隐藏所有fragment
        hideFragment(transaction);

        //显示需要显示的fragment
        transaction.show(fragment);
        transaction.commit();
    }

    public void showFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        showFragment(transaction,fragment);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        showFragment(getLoginFragment());
    }

    //隐藏所有的fragment
    private void hideFragment(FragmentTransaction transaction){
        transaction.hide(getLoginFragment());
        transaction.hide(getLoginSettingFragment());
    }


    public LoginFragmentV2 getLoginFragment(){
        if(loginFragment_1==null){
            loginFragment_1 = new LoginFragmentV2();
        }
        return loginFragment_1;
    }

    public LoginSettingFragment getLoginSettingFragment(){
        if(loginSettingFragment_2==null){
            loginSettingFragment_2 = new LoginSettingFragment();
        }
        return loginSettingFragment_2;
    }

    public LoginListener getLoginListener() {
        return loginListener;
    }

}
