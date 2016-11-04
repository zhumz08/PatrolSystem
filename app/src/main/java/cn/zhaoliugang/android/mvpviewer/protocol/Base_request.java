package cn.zhaoliugang.android.mvpviewer.protocol;

import java.util.ArrayList;

/**
 * Created by i7 on 2016/7/2.
 */


public class Base_request {

    public static String session = "0";

    protected String roottag = "msg";
    protected String type = "unknown_request";

    protected ArrayList<String> keys = new ArrayList<String>();//保存字段名称
    protected ArrayList<Object> values = new ArrayList<Object>();//保存值

    public String GetType()
    {
        return type;
    }

    public ArrayList<String> GetKeys()
    {
        return keys;
    }
    public ArrayList<Object> GetValues()
    {
        return values;
    }

    public void BuildItems()
    {
    }

    public void BuildData()
    {
        keys.add("startTag");
        values.add(roottag);

        keys.add("Attr");
        String[] str=new String[2];
        str[0]="type";
        str[1]=GetType();
        values.add(str);

        BuildItems();

        keys.add("endTag");
        values.add(roottag);

    }
}
