package cn.zhaoliugang.android.mvpviewer.protocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.net.Socket;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.mvp.v2.listeners.LoginListener;

/**
 * Created by fb on 2016/7/9.
 */
public class MVPSimpleProtocol {
    //私有的默认构造子
    private MVPSimpleProtocol() {
    }

    //已经自行实例化
    private static final MVPSimpleProtocol single = new MVPSimpleProtocol();

    //静态工厂方法
    public static MVPSimpleProtocol getInstance() {
        return single;
    }

    // 客户端套接字
    Socket m_clientSocket = null;
    // MVP服务器地址
    private String m_sAddr = "192.168.1.232";
    // MVP服务器端口
    private int m_nPort = 4511;
    // 用户名
    private String m_sUser = "1";
    // 密码
    private String m_sPwd = "0";

    // 会话编号
    private String m_sSession = "";

    // 心跳线程
    private Thread m_heartbeatThread;
    // 心跳线程运行中
    private boolean m_bHeartbeatThreadRunning = false;
    // 心跳线程退出标志
    private boolean m_bHeartbeatThreadExited = true;

    Handler m_handler = null;
    private String m_sRequest = "";
    private boolean m_bResponse = false;
    private String m_sResponse = "";

    long mCameraId = -1;

    // 开始连接
    public boolean Start(String sAddr, int nPort) {
        // 断开之前的连接
        Stop();

        m_sAddr = sAddr;
        m_nPort = nPort;

        // 连接
//        if (!connect(sAddr, nPort)){
//            return false;
//        }

        // 启动心跳线程
        return StartHeartbeatThread();
    }

