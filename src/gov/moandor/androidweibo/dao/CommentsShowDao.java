package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.HttpParams;

public class CommentsShowDao extends BaseWeiboCommentTimelineDao {
    private long mWeiboId;
    
    @Override
    protected String getUrl() {
        return UrlHelper.COMMENTS_SHOW;
    }
    
    @Override
    protected void addParams(HttpParams params) {
        super.addParams(params);
        params.putParam("id", mWeiboId);
    }
    
    public void setWeiboId(long weiboId) {
        mWeiboId = weiboId;
    }
}
