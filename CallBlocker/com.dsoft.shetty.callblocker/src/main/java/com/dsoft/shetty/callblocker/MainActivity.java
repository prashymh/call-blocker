package com.dsoft.shetty.callblocker;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    final int PERMISSION_ACCESS_CALL_PHONE = 10;
    public static final String SETTING_PREFS_NAME = "CallBlockerSettings";
    public static final String REJECT_LOG_FILE = "RejectedCallDetails";
    ArrayList<BlockNumber> blockNumbers;
    String pattern = "Starts with";
    NumberAdapter adapter;
    final int PERMISSION_WRITE_CALL_LOG = 0;
    final int PERMISSION_READ_CALL_LOG = 1;
    int blocked_call_count;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BlockNumber blockNumber;
                blockNumber = new BlockNumber();

                EditText input = (EditText) findViewById(R.id.TextInput);
                String blocked_number = input.getText().toString();
                if(blocked_number.equals(""))
                {
                    Snackbar.make(view, "Number not entered !", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }
                blockNumber.setPhoneNumber(blocked_number);
                blockNumber.setPattern(pattern);

                // DO not add numbers if already exist in the list
                for(int i=0; i< blockNumbers.size();i++)
                {
                    if(blockNumbers.get(i).getPhoneNumber().equals(blockNumber.getPhoneNumber()))
                    {
                        if(blockNumbers.get(i).getPattern().equals(blockNumber.getPattern())) {
                            Snackbar.make(view, "Number already exist !", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            return;
                        }
                    }
                }

                blockNumbers.add(blockNumber);
                adapter.notifyDataSetChanged();
                input.setText("");

                SharedPreferences.Editor editor = settings.edit();

                Gson gson = new Gson();
                String jsonBlockNumbers = gson.toJson(blockNumbers);
                editor.putString("jsonBlockNumbers", jsonBlockNumbers);

                // Commit the edits!
                editor.commit();

                Snackbar.make(view, blocked_number + " added to block list !", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        EditText input = (EditText) findViewById(R.id.TextInput);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);

        settings = getSharedPreferences(SETTING_PREFS_NAME, 0);
        boolean enable_call_block = settings.getBoolean("enableCallBlock", false);
        boolean delete_call_Log = settings.getBoolean("deleteCallLog", false);

        blocked_call_count = settings.getInt("blockedCallCount", 0);
        Snackbar.make(fab, blocked_call_count + " calls blocked until today !", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        Switch my_switch_block = (Switch)findViewById(R.id.switchBlock);
        my_switch_block.setChecked(enable_call_block);
        my_switch_block.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("enableCallBlock", isChecked);
                editor.commit();
            }
        });

        Switch my_switch_log = (Switch)findViewById(R.id.switchDeleteLogs);
        my_switch_log.setChecked(delete_call_Log);
        my_switch_log.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("deleteCallLog", isChecked);;
                editor.commit();
            }
        });

        String jsonBlockNumbers = settings.getString("jsonBlockNumbers", "");
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<BlockNumber>>(){}.getType();

        if(!jsonBlockNumbers.equals("")) {
            blockNumbers = gson.fromJson(jsonBlockNumbers, type);
        } else {
            blockNumbers = new ArrayList<BlockNumber>();
        }

        final ListView listView = (ListView)findViewById(R.id.list_view);
        adapter = new NumberAdapter(this, blockNumbers);
        listView.setAdapter(adapter);
        listView.setEmptyView(findViewById(R.id.emptyElement));

        /*
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final BlockNumber item = (BlockNumber) parent.getItemAtPosition(position);
                view.animate().setDuration(2000).alpha(0)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                blockNumbers.remove(item);
                                adapter.notifyDataSetChanged();
                                view.setAlpha(1);
                            }
                        });
            }
        });
        */

        final RadioGroup group1 = (RadioGroup) findViewById(R.id.orientation);
        group1.check(R.id.startswith);
        group1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.startswith:
                        pattern = "Starts with";
                        break;
                    case R.id.endswith:
                        pattern = "Ends with";
                        break;
                    case R.id.contains:
                        pattern = "Contains";
                        break;
                }
            }
        });

        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_ACCESS_CALL_PHONE);
        }
        if(checkSelfPermission(Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CALL_LOG}, PERMISSION_WRITE_CALL_LOG);
        }
        if(checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG}, PERMISSION_READ_CALL_LOG);
        }

    }

    public void saveBlockList()
    {
        SharedPreferences.Editor editor = settings.edit();

        Gson gson = new Gson();
        String jsonBlockNumbers = gson.toJson(blockNumbers);
        editor.putString("jsonBlockNumbers", jsonBlockNumbers);

        // Commit the edits!
        editor.commit();
    }

    private void saveAllSettings()
    {
        Switch my_switch_block = (Switch)findViewById(R.id.switchBlock);
        Switch my_switch_log = (Switch)findViewById(R.id.switchDeleteLogs);

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("enableCallBlock", my_switch_block.isChecked());
        editor.putBoolean("deleteCallLog", my_switch_log.isChecked());

        Gson gson = new Gson();
        String jsonBlockNumbers = gson.toJson(blockNumbers);
        editor.putString("jsonBlockNumbers", jsonBlockNumbers);

        // Commit the edits!
        editor.commit();
    }

    @Override
    protected void onStop(){
        super.onStop();

        // No need to save now, as settings as saved as they change.
        //saveAllSettings();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_CALL_PHONE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Thanks for accepting, All Good", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Need call phone permission", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case PERMISSION_WRITE_CALL_LOG: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Aquired!", Toast.LENGTH_SHORT);
                } else {
                    Toast.makeText(this, "Without CALL_LOG_WRITE permission, call log cannot be deleted", Toast.LENGTH_SHORT);
                }
                break;
            }
            case PERMISSION_READ_CALL_LOG: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Aquired!", Toast.LENGTH_SHORT);
                } else {
                    Toast.makeText(this, "Without CALL_LOG_WRITE permission, call log cannot be deleted", Toast.LENGTH_SHORT);
                }
                break;
            }
        }

        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, "Developed by Prasad Shetty", Toast.LENGTH_LONG).show();
            return true;
        }

        if(id == R.id.show_counter) {
            Intent intent = new Intent(this, RejectLogsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
