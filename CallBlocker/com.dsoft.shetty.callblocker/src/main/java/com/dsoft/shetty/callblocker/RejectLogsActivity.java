package com.dsoft.shetty.callblocker;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static com.dsoft.shetty.callblocker.MainActivity.REJECT_LOG_FILE;
import static com.dsoft.shetty.callblocker.MainActivity.SETTING_PREFS_NAME;

public class RejectLogsActivity extends AppCompatActivity {
    ArrayList<RejectLog> rejectLogs;
    RejectLogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reject_logs);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_log);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Add code to delete all the logs
                SharedPreferences settings = getSharedPreferences(SETTING_PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("blockedCallCount", 0);
                editor.commit();

                rejectLogs.clear();
                saveRejectLogs();
                adapter.notifyDataSetChanged();
                Snackbar.make(view, "All reject logs cleared", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences call_log = getSharedPreferences(REJECT_LOG_FILE, 0);
        String jsonRejectNumbers = call_log.getString("jsonRejectNumbers", "");
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<RejectLog>>(){}.getType();

        if(!jsonRejectNumbers.equals("")) {
            rejectLogs = gson.fromJson(jsonRejectNumbers, type);
        } else {
            rejectLogs = new ArrayList<RejectLog>();
        }

        final ListView listView = (ListView)findViewById(R.id.list_view_log);
        adapter = new RejectLogAdapter(this, rejectLogs);
        listView.setAdapter(adapter);
        listView.setEmptyView(findViewById(R.id.emptyLog));
        registerForContextMenu(listView);
    }

    private void saveRejectLogs()
    {
        SharedPreferences call_log = getSharedPreferences(REJECT_LOG_FILE, 0);
        SharedPreferences.Editor editor_log = call_log.edit();
        Gson gson = new Gson();
        String jsonRejectNumbers = gson.toJson(rejectLogs);
        editor_log.putString("jsonRejectNumbers", jsonRejectNumbers);
        editor_log.commit();
        return;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.list_view_log) {
            getMenuInflater().inflate(R.menu.menu_context, menu);
        }
        return;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        int length = rejectLogs.size() - 1;
        switch(item.getItemId()) {
            case R.id.action_delete:
                rejectLogs.remove(length - index);
                saveRejectLogs();
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Selected call log deleted !", Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_copy:
                ClipboardManager myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                String text = rejectLogs.get(length - index).getPhoneNumber();
                ClipData myClip = ClipData.newPlainText("text", text);
                myClipboard.setPrimaryClip(myClip);
                Toast.makeText(this, "Selected number copied to clipboard.", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
