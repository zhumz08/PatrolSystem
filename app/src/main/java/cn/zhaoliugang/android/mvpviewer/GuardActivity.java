package cn.zhaoliugang.android.mvpviewer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class GuardActivity extends Activity {

    private static final String TAG = GuardActivity.class.getSimpleName();
    private GuardFragment mGuardFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guard);

        ActionBar actionBar = getActionBar();
        // 是否显示应用程序图标，默认为true
        actionBar.setDisplayShowHomeEnabled(true);
        // 是否显示应用程序标题，默认为true
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            mGuardFragment = new GuardFragment();
            Log.v(TAG, "GuardFragment Creation");
           /* getFragmentManager().beginTransaction()
                    .add(R.id.fragment_guard_container, mGuardFragment)
                    .commit();*/
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.guard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        //System.out.println("onOptionsItemSelected:"+item.toString()+" id:"+id);
        if (id == R.id.action_settings) {
            // Setup option
            Intent serverIntent = new Intent(GuardActivity.this, OptionSetupActivity.class);
            startActivity(serverIntent);
            return true;
        } else if (id == android.R.id.home) {
            System.out.println("guard back to mvp");
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public String getServerIPAdress() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String serverip = settings.getString(getString(R.string.serverip_key), OptionSetupActivity.DEFAULT_SERVER_IP);
        return serverip;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mGuardFragment.mWebView != null
                    && mGuardFragment.mWebView.canGoBack()) {
                mGuardFragment.mWebView.goBack();
                return true;
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
