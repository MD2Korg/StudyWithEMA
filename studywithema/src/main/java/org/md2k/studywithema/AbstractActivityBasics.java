package org.md2k.studywithema;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import org.md2k.mcerebrum.system.update.Update;
import org.md2k.studywithema.configuration.CConfig;
import org.md2k.studywithema.configuration.ConfigManager;
import org.md2k.studywithema.menu.MyMenu;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public abstract class AbstractActivityBasics extends AppCompatActivity {
    static final String TAG = AbstractActivityBasics.class.getSimpleName();
    Toolbar toolbar;
    public CConfig cConfig;
    public DataQualityManager dataQualityManager;
    public ControllerDay controllerDay;
    Subscription subscriptionCheckUpdate;
    public boolean isServiceRunning;
    boolean hasPermission = false;

    abstract void createMenu();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utils.init(this);
        loadToolbar();
        isServiceRunning=false;
        getPermission();
    }
    void loadDay(){
        long so, wo;
        String sleepOffset = cConfig.ui.home_screen.day.sleep_offset;
        String wakeupOffset = cConfig.ui.home_screen.day.wakeup_offset;
        if(sleepOffset==null) so=0;else so= DateTime.getTimeInMillis(sleepOffset);
        if(wakeupOffset==null) wo=0;else wo=DateTime.getTimeInMillis(wakeupOffset);
        controllerDay = new ControllerDay(new ViewDay(this), new ModelDay(this,wo, so));

    }

    void getPermission() {
        SharedPreferences sharedpreferences = getSharedPreferences("permission", Context.MODE_PRIVATE);
        if (sharedpreferences.getBoolean("permission", false) == true) {
            hasPermission=true;
            loadConfig();
            loadDay();
            //checkUpdate();
            connectDataKit();
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
                        hasPermission=true;
                        loadConfig();
//                        checkUpdate();
                        connectDataKit();
                    }
                }
            });
        }

    }
    Runnable r = new Runnable() {
        @Override
        public void run() {
            try {
                Log.d("abc","connect()...100");
                DataKitAPI.getInstance(AbstractActivityBasics.this).connect(new OnConnectionListener() {
                    @Override
                    public void onConnected() {
                        Log.d("abc","AbstractActivityBasics -> DataKit connected");
                        dataQualityStart();
                        controllerDay.start();
                        createMenu();
                    }
                });
            } catch (DataKitException e) {
                Toasty.error(getApplicationContext(), "StudyWithEMA ... Failed to connect datakit..", Toast.LENGTH_SHORT).show();
                finish();
            }

        }
    };
    void connectDataKit() {
       new Handler().postDelayed(r, 2000);
    }

    void dataQualityStart() {
        ArrayList<DataSource> dataSources=new ArrayList<>();
        CDataQuality[] cDataQualities=cConfig.ui.home_screen.data_quality;
        if (cDataQualities == null || cDataQualities.length==0) return;
        for(int i=0;i<cDataQualities.length;i++){
            dataSources.add(cDataQualities[i].read);
        }
        dataQualityManager = new DataQualityManager();
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

    void stop() {
        controllerDay.stop();
        if (dataQualityManager != null)
            dataQualityManager.clear();
        try {
            Log.d("abc","disconnect()...100");
            DataKitAPI.getInstance(this).disconnect();
        } catch (Exception e) {

        }
    }

    @Override
    public void onDestroy() {
        stop();
        if (subscriptionCheckUpdate != null && !subscriptionCheckUpdate.isUnsubscribed())
            subscriptionCheckUpdate.unsubscribe();

        super.onDestroy();
    }

    public String getStudyName() {
        return StudyCP.getTitle(this);
    }
    public void checkUpdate(){
        if(MyMenu.hasMenuItem(cConfig.ui.menu, MyMenu.MENU_UPDATE)) {
            subscriptionCheckUpdate = Observable.just(true).subscribeOn(Schedulers.newThread())
                    .observeOn(Schedulers.newThread())
                    .flatMap(new Func1<Boolean, Observable<Boolean>>() {
                        @Override
                        public Observable<Boolean> call(Boolean aBoolean) {
                            return Update.checkUpdate(AbstractActivityBasics.this);
                        }
                    }).subscribe(new Observer<Boolean>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("abc", "abeeee");
                        }

                        @Override
                        public void onNext(Boolean aBoolean) {
                        }
                    });
        }

    }

}

