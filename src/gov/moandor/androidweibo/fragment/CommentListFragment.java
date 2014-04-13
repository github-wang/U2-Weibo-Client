package gov.moandor.androidweibo.fragment;

import android.content.Intent;
import android.support.v7.view.ActionMode;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;

import gov.moandor.androidweibo.activity.MainActivity;
import gov.moandor.androidweibo.activity.WriteCommentActivity;
import gov.moandor.androidweibo.adapter.CommentListAdapter;
import gov.moandor.androidweibo.bean.TimelinePosition;
import gov.moandor.androidweibo.bean.WeiboComment;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.BaseTimelineJsonDao;
import gov.moandor.androidweibo.dao.CommentsByMeDao;
import gov.moandor.androidweibo.dao.CommentsMentionsDao;
import gov.moandor.androidweibo.dao.CommentsToMeDao;
import gov.moandor.androidweibo.util.CommentListActionModeCallback;
import gov.moandor.androidweibo.util.DatabaseUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.List;

public class CommentListFragment extends AbsMainTimelineFragment<WeiboComment, CommentListAdapter> {
    public static final int ALL = 0;
    public static final int FOLLOWED = 1;
    public static final int ATME = 2;
    public static final int BY_ME = 3;
    
    @Override
    CommentListAdapter createListAdapter() {
        return new CommentListAdapter();
    }
    
    @Override
    List<WeiboComment> getBeansFromJson(String json) throws WeiboException {
        return JsonUtils.getWeiboCommentsFromJson(json);
    }
    
    @Override
    List<WeiboComment> getBeansFromDatabase() {
        long accountId = GlobalContext.getCurrentAccount().user.id;
        int filter = GlobalContext.getCommentFilter();
        return DatabaseUtils.getComments(accountId, filter);
    }
    
    @Override
    void saveRefreshResultToDatabase(List<WeiboComment> comments) {
        long accountId = GlobalContext.getCurrentAccount().user.id;
        int filter = GlobalContext.getCommentFilter();
        DatabaseUtils.removeComments(accountId, filter);
        DatabaseUtils.insertComments(comments, accountId, filter);
    }
    
    @Override
    void saveLoadMoreResultToDatabase(SparseArray<WeiboComment> comments) {
        long accountId = GlobalContext.getCurrentAccount().user.id;
        int filter = GlobalContext.getCommentFilter();
        DatabaseUtils.insertComments(comments, accountId, filter);
    }
    
    @Override
    protected BaseTimelineJsonDao<WeiboComment> onCreateDao() {
        switch (GlobalContext.getCommentFilter()) {
        case ATME:
            return new CommentsMentionsDao();
        case BY_ME:
            return new CommentsByMeDao();
        case ALL:
        case FOLLOWED:
        default:
            CommentsToMeDao dao = new CommentsToMeDao();
            if (GlobalContext.getCommentFilter() == FOLLOWED) {
                dao.setFilter(1);
            }
            return dao;
        }
    }
    
    @Override
    public void saveListPosition() {
        View view = mListView.getChildAt(0);
        if (view != null) {
            final TimelinePosition position = new TimelinePosition();
            position.position = mListView.getFirstVisiblePosition();
            position.top = mListView.getChildAt(0).getTop();
            final long accountId = GlobalContext.getCurrentAccount().user.id;
            final int filter = GlobalContext.getCommentFilter();
            MyAsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    DatabaseUtils
                            .insertOrUpdateTimelinePosition(position, MainActivity.COMMENT_LIST, filter, accountId);
                }
            });
            
        }
    }
    
    @Override
    void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), WriteCommentActivity.class);
        WeiboComment comment = mAdapter.getItem(position);
        intent.putExtra(WriteCommentActivity.COMMENTED_WEIBO_STATUS, comment.weiboStatus);
        intent.putExtra(WriteCommentActivity.REPLIED_WEIBO_COMMENT, comment);
        startActivity(intent);
    }
    
    @Override
    ActionMode.Callback getActionModeCallback() {
        CommentListActionModeCallback callback = new CommentListActionModeCallback() {
            @Override
            protected void removeFromDatabase(final int position, final long accountId, final int group) {
                MyAsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        DatabaseUtils.removeComment(position, accountId, group);
                    }
                });
            }
        };
        callback.setAdapter(mAdapter);
        callback.setFragment(this);
        return callback;
    }
    
    @Override
    RefreshTask createRefreshTask() {
        return new MainRefreshTask();
    }
    
    @Override
    LoadMoreTask createLoadMoreTask() {
        return new MainLoadMoreTask();
    }
    
    @Override
    TimelinePosition onRestoreListPosition() {
        return DatabaseUtils.getTimelinePosition(MainActivity.COMMENT_LIST, GlobalContext.getCommentFilter());
    }
}
