package gov.moandor.androidweibo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.adapter.MainPagerAdapter;
import gov.moandor.androidweibo.bean.UnreadCount;
import gov.moandor.androidweibo.fragment.AbsMainTimelineFragment;
import gov.moandor.androidweibo.fragment.AtmeListFragment;
import gov.moandor.androidweibo.fragment.CommentListFragment;
import gov.moandor.androidweibo.fragment.MainDrawerFragment;
import gov.moandor.androidweibo.fragment.ProfileFragment;
import gov.moandor.androidweibo.fragment.WeiboListFragment;
import gov.moandor.androidweibo.util.ActivityUtils;
import gov.moandor.androidweibo.util.ConfigManager;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Utilities;

public class MainActivity extends AbsActivity implements ViewPager.OnPageChangeListener,
        MainDrawerFragment.OnAccountClickListener, ActionBar.OnNavigationListener {
    private static final String STATE_TAB = "state_tab";
    private static final String STATE_UNREAD_COUNT = "state_unread_count";
    public static final int WEIBO_LIST = 0;
    public static final int ATME_LIST = 1;
    public static final int COMMENT_LIST = 2;
    private static final int PROFILE = 3;
    public static final String UNREAD_PAGE_POSITION;
    public static final String UNREAD_GROUP;
    public static final String ACCOUNT_INDEX;
    public static final String ACTION_UNREAD_UPDATED;
    public static final String UNREAD_COUNT;
    
    static {
        String packageName = GlobalContext.getInstance().getPackageName();
        UNREAD_PAGE_POSITION = packageName + ".UNREAD_PAGE_POSITION";
        UNREAD_GROUP = packageName + ".UNREAD_GROUP";
        ACCOUNT_INDEX = packageName + ".ACCOUNT_INDEX";
        ACTION_UNREAD_UPDATED = packageName + ".action.UNREAD_UPDATED";
        UNREAD_COUNT = packageName + ".UNREAD_COUNT";
    }
    
    private static boolean sRunning;
    private int mUnreadPage = -1;
    private int mUnreadGroup = -1;
    private ViewPager mViewPager;
    private WeiboListFragment mWeiboListFragment;
    private AtmeListFragment mAtmeListFragment;
    private CommentListFragment mCommentListFragment;
    private ProfileFragment mProfileFragment;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mWeiboListSpinnerAdapter;
    private ArrayAdapter<String> mAtmeListSpinnerAdapter;
    private ArrayAdapter<String> mCommentListSpinnerAdapter;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private PagerSlidingTabStrip mTabStrip;
    private MainPagerAdapter mPagerAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (GlobalContext.getCurrentAccount() == null) {
            startActivity(ActivityUtils.authorizeActivity());
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        mWeiboListSpinnerAdapter =
                new ArrayAdapter<String>(GlobalContext.getInstance(), R.layout.main_spinner, android.R.id.text1,
                        getResources().getStringArray(R.array.weibo_list_spinner));
        mWeiboListSpinnerAdapter.setDropDownViewResource(R.layout.main_navigation_spinner_item);
        mAtmeListSpinnerAdapter =
                new ArrayAdapter<String>(GlobalContext.getInstance(), R.layout.main_spinner, android.R.id.text1,
                        getResources().getStringArray(R.array.atme_list_spinner));
        mAtmeListSpinnerAdapter.setDropDownViewResource(R.layout.main_navigation_spinner_item);
        mCommentListSpinnerAdapter =
                new ArrayAdapter<String>(GlobalContext.getInstance(), R.layout.main_spinner, android.R.id.text1,
                        getResources().getStringArray(R.array.comment_list_spinner));
        mCommentListSpinnerAdapter.setDropDownViewResource(R.layout.main_navigation_spinner_item);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActionBarDrawerToggle =
                new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.open_drawer,
                        R.string.close_drawer);
        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment drawer = fragmentManager.findFragmentById(R.id.left_drawer);
        if (drawer == null) {
            drawer = new MainDrawerFragment();
        }
        if (!drawer.isAdded()) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.left_drawer, drawer);
            fragmentTransaction.commit();
            fragmentManager.executePendingTransactions();
        }
        mWeiboListFragment =
                (WeiboListFragment) fragmentManager.findFragmentByTag(MainPagerAdapter.makeFragmentName(WEIBO_LIST));
        if (mWeiboListFragment == null) {
            mWeiboListFragment = new WeiboListFragment();
        }
        mAtmeListFragment =
                (AtmeListFragment) fragmentManager.findFragmentByTag(MainPagerAdapter.makeFragmentName(ATME_LIST));
        if (mAtmeListFragment == null) {
            mAtmeListFragment = new AtmeListFragment();
        }
        mCommentListFragment =
                (CommentListFragment) fragmentManager
                        .findFragmentByTag(MainPagerAdapter.makeFragmentName(COMMENT_LIST));
        if (mCommentListFragment == null) {
            mCommentListFragment = new CommentListFragment();
        }
        mProfileFragment =
                (ProfileFragment) fragmentManager.findFragmentByTag(MainPagerAdapter.makeFragmentName(PROFILE));
        if (mProfileFragment == null) {
            mProfileFragment = new ProfileFragment();
        }
        Fragment[] fragments = {mWeiboListFragment, mAtmeListFragment, mCommentListFragment, mProfileFragment};
        mPagerAdapter = new MainPagerAdapter(fragmentManager, fragments);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount() - 1);
        mUnreadPage = getIntent().getIntExtra(UNREAD_PAGE_POSITION, -1);
        if (savedInstanceState != null) {
            int tab = savedInstanceState.getInt(STATE_TAB);
            mViewPager.setCurrentItem(tab);
            onPageSelected(tab);
            int unreadCount = savedInstanceState.getInt(STATE_UNREAD_COUNT);
            mPagerAdapter.setWeiboUnreadCount(unreadCount);
        } else if (mUnreadPage != -1) {
            int accountIndex = getIntent().getIntExtra(ACCOUNT_INDEX, -1);
            ConfigManager.setCurrentAccountIndex(accountIndex);
            mUnreadGroup = getIntent().getIntExtra(UNREAD_GROUP, -1);
            Bundle args = new Bundle();
            args.putBoolean(AbsMainTimelineFragment.IS_FROM_UNREAD, true);
            switch (mUnreadPage) {
            case ATME_LIST:
                mAtmeListFragment.setArguments(args);
                break;
            case COMMENT_LIST:
                mCommentListFragment.setArguments(args);
                break;
            }
            getIntent().removeExtra(UNREAD_PAGE_POSITION);
            getIntent().removeExtra(UNREAD_GROUP);
            mViewPager.setCurrentItem(mUnreadPage);
            onPageSelected(mUnreadPage);
        } else {
            onPageSelected(0);
        }
        mTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mTabStrip.setViewPager(mViewPager);
        mTabStrip.setOnPageChangeListener(this);
        mTabStrip.setDividerColorResource(R.color.tab_divider);
        mTabStrip.setTextColorResource(android.R.color.white);
        mTabStrip.setIndicatorColorResource(R.color.holo_blue_light);
        mTabStrip.setTabPaddingLeftRight(getResources().getDimensionPixelSize(R.dimen.tab_padding));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UNREAD_UPDATED);
        Utilities.registerReceiver(mUnreadUpdateReciever, intentFilter);
        sRunning = true;
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActionBarDrawerToggle.syncState();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_TAB, mViewPager.getCurrentItem());
        outState.putInt(STATE_UNREAD_COUNT, mPagerAdapter.getWeiboUnreadCount());
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        sRunning = false;
        Utilities.unregisterReceiver(mUnreadUpdateReciever);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem orientation = menu.findItem(R.id.orientation);
        switch (ConfigManager.getScreenOrientation()) {
        case ConfigManager.ORIENTATION_USER:
            orientation.setTitle(R.string.lock_orientation);
            if (Utilities.isScreenLandscape()) {
                orientation.setIcon(R.drawable.ic_menu_lock_orientation_land);
            } else {
                orientation.setIcon(R.drawable.ic_menu_lock_orientation_port);
            }
            break;
        case ConfigManager.ORIENTATION_LANDSCAPE:
        case ConfigManager.ORIENTATION_PORTRAIT:
            orientation.setTitle(R.string.unlock_orientation);
            orientation.setIcon(R.drawable.ic_menu_unlock_orientation);
            break;
        }
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mActionBarDrawerToggle.onOptionsItemSelected(item);
        switch (item.getItemId()) {
        case R.id.write_weibo:
            writeWeibo();
            return true;
        case R.id.refresh:
            refresh();
            return true;
        case R.id.settings:
            settings();
            return true;
        case R.id.orientation:
            toggleOrientationLock();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    public void refresh() {
        switch (mViewPager.getCurrentItem()) {
        case WEIBO_LIST:
            mWeiboListFragment.refresh();
            break;
        case ATME_LIST:
            mAtmeListFragment.refresh();
            break;
        case COMMENT_LIST:
            mCommentListFragment.refresh();
            break;
        case PROFILE:
            mProfileFragment.refresh();
            break;
        }
    }
    
    @Override
    public void onPageScrollStateChanged(int state) {}
    
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
    
    @Override
    public void onPageSelected(int position) {
        ActionBar actionBar = getSupportActionBar();
        if (position == mUnreadPage) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            actionBar.setTitle("");
            switch (position) {
            case ATME_LIST:
                actionBar.setListNavigationCallbacks(mAtmeListSpinnerAdapter, this);
                actionBar.setSelectedNavigationItem(mUnreadGroup);
                break;
            case COMMENT_LIST:
                actionBar.setListNavigationCallbacks(mCommentListSpinnerAdapter, this);
                actionBar.setSelectedNavigationItem(mUnreadGroup);
                break;
            }
            mUnreadPage = -1;
            mUnreadGroup = -1;
            return;
        }
        switch (position) {
        case WEIBO_LIST:
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            actionBar.setTitle("");
            actionBar.setListNavigationCallbacks(mWeiboListSpinnerAdapter, this);
            actionBar.setSelectedNavigationItem(ConfigManager.getWeiboGroup());
            break;
        case ATME_LIST:
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            actionBar.setTitle("");
            actionBar.setListNavigationCallbacks(mAtmeListSpinnerAdapter, this);
            actionBar.setSelectedNavigationItem(ConfigManager.getAtmeFilter());
            break;
        case COMMENT_LIST:
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            actionBar.setTitle("");
            actionBar.setListNavigationCallbacks(mCommentListSpinnerAdapter, this);
            actionBar.setSelectedNavigationItem(ConfigManager.getCommentFilter());
            break;
        case PROFILE:
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setTitle(GlobalContext.getCurrentAccount().user.name);
            break;
        }
    }
    
    @Override
    public void onAccountClick(int position) {
        mDrawerLayout.closeDrawer(Gravity.LEFT);
        mWeiboListFragment.saveListPosition();
        mAtmeListFragment.saveListPosition();
        mCommentListFragment.saveListPosition();
        ConfigManager.setCurrentAccountIndex(position);
        mWeiboListFragment.notifyAccountOrGroupChanged();
        mAtmeListFragment.notifyAccountOrGroupChanged();
        mCommentListFragment.notifyAccountOrGroupChanged();
        mProfileFragment.notifyAccountChanged();
        if (mViewPager.getCurrentItem() == PROFILE) {
            getSupportActionBar().setTitle(GlobalContext.getCurrentAccount().user.name);
        }
    }
    
    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        switch (mViewPager.getCurrentItem()) {
        case WEIBO_LIST:
            if (itemPosition != ConfigManager.getWeiboGroup()) {
                mWeiboListFragment.saveListPosition();
                ConfigManager.setWeiboGroup(itemPosition);
                mWeiboListFragment.notifyAccountOrGroupChanged();
            }
            break;
        case ATME_LIST:
            if (itemPosition != ConfigManager.getAtmeFilter()) {
                mAtmeListFragment.saveListPosition();
                ConfigManager.setAtmeFilter(itemPosition);
                mAtmeListFragment.notifyAccountOrGroupChanged();
            }
            break;
        case COMMENT_LIST:
            if (itemPosition != ConfigManager.getCommentFilter()) {
                mCommentListFragment.saveListPosition();
                ConfigManager.setCommentFilter(itemPosition);
                mCommentListFragment.notifyAccountOrGroupChanged();
            }
            break;
        }
        return true;
    }
    
    public boolean isCurrentFragment(Fragment fragment) {
        switch (mViewPager.getCurrentItem()) {
        case WEIBO_LIST:
            return fragment instanceof WeiboListFragment;
        case ATME_LIST:
            return fragment instanceof AtmeListFragment;
        case COMMENT_LIST:
            return fragment instanceof CommentListFragment;
        case PROFILE:
            return fragment instanceof ProfileFragment;
        default:
            return false;
        }
    }
    
    private void writeWeibo() {
        startActivity(ActivityUtils.writeWeiboActivity());
    }
    
    private void settings() {
        startActivity(ActivityUtils.settingsActivity());
    }
    
    private void toggleOrientationLock() {
        if (ConfigManager.getScreenOrientation() == ConfigManager.ORIENTATION_USER) {
            if (Utilities.isScreenLandscape()) {
                ConfigManager.setScreenOrientation(ConfigManager.ORIENTATION_LANDSCAPE);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                ConfigManager.setScreenOrientation(ConfigManager.ORIENTATION_PORTRAIT);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            ConfigManager.setScreenOrientation(ConfigManager.ORIENTATION_USER);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        }
        supportInvalidateOptionsMenu();
    }
    
    public void resetWeiboUnreadCount() {
        mPagerAdapter.setWeiboUnreadCount(0);
        mTabStrip.notifyDataSetChanged();
    }
    
    public static boolean isRunning() {
        return sRunning;
    }
    
    private BroadcastReceiver mUnreadUpdateReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            UnreadCount count = intent.getParcelableExtra(UNREAD_COUNT);
            if (count.weiboStatus > 0) {
                mPagerAdapter.setWeiboUnreadCount(count.weiboStatus);
            }
            mTabStrip.notifyDataSetChanged();
        }
    };
}
