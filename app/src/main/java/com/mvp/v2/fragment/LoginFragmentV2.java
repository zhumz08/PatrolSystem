package com.mvp.v2.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.mvp.v2.LoginActivity;

import cn.zhaoliugang.android.mvpviewer.MainActivity;
import cn.zhaoliugang.android.mvpviewer.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragmentV2 extends Fragment implements View.OnClickListener{

    private Button loginServerButton = null;
    private Button loginBtn = null;

    private EditText usernameTxt = null;
    private EditText passwordTxt = null;

    private Button pwdClearBtn;
    private Button pwdEyeBtn;
    private Button userCleanBtn;

    private TextWatcher user_watcher;
    private TextWatcher pwd_watcher;


    public LoginFragmentV2() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_fragment_v2, container, false);
        LoginActivity mainActivity = (LoginActivity)getActivity();

        //添加事件
        loginServerButton = (Button)view.findViewById(R.id.login_server_button);
        loginServerButton.setOnClickListener(mainActivity);

        loginBtn = (Button)view.findViewById(R.id.login_v2_btn);
        loginBtn.setOnClickListener(mainActivity.getLoginListener());

        usernameTxt = (EditText) view.findViewById(R.id.login_username_id);
        usernameTxt.setOnClickListener(this);
        passwordTxt = (EditText) view.findViewById(R.id.login_password_id);
        passwordTxt.setOnClickListener(this);

        pwdClearBtn = (Button) view.findViewById(R.id.bt_pwd_clear);
        pwdClearBtn.setOnClickListener(this);
        userCleanBtn = (Button) view.findViewById(R.id.bt_username_clear);
        userCleanBtn.setOnClickListener(this);
        pwdEyeBtn = (Button) view.findViewById(R.id.bt_pwd_eye);
        pwdEyeBtn.setOnClickListener(this);

        initWatcher();

        usernameTxt.addTextChangedListener(user_watcher);
        passwordTxt.addTextChangedListener(pwd_watcher);

        return view;
    }

    public EditText getUsernameTxt() {
        return usernameTxt;
    }

    public EditText getPasswordTxt() {
        return passwordTxt;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_username_clear:
                usernameTxt.setText("");
                passwordTxt.setText("");
                break;
            case R.id.bt_pwd_clear:
                passwordTxt.setText("");
                break;
            case R.id.bt_pwd_eye:
                if(passwordTxt.getInputType() == (InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD)){
                    passwordTxt.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_NORMAL);
                }else{
                    passwordTxt.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                break;
            default:
                break;
        }
    }


    /**
     * 手机号，密码输入控件公用这一个watcher
     */
    private void initWatcher() {
        user_watcher = new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
            public void afterTextChanged(Editable s) {
                if(s.toString().length()>0){
                    userCleanBtn.setVisibility(View.VISIBLE);
                }else{
                    userCleanBtn.setVisibility(View.INVISIBLE);
                }
            }
        };

        pwd_watcher = new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
            public void afterTextChanged(Editable s) {
                if(s.toString().length()>0){
                    pwdClearBtn.setVisibility(View.VISIBLE);
                }else{
                    pwdClearBtn.setVisibility(View.INVISIBLE);
                }
            }
        };
    }

}
