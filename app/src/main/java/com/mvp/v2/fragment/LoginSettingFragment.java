package com.mvp.v2.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.mvp.v2.LoginActivity;

import cn.zhaoliugang.android.mvpviewer.MainActivity;
import cn.zhaoliugang.android.mvpviewer.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginSettingFragment extends Fragment {

    private Button confirmBtn = null;
    private Button cancelBtn = null;

    private EditText ipTxt;
    private EditText userTxt;
    private EditText passTxt;


    public LoginSettingFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_setting, container, false);

        LoginActivity mainActivity = (LoginActivity)getActivity();

        //添加事件
        confirmBtn = (Button)view.findViewById(R.id.setting_ok_btn);
        confirmBtn.setOnClickListener(mainActivity.getLoginListener());

        cancelBtn = (Button)view.findViewById(R.id.setting_cancel_btn);
        cancelBtn.setOnClickListener(mainActivity);

        ipTxt = (EditText) view.findViewById(R.id.id_server_ip);
        userTxt = (EditText) view.findViewById(R.id.id_server_user);
        passTxt = (EditText) view.findViewById(R.id.id_server_pass);

        return view;
    }



    public EditText getIpTxt() {
        return ipTxt;
    }

    public void setIpTxt(EditText ipTxt) {
        this.ipTxt = ipTxt;
    }

    public EditText getUserTxt() {
        return userTxt;
    }

    public void setUserTxt(EditText userTxt) {
        this.userTxt = userTxt;
    }

    public EditText getPassTxt() {
        return passTxt;
    }

    public void setPassTxt(EditText passTxt) {
        this.passTxt = passTxt;
    }
}
