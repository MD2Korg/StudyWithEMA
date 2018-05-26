package org.md2k.studywithema;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

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
import org.md2k.mcerebrum.commons.storage.Storage;
import org.md2k.mcerebrum.core.access.appinfo.AppBasicInfo;
import org.md2k.mcerebrum.core.access.appinfo.AppInfo;
import org.md2k.mcerebrum.core.access.serverinfo.ServerCP;
import org.md2k.mcerebrum.core.access.studyinfo.StudyCP;
import org.md2k.mcerebrum.system.update.Update;
import org.md2k.studywithema.configuration.ConfigManager;
import org.md2k.studywithema.menu.MyMenu;
import org.md2k.studywithema.menu.ResponseCallBack;

import java.io.File;

import es.dmoral.toasty.Toasty;
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
        handler = new Handler();
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
        if (MyMenu.hasMenuItem(cConfig.ui.menu, MyMenu.MENU_UPDATE)) {
            int badgeValue = Update.hasUpdate(AbstractActivityMenu.this);
            if (badgeValue > 0) {
                StringHolder a = new StringHolder(String.valueOf(badgeValue));
                result.updateBadge(MyMenu.MENU_UPDATE, a);
            } else {
                StringHolder a = new StringHolder("");
                result.updateBadge(MyMenu.MENU_UPDATE, a);
            }
        }
        if (MyMenu.hasMenuItem(cConfig.ui.menu, MyMenu.MENU_START_STOP_DATA_COLLECTION)) {
            PrimaryDrawerItem pd = (PrimaryDrawerItem) result.getDrawerItem(MyMenu.MENU_START_STOP_DATA_COLLECTION);
            if (!isServiceRunning()) {
                pd = pd.withName("Start Data Collection").withIcon(FontAwesome.Icon.faw_play_circle_o);
            } else {
                pd = pd.withName("Stop Data Collection").withIcon(FontAwesome.Icon.faw_pause_circle_o);
            }
            int pos = result.getPosition(MyMenu.MENU_START_STOP_DATA_COLLECTION);
            result.removeItem(MyMenu.MENU_START_STOP_DATA_COLLECTION);
            result.addItemAtPosition(pd, pos);
        }
    }
    private boolean isServiceRunning(){
        return AppInfo.isServiceRunning(this, ServiceStudy.class.getName());
    }

    void createDrawer() {
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.mcerebrum_white);
        try {
            if (cConfig.ui.icon != null) {
                drawable = Drawable.createFromPath(ConfigManager.getConfigDirectory() + cConfig.ui.icon);
            }
        }catch (Exception e){}
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.cover_image4)
                .withCompactStyle(true)
                .addProfiles(new MyMenu().getHeaderContent(ServerCP.getUserName(getBaseContext()), drawable, responseCallBack))
