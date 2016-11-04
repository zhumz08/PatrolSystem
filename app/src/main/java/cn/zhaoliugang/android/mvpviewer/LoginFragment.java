package cn.zhaoliugang.android.mvpviewer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;

import cn.zhaoliugang.android.mvpviewer.protocol.MVPSimpleProtocol;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@Deprecated
public class LoginFragment extends Fragment implements View.OnClickListener {
    EditText tServerIP, tUserName, tPassword;
    Switch sRememberPwd;
    ImageView vLogin, vLogout;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        vLogin = (ImageView)view.findViewById(R.id.login_2x);
        vLogin.setOnClickListener(this);
        vLogout = (ImageView)view.findViewById(R.id.logout_2x);
        vLogout.setOnClickListener(this);
        tServerIP = (EditText)view.findViewById(R.id.et_server_name);
        tUserName = (EditText)view.findViewById(R.id.et_user_name);
        tPassword = (EditText)view.findViewById(R.id.et_user_pwd);
        sRememberPwd = (Switch)view.findViewById(R.id.switch_remember_pwd);

        // 获取保存的配置
        Context context = getContext();
        SharedPreferences preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        String sAddr = preferences.getString("addr", "192.168.1.232");
        tServerIP.setText(sAddr);
        String sUser = preferences.getString("user", "1");
        tUserName.setText(sUser);
        String sPwd = preferences.getString("pwd", "0");
        tPassword.setText(sPwd);

        return view;
    }


    @Override
    public void onClick(View v) {
        if (v == vLogin) {
            String sAddr = tServerIP.getText().toString();
            String sUser = tUserName.getText().toString();
            String sPwd = tPassword.getText().toString();
            //Base_request br = new Login_request(sUser,sPwd);
            //String proto = Protocol_builder.BuildXmlProtocol(br);

            // 写入配置文件
            Context context = getContext();
            SharedPreferences preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
            Editor editor = preferences.edit();
            editor.putString("addr", sAddr);
            editor.putString("user", sUser);
            if (sRememberPwd.isChecked()) {
                // 记住用户名密码
                editor.putString("pwd", sPwd);
            }
            else{
                // 不记住用户名密码
                editor.putString("pwd", "");
            }
            editor.commit();

            // 连接
            if (MVPSimpleProtocol.getInstance().Start(sAddr, 4511)){
                // 登录
                if (MVPSimpleProtocol.getInstance().login(sUser, sPwd)){
                    // 获取摄像机组
                    ArrayList groupList = new ArrayList();
                    boolean bRet = MVPSimpleProtocol.getInstance().getgrouplist(groupList);
                    if (bRet && groupList.size()>0){
                        Toast.makeText(getActivity(),"获取摄像机组失败", Toast.LENGTH_LONG).show();
                    }

                    // 获取摄像机列表
;                    ArrayList cameraList = new ArrayList();
                    bRet = MVPSimpleProtocol.getInstance().getcameralist(cameraList);
                    if (bRet && cameraList.size()>0){
                        Toast.makeText(getActivity(),"登录成功", Toast.LENGTH_LONG).show();

/*
                        CameraListFragment cameralistfragment = (CameraListFragment) ((MainActivity)getActivity()).getFragment(MainActivity.id_cameralistfragment);
                        if(cameralistfragment!=null) {
                            cameralistfragment.setGroupCameraList(groupList, cameraList);
                        }
*/
                      //  ((MainActivity) getActivity()).SwitchTab(R.id.re_camera_list);

                        // 设置摄像机组
//                FragmentManager fm  = getActivity().getSupportFragmentManager();
//                CameraListFragment cameralistfragment = (CameraListFragment) fm.findFragmentById(R.id.re_camera_list);
//                if(cameralistfragment!=null) {
//                    cameralistfragment.setGroupCameraList(groupList, cameraList);
//                }


//                        MVPCamera camera = (MVPCamera)cameraList.get(1);
//                        // 开始直播
//                        String sURL = MVPSimpleProtocol.getInstance().startlive(2);
//                        // PTZ控制
//                        MVPSimpleProtocol.getInstance().ptzcontrol(2, 1, 1);
//                        try{
//                            Thread.currentThread().sleep(1000);
//                        }
//                        catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        MVPSimpleProtocol.getInstance().ptzcontrol(2, 0, 1);
                    }
                    else{
                        Toast.makeText(getActivity(),"获取摄像机列表失败", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    Toast.makeText(getActivity(),"登录失败", Toast.LENGTH_LONG).show();
                }
            } else{
                Toast.makeText(getActivity(),"连接失败", Toast.LENGTH_LONG).show();
            }
        }
        else if (v == vLogout) {

                new AlertDialog.Builder(getContext()).setTitle("确认退出吗？")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“确认”后的操作
                                // 登出
                                MVPSimpleProtocol.getInstance().Stop();
                                // 退出程序
                                getActivity().finish();
                                //android.os.Process.killProcess(android.os.Process.myPid());
                               //System.exit(0);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“返回”后的操作,这里不设置没有任何操作
                            }
                        }).show();
                // super.onBackPressed();

        }
    }

}
