package gov.moandor.androidweibo.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class WeiboStatus extends AbsItemBean {
    public boolean favorited;
    public int picCount;
    public String[] thumbnailPic;
    public String[] bmiddlePic;
    public String[] originalPic;
    public WeiboGeo weiboGeo;
    public int userOnlineStatus;
    public WeiboStatus retweetStatus;
    public int repostCount;
    public int commentCount;
    public int attitudeCount;
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeBooleanArray(new boolean[]{favorited});
        dest.writeInt(picCount);
        if (picCount > 0) {
            dest.writeStringArray(thumbnailPic);
            dest.writeStringArray(bmiddlePic);
            dest.writeStringArray(originalPic);
        }
        dest.writeParcelable(weiboGeo, flags);
        dest.writeInt(userOnlineStatus);
        dest.writeParcelable(retweetStatus, flags);
        dest.writeInt(repostCount);
        dest.writeInt(commentCount);
        dest.writeInt(attitudeCount);
    }
    
    public static final Parcelable.Creator<WeiboStatus> CREATOR = new ParcelableCreator<WeiboStatus>() {
        @Override
        public WeiboStatus createFromParcel(Parcel source) {
            WeiboStatus result = super.createFromParcel(source);
            boolean[] bools = new boolean[1];
            source.readBooleanArray(bools);
            result.favorited = bools[0];
            result.picCount = source.readInt();
            if (result.picCount > 0) {
                result.thumbnailPic = new String[result.picCount];
                result.bmiddlePic = new String[result.picCount];
                result.originalPic = new String[result.picCount];
                source.readStringArray(result.thumbnailPic);
                source.readStringArray(result.bmiddlePic);
                source.readStringArray(result.originalPic);
            }
            result.weiboGeo = source.readParcelable(WeiboGeo.class.getClassLoader());
            result.userOnlineStatus = source.readInt();
            result.retweetStatus = source.readParcelable(WeiboStatus.class.getClassLoader());
            result.repostCount = source.readInt();
            result.commentCount = source.readInt();
            result.attitudeCount = source.readInt();
            return result;
        }
        
        @Override
        public WeiboStatus[] newArray(int size) {
            return new WeiboStatus[size];
        }
        
        @Override
        protected WeiboStatus onCreateObject() {
            return new WeiboStatus();
        }
    };
}
