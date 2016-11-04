package cn.zhaoliugang.android.mvpviewer.protocol;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.StringWriter;

/**
 * Created by i7 on 2016/7/2.
 */
public class Protocol_builder {

    final static String TAG="PROTOCOL_BUILDER";

    public static String BuildXmlProtocol(Base_request handler)
    {
        String text="";
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8",null);

            handler.BuildData();
            for(int i=0;i<handler.GetKeys().size();i++)
            {
                if(handler.GetKeys().get(i).equals("startTag"))
                {
                    serializer.startTag("", (String) handler.GetValues().get(i));
                }
                else if(handler.GetKeys().get(i).equals("Attr")){
                    String[] str= (String[]) handler.GetValues().get(i);
                    serializer.attribute("",str[0],str[1]);
                }
                else if(handler.GetKeys().get(i).equals("text"))
                    serializer.text((String)handler.GetValues().get(i));
                else if(handler.GetKeys().get(i).equals("endTag"))
                {
                    serializer.endTag("", (String) handler.GetValues().get(i));
                }
            }
            serializer.endDocument();
            text=writer.toString();
            text=text.replace("><", ">\r\n<");
            Log.e(TAG,text);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return text;
    }
}
