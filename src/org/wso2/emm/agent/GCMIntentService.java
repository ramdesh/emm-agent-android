/*
 ~ Copyright (c) 2014, WSO2 Inc. (http://wso2.com/) All Rights Reserved.
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~      http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
*/
package org.wso2.emm.agent;

import org.wso2.emm.agent.R;
import org.wso2.emm.agent.api.ApplicationManager;
import org.wso2.emm.agent.services.Config;
import org.wso2.emm.agent.services.ProcessMessage;
import org.wso2.emm.agent.utils.CommonUtilities;
import org.wso2.emm.agent.utils.ServerUtilities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {
	DevicePolicyManager devicePolicyManager;
	ApplicationManager appList;
	static final int ACTIVATION_REQUEST = 47; 
	ProcessMessage processMsg = null;
	OperationsAsync getOperations = null;
	Context context;
		
    @SuppressWarnings("hiding")
    private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
        super(CommonUtilities.SENDER_ID);
    }
   

    @Override
    protected void onRegistered(Context context, String registrationId) {
    	if(CommonUtilities.DEBUG_MODE_ENABLED){Log.i(TAG, "Device registered: regId = " + registrationId);}
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getResources().getString(R.string.shared_pref_regId), registrationId);
        editor.commit();
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
    	if(CommonUtilities.DEBUG_MODE_ENABLED){Log.i(TAG, "Device unregistered");}

        if (GCMRegistrar.isRegisteredOnServer(context)) {

        	
        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
        	if(CommonUtilities.DEBUG_MODE_ENABLED){Log.i(TAG, "Ignoring unregister callback");}
        }
    }
    
	@Override
    protected void onMessage(Context context, Intent intent) {
		this.context=context;
		if(CommonUtilities.GCM_ENABLED){
			if(!CommonUtilities.NEW_MESSAGE_FLOW_ENABLED){
				String code = intent.getStringExtra(getResources().getString(R.string.intent_extra_message)).trim();
		        Config.context = this;
		    	processMsg = new ProcessMessage(Config.context, CommonUtilities.MESSAGE_MODE_GCM, intent);
			}else{
				getOperations = new OperationsAsync();
				getOperations.execute("");
			}
		}
    }
	    

    @Override
    protected void onDeletedMessages(Context context, int total) {
    	if(CommonUtilities.DEBUG_MODE_ENABLED){Log.i(TAG, "Received deleted messages notification");}
        String message = getString(R.string.gcm_deleted, total);
      //  displayMessage(context, message);
        // notifies user
        generateNotification(context, message);
    }

    @Override
    public void onError(Context context, String errorId) {
    	if(CommonUtilities.DEBUG_MODE_ENABLED){Log.i(TAG, "Received error: " + errorId);}

    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
    	if(CommonUtilities.DEBUG_MODE_ENABLED){Log.i(TAG, "Received recoverable error: " + errorId);}

        return super.onRecoverableError(context, errorId);
    }
    
    class OperationsAsync extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            try {
            	return getOperations();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(String result) {
        	//IMPLEMENT MESSAGE PROCESSING
        }
     }
    
    public String getOperations(){
		String server_res = null;
		try {
			server_res = ServerUtilities.getOperationList(this.context);			
			return server_res;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message) {
        int icon = R.drawable.ic_stat_gcm;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        String title = context.getString(R.string.app_name);
        Intent notificationIntent = new Intent(context, NotifyActivity.class);
        notificationIntent.putExtra(context.getResources().getString(R.string.intent_extra_notification), message);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }
}
