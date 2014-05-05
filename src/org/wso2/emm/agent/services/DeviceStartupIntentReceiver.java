package org.wso2.emm.agent.services;

import org.wso2.emm.agent.utils.CommonUtilities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.Toast;

public class DeviceStartupIntentReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(final Context context, Intent intent1) {
    	setRecurringAlarm(context);
    }
    
    private void setRecurringAlarm(Context context) {
    	if(CommonUtilities.LOCAL_NOTIFICATIONS_ENABLED){
		    long firstTime = SystemClock.elapsedRealtime();
		    firstTime += 3 * 1000;
		    
		    Intent downloader = new Intent(context, AlarmReceiver.class);
		    PendingIntent recurringDownload = PendingIntent.getBroadcast(context,
		            0, downloader, PendingIntent.FLAG_CANCEL_CURRENT);
		    AlarmManager alarms = (AlarmManager) context.getSystemService(
		            Context.ALARM_SERVICE);
		    alarms.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime,
		            60000, recurringDownload);
    	}
	}
}