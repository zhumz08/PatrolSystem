package cn.zhaoliugang.android.mvpviewer;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {

    private static final String a = MapFragment.class.getSimpleName();
    private MapView mMapView;
    private BaiduMapOptions mMapOpt;
    private BaiduMap mBaiduMap;
    private Marker mMarkerA;
    private InfoWindow mInfoWindow;

    public MapFragment() {

    }

    public void setBaiduMapOptions(BaiduMapOptions bo) {
        this.mMapOpt = bo;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(String param1, String param2) {
        MapStatus ms = new MapStatus.Builder().overlook(-20).zoom(15).build();
        BaiduMapOptions bo = new BaiduMapOptions().mapStatus(ms)
                .compassEnabled(true).zoomControlsEnabled(true);
        MapFragment fragment = new MapFragment();
        fragment.setBaiduMapOptions(bo);

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }


    public BaiduMap getBaiduMap() {
        return this.mMapView == null?null:this.mMapView.getMap();
    }

    public MapView getmMapView() {
        return this.mMapView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(getActivity().getApplicationContext());
    }

    public void hideView()
    {
        mMapView.setVisibility(View.INVISIBLE);
    }
    public void showView()
    {
        mMapView.setVisibility(View.VISIBLE);
    }

    public void initOverlay() {
        // add marker overlay
        double points[][]={
                {39.963175, 116.400244},
                {39.942821, 116.369199},
                {39.939723, 116.425541},
                {39.906965, 116.401394},
        };
        int i;
        for(i = 0; i<points.length; i++) {
            LatLng ll = new LatLng(points[i][0],points[i][1]);
            //构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_camera);
            MarkerOptions option = new MarkerOptions().position(ll).icon(bitmap)
                    .perspective(true).anchor(0.5f, 0.5f).zIndex(5);
            // 生长动画
            option.animateType(MarkerOptions.MarkerAnimateType.grow);
            mMarkerA = (Marker) (mBaiduMap.addOverlay(option));

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) view.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14.0f);
        mBaiduMap.setMapStatus(msu);
        initOverlay();
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            public boolean onMarkerClick(final Marker marker) {
                Button button = new Button(getActivity().getApplicationContext());
                button.setBackgroundResource(R.drawable.popup);
                InfoWindow.OnInfoWindowClickListener listener = null;
                button.setText("播放视频");
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Toast.makeText(getActivity(),
                                "视频直播",
                                Toast.LENGTH_SHORT).show();
                        mBaiduMap.hideInfoWindow();
                    }
                });
                LatLng ll = marker.getPosition();
                mInfoWindow = new InfoWindow(button, ll, -47);
                mBaiduMap.showInfoWindow(mInfoWindow);
                return true;
            }
        });
        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            /**
             * 当用户触摸地图时回调函数
             * @param event 触摸事件
             */
            public void onTouch(MotionEvent event) {
                mBaiduMap.hideInfoWindow();
            }
        });

        return view;
    }


    public void onViewCreated(View var1, Bundle var2) {
        super.onViewCreated(var1, var2);
    }

    public void onActivityCreated(Bundle var1) {
        super.onActivityCreated(var1);
    }

    public void onViewStateRestored(Bundle var1) {
        super.onViewStateRestored(var1);
        if(var1 != null) {
            ;
        }
    }

    public void onStart() {
        super.onStart();
    }

    public void onResume() {
        super.onResume();
        this.mMapView.onResume();
    }

    public void onSaveInstanceState(Bundle var1) {
        super.onSaveInstanceState(var1);
    }

    public void onPause() {
        super.onPause();
        this.mMapView.onPause();
    }

    public void onStop() {
        super.onStop();
    }

    public void onDestroyView() {
        super.onDestroyView();
        this.mMapView.onDestroy();
    }

    public void onDestroy() {
        super.onDestroy();
    }


    public void onConfigurationChanged(Configuration var1) {
        super.onConfigurationChanged(var1);
    }
}
