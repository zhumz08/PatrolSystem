package cn.zhaoliugang.android.mvpviewer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceActivity;
import android.util.Log;

import java.util.regex.Pattern;

public class OptionSetupActivity extends PreferenceActivity
        implements OnPreferenceChangeListener,OnPreferenceClickListener{

    public static final String INVIALID_SERVER_IP = "0.0.0.0";
    public static final String DEFAULT_SERVER_IP = "0.0.0.0";
    static final String TAG="OptionSetupActivity";
    SharedPreferences mPreference =null;
    EditTextPreference mServerIpPreference =null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置显示Preferences
        addPreferencesFromResource(R.xml.optionsetup);
        //获得SharedPreferences
        mPreference =PreferenceManager.getDefaultSharedPreferences(this);
        //找到preference对应的Key标签并转化
        mServerIpPreference =(EditTextPreference)findPreference(getString(R.string.serverip_key));

        //为Preference注册监听
        mServerIpPreference.setOnPreferenceChangeListener(this);

        InitTextSummary();
    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        //判断是哪个Preference改变了
        if(preference.getKey().equals(getString(R.string.serverip_key))){
            Log.e(TAG, getString(R.string.serverip_key));
        }

        //返回true表示允许改变
        return true;
    }
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        //判断是哪个Preference改变了
        if(preference.getKey().equals(getString(R.string.serverip_key))){

            Log.e(TAG, getString(R.string.serverip_key));
            if (isIPAdress((String)newValue))
            {
                //服务地址
                InitTextSummary();
                return true;
            }
            else {
                return false;
            }
        }

        //返回true表示允许改变
        return true;
    }

    public static boolean isIPAdress( String str )
    {
        Pattern pattern = Pattern.compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
        return pattern.matcher( str ).matches();
    }

    public void InitTextSummary()
    {
        String str=mServerIpPreference.getEditText().getText().toString();
        if(str.equals(""))
        {
            str = mServerIpPreference.getText();
        }
        if(str.equals(""))
        {
            mServerIpPreference.setSummary("请输入服务地址，例如：159.99.251.235");
        }
        else
        {
            mServerIpPreference.setSummary(str);
        }
    }

}
