package gov.moandor.androidweibo.concurrency;

import android.graphics.drawable.ColorDrawable;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.util.Utilities;

import java.lang.ref.WeakReference;

public class AsyncDrawable extends ColorDrawable {
    private WeakReference<ImageReadTask> mTaskRef;
    
    public AsyncDrawable(ImageReadTask task) {
        super(Utilities.getColor(R.attr.image_place_holder));
        mTaskRef = new WeakReference<ImageReadTask>(task);
    }
    
    public ImageReadTask getTask() {
        return mTaskRef.get();
    }
}
