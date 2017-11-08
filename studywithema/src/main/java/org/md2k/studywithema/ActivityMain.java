package org.md2k.studywithema;

import android.os.Bundle;

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

    public void onDestroy() {
        super.onDestroy();
    }

}