    // 停止连接
    public void Stop() {
        // 停止心跳线程
        StopHeartbeatThread();

        // 等待线程结束
        int nSleepTime = 2000;
        int nSleepOnceTime = 100;
        int nSleepCount = nSleepTime/nSleepOnceTime;
        while (!m_bHeartbeatThreadExited && nSleepCount>=0) {
            try{
                Thread.currentThread().sleep(nSleepOnceTime);
                nSleepCount--;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        m_sAddr = "";
        m_nPort = 0;
    }

    // 连接
    private boolean connect(String sAddr, int nPort) {
        Disconnect();

        if (sAddr.length() <= 0 || nPort <= 0) {
            return false;
        }

        try {
            m_clientSocket = new Socket(sAddr, nPort);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (m_clientSocket == null) {
            return false;
        }

        return m_clientSocket.isConnected();
    }

    // 断开连接
    private void Disconnect() {
        if (m_clientSocket == null) {
            return;
        }

        try{
            m_clientSocket.close();
        }catch(IOException e){
            e.printStackTrace();
        }

        m_clientSocket = null;
    }

    // 检查连接并重连
    private boolean CheckAndReconnect() {
        if (m_clientSocket != null && m_clientSocket.isConnected()) {
            return true;
        }
        return connect(m_sAddr, m_nPort);
    }

    // 登录
    public boolean login(String sUser, String sPwd) {
        // 先退出之前的登录session
        if (m_sSession.length() > 0) {
            logout();
        }

        // 构造请求消息
        StringBuilder rRequestBuilder = new StringBuilder();
        rRequestBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        rRequestBuilder.append("<msg type = \"login_request\">");
        rRequestBuilder.append("  <user>" + sUser + "</user>");
        rRequestBuilder.append("  <pwd>" + sPwd + "</pwd>");
        rRequestBuilder.append("</msg>");

        // 发送返回消息
        String sResponse = RequestAndResponse(rRequestBuilder.toString());
        if (sResponse.length() <= 0) {
            return false;
        }

        // 解析消息返回值
        if (ParseResponseRet(sResponse) != "OK") {
            return false;
        }

        // 解析返回消息
        String sSession = parse_login_response(sResponse);
        if (sSession.length() <= 0) {
            return false;
        }

        // 设置session
        m_sSession = sSession;

        return true;
    }

    // 解析响应
    private String parse_login_response(String sResponse) {
        String session = "";

        InputStream inputStream = new ByteArrayInputStream(sResponse.getBytes());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            Element rootElement = doc.getDocumentElement();
            NodeList sessionNodeList = rootElement.getElementsByTagName("session");
            if (sessionNodeList.getLength() > 0) {
                Element sessionNode = (Element) sessionNodeList.item(0);
                session = sessionNode.getFirstChild().getNodeValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return session;
    }

    // 登出
    public void logout() {
        if (m_sSession.length() <= 0) {
            return;
        }
        // 构造请求消息
        StringBuilder rRequestBuilder = new StringBuilder();
        rRequestBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        rRequestBuilder.append("<msg type = \"logout_request\">");
        rRequestBuilder.append("  <session>" + m_sSession + "</session>");
        rRequestBuilder.append("</msg>");

        // 发送返回消息
        String sResponse = RequestAndResponse(rRequestBuilder.toString());
        if (sResponse.length() <= 0) {
            return;
        }

        // 解析消息返回值
        if (ParseResponseRet(sResponse) != "OK") {
            return;
        }

        // 设置session
        m_sSession = "";
    }

    // 心跳
    public boolean heartbeat() {
        if (m_sSession.length() <= 0) {
            return false;
        }
        // 构造请求消息
        StringBuilder rRequestBuilder = new StringBuilder();
        rRequestBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        rRequestBuilder.append("<msg type = \"heartbeat_request\">");
        rRequestBuilder.append("  <session>" + m_sSession + "</session>");
        rRequestBuilder.append("</msg>");

        // 发送返回消息
        String sResponse = RequestAndResponse(rRequestBuilder.toString());
        if (sResponse.length() <= 0) {
            return false;
        }

        // 解析消息返回值
        if (ParseResponseRet(sResponse) != "OK") {
            return false;
        }

        return true;
    }

    // 获取相机组列表
    public boolean getgrouplist(List arrayList) {
        if (m_sSession.length() <= 0) {
            return false;
        }
        // 构造请求消息
        StringBuilder rRequestBuilder = new StringBuilder();
        rRequestBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        rRequestBuilder.append("<msg type = \"getgrouplist_request\">");
        rRequestBuilder.append("  <session>" + m_sSession + "</session>");
        rRequestBuilder.append("</msg>");

        // 发送返回消息
        String sResponse = RequestAndResponse(rRequestBuilder.toString());
        if (sResponse.length() <= 0) {
            return false;
        }

        // 解析消息返回值
        if (ParseResponseRet(sResponse) != "OK") {
            return false;
        }

        return parse_getgrouplist_response(sResponse, arrayList);
    }

    // 解析响应
    private boolean parse_getgrouplist_response(String sResponse, List arrayList) {
        boolean bRet = false;
        InputStream inputStream = new ByteArrayInputStream(sResponse.getBytes());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            Element rootElement = doc.getDocumentElement();
            NodeList groupNodeList = rootElement.getElementsByTagName("group");
            for (int i = 0; i < groupNodeList.getLength(); i++) {
                Element groupNode = (Element) groupNodeList.item(i);
                String id = groupNode.getAttribute("id");
                String name = groupNode.getAttribute("name");
                String parent_id = groupNode.getAttribute("parent_id");
                String parent_name = groupNode.getAttribute("parent_name");

                MVPGroup group = new MVPGroup();
                group.id = Integer.parseInt(id);
                group.name = name;
                group.parent_id = Integer.parseInt(parent_id);
                group.parent_name = parent_name;
                // 添加到结果中
                arrayList.add(group);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bRet;
    }

    // 获取相机列表
    public boolean getcameralist(List arrayList) {
        if (m_sSession.length() <= 0) {
            return false;
        }
        // 构造请求消息
        StringBuilder rRequestBuilder = new StringBuilder();
        rRequestBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        rRequestBuilder.append("<msg type = \"getcameralist_request\">");
        rRequestBuilder.append("  <session>" + m_sSession + "</session>");
        rRequestBuilder.append("</msg>");

        // 发送返回消息
        String sResponse = RequestAndResponse(rRequestBuilder.toString());
        if (sResponse.length() <= 0) {
            return false;
        }

        // 解析消息返回值
        if (ParseResponseRet(sResponse) != "OK") {
            return false;
        }

        return parse_getcameralist_response(sResponse, arrayList);
    }

    // 解析响应
    private boolean parse_getcameralist_response(String sResponse, List arrayList) {
        boolean bRet = false;
        InputStream inputStream = new ByteArrayInputStream(sResponse.getBytes());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            Element rootElement = doc.getDocumentElement();
            NodeList groupNodeList = rootElement.getElementsByTagName("camera");
            for (int i = 0; i < groupNodeList.getLength(); i++) {
                Element groupNode = (Element) groupNodeList.item(i);
                String id = groupNode.getAttribute("id");
                String name = groupNode.getAttribute("name");
                String group_id = groupNode.getAttribute("group_id");
                String group_name = groupNode.getAttribute("group_name");

                MVPCamera camera = new MVPCamera();
                camera.id = Integer.parseInt(id);
                camera.name = name;
                camera.group_id = Integer.parseInt(group_id);
                camera.group_name = group_name;
                // 添加到结果中
                arrayList.add(camera);
            }
            bRet = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bRet;
    }


    // 开始直播
    public String startlive(long camera_id) {
        if (m_sSession.length() <= 0) {
            return "";
        }
        // 停止之前的直播
        stoplive();
        // 保存相机编号
        mCameraId = camera_id;

        // 构造请求消息
        StringBuilder rRequestBuilder = new StringBuilder();
        rRequestBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        rRequestBuilder.append("<msg type = \"startlive_request\">");
        rRequestBuilder.append("  <session>" + m_sSession + "</session>");
        rRequestBuilder.append("  <camera_id>" + mCameraId + "</camera_id>");
        rRequestBuilder.append("</msg>");

        // 发送返回消息
        String sResponse = RequestAndResponse(rRequestBuilder.toString());
        if (sResponse.length() <= 0) {
            return "";
        }

        // 解析消息返回值
        if (ParseResponseRet(sResponse) != "OK") {
            return "";
        }

        return parse_startlive_response(sResponse);
    }

    // 解析响应
    private String parse_startlive_response(String sResponse) {
        String url = "";

        InputStream inputStream = new ByteArrayInputStream(sResponse.getBytes());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            Element rootElement = doc.getDocumentElement();
            NodeList sessionNodeList = rootElement.getElementsByTagName("url");
            if (sessionNodeList.getLength() > 0) {
                Element sessionNode = (Element) sessionNodeList.item(0);
                url = sessionNode.getFirstChild().getNodeValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return url;
    }

    // 停止直播
    public boolean stoplive() {
        if (m_sSession.length() <= 0 || mCameraId<0) {
            return false;
        }

        // 构造请求消息
        StringBuilder rRequestBuilder = new StringBuilder();
        rRequestBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        rRequestBuilder.append("<msg type = \"stoplive_request\">");
        rRequestBuilder.append("  <session>" + m_sSession + "</session>");
        rRequestBuilder.append("  <camera_id>" + mCameraId + "</camera_id>");
        rRequestBuilder.append("</msg>");

        mCameraId = -1;

        // 发送返回消息
        String sResponse = RequestAndResponse(rRequestBuilder.toString());
        if (sResponse.length() <= 0) {
            return false;
        }

        // 解析消息返回值
        if (ParseResponseRet(sResponse) != "OK") {
            return false;
        }

        return true;
    }

    // PTZ控制
    public boolean ptzcontrol(long camera_id, int ptz_type, int ptz_param) {
        if (m_sSession.length() <= 0) {
            return false;
        }
        // 构造请求消息
        StringBuilder rRequestBuilder = new StringBuilder();
        rRequestBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        rRequestBuilder.append("<msg type = \"ptzcontrol_request\">");
        rRequestBuilder.append("  <session>" + m_sSession + "</session>");
        rRequestBuilder.append("  <camera_id>" + camera_id + "</camera_id>");
        rRequestBuilder.append("  <ptz_type>" + ptz_type + "</ptz_type>");
        rRequestBuilder.append("  <ptz_param>" + ptz_param + "</ptz_param>");
        rRequestBuilder.append("</msg>");

        // 发送返回消息
        String sResponse = RequestAndResponse(rRequestBuilder.toString());
        if (sResponse.length() <= 0) {
            return false;
        }

        // 解析消息返回值
        if (ParseResponseRet(sResponse) != "OK") {
            return false;
        }

        return true;
    }

    // 添加相机
    public boolean addcamera(MVPNewCameraInfo newCameraInfo ) {
        if (m_sSession.length() <= 0) {
            return false;
        }
        // 构造请求消息
        StringBuilder rRequestBuilder = new StringBuilder();
        rRequestBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        rRequestBuilder.append("<msg type = \"add_camera_request\">");
        rRequestBuilder.append("  <session>" + m_sSession + "</session>");
        rRequestBuilder.append("  <xjid>" + newCameraInfo.xjid + "</xjid>");
        rRequestBuilder.append("  <xjbh>" + newCameraInfo.xjbh + "</xjbh>");
        rRequestBuilder.append("  <xjmc>" + newCameraInfo.xjmc + "</xjmc>");
        rRequestBuilder.append("  <ipdz>" + newCameraInfo.ipdz + "</ipdz>");
        rRequestBuilder.append("  <bz_sheng>" + newCameraInfo.bz_sheng + "</bz_sheng>");
        rRequestBuilder.append("  <bz_shi>" + newCameraInfo.bz_shi + "</bz_shi>");
        rRequestBuilder.append("  <bz_xqq>" + newCameraInfo.bz_xqq + "</bz_xqq>");
        rRequestBuilder.append("  <bz_sd_xzjd>" + newCameraInfo.bz_sd_xzjd + "</bz_sd_xzjd>");
        rRequestBuilder.append("  <bz_dd>" + newCameraInfo.bz_dd + "</bz_dd>");
        rRequestBuilder.append("  <sfkk>" + newCameraInfo.sfkk + "</sfkk>");
        rRequestBuilder.append("  <qybz>" + newCameraInfo.qybz + "</qybz>");
        rRequestBuilder.append("  <spxy>" + newCameraInfo.spxy + "</spxy>");
        rRequestBuilder.append("  <xjtdh>" + newCameraInfo.xjtdh + "</xjtdh>");
        rRequestBuilder.append("  <xyyhm>" + newCameraInfo.xyyhm + "</xyyhm>");
        rRequestBuilder.append("  <xymm>" + newCameraInfo.xymm + "</xymm>");
        rRequestBuilder.append("  <zcmllx>" + newCameraInfo.zcmllx + "</zcmllx>");
        rRequestBuilder.append("  <mlljlx>" + newCameraInfo.mlljlx + "</mlljlx>");
        rRequestBuilder.append("  <zmlurl>" + newCameraInfo.zmlurl + "</zmlurl>");
        rRequestBuilder.append("  <gisjd>" + newCameraInfo.gisjd + "</gisjd>");
        rRequestBuilder.append("  <giswd>" + newCameraInfo.giswd + "</giswd>");
        rRequestBuilder.append("  <pgisjd>" + newCameraInfo.pgisjd + "</pgisjd>");
        rRequestBuilder.append("  <pgiswd>" + newCameraInfo.pgiswd + "</pgiswd>");
        rRequestBuilder.append("  <zsfx>" + newCameraInfo.xjbh + "</zsfx>");
        rRequestBuilder.append("</msg>");

        // 发送返回消息
        String sResponse = RequestAndResponse(rRequestBuilder.toString());
        if (sResponse.length() <= 0) {
            return false;
        }

        // 解析消息返回值
        if (ParseResponseRet(sResponse) != "OK") {
            return false;
        }

        return true;
    }

    // 发送请求并获取响应
    private String RequestAndResponse(String sRequest) {
        String sResponse = "";
        synchronized (this) {
            // 保存请求消息
            m_sRequest = sRequest;
            m_bResponse = false;
            m_sResponse = "";

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String sResponse = "";
                    if (!CheckAndReconnect()) {
                        m_bResponse = true;
                        m_sResponse = "";
                    }

                    // 发送命令
                    try {
                        OutputStream socketOutputStream = m_clientSocket.getOutputStream();
                        if (socketOutputStream != null) {
                            byte byteRequest[] = m_sRequest.getBytes();
                            socketOutputStream.write(byteRequest, 0, byteRequest.length);
                            socketOutputStream.flush();

                            m_sResponse = "";
                            // 等待数据到来
                            int nSleepTime = 2000;
                            int nSleepOnceTime = 20;
                            int nSleepCount = nSleepTime/nSleepOnceTime;
                            while  (nSleepCount>=0) {
                                // 获取数据长度
                                DataInputStream socketInputStream = new DataInputStream(m_clientSocket.getInputStream());
                                int nResponseSize = socketInputStream.available();
                                if (nResponseSize>0){
                                    byte[] byteResponse = new byte[nResponseSize];
                                    socketInputStream.read(byteResponse, 0, byteResponse.length);
                                    String sTemp = new String(byteResponse);
                                    m_sResponse += sTemp;
                                    if (m_sResponse.contains("</msg>")) {
                                        m_bResponse = true;
                                        break;
                                    }
                                }
                                try{
                                    Thread.currentThread().sleep(nSleepOnceTime);
                                    nSleepCount--;
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            int nSleepTime = 2000;
            int nSleepOnceTime = 100;
            int nSleepCount = nSleepTime/nSleepOnceTime;
            while (!m_bResponse && nSleepCount>=0) {
                try{
                    Thread.currentThread().sleep(nSleepOnceTime);
                    nSleepCount--;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sResponse = m_sResponse;
        }
        return sResponse;
    }

    // 解析返回结果
    private String ParseResponseRet(String sResponse) {
        int nRet = -1;
        String sError_Descrp = "";

        InputStream inputStream = new ByteArrayInputStream(sResponse.getBytes());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            Element rootElement = doc.getDocumentElement();

            NodeList resultNodeList = rootElement.getElementsByTagName("result");
            if (resultNodeList.getLength() > 0) {
                Element resultElement = (Element) resultNodeList.item(0);
                String result = resultElement.getFirstChild().getNodeValue();
                nRet = Integer.parseInt(result);
            }

            NodeList error_descrpNodeList = rootElement.getElementsByTagName("error_descrp");
            if (error_descrpNodeList.getLength() > 0) {
                Element error_descrpElement = (Element) error_descrpNodeList.item(0);
                sError_Descrp = error_descrpElement.getFirstChild().getNodeValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (nRet == 0) ? "OK" : sError_Descrp;
    }

    // 启动心跳线程
    private boolean StartHeartbeatThread() {
        m_bHeartbeatThreadRunning = true;
        // 创建线程

        m_heartbeatThread = new Thread(new Runnable() {
            @Override
            public void run() {
                m_bHeartbeatThreadExited = false;
                long tLast = System.currentTimeMillis();
                while (m_bHeartbeatThreadRunning) {
                    try {
                        long tNow = System.currentTimeMillis();
                        if ((tNow - tLast)>5000){
                            // 5s发送一次心跳
                            heartbeat();
                            tLast = tNow;
                        }

                        Thread.currentThread().sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // 停止直播
                stoplive();
                // 退出登录
                logout();
                // 断开连接
                Disconnect();
                // 设置线程已结束标志
                m_bHeartbeatThreadExited = true;
            }
        });
        // 启动线程
        m_heartbeatThread.start();
        return true;
    }

    // 停止心跳线程
    private void StopHeartbeatThread() {
        // 停止线程
        m_bHeartbeatThreadRunning = false;
    }

    public boolean isConnect(){
        boolean flag = (m_sSession != null && m_sSession.length() > 0 && !m_sSession.isEmpty() ) ? true : false;
        return flag;
    }
}
