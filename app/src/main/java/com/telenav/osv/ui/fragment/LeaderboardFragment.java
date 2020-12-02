package com.telenav.osv.ui.fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.telenav.osv.R;
import com.telenav.osv.activity.MainActivity;
import com.telenav.osv.application.PreferenceTypes;
import com.telenav.osv.item.LeaderboardData;
import com.telenav.osv.item.network.UserCollection;
import com.telenav.osv.listener.network.NetworkResponseDataListener;
import com.telenav.osv.ui.ScreenComposer;
import com.telenav.osv.ui.custom.CenterLayoutManager;
import com.telenav.osv.ui.list.LeaderboardAdapter;
import com.telenav.osv.utils.BackgroundThreadPool;
import com.telenav.osv.utils.Log;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * Fragmnent holding the ui for the leaderboard screen
 * Created by Kalman on 22/11/2016.
 */
public class LeaderboardFragment extends OSVFragment {

    public final static String TAG = "LeaderboardFragment";

    private static final String LOADER_THREAD_NAME = "leaderboardLoader";

    private MainActivity activity;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private TabLayout mTabLayout;

    private ViewPager mTimeViewPager;

    private ArrayList<LeaderboardData> mUserList = new ArrayList<>();

    private LeaderboardAdapter mLeaderboardAdapter;

    private int mTimePeriod = 0;

    private int mRegionType = 0;

    private String mCountryCode = "us";

    private int mUserPosition = 0;

    private RecyclerView mRecyclerView;

    private String savedUsername;

    private Handler mBackgroundHandler;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Timer mTimer = new Timer();

