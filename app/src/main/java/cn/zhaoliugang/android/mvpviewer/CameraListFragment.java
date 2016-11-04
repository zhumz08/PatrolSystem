package cn.zhaoliugang.android.mvpviewer;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.zhaoliugang.android.mvpviewer.protocol.MVPCamera;
import cn.zhaoliugang.android.mvpviewer.protocol.MVPGroup;
import cn.zhaoliugang.android.mvpviewer.protocol.MVPSimpleProtocol;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CameraListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CameraListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraListFragment extends Fragment {

    private static final String TAG = "CameraListFragment" ;
    private ExpandableListView listView;
    private MyExpandableListAdapter mAdapter;


    public CameraListFragment() {
        // Required empty public constructor
    }

    // 设置摄像机组和摄像机列表
    public boolean setGroupCameraList(List groupList, List cameras){
        //摄像机测试数据
/*        for(int g=1;g<10;g++){
            MVPGroup group = new MVPGroup();
            group.id=g;
            group.name="摄像机分组"+String.valueOf(g);
            group.parent_id=0;
            group.parent_name="默认分组";
            groupList.add(group);
            for (int c=(g-1)*9+1;c<g*9+1;c++){
                MVPCamera camera = new MVPCamera();
                camera.id=c;
                camera.name="摄像机"+String.valueOf(c);
                camera.group_id=g;
                camera.group_name=group.name;
                cameras.add(camera);
            }
        }
        */

        if (mAdapter==null) {
            return false;
        }

        // 添加顶级节点
        MVPGroup rootGroup = new MVPGroup();
        rootGroup.id = 0;
        rootGroup.name = "默认分组";
        rootGroup.parent_id = -1;
        rootGroup.parent_name = "";
        groupList.add(rootGroup);

        Map<Long, List<MVPCamera>> mapGroupCamera = new LinkedHashMap<>();
        for (int i=0; i<groupList.size(); i++){
            MVPGroup group = (MVPGroup)groupList.get(i);
            List<MVPCamera> lstCamera = new ArrayList<>();
            mapGroupCamera.put(group.id, lstCamera);
        }
        for (int i=0; i<cameras.size(); i++){
            MVPCamera camera = (MVPCamera)cameras.get(i);
            mapGroupCamera.get(camera.group_id).add(camera);
        }

        List<List<MVPCamera>> cameraGroupList = new ArrayList<>();
        for (Entry<Long, List<MVPCamera>> entry : mapGroupCamera.entrySet()) {
            cameraGroupList.add(entry.getValue());
        }

        // 设置
        boolean bRet =  mAdapter.setGroupCameraList(groupList, cameraGroupList);
        if (bRet){
            // 设置成功，刷新目录
            mAdapter.notifyDataSetChanged();
        }
        return  bRet;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera_list, container, false);
        listView = (ExpandableListView) view.findViewById(R.id.cameralist);
        listView.setGroupIndicator(null);
        mAdapter = new MyExpandableListAdapter();
        listView.setAdapter(mAdapter);
        //设置item点击的监听器
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {

                Toast.makeText( getActivity(),
                        "视频直播" + mAdapter.getChildId(groupPosition, childPosition),
                        Toast.LENGTH_SHORT).show();

                long cameraId=mAdapter.getChildId(groupPosition, childPosition);

                String playaddr= MVPSimpleProtocol.getInstance().startlive(1L);
                if (playaddr!="")
                {
                   /* LiveFragment livefragment = (LiveFragment) ((MainActivity)getActivity()).getFragment(MainActivity.id_livefragment);
                    if(livefragment!=null) {
                       // Log.d(TAG, "onChildClick: " + "dizhi:"+playaddr+" cameraId:" + cameraId);
                        //Toast.makeText(getActivity(),"dizhi:"+playaddr+" cameraId:" + cameraId,Toast.LENGTH_LONG).show();
                        livefragment.setmStrPlayAddr(playaddr, cameraId);
                        //livefragment.startPlay();
                    }*/
                    //((MainActivity) getActivity()).SwitchTab(R.id.re_live_play);
                }
                return true;
            }
        });

        //摄像机测试数据
/*
        ArrayList cameraList = new ArrayList();
        ArrayList groupList = new ArrayList();
        setGroupCameraList(groupList, cameraList);
*/

        return view;
    }


    public class MyExpandableListAdapter extends BaseExpandableListAdapter {
        //设置组视图的图片
        int[] logos = new int[] { R.drawable.icon_camera };

        //设置组视图的显示文字
        //private String[] generalsTypes = new String[] { "摄像机分组1", "摄像机分组2", "摄像机分组3" };
        //子视图显示文字
//        private String[][] generals = new String[][] {
//                { "摄像机1", "摄像机2", "摄像机3" },
//                { "摄像机4", "摄像机5", "摄像机6" },
//                { "摄像机7", "摄像机8", "摄像机9" }
//        };

        List<MVPGroup> m_groupList = new ArrayList<MVPGroup>();
        List<List<MVPCamera>> m_groupCameraList = new ArrayList<>();

        // 设置摄像机组和摄像机列表
        public boolean setGroupCameraList(List<MVPGroup> groupList, List<List<MVPCamera>> groupCameraList){
            m_groupList = groupList;
            m_groupCameraList = groupCameraList;
            return  true;
        }

        //子视图图片
        public int[][] generallogos = new int[][] {
                { R.drawable.cameraplay } };

        //自己定义一个获得文字信息的方法
        TextView getTextView() {
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            TextView textView = new TextView(getActivity());
            textView.setLayoutParams(lp);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setPadding(60, 0, 0, 0);
            //textView.setTextSize(20);
            textView.setTextColor(Color.WHITE);
            return textView;
        }


        //重写ExpandableListAdapter中的各个方法
        @Override
        public int getGroupCount() {
            // TODO Auto-generated method stub
            return m_groupList.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            // TODO Auto-generated method stub
            return m_groupList.get(groupPosition).name;
        }

        @Override
        public long getGroupId(int groupPosition) {
            // TODO Auto-generated method stub
            return m_groupList.get(groupPosition).id;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            // TODO Auto-generated method stub
            return m_groupCameraList.get(groupPosition).size();
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return m_groupCameraList.get(groupPosition).get(childPosition).name;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return m_groupCameraList.get(groupPosition).get(childPosition).id;
        }

        @Override
        public boolean hasStableIds() {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            LinearLayout ll = new LinearLayout(getActivity());
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setPadding(5, 5, 5, 5);
            ImageView logo = new ImageView(getActivity());
            logo.setImageResource(logos[0]);
            logo.setPadding(75, 0, 0, 0);
            ll.addView(logo);
            TextView textView = getTextView();
            //textView.setTextColor(Color.BLACK);
            //textView.setBackgroundColor(Color.GRAY);
            textView.setTextSize(20);
            textView.setText(getGroup(groupPosition).toString());
            ll.addView(textView);
            ll.setBackgroundColor(Color.LTGRAY);

            return ll;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            LinearLayout ll = new LinearLayout(getActivity());
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setPadding(2, 2, 2, 2);
            ImageView generallogo = new ImageView(getActivity());
            generallogo.setImageResource(generallogos[0][0]);
            generallogo.setPadding(150, 0, 0, 0);
            ll.addView(generallogo);
            TextView textView = getTextView();
            textView.setText(getChild(groupPosition, childPosition).toString());
            ll.addView(textView);
            return ll;
        }

        @Override
        public boolean isChildSelectable(int groupPosition,
                                         int childPosition) {
            // TODO Auto-generated method stub
            return true;
        }

    }
}
