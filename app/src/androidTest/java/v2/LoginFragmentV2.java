package v2;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import cn.zhaoliugang.android.mvpviewer.MainActivity;
import cn.zhaoliugang.android.mvpviewer.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragmentV2 extends Fragment implements View.OnClickListener{

    private Button loginServerButton = null;
    private Button loginBtn = null;

    public LoginFragmentV2() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_fragment_v2, container, false);
        MainActivity mainActivity = (MainActivity)getActivity();

        //添加事件
        loginServerButton = (Button)view.findViewById(R.id.login_server_button);
        loginServerButton.setOnClickListener(mainActivity);

        loginBtn = (Button)view.findViewById(R.id.login_v2_btn);
        loginBtn.setOnClickListener(mainActivity);

        return view;
    }

    @Override
    public void onClick(View v) {

    }
}
