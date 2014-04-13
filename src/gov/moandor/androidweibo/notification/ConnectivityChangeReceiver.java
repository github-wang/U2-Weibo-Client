package gov.moandor.androidweibo.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import gov.moandor.androidweibo.util.GlobalContext;

public class ConnectivityChangeReceiver extends BroadcastReceiver {
    private static final int REQUEST_CODE = 0;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        judgeAlarm(context);
    }
    
    public static void judgeAlarm(Context context) {
        if (isConnected(context) && GlobalContext.isNotificationEnabled()) {
            startAlarm(context);
        } else {
            stopAlarm(context);
        }
    }
    
    private static void startAlarm(Context context) {
        long time;
        switch (GlobalContext.getNotificationFrequency()) {
        case GlobalContext.THREE_MINUTES:
            time = 3 * 60 * 1000;
            break;
        case GlobalContext.FIFTEEN_MINUTES:
        default:
            time = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
            break;
        case GlobalContext.HALF_HOUR:
            time = AlarmManager.INTERVAL_HALF_HOUR;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setClass(context, FetchUnreadMessageService.class);
        PendingIntent pendingIntent =
                PendingIntent.getService(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, time, pendingIntent);
    }
    
    private static void stopAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setClass(context, FetchUnreadMessageService.class);
        PendingIntent pendingIntent =
                PendingIntent.getService(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }
    
    private static boolean isConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
}