    private TimerTask mCancelTask = new TimerTask() {

        @Override
        public void run() {
            if (mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, null);

        activity = (MainActivity) getActivity();
        HandlerThread thread = new HandlerThread(LOADER_THREAD_NAME, Process.THREAD_PRIORITY_LOWEST);
        thread.start();
        mBackgroundHandler = new Handler(thread.getLooper());
        savedUsername = activity.getApp().getAppPrefs().getStringPreference(PreferenceTypes.K_USER_NAME);

        mTabLayout = view.findViewById(R.id.tab_layout);
        refreshRegionTab();
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mRegionType = tab.getPosition();
                refreshContent();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mTimeViewPager = view.findViewById(R.id.time_view_pager);
        mRecyclerView = view.findViewById(R.id.rank_recycler_view);
        ImageView mTimePagerLeft = view.findViewById(R.id.button_time_pager_left);
        ImageView mTimePagerRight = view.findViewById(R.id.button_time_pager_right);
        mTimePagerLeft.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mTimeViewPager.setCurrentItem(Math.max(mTimeViewPager.getCurrentItem() - 1, 0), true);
            }
        });
        mTimePagerRight.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mTimeViewPager.setCurrentItem(Math.min(mTimeViewPager.getCurrentItem() + 1, 3), true);
            }
        });
        mTimeViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTimePeriod = position;
                refreshContent();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mLeaderboardAdapter = new LeaderboardAdapter(mUserList, activity);
        mRecyclerView.setAdapter(mLeaderboardAdapter);
        mRecyclerView.setLayoutManager(new CenterLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
        mSwipeRefreshLayout = view.findViewById(R.id.leaderboard_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                refreshContent();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTimeViewPager.setOffscreenPageLimit(4);
        mTimeViewPager.setAdapter(new PagerAdapter() {

            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                TextView tv = new TextView(activity);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                tv.setLayoutParams(lp);
                tv.setGravity(Gravity.CENTER);
                switch (position) {
                    case 0:
                        tv.setText(R.string.leaderboard_all_time_label);
                        break;
                    case 1:
                        tv.setText(R.string.leaderboard_this_month_label);
                        break;
                    case 2:
                        tv.setText(R.string.leaderboard_this_week_label);
                        break;
                    case 3:
                        tv.setText(R.string.leaderboard_this_day_label);
                        break;
                }
                tv.setTextColor(activity.getResources().getColor(R.color.default_black_lighter));
                container.addView(tv);
                tv.setTextSize(18);
                return tv;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {

            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
        });
        mTimeViewPager.setCurrentItem(mTimePeriod);
        TabLayout.Tab tab = mTabLayout.getTabAt(mRegionType);
        if (tab != null) {
            tab.select();
        }
        refreshContent();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mLeaderboardAdapter != null && mRecyclerView != null) {
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    mRecyclerView.smoothScrollToPosition(mUserPosition);
                }
            }, 200);
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroyView() {
        try {
            mBackgroundHandler.getLooper().getThread().interrupt();
        } catch (Exception ignored) {
        }
        super.onDestroyView();
    }

    private void refreshContent() {
        BackgroundThreadPool.post(new Runnable() {

            @Override
            public void run() {
                Log.d(TAG, "refreshContent ");
                requestLeaderboardData(mRegionType, mTimePeriod);
                startRefreshing();
                mRecyclerView.post(new Runnable() {

                    @Override
                    public void run() {
                        mUserList.clear();
                        mLeaderboardAdapter.resetLastAnimatedItem();
                        mLeaderboardAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void startRefreshing() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.post(new Runnable() {

                @Override
                public void run() {
                    if (mSwipeRefreshLayout != null) {
                        mSwipeRefreshLayout.setRefreshing(true);
                        if (mCancelTask != null) {
                            mCancelTask.cancel();
                        }
                        mCancelTask = new TimerTask() {

                            @Override
                            public void run() {
                                if (activity != null) {
                                    activity.runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            if (mSwipeRefreshLayout != null) {
                                                if (activity.getCurrentScreen() == ScreenComposer.SCREEN_MY_PROFILE) {
                                                    activity.showSnackBar(R.string.loading_too_long, Snackbar.LENGTH_LONG);
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        };
                        mTimer.schedule(mCancelTask, 10000);
                    }
                }
            });
        }
    }

    private void stopRefreshing() {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mSwipeRefreshLayout != null) {
                        if (mCancelTask != null) {
                            mCancelTask.cancel();
                        }
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }
    }

    private void requestLeaderboardData(final int regionType, final int periodType) {
        mBackgroundHandler.post(new Runnable() {

            @Override
            public void run() {
                String date;
                Calendar calendar = Calendar.getInstance();
                @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                switch (periodType) {
                    default:
                    case 0://ALL TIME
                        date = null;
                        break;
                    case 1://MONTHLY
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        date = df.format(calendar.getTime());
                        break;
                    case 2://WEEKLY
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                        date = df.format(calendar.getTime());
                        break;
                    case 3://DAILY
                        date = df.format(calendar.getTime());
                        break;
                }
                final String region;
                switch (regionType) {
                    default:
                    case 0://ALL WORLD
                        region = null;
                        break;
                    case 1://USER'S COUNTRY
                        region = mCountryCode;
                        break;
                }
                Log.d(TAG, "requestLeaderboardData: date " + date);
                mUserPosition = 0;
                activity.getUserDataManager().getLeaderboardData(new NetworkResponseDataListener<UserCollection>() {

                    @Override
                    public void requestFailed(int status, UserCollection details) {
                        activity.showSnackBar("No Internet connection detected.", Snackbar.LENGTH_LONG);
                        Log.d(TAG, "requestLeaderboardData: " + details);
                        stopRefreshing();
                    }

                    @Override
                    public void requestFinished(final int status, final UserCollection collection) {
                        mBackgroundHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                if (collection != null) {
                                    if (!savedUsername.equals("")) {
                                        for (LeaderboardData user : collection.getUserList()) {
                                            if (user.getUsername().equals(savedUsername)) {
                                                mCountryCode = user.getCountryCode();
                                                mUserPosition = user.getRank();
                                                refreshRegionTab();
                                                break;
                                            }
                                        }
                                    } else {
                                        mUserPosition = 0;
                                        mCountryCode = "";
                                    }
                                    if (mTimePeriod == periodType && mRegionType == regionType) {
                                        mUserList.clear();
                                        mLeaderboardAdapter.resetLastAnimatedItem();
                                        mUserList.addAll(collection.getUserList());
                                        if (mLeaderboardAdapter != null) {
                                            mHandler.post(new Runnable() {

                                                @Override
                                                public void run() {
                                                    mLeaderboardAdapter.setUserPosition(mUserPosition);
                                                    mLeaderboardAdapter.notifyDataSetChanged();
                                                    mRecyclerView.scrollToPosition(0);
                                                    mRecyclerView.stopScroll();
                                                    mRecyclerView.smoothScrollToPosition(mUserPosition);
                                                }
                                            });
                                        }
                                    }
                                }
                                stopRefreshing();
                            }
                        });
                    }
                }, date, region);
            }
        });
    }

    private void refreshRegionTab() {
        if (activity != null && mTabLayout != null) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (savedUsername.equals("")) {
                        mTabLayout.removeTabAt(1);
                    } else {
                        TabLayout.Tab tab = mTabLayout.getTabAt(1);
                        if (tab != null) {
                            tab.setText(new Locale("", mCountryCode, "").getDisplayCountry());
                        }
                    }
                }
            });
        }
    }
}
