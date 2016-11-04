package com.mvp.v2.http;

import android.app.Activity;
import android.content.Context;

import com.litesuits.http.HttpClient;
import com.litesuits.http.HttpConfig;
import com.litesuits.http.LiteHttp;
import com.litesuits.http.data.GsonImpl;
import com.litesuits.http.impl.huc.HttpUrlClient;
import com.litesuits.http.utils.HttpUtil;

/**
 * Created by mjang on 2016/11/2.
 */

public class LiteHttpClient {

    private LiteHttpClient(){

    }

    private static LiteHttp liteHttp;


    /**
     * 单例 keep an singleton instance of litehttp
     */
    public static LiteHttp getLiteHttp(Context context,String url) {
        if (liteHttp == null) {
            liteHttp = LiteHttp.build(context)
                    .setHttpClient(new HttpUrlClient())       // http client
                    .setJsonConvertor(new GsonImpl())        // json convertor
                    .setDebugged(true)                     // log output when debugged
                    .setDoStatistics(true)                // statistics of time and traffic
                    .setDetectNetwork(true)              // detect network before connect
                    .setUserAgent("Mozilla/5.0 (...)")  // set custom User-Agent
                    .setSocketTimeout(10000)           // socket timeout: 10s
                    .setBaseUrl(url)
                    .setConnectTimeout(10000)         // connect timeout: 10s
                    .create();
        } else {
            liteHttp.getConfig()                   // configuration directly
                    .setSocketTimeout(5000)       // socket timeout: 5s
                    .setConnectTimeout(5000);    // connect timeout: 5s
        }
        return liteHttp;
    }



}
