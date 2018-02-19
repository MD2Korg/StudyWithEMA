package org.md2k.studywithema;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import rx.Subscription;


public class ActivityMain extends AbstractActivityMenu {
    Subscription subscriptionCheckUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startDataCollection();
        if (getIntent().getBooleanExtra("background", false))
            finish();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent intent;
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                break;

            case R.id.action_refresh:

         resetDataCollection();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public void onDestroy() {
        super.onDestroy();
    }

}
