package cn.zhaoliugang.android.mvpviewer.protocol;

import java.util.ArrayList;

/**
 * Created by i7 on 2016/7/2.
 */
public class Login_request extends Base_request {
    public String type = "login_request";
    private String user;
    private String pwd;

    public String GetType()
    {
        return type;
    }

    public Login_request(String u,String p)
    {
        user=u;
        pwd=p;
    }

    public void BuildItems()
    {
        Build_user();
        Build_pwd();
    }

    public void Build_user()
    {
        keys.add("startTag");
        values.add("user");

        keys.add("text");
        values.add(user);

        keys.add("endTag");
        values.add("user");
    }

    public void Build_pwd()
    {
        keys.add("startTag");
        values.add("pwd");

        keys.add("text");
        values.add(pwd);

        keys.add("endTag");
        values.add("pwd");
    }



}
