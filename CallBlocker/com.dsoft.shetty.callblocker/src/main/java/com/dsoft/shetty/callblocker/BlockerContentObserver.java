package com.dsoft.shetty.callblocker;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.provider.CallLog;
import android.widget.Toast;

/* Calss to observe the Call log and take action if there is any blocked number apears here */
public class BlockerContentObserver extends ContentObserver {
    private Context context;
    private String phoneNumber;

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public BlockerContentObserver(Handler handler, Context context) {
        super(handler);
        this.context=context;
    }

    @Override
    public boolean deliverSelfNotifications() {
        return false;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        deleteNumber(phoneNumber);
        unregisterContentObserver(this);
    }

    private void unregisterContentObserver(BlockerContentObserver mContentObserver)
    {
        try {
            context.getContentResolver().unregisterContentObserver(mContentObserver);
        } catch (IllegalStateException ise) {
            // Do Nothing.  Observer has already been unregistered.
        }
    }

    private void deleteNumber(String phoneNumber) {
            try
            {
                String strNumberOne[] = {phoneNumber};
                Cursor cursor = context.getContentResolver().query(
                        CallLog.Calls.CONTENT_URI, null,
                        CallLog.Calls.NUMBER + " = ? ", strNumberOne, CallLog.Calls.DATE + " DESC");

                if (cursor.moveToFirst()) {
                    int idOfRowToDelete = cursor.getInt(cursor.getColumnIndex(CallLog.Calls._ID));
                    int foo = context.getContentResolver().delete(
                            CallLog.Calls.CONTENT_URI,
                            CallLog.Calls._ID + " = ? ",
                            new String[]{String.valueOf(idOfRowToDelete)});

                }
            } catch (SecurityException ex) {
                Toast.makeText(context, "Delete Call Log Failed !", Toast.LENGTH_SHORT);
            }
    }
}
