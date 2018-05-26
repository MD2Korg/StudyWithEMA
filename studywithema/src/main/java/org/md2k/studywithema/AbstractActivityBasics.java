package org.md2k.studywithema;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.blankj.utilcode.util.Utils;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.mcerebrum.commons.permission.Permission;
import org.md2k.mcerebrum.commons.permission.PermissionCallback;
import org.md2k.mcerebrum.commons.ui.data_quality.CDataQuality;
import org.md2k.mcerebrum.commons.ui.data_quality.DataQualityManager;
import org.md2k.mcerebrum.commons.ui.day.ControllerDay;
import org.md2k.mcerebrum.commons.ui.day.ModelDay;
import org.md2k.mcerebrum.commons.ui.day.ViewDay;
import org.md2k.mcerebrum.core.access.studyinfo.StudyCP;
import org.md2k.studywithema.configuration.CConfig;
import org.md2k.studywithema.configuration.ConfigManager;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public abstract class AbstractActivityBasics extends AppCompatActivity {
    static final String TAG = AbstractActivityBasics.class.getSimpleName();
    Toolbar toolbar;
    public CConfig cConfig;
    public DataQualityManager dataQualityManager;
    public ControllerDay controllerDay;
    boolean hasPermission = false;
    Handler handlerDataKit;

    abstract void createMenu();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataQualityManager = new DataQualityManager();
        handlerDataKit = new Handler();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(DataKitException.class.getSimpleName()));
        setContentView(R.layout.activity_main);
        Utils.init(this);
        loadToolbar();
        getPermission();
    }
    public abstract void startDataCollection();

    void loadDay() {
        long so, wo;
        controllerDay = null;
        if (cConfig == null || cConfig.ui == null || cConfig.ui.home_screen == null || cConfig.ui.home_screen.day == null)
            return;

        String sleepOffset = cConfig.ui.home_screen.day.sleep_offset;
        String wakeupOffset = cConfig.ui.home_screen.day.wakeup_offset;
        if (sleepOffset == null) so = 0;
        else so = DateTime.getTimeInMillis(sleepOffset);
        if (wakeupOffset == null) wo = 0;
        else wo = DateTime.getTimeInMillis(wakeupOffset);
        controllerDay = new ControllerDay(this, new ViewDay(this), new ModelDay(this, wo, so));

    }

    void getPermission() {
        SharedPreferences sharedpreferences = getSharedPreferences("permission", Context.MODE_PRIVATE);
        if (sharedpreferences.getBoolean("permission", false) == true) {
            hasPermission = true;
            loadConfig();
            loadDay();
            //checkUpdate();
            connectDataKit();
            startDataCollection();

        } else {
            Permission.requestPermission(this, new PermissionCallback() {
                @Override
                public void OnResponse(boolean isGranted) {
                    SharedPreferences sharedpreferences = getSharedPreferences("permission", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean("permission", isGranted);
                    editor.apply();
                    if (!isGranted) {
                        Toasty.error(getApplicationContext(), "StudyWithEMA ... !PERMISSION DENIED !!! Could not continue...", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        hasPermission = true;
                        loadConfig();
                        loadDay();
//                        checkUpdate();
                        connectDataKit();
                        startDataCollection();
                    }
                }
            });
        }

    }

    Runnable runnableDataKit = new Runnable() {
        @Override
        public void run() {
            try {
                Log.d("abc", "connect()...100");
                if (controllerDay != null)
                    controllerDay.stop();
                dataQualityStop();
                if (DataKitAPI.getInstance(AbstractActivityBasics.this).isConnected()) {
                    dataQualityStart();
                    if (controllerDay != null)
                        controllerDay.start();
                    return;
                }
                DataKitAPI.getInstance(AbstractActivityBasics.this).connect(new OnConnectionListener() {
                    @Override
                    public void onConnected() {
                        Log.d("abc", "AbstractActivityBasics -> DataKit connected");
                        dataQualityStart();
                        if (controllerDay != null)
                            controllerDay.start();
                        createMenu();
                    }
                });
//                handlerDataKit.postDelayed(this, 5000);
            } catch (DataKitException e) {
                Toasty.error(getApplicationContext(), "StudyWithEMA ... Failed to connect datakit..", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    };

    public void connectDataKit() {
        handlerDataKit.removeCallbacks(runnableDataKit);
        handlerDataKit.postDelayed(runnableDataKit,3000);
    }
    void dataQualityStop(){
        if (dataQualityManager != null)
            dataQualityManager.clear();
    }

    void dataQualityStart() {
        ArrayList<DataSource> dataSources = new ArrayList<>();
        CDataQuality[] cDataQualities = cConfig.ui.home_screen.data_quality;
        if (cDataQualities == null || cDataQualities.length == 0){
            Log.d("abc","dataQuaityStart()...length=0");
            return;
        }
        for (int i = 0; i < cDataQualities.length; i++) {
            dataSources.add(cDataQualities[i].read);
        }
        dataQualityManager.set(AbstractActivityBasics.this, dataSources);
    }

    void loadConfig() {
        cConfig = ConfigManager.read();
    }

    void loadToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getStudyName());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Handle Code
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handle the click on the back arrow click
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void disconnectDataKit() {
        handlerDataKit.removeCallbacks(runnableDataKit);
        if (controllerDay != null) {
            controllerDay.stop();
        }
        dataQualityStop();
        try {
            Log.d("abc", "disconnect()...100");
            DataKitAPI.getInstance(this).disconnect();
        } catch (Exception e) {

        }
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        disconnectDataKit();
        super.onDestroy();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            disconnectDataKit();
            connectDataKit();
            String message = intent.getStringExtra("message");
            Log.d("receiver", "Got message: " + message);
        }
    };

    public String getStudyName() {
        return StudyCP.getTitle(this);
    }

}

