package org.md2k.studywithema;
/*
 * Copyright (c) 2016, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import android.content.Context;
import android.content.Intent;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.mcerebrum.commons.permission.ActivityPermission;
import org.md2k.mcerebrum.commons.permission.Permission;
import org.md2k.mcerebrum.core.access.MCerebrum;
import org.md2k.mcerebrum.core.access.MCerebrumInfo;
import org.md2k.studywithema.configuration.CConfig;
import org.md2k.studywithema.configuration.CList;
import org.md2k.studywithema.configuration.ConfigManager;

import java.util.ArrayList;

public class MyMCerebrumInit extends MCerebrumInfo {
    DataKitAPI dataKitAPI;

    @Override
    public void update(final Context context) {
        MCerebrum.setBackgroundService(context, ServiceStudy.class);
        if (!Permission.hasPermission(context)) {
            Intent intent = new Intent(context, ActivityPermission.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        MCerebrum.setConfigureActivity(context, ActivitySettings.class);
        checkSettings(context);
    }

    private void setResult(Context context, boolean isPartiallyConfigured, boolean isConfigured) {
        MCerebrum.setConfigured(context, isPartiallyConfigured);
        MCerebrum.setConfigureExact(context, isConfigured);
    }

    private void checkSettings(final Context context) {
        CConfig cConfig = ConfigManager.read();
        if (cConfig == null || cConfig.settings == null || cConfig.settings.list == null || cConfig.settings.list.length == 0) {
            setResult(context, true, true);
            return;
        }
        final CList[] cList = cConfig.settings.list;


        dataKitAPI = DataKitAPI.getInstance(context);
        try {
            dataKitAPI.connect(new OnConnectionListener() {
                @Override
                public void onConnected() {
                    boolean pc=false, c=true;
                    for (CList aCList : cList) {
                        if (isConfigured(aCList.save)) {
                            pc = true;
                        } else {
                            if (aCList.use_as.equalsIgnoreCase("REQUIRED"))
                                c = false;
                        }
                    }
                    setResult(context, pc, c);
                    dataKitAPI.disconnect();
                }
            });
        } catch (DataKitException e) {
            setResult(context, true, true);
        }
    }

    private boolean isConfigured(DataSource d) {
        try {
            ArrayList<DataSourceClient> dsc = dataKitAPI.find(new DataSourceBuilder(d));
            if (dsc.size() == 0) return false;
            return true;
        } catch (Exception e) {
            return true;
        }
    }
}
