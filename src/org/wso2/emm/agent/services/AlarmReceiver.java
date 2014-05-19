package org.wso2.emm.agent.services;

import java.util.Map;

import org.wso2.emm.agent.R;
import org.wso2.emm.agent.utils.CommonUtilities;
import org.wso2.emm.agent.utils.ServerUtilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
	 
    private static final String DEBUG_TAG = "AlarmReceiver";
    Map<String, String> server_res = null;
    Context context;
    OperationsAsync getOperations = null;
    ProcessMessage processMsg = null;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(DEBUG_TAG, "Recurring alarm; requesting download service.");
        this.context = context;
        String mode=CommonUtilities.getPref(context, context.getResources().getString(R.string.shared_pref_notifier));
		if(mode.trim().toUpperCase().equals("LOCAL")){
            getOperations = new OperationsAsync();
    		getOperations.execute("");
		}
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
        	processMsg = new ProcessMessage(context, CommonUtilities.MESSAGE_MODE_LOCAL, result);
        }
     }
    
    public String getOperations(){
		String server_res = null;
		try {
//			server_res = "[{\"feature_code\": \"500P\",\"data\": ["+
//			                 " {\"mesage_id\": \"2\", \"data\": {\"feature_code\": \"506A\",\"data\": {\"notification\":\"sdfsdf\"}}}"+
//			             " ]},{\"feature_code\": \"513A\",\"data\": [{\"mesage_id\": \"2\",\"data\": {\"function\": \"disabled\"}}]}]";
			server_res = ServerUtilities.getOperationList(context);	
			//Log.e("server_res",server_res);
			//server_res=ServerUtilities.readJson(context);
			return server_res;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
 
}
