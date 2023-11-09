package com.example.videoplayer;

import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainVideoList extends Fragment {
    private View root;
    public static RecyclerView mRecyclerView;
    public static VideoPreviewAdapter mPreviewAdapter;
    LinearLayout loading;
    TextView failed;
    RecyclerView videoList;
    SwipeRefreshLayout swipeRefresh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (root == null)
            root = inflater.inflate(R.layout.fragment_main_video_list, container, false);

        // 初始化所有控件
        mRecyclerView = root.findViewById(R.id.recyclerView);
        loading = root.findViewById(R.id.MainVideoList_loading);
        failed = root.findViewById(R.id.MainVideoList_failed);
        videoList = root.findViewById(R.id.recyclerView);
        swipeRefresh = root.findViewById(R.id.MainVideoList_swipeRefresh);
        swipeRefresh.setOnRefreshListener(refresh);

        // 异步添加数据，一次添加三个
        new Thread(firstLoadRunnable).start();


        return root;
    }

    SwipeRefreshLayout.OnRefreshListener refresh = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (mPreviewAdapter != null && mPreviewAdapter.mPreviewBeans != null)
                mPreviewAdapter.mPreviewBeans.clear();
            // 刷新adapter数据时要确保RecyclerView不在ComputingLayout
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if (mPreviewAdapter != null && mPreviewAdapter.mPreviewBeans != null)
                        mPreviewAdapter.notifyDataSetChanged();
                }
            });
            if (mPreviewAdapter != null && mPreviewAdapter.mPreviewBeans != null)
                new Thread(firstLoadRunnable).start();
            else
                new Thread(loadingRunnable).start();
        }
    };

    public static PreviewBean getBeans(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String videoName = rs.getString("videoName");
        String videoTag = rs.getString("videoTag");
        String videoDescription = rs.getString("videoDescription").replace("\\n", "\n");
        String uploadDate = rs.getString("uploadDate");
        byte[] videoThumb = rs.getBytes("videoThumb");
        int playCount = rs.getInt("playCount");
        int like = rs.getInt("like");
        int dispatchCount = rs.getInt("dispatchCount");
        int memberOnly = rs.getInt("memberOnly");

        PreviewBean pBean = new PreviewBean(id, videoName, videoTag, videoDescription, videoThumb,
                uploadDate, playCount, like, dispatchCount, memberOnly);
        return pBean;
    }

    private Runnable loadingRunnable = new Runnable() {
        @Override
        public void run() {
            // 添加数据，一次添加三个
            Connection conn = null;
            try {
                int first;
                if (mPreviewAdapter.mPreviewBeans.isEmpty())
                    first = 1;
                else
                    first = mPreviewAdapter.mPreviewBeans.get(mPreviewAdapter.mPreviewBeans.size()-1).getId()+1;

                conn = DriverManager.getConnection("jdbc:mysql://1.15.179.230:3306/VideoPlayer?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", "root", "114514");
                Statement stmt = conn.createStatement();

                String query = "SELECT * FROM VideoDB WHERE id IN" +
                        "("+first+"," +
                        "(SELECT id FROM VideoDB WHERE id>"+first+" ORDER BY id LIMIT 1)," +
                        "(SELECT id FROM VideoDB WHERE id>(SELECT id FROM VideoDB WHERE id>"+first+" ORDER BY id LIMIT 1) " +
                        "ORDER BY id LIMIT 1)) ORDER BY id";
                ResultSet rs = stmt.executeQuery(query);
                for(int i=0;i<3;i++) {
                    if (!rs.next()) {
                        // 已经划到底了
                        MainActivity.mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.mainActivity, "已经到底了", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
                        break;
                    }
                    // 加载出一个视频，放到list里
                    PreviewBean pBean = getBeans(rs);
                    mPreviewAdapter.mPreviewBeans.add(pBean);
                }
                rs.close();
            } catch (NullPointerException | SQLException e) {
                e.printStackTrace();
                noInternet();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    e.printStackTrace();
                }
                swipeRefresh.setRefreshing(false);
            } finally {
                try {
                    if (conn != null)
                        conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            MainActivity.mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 加载完毕，刷新视图
                    videoList.setVisibility(ViewGroup.VISIBLE);
                    loading.setVisibility(ViewGroup.GONE);
                    failed.setVisibility(ViewGroup.GONE);
                    // 刷新adapter数据时要确保RecyclerView不在ComputingLayout
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (mPreviewAdapter != null)
                                mPreviewAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });
            // 刷新冷却1s
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            swipeRefresh.setRefreshing(false);
        }
    };

    Runnable firstLoadRunnable = new Runnable() {
        @Override
        public void run() {
            Connection conn = null;
            try {
                conn = DriverManager.getConnection("jdbc:mysql://1.15.179.230:3306/VideoPlayer?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", "root", "114514");
                Statement stmt = conn.createStatement();

                ResultSet rs = stmt.executeQuery("SELECT MIN(id) FROM VideoDB");
                rs.next();
                int first = rs.getInt("MIN(id)");
                String query = "SELECT * FROM VideoDB WHERE id IN" +
                        "("+first+"," +
                        "(SELECT id FROM VideoDB WHERE id>"+first+" ORDER BY id LIMIT 1)," +
                        "(SELECT id FROM VideoDB WHERE id>(SELECT id FROM VideoDB WHERE id>"+first+" ORDER BY id LIMIT 1) " +
                        "ORDER BY id LIMIT 1)) ORDER BY id";
                rs = stmt.executeQuery(query);
                List<PreviewBean> mPreviewList = new ArrayList<>(114);
                for(int i=0;i<3;i++) {
                    if (!rs.next())
                        return;

                    PreviewBean pBean = getBeans(rs);
                    mPreviewList.add(pBean);
                }
                rs.close();
                // 加载成功，正在加载视频列表
                MainActivity.mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        videoList.setVisibility(ViewGroup.VISIBLE);
                        loading.setVisibility(ViewGroup.GONE);
                        failed.setVisibility(ViewGroup.GONE);
                        // 初始化适配器
                        mPreviewAdapter = new VideoPreviewAdapter(mPreviewList);
                        mRecyclerView.addOnScrollListener(scrollToLastListener);
                        // 设置适配器
                        mRecyclerView.setAdapter(mPreviewAdapter);
                    }
                });
            } catch (NullPointerException|SQLException e) {
                e.printStackTrace();
                // 无网络连接
                noInternet();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    e.printStackTrace();
                }
                swipeRefresh.setRefreshing(false);
                return;
            } finally {
                try {
                    if (conn != null)
                        conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private Thread loadingThread;
    public RecyclerView.OnScrollListener scrollToLastListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
            if(lm == null)
                return;

            int lastVisiblePosition = lm.findLastVisibleItemPosition();
            int totalCnt = lm.getItemCount();

            if (loadingThread == null || !loadingThread.isAlive() && lastVisiblePosition == totalCnt-1 && (totalCnt > 0)) {
                loadingThread = new Thread(loadingRunnable);
                loadingThread.start();
            }
        }
    };

    private void noInternet() {
        MainActivity.mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                videoList.setVisibility(ViewGroup.GONE);
                loading.setVisibility(ViewGroup.GONE);
                failed.setVisibility(ViewGroup.VISIBLE);
                Toast.makeText(MainActivity.mainActivity, "无网络连接", Toast.LENGTH_LONG)
                        .show();
            }
        });
    }
}