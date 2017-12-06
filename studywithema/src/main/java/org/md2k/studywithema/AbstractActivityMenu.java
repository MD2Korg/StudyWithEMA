package org.md2k.studywithema;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;

import com.beardedhen.androidbootstrap.AwesomeTextView;
import com.beardedhen.androidbootstrap.BootstrapText;
import com.beardedhen.androidbootstrap.api.attributes.BootstrapBrand;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import org.md2k.mcerebrum.commons.dialog.Dialog;
import org.md2k.mcerebrum.commons.dialog.DialogCallback;
import org.md2k.mcerebrum.core.access.appinfo.AppBasicInfo;
import org.md2k.mcerebrum.core.access.appinfo.AppInfo;
import org.md2k.mcerebrum.core.access.serverinfo.ServerCP;
import org.md2k.mcerebrum.core.access.studyinfo.StudyCP;
import org.md2k.mcerebrum.system.update.Update;
import org.md2k.studywithema.menu.MyMenu;
import org.md2k.studywithema.menu.ResponseCallBack;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public abstract class AbstractActivityMenu extends AbstractActivityBasics {
    private Drawer result = null;
    Handler handler;
    int selectedMenu = -1;
    AwesomeTextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tv = (AwesomeTextView) findViewById(R.id.textview_status);
        handler=new Handler();
    }

    @Override
    public void createMenu() {
        createDrawer();
        result.resetDrawerContent();
        result.getHeader().refreshDrawableState();
        result.setSelection(MyMenu.MENU_HOME);
    }

    public void updateMenu() {
        if (result == null) {
            createMenu();
            return;
        }
        if(MyMenu.hasMenuItem(cConfig.ui.menu, MyMenu.MENU_UPDATE)) {
            int badgeValue = Update.hasUpdate(AbstractActivityMenu.this);
            if (badgeValue > 0) {
                StringHolder a = new StringHolder(String.valueOf(badgeValue));
                result.updateBadge(MyMenu.MENU_UPDATE, a);
            } else {
                StringHolder a = new StringHolder("");
                result.updateBadge(MyMenu.MENU_UPDATE, a);
            }
        }
        if(MyMenu.hasMenuItem(cConfig.ui.menu, MyMenu.MENU_START_STOP_DATA_COLLECTION)) {
            boolean start = AppInfo.isServiceRunning(this, ServiceStudy.class.getName());
            PrimaryDrawerItem pd = (PrimaryDrawerItem) result.getDrawerItem(MyMenu.MENU_START_STOP_DATA_COLLECTION);
            if (start == false) {
                pd = pd.withName("Start Data Collection").withIcon(FontAwesome.Icon.faw_play_circle_o);
            } else {
                pd = pd.withName("Stop Data Collection").withIcon(FontAwesome.Icon.faw_pause_circle_o);
            }
            int pos = result.getPosition(MyMenu.MENU_START_STOP_DATA_COLLECTION);
            result.removeItem(MyMenu.MENU_START_STOP_DATA_COLLECTION);
            result.addItemAtPosition(pd, pos);
        }
    }

    void createDrawer() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.cover_image4)
                .withCompactStyle(true)
                .addProfiles(new MyMenu().getHeaderContent(ServerCP.getUserName(getBaseContext()), responseCallBack))
                .build();
        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                .addDrawerItems(MyMenu.getMenuContent(cConfig.ui.menu, responseCallBack))
                .build();
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            if (selectedMenu != MyMenu.MENU_HOME) {
                responseCallBack.onResponse(null, MyMenu.MENU_HOME);
            } else {
                super.onBackPressed();
            }
        }
    }

    public ResponseCallBack responseCallBack = new ResponseCallBack() {
        @Override
        public void onResponse(final IDrawerItem drawerItem, final int responseId) {
//            if(selectedMenu==responseId) return;
            selectedMenu = responseId;
            if (drawerItem != null)
                toolbar.setTitle(getStudyName() + ": " + ((Nameable) drawerItem).getName().getText(AbstractActivityMenu.this));
            else toolbar.setTitle(getStudyName());
            switch (responseId) {
                case MyMenu.MENU_HOME:
                    result.setSelection(MyMenu.MENU_HOME, false);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentHome()).commitAllowingStateLoss();
                    break;
                case MyMenu.MENU_START_STOP_DATA_COLLECTION:
                    if (AppInfo.isServiceRunning(AbstractActivityMenu.this, ServiceStudy.class.getName())) {
                        stopDataCollection();
                    } else {
                        startDataCollection();
                    }
                    result.setSelection(MyMenu.MENU_HOME, false);
                    toolbar.setTitle(getStudyName());
                    break;
                case MyMenu.MENU_RESET:
                    if (AppInfo.isServiceRunning(AbstractActivityMenu.this, ServiceStudy.class.getName())) {
                        resetDataCollection();
                    } else {
                        startDataCollection();
                    }
                    result.setSelection(MyMenu.MENU_HOME, false);
                    toolbar.setTitle(getStudyName());
                    break;
                case MyMenu.MENU_UPDATE:
                    try {
                        Intent ii = new Intent(AbstractActivityMenu.this, ServiceStudy.class);
                        stopService(ii);
                        StudyCP.setStarted(AbstractActivityMenu.this, false);
                    }catch (Exception e){}
                    Intent intent = new Intent();
                    String p = AppBasicInfo.getMCerebrum(AbstractActivityMenu.this);

                    intent.putExtra("STUDY", getPackageName());
                    intent.setComponent(new ComponentName(p, p + ".UI.check_update.ActivityCheckUpdate"));
                    startActivity(intent);
                    finish();
                    break;
                case MyMenu.MENU_SETTINGS:
                    settings();
                    break;
                case MyMenu.MENU_HELP:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentHelp()).commitAllowingStateLoss();
                    break;
                case MyMenu.MENU_CONTACT_US:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentContactUs()).commitAllowingStateLoss();
                    break;

                default:
            }
        }
    };
    public void startDataCollection() {
        Observable.just(true).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).map(new Func1<Boolean, Boolean>() {
            @Override
            public Boolean call(Boolean aBoolean) {
                if (!AppInfo.isServiceRunning(AbstractActivityMenu.this, ServiceStudy.class.getName())) {
                    Intent intent = new Intent(AbstractActivityMenu.this, ServiceStudy.class);
                    startService(intent);
                }
                StudyCP.setStarted(AbstractActivityMenu.this, true);
                isServiceRunning=true;
                updateMenu();
                updateStatus();
                return true;
            }
        }).subscribe();
    }

    public void stopDataCollection() {
        Dialog.simple(this, "Stop Data Collection", "Do you want to stop data collection?", "Yes", "Cancel", new DialogCallback() {
            @Override
            public void onSelected(String value) {
                if (value.equals("Yes")) {
                    Intent intent = new Intent(AbstractActivityMenu.this, ServiceStudy.class);
                    stopService(intent);
                    StudyCP.setStarted(AbstractActivityMenu.this, false);
                    isServiceRunning=false;
                    updateMenu();
                    updateStatus();
                }
            }
        }).show();
    }

    public void resetDataCollection() {
        Dialog.simple(this, "Reset Application", "Do you want to reset application?", "Yes", "Cancel", new DialogCallback() {
            @Override
            public void onSelected(String value) {
                if (value.equals("Yes")) {
                    Intent intent = new Intent(AbstractActivityMenu.this, ServiceStudy.class);
                    stopService(intent);
                    handler.postDelayed(runnable, 3000);
                    updateMenu();
                }
            }
        }).show();
    }

    public void settings() {
        boolean start = AppInfo.isServiceRunning(this, ServiceStudy.class.getName());
        if (start)
            Dialog.simple(this, "Settings", "Do you want to stop data collection and open settings?", "Yes", "Cancel", new DialogCallback() {
                @Override
                public void onSelected(String value) {
                    if (value.equals("Yes")) {
                        Intent intent = new Intent(AbstractActivityMenu.this, ServiceStudy.class);
                        stopService(intent);
                        StudyCP.setStarted(AbstractActivityMenu.this, false);
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("org.md2k.mcerebrum");
                        startActivity(launchIntent);
                        finish();
                    } else {
                        updateMenu();
//                        responseCallBack.onResponse(null, MyMenu.MENU_HOME);
//                        setTitle(getStudyName());
                    }
                }
            }).show();
        else {
            StudyCP.setStarted(AbstractActivityMenu.this, false);
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("org.md2k.mcerebrum");
            startActivity(launchIntent);
            finish();
        }
    }
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            startDataCollection();
        }
    };
    @Override
    public void onResume(){
        if(hasPermission) updateStatus();
        super.onResume();
    }
    void updateStatus(){
        if(!isServiceRunning) {
            updateStatus("Data collection off", DefaultBootstrapBrand.DANGER, false);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(ServiceStudy.NOTIFY_ID, ServiceStudy.getCompatNotification(this,"Data Collection - OFF (click to start)"));
        }else{
            updateStatus(null, DefaultBootstrapBrand.SUCCESS, true);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(ServiceStudy.NOTIFY_ID, ServiceStudy.getCompatNotification(this,"Data Collection - ON"));
        }

    }

    void updateStatus(String msg, BootstrapBrand brand, boolean isSuccess){

        tv.setBootstrapBrand(brand);
        if(isSuccess) {
            int uNo= Update.hasUpdate(this);
            if(uNo==0)
                tv.setBootstrapText(new BootstrapText.Builder(this).addText("Status: ").addFontAwesomeIcon("fa_check_circle").build());
            else {
                tv.setBootstrapBrand(DefaultBootstrapBrand.WARNING);
                tv.setBootstrapText(new BootstrapText.Builder(this).addText("Status: ").addFontAwesomeIcon("fa_check_circle").addText(" (Update Available)").build());
            }
        }
        else
            tv.setBootstrapText(new BootstrapText.Builder(this).addText("Status: ").addFontAwesomeIcon("fa_times_circle").addText(" ("+msg+")").build());
    }

}

