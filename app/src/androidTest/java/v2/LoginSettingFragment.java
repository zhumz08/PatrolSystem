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
public class LoginSettingFragment extends Fragment {

    private Button confirmBtn = null;
    private Button cancelBtn = null;

    public LoginSettingFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_setting, container, false);

        MainActivity mainActivity = (MainActivity)getActivity();

        //添加事件
        confirmBtn = (Button)view.findViewById(R.id.setting_ok_btn);
        confirmBtn.setOnClickListener(mainActivity);

        cancelBtn = (Button)view.findViewById(R.id.setting_cancel_btn);
        cancelBtn.setOnClickListener(mainActivity);

        return view;
    }


}
