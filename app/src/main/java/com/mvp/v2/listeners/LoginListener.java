package com.mvp.v2.listeners;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.litesuits.http.LiteHttp;
import com.litesuits.http.annotation.HttpUri;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.listener.HttpListener;
import com.litesuits.http.request.StringRequest;
import com.litesuits.http.request.param.HttpRichParamModel;
import com.litesuits.http.response.Response;
import com.litesuits.http.utils.HttpUtil;
import com.mvp.v2.LoginActivity;
import com.mvp.v2.fragment.LoginFragmentV2;
import com.mvp.v2.fragment.LoginSettingFragment;
import com.mvp.v2.http.LiteHttpClient;
import com.zxing.activity.CaptureActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.zhaoliugang.android.mvpviewer.CameraListFragment;
import cn.zhaoliugang.android.mvpviewer.LoginFragment;
import cn.zhaoliugang.android.mvpviewer.MainActivity;
import cn.zhaoliugang.android.mvpviewer.R;
import cn.zhaoliugang.android.mvpviewer.protocol.MVPSimpleProtocol;

import static com.baidu.mapapi.BMapManager.getContext;

/**
 * 登陆的监听
 * Created by mjang on 2016/11/1.
 */
public class LoginListener implements View.OnClickListener {

    public final static String Persistent_NAME = "loginInfo";  //缓存本地的名字
    private static final String TAG = "LoginListener";
    public String loginUrl = "http://localhost:8080/guardtoursystem/action/LoginServlet?action=loginRemote";

    private LoginActivity activtiy = null;

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            //系统登录
            case R.id.login_v2_btn:{
                loginV2();
                break;
            }

            //设置服务器
            case R.id.setting_ok_btn:{
                settingServer();
                break;
            }
            default:
                break;
        }

    }

    /**
     * 设置视频服务器
     */
    private void settingServer() {
        try {
            LoginSettingFragment fragment = activtiy.getLoginSettingFragment();

            String serverIp = fragment.getIpTxt().getText().toString();
            String serverUser = fragment.getUserTxt().getText().toString();
            String serverPass = fragment.getPassTxt().getText().toString();

            ///将数据缓存到本地XML文件
            getSharedPreferenceEdit().putString("serverIp", serverIp);
            getSharedPreferenceEdit().putString("serverUser", serverUser);
            getSharedPreferenceEdit().putString("serverPass", serverPass);
            getSharedPreferenceEdit().commit();

            validateServer(serverIp,serverPass,serverUser);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activtiy,"连接服务器异常"+e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private void validateServer(String serverIp,String serverPass,String serverUser) {
        // 连接
        if (!MVPSimpleProtocol.getInstance().Start(serverIp, 4511)) {
            Toast.makeText(activtiy,"连接服务器"+serverIp+":4511端口异常", Toast.LENGTH_LONG).show();
            return;
        }

        //登陆服务器
        if (!MVPSimpleProtocol.getInstance().login(serverUser, serverPass)) {
            Toast.makeText(activtiy,"登陆服务器"+serverIp+"错误,请检查服务器是否正确！", Toast.LENGTH_LONG).show();
            return;
        }

            /*
             不再获取摄像机列表 2016-11-01 18:47:00 zhumz By Mr Yu

            // 获取摄像机组
            List groupList = new ArrayList();
            boolean bRet = MVPSimpleProtocol.getInstance().getgrouplist(groupList);
            if (bRet && groupList.size()>0){
                Toast.makeText(activtiy,"获取摄像机组失败", Toast.LENGTH_LONG).show();
                return;
            }

            //获取摄像机
            List cameraList = new ArrayList();
            bRet = MVPSimpleProtocol.getInstance().getcameralist(cameraList);
            if (bRet && cameraList.size()>0) {
                Toast.makeText(activtiy, "服务器信息设置成功", Toast.LENGTH_LONG).show();

                CameraListFragment cameralistfragment = (CameraListFragment) ((MainActivity) getActivity()).getFragment(MainActivity.id_cameralistfragment);
                if (cameralistfragment != null) {
                    cameralistfragment.setGroupCameraList(groupList, cameraList);
                }
            }
            */

        Toast.makeText(activtiy, "服务器信息设置成功", Toast.LENGTH_LONG).show();
        activtiy.showFragment(activtiy.getLoginFragment());
    }


    //登陆系统
    private void loginV2() {
        String serverIp = getSharedPreferences().getString("serverIp", "localhost");

        boolean isConnect = MVPSimpleProtocol.getInstance().isConnect();
        if(!isConnect){
            String serverUser = getSharedPreferences().getString("serverUser", "1");
            String serverPass = getSharedPreferences().getString("serverPass", "0");

            validateServer(serverIp,serverPass,serverUser);
        }

        LoginFragmentV2 fragment = activtiy.getLoginFragment();
        String username = fragment.getUsernameTxt().getText().toString();
        String password = fragment.getPasswordTxt().getText().toString();

        getSharedPreferenceEdit().putString("user.username",username);
        getSharedPreferenceEdit().putString("user.password",password);

        //设置服务器的IP
        /* start 2016-11-02 16:39:46 调试时注释，该代码有用
        //
        end */
        loginUrl = loginUrl.replace("localhost",serverIp);

        LiteHttp liteHttp = LiteHttpClient.getLiteHttp(getContext(),loginUrl);
        liteHttp.executeAsync(new LoginParam(username, password).setHttpListener(new HttpListener<User>() {

                @Override
                public void onSuccess(User user, Response<User> response) {

                if(user!=null){
                    getSharedPreferenceEdit().putInt("user.userid",user.getId());
                    getSharedPreferenceEdit().putString("user.name",user.getName());
                    getSharedPreferenceEdit().putInt("user.type",user.getType());
                    getSharedPreferenceEdit().commit();

                    //登陆成功,跳转到扫码页面
                    activtiy.intentCapture();
                }else{
                    Toast.makeText(getContext(),"系统登录异常，请检查信息设置是否正确", Toast.LENGTH_LONG).show();
                }
                }

            }
        ));

    }

    class LoginParam extends HttpRichParamModel<User> {
        private String username;
        private String password;

        public LoginParam(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }


    SharedPreferences.Editor editor = null;
    public SharedPreferences.Editor getSharedPreferenceEdit(){
        if(editor==null){
            Context context = getContext();
            SharedPreferences preferences = context.getSharedPreferences(Persistent_NAME, Context.MODE_PRIVATE);
            editor = preferences.edit();
        }

        return editor;
    }

    public SharedPreferences getSharedPreferences(){
        Context context = getContext();
        SharedPreferences preferences = context.getSharedPreferences(Persistent_NAME, Context.MODE_PRIVATE);
        return preferences;
    }

    public LoginListener(LoginActivity activtiy){
        this.activtiy = activtiy;
    }

}