//                .addProfiles(new MyMenu().getHeaderContent(ServerCP.getUserName(getBaseContext()), responseCallBack))
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
//            if (drawerItem != null)
//                toolbar.setTitle(getStudyName() + ": " + ((Nameable) drawerItem).getName().getText(AbstractActivityMenu.this));
            toolbar.setTitle(getStudyName());
            switch (responseId) {
                case MyMenu.MENU_HOME:
                    result.setSelection(MyMenu.MENU_HOME, false);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentHome()).commitAllowingStateLoss();
                    break;
                case MyMenu.MENU_START_STOP_DATA_COLLECTION:
                    if (isServiceRunning()) {
                        stopDataCollection();
                    } else {
                        startDataCollection();
                    }
                    result.setSelection(MyMenu.MENU_HOME, false);
                    toolbar.setTitle(getStudyName());
                    break;
                case MyMenu.MENU_RESET:
                    if (isServiceRunning()) {
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
                    } catch (Exception e) {
                    }
                    Intent intent = new Intent();
                    String p = AppBasicInfo.getMCerebrum(AbstractActivityMenu.this);

                    intent.putExtra("STUDY", getPackageName());
                    intent.setComponent(new ComponentName(p, p + ".UI.check_update.ActivityCheckUpdate"));
                    startActivity(intent);
                    finish();
                    break;
                case MyMenu.MENU_SETTINGS:

                    String password = getPassword();
                    if (password == null || password.length() == 0)
                        settings();
                    else settingsWithPassword(password);
                    break;
                case MyMenu.MENU_HELP:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentHelp()).commitAllowingStateLoss();
                    break;
                case MyMenu.MENU_CONTACT_US:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentContactUs()).commitAllowingStateLoss();
                    break;
                case MyMenu.MENU_TUTORIAL:
                    openPDF();

                default:
            }
        }
    };
    private void openPDF() {
        Intent intent = new Intent();
        intent.setPackage("com.adobe.reader");
        File file = new File(ConfigManager.getConfigDirectory() + "tutorial.pdf");
        intent.setDataAndType(Uri.fromFile(file), "application/pdf");
        startActivity(intent);
    }

    public void startDataCollection() {
        if (!isServiceRunning()) {
            Intent intent = new Intent(AbstractActivityMenu.this, ServiceStudy.class);
            startService(intent);
        }
        StudyCP.setStarted(AbstractActivityMenu.this, true);
        try {
            updateMenu();
            updateStatus();
        }catch (Exception e){}
    }

    public void stopDataCollection() {
        Dialog.simple(this, "Stop Data Collection", "Do you want to stop data collection?", "Yes", "Cancel", new DialogCallback() {
            @Override
            public void onSelected(String value) {
                if (value.equals("Yes")) {
                    Intent intent = new Intent(AbstractActivityMenu.this, ServiceStudy.class);
                    stopService(intent);
                    StudyCP.setStarted(AbstractActivityMenu.this, false);
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
                    StudyCP.setStarted(AbstractActivityMenu.this, false);
                    connectDataKit();
                    handler.postDelayed(runnable, 3000);
                    updateMenu();
                }
            }
        }).show();
    }

    public void settings() {
        if (isServiceRunning()) {
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
        } else {
            StudyCP.setStarted(AbstractActivityMenu.this, false);
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("org.md2k.mcerebrum");
            startActivity(launchIntent);
            finish();
        }
    }

    public void settingsWithPassword(final String password) {
        Dialog.editbox(this, "Settings", "Enter Password", new DialogCallback() {
            @Override
            public void onSelected(String value) {
                if (password.equals(value)) {
                    Intent intent = new Intent(AbstractActivityMenu.this, ServiceStudy.class);
                    stopService(intent);
                    StudyCP.setStarted(AbstractActivityMenu.this, false);
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage("org.md2k.mcerebrum");
                    startActivity(launchIntent);
                    finish();

                } else {
                    Toasty.error(AbstractActivityMenu.this, "Error: Incorrect password", Toast.LENGTH_SHORT).show();
                    updateMenu();
                }
            }
        }).show();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            startDataCollection();
        }
    };

    @Override
    public void onResume() {
        if (hasPermission) updateStatus();
        super.onResume();
    }

    void updateStatus() {
        if (!isServiceRunning()) {
            updateStatus("Data collection off", DefaultBootstrapBrand.DANGER, false);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(ServiceStudy.NOTIFY_ID, ServiceStudy.getCompatNotification(this, "Data Collection - OFF (click to start)"));
        } else {
            updateStatus(null, DefaultBootstrapBrand.SUCCESS, true);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(ServiceStudy.NOTIFY_ID, ServiceStudy.getCompatNotification(this, "Data Collection - ON"));
        }

    }

    void updateStatus(String msg, BootstrapBrand brand, boolean isSuccess) {

        tv.setBootstrapBrand(brand);
        if (isSuccess) {
            int uNo = Update.hasUpdate(this);
            if (uNo == 0)
                tv.setBootstrapText(new BootstrapText.Builder(this).addText("Status: ").addFontAwesomeIcon("fa_check_circle").build());
            else {
                tv.setBootstrapBrand(DefaultBootstrapBrand.WARNING);
                tv.setBootstrapText(new BootstrapText.Builder(this).addText("Status: ").addFontAwesomeIcon("fa_check_circle").addText(" (Update Available)").build());
            }
        } else
            tv.setBootstrapText(new BootstrapText.Builder(this).addText("Status: ").addFontAwesomeIcon("fa_times_circle").addText(" (" + msg + ")").build());
    }

    String getPassword() {
        if (cConfig.ui == null) return null;
        if (cConfig.ui.menu == null) return null;
        for (int i = 0; i < cConfig.ui.menu.length; i++)
            if (cConfig.ui.menu[i].id.equalsIgnoreCase("settings")) {
                if (cConfig.ui.menu[i].parameter == null || cConfig.ui.menu[i].parameter.length != 2)
                    return null;
                return cConfig.ui.menu[i].parameter[1];
            }
        return null;
    }

}

