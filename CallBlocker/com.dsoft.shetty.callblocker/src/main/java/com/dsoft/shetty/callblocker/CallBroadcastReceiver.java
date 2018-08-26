package com.dsoft.shetty.callblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.dsoft.shetty.callblocker.MainActivity.REJECT_LOG_FILE;
import static com.dsoft.shetty.callblocker.MainActivity.SETTING_PREFS_NAME;

public class CallBroadcastReceiver extends BroadcastReceiver {
    BlockerContentObserver mContentObserver;
    ArrayList<RejectLog> rejectLogs;

    @Override
    public void onReceive(Context context, Intent intent) {
        ArrayList<BlockNumber> blockNumbers;
        SharedPreferences settings = context.getSharedPreferences(SETTING_PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        boolean enable_call_block = settings.getBoolean("enableCallBlock", false);

        if(!enable_call_block) return;

        SharedPreferences call_logs = context.getSharedPreferences(REJECT_LOG_FILE, 0);
        SharedPreferences.Editor editor_log = call_logs.edit();

        String jsonRejectNumbers = call_logs.getString("jsonRejectNumbers", "");
        Gson gson_log = new Gson();
        Type type_log = new TypeToken<ArrayList<RejectLog>>(){}.getType();

        if(!jsonRejectNumbers.equals("")) {
            rejectLogs = gson_log.fromJson(jsonRejectNumbers, type_log);
        } else {
            rejectLogs = new ArrayList<RejectLog>();
        }

        boolean delete_call_Log = settings.getBoolean("deleteCallLog", false);
        int blocked_call_count = settings.getInt("blockedCallCount", 0);

        if(intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING))
        {
            String incoming_number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

            String jsonBlockNumbers = settings.getString("jsonBlockNumbers", "");
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<BlockNumber>>(){}.getType();

            if(!jsonBlockNumbers.equals("")) {
                blockNumbers = gson.fromJson(jsonBlockNumbers, type);
            } else {
                return;
            }

            boolean flag = false;
            for(int i=0; i<blockNumbers.size(); i++)
            {
                if(blockNumbers.get(i).getPattern().equals("Starts with"))
                {
                    if(incoming_number.startsWith(blockNumbers.get(i).getPhoneNumber()))
                        flag = true;
                }
                else if(blockNumbers.get(i).getPattern().equals("Ends with"))
                {
                    if(incoming_number.endsWith(blockNumbers.get(i).getPhoneNumber()))
                        flag = true;
                }
                else if(blockNumbers.get(i).getPattern().equals("Contains"))
                {
                    if(incoming_number.contains(blockNumbers.get(i).getPhoneNumber()))
                        flag = true;
                }

                if(flag) break; // number is matched, no need to match further, optimize search
            }
            if(flag)
            {
                try
                {
                    declinePhone(context);
                    blocked_call_count++;
                    if(delete_call_Log) {
                        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                        RejectLog rejectLog = new RejectLog();
                        rejectLog.setPhoneNumber(incoming_number);
                        rejectLog.setDateTimeOfCall(currentDateTimeString);
                        rejectLogs.add(rejectLog);

                        jsonRejectNumbers = gson_log.toJson(rejectLogs);
                        editor_log.putString("jsonRejectNumbers", jsonRejectNumbers);
                        //editor_log.putString("dateTimeOfCall", currentDateTimeString);
                        //editor_log.putString("phoneNumber", incoming_number);
                        editor_log.commit();

                        mContentObserver = new BlockerContentObserver(new Handler(), context);
                        mContentObserver.setPhoneNumber(incoming_number);
                        RegisterContentObserver(context);
                    }
                    Toast.makeText(context, "Call from : " + incoming_number + " is rejected", Toast.LENGTH_LONG).show();
                    editor.putInt("blockedCallCount", blocked_call_count);
                    editor.commit();

                } catch (Exception e)
                {
                    Toast.makeText(context, "Unable to End Call from : " + incoming_number, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void RegisterContentObserver(Context context){
            context.getContentResolver().registerContentObserver(
                    android.provider.CallLog.Calls.CONTENT_URI,
                    true,
                    mContentObserver);
    }


    private void declinePhone(Context context) throws Exception {
        try {
            String serviceManagerName = "android.os.ServiceManager";
            String serviceManagerNativeName = "android.os.ServiceManagerNative";
            String telephonyName = "com.android.internal.telephony.ITelephony";
            Class<?> telephonyClass;
            Class<?> telephonyStubClass;
            Class<?> serviceManagerClass;
            Class<?> serviceManagerNativeClass;
            Method telephonyEndCall;
            Object telephonyObject;
            Object serviceManagerObject;
            telephonyClass = Class.forName(telephonyName);
            telephonyStubClass = telephonyClass.getClasses()[0];
            serviceManagerClass = Class.forName(serviceManagerName);
            serviceManagerNativeClass = Class.forName(serviceManagerNativeName);
            Method getService = serviceManagerClass.getMethod("getService", String.class);
            Method tempInterfaceMethod = serviceManagerNativeClass.getMethod("asInterface", IBinder.class);
            Binder tmpBinder = new Binder();
            tmpBinder.attachInterface(null, "fake");
            serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
            IBinder retbinder = (IBinder) getService.invoke(serviceManagerObject, "phone");
            Method serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder.class);
            telephonyObject = serviceMethod.invoke(null, retbinder);
            telephonyEndCall = telephonyClass.getMethod("endCall");
            telephonyEndCall.invoke(telephonyObject);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("unable", "msg cant dissconect call....");
        }
    }
}
