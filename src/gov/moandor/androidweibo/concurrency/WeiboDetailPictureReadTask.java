package gov.moandor.androidweibo.concurrency;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import gov.moandor.androidweibo.util.FileUtils;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.widget.ImageWebView;
import gov.moandor.androidweibo.widget.WeiboDetailPicView;

public class WeiboDetailPictureReadTask extends MyAsyncTask<Void, Integer, Boolean> {
    private String mUrl;
    private String mPath;
    private ImageDownloader.ImageType mType;
    private WeiboDetailPicView mView;
    private ImageView mImageView;
    private ImageWebView mImageWebView;
    private Button mRetryButton;
    private ProgressBar mProgressBar;
    
    public WeiboDetailPictureReadTask(String url, ImageDownloader.ImageType type, WeiboDetailPicView view) {
        mUrl = url;
        mType = type;
        mView = view;
        mImageView = view.getImageView();
        mImageWebView = view.getImageWebView();
        mRetryButton = view.getRetryButton();
        mProgressBar = view.getProgressBar();
    }
    
    @Override
    protected void onPreExecute() {
        mImageView.setVisibility(View.GONE);
        mImageWebView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mRetryButton.setVisibility(View.GONE);
    }
    
    @Override
    protected Boolean doInBackground(Void... params) {
        mPath = FileUtils.getImagePathFromUrl(mUrl, mType);
        return ImageDownloadTaskCache.waitForPictureDownload(mUrl, mDownloadListener, mPath, mType);
    }
    
    @Override
    protected void onProgressUpdate(Integer... values) {
        mProgressBar.setProgress(values[0]);
        mProgressBar.setMax(values[1]);
    }
    
    @Override
    protected void onPostExecute(Boolean result) {
        mProgressBar.setVisibility(View.GONE);
        if (result) {
            mView.setImage(mPath);
        } else {
            mRetryButton.setVisibility(View.VISIBLE);
            mRetryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new WeiboDetailPictureReadTask(mUrl, mType, mView).execute();
                }
            });
        }
    }
    
    private HttpUtils.DownloadListener mDownloadListener = new HttpUtils.DownloadListener() {
        @Override
        public void onPushProgress(int progress, int max) {
            publishProgress(progress, max);
        }
        
        @Override
        public void onComplete() {}
        
        @Override
        public void onCancelled() {}
    };
}
