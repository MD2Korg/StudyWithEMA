package org.md2k.studywithema;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeInt;
import org.md2k.datakitapi.datatype.DataTypeLong;
import org.md2k.datakitapi.datatype.DataTypeString;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.mcerebrum.commons.dialog.Dialog;
import org.md2k.mcerebrum.commons.dialog.DialogCallback;
import org.md2k.studywithema.configuration.CConfig;
import org.md2k.studywithema.configuration.CList;
import org.md2k.studywithema.configuration.ConfigManager;

import java.util.ArrayList;
import java.util.Calendar;

import es.dmoral.toasty.Toasty;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p/>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p/>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p/>
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
public class PrefsFragmentSettings extends PreferenceFragment {
    public static final String TIME = "TIME";
    public static final String DATE = "DATE";
    public static final String DATE_TIME = "DATE_TIME";
    public static final String TEXT = "TEXT";
    public static final String NUMBER = "NUMBER";
    public static final String SINGLE_SELECT = "SINGLE_SELECT";
    public static final String MULTI_SELECT = "MULTI_SELECT";

    CList[] cLists;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
        CConfig cConfig = ConfigManager.read();
        if (cConfig == null || cConfig.settings == null || cConfig.settings.list == null || cConfig.settings.list.length == 0) {
            Toasty.error(getActivity(), getActivity().getPackageName() + ": Configuration file not found", Toast.LENGTH_SHORT).show();
            getActivity().finish();
            return;
        }
        cLists = cConfig.settings.list;

        try {
            Log.d("abc","connect()...10");
            DataKitAPI.getInstance(getActivity()).connect(new OnConnectionListener() {
                @Override
                public void onConnected() {
                    try {
                        setSettings();
                    } catch (DataKitException e) {
                        Toasty.error(getActivity(), getActivity().getPackageName() + ": DataKit connection error", Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }
                }
            });
        } catch (DataKitException e) {
            Toasty.error(getActivity(), getActivity().getPackageName() + ": DataKit connection error", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }


    void setSettings() throws DataKitException {
        PreferenceCategory category = (PreferenceCategory) findPreference("key_settings");
        category.removeAll();
        for (CList cList : cLists) {
            Preference preference = createPreference(cList);
            category.addPreference(preference);
        }
    }

    private DataType getData(DataSource dataSource) {
        try {
            DataSourceClient dataSourceClient = getDataSourceClient(dataSource);
            Log.d("abc","connected="+DataKitAPI.getInstance(getActivity()).isConnected());
            ArrayList<DataType> dataTypes = DataKitAPI.getInstance(getActivity()).query(dataSourceClient, 1);
            Log.d("abc","datasource="+dataSource.getType()+" size="+dataTypes.size());
            if (dataTypes.size() == 0) return null;
            return dataTypes.get(0);
        } catch (Exception e) {
            Log.d("abc", "error=" + e.getMessage());
            return null;
        }
    }

    String formatData(String type, DataType dataType) {
        if (dataType == null) return null;
        switch (type.toUpperCase()) {
            case TIME:
                Calendar c = Calendar.getInstance();
                long time = ((DataTypeLong) dataType).getSample() / 1000;
                c.set(Calendar.SECOND, (int) (time % 60));
                time /= 60;
                c.set(Calendar.MINUTE, (int) (time % 60));
                time /= 60;
                c.set(Calendar.HOUR_OF_DAY, (int) (time % 24));
                return DateTime.convertTimeStampToDateTime(c.getTimeInMillis(), "h:mm aaa");
            case DATE:
                return DateTime.convertTimeStampToDateTime(((DataTypeLong) dataType).getSample(), "dd-MMM-yyyy,  (EEEE)");
            case DATE_TIME:
                return DateTime.convertTimeStampToDateTime(((DataTypeLong) dataType).getSample(), "dd-MMM-yyyy,  h:mm aaa,  (EEEE)");
            case NUMBER:
                return String.valueOf(((DataTypeInt) dataType).getSample());
            case TEXT:
                return ((DataTypeString) dataType).getSample();
            case SINGLE_SELECT:
                return ((DataTypeString) dataType).getSample();
            case MULTI_SELECT:
                break;
        }
        return null;
    }

    Preference createPreference(final CList cList) throws DataKitException {
        Preference preference = new Preference(getActivity());
        final DataType dataType = getData(cList.save);
        String summary = formatData(cList.input_type, dataType);
        preference.setKey(cList.id);
        preference.setTitle(cList.title);
        if (summary == null) {
            Spannable s = new SpannableString("(not configured)");
            s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.red_700)), 0, s.length(), 0);
            preference.setSummary(s);
        } else {
            Spannable s = new SpannableString(summary);
            s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.accent)), 0, s.length(), 0);
            preference.setSummary(s);
        }
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DataType dataType = getData(cList.save);
                switch (cList.input_type.toUpperCase()) {
                    case TIME:
                        if (dataType == null)
                            inputTime(-1, cList.save);
                        else {
                            inputTime(((DataTypeLong) dataType).getSample(), cList.save);
                        }
                        break;
                    case DATE_TIME:
                        if (dataType == null)
                            inputDateTime(-1, cList.save);
                        else
                            inputDateTime(((DataTypeLong) dataType).getSample(), cList.save);
                        break;
                    case DATE:
                        if (dataType == null)
                            inputDate(-1, cList.save);
                        else
                            inputDate(((DataTypeLong) dataType).getSample(), cList.save);
                        break;

                    case TEXT:
                        if (dataType == null)
                            inputEditText(cList.title, "", cList.save);
                        else
                            inputEditText(cList.title, ((DataTypeString) dataType).getSample(), cList.save);
                        break;
                    case SINGLE_SELECT:
                        if (dataType == null)
                            inputSingleSelect(cList.title, cList.options, null, cList.save);
                        else
                            inputSingleSelect(cList.title, cList.options, ((DataTypeString) dataType).getSample(), cList.save);
                        break;
                    case MULTI_SELECT:
/*
                        if (dataType == null)
                            inputMultiSelect(cList.title, cList.options, null, cList.save);
                        else
                            inputMultiSelect(cList.title, cList.options, null, cList.save);
                            inputNumeric(cList.title, ((DataTypeLong) dataType).getSample(), cList.save);
*/
                        break;

                    case NUMBER:
                        if (dataType == null)
                            inputNumeric(cList.title, -1, cList.save);
                        else
                            inputNumeric(cList.title, ((DataTypeLong) dataType).getSample(), cList.save);
                        break;
                }
                return false;
            }


        });
        return preference;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        assert v != null;
        ListView lv = (ListView) v.findViewById(android.R.id.list);
        lv.setPadding(0, 0, 0, 0);
        return v;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    DataSourceClient getDataSourceClient(DataSource dataSource) throws DataKitException {
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder(dataSource);
        DataSourceClient dataSourceClient;
            dataSourceClient = DataKitAPI.getInstance(getActivity()).register(dataSourceBuilder);
        return dataSourceClient;
    }

    void inputTime(long time, final DataSource dataSource) {
        int minute, hour;
        if (time <= 0) {
            final Calendar c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        } else {
            time /= 60 * 1000;
            minute = (int) (time % 60);
            time /= 60;
            hour = (int) (time % 60);
        }

        // Launch Time Picker Dialog
        Dialog.timePicker(getActivity(), hour, minute, new DialogCallback() {
            @Override
            public void onSelected(String value) {
                DataTypeLong d = new DataTypeLong(DateTime.getDateTime(), Long.valueOf(value));
                try {
                    Log.d("abc","insert...datasource="+dataSource.getType()+" connected="+DataKitAPI.getInstance(getActivity()).isConnected());
                    DataKitAPI.getInstance(getActivity()).insert(getDataSourceClient(dataSource), d);
                    setSettings();
                } catch (DataKitException ignored) {
                    Log.d("abc", "error=" + ignored.getMessage());
                    getActivity().finish();
                }
            }
        }).show();
    }

    void inputDate(long time, final DataSource dataSource) {
        int year = -1, month = -1, day = -1;
        if (time <= 0) time = DateTime.getDateTime();
        if (time > 0) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(time);
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
        }
        Dialog.dateTimePicker(getActivity(), year, month, day, new DialogCallback() {
            @Override
            public void onSelected(final String date) {
                long value = Long.valueOf(date) + Long.valueOf(date);
                DataTypeLong d = new DataTypeLong(DateTime.getDateTime(), value);
                try {
                    Log.d("abc","insert...datasource="+dataSource.getType()+" connected="+DataKitAPI.getInstance(getActivity()).isConnected());
                    DataKitAPI.getInstance(getActivity()).insert(getDataSourceClient(dataSource), d);
                    setSettings();
                } catch (DataKitException ignored) {
                    Log.d("abc", "error=" + ignored.getMessage());
                    getActivity().finish();
                }
            }
        }).show();
    }

    void inputDateTime(long time, final DataSource dataSource) {
        int year, month, day, hour, minute;
        if (time <= 0) time = DateTime.getDateTime();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        final int finalHour = hour;
        final int finalMinute = minute;
        Dialog.dateTimePicker(getActivity(), year, month, day, new DialogCallback() {
            @Override
            public void onSelected(final String date) {
                Dialog.timePicker(getActivity(), finalHour, finalMinute, new DialogCallback() {
                    @Override
                    public void onSelected(String time) {
                        long value = Long.valueOf(date) + Long.valueOf(time);
                        DataTypeLong d = new DataTypeLong(DateTime.getDateTime(), value);
                        try {
                            Log.d("abc","insert...datasource="+dataSource.getType()+" connected="+DataKitAPI.getInstance(getActivity()).isConnected());
                            DataKitAPI.getInstance(getActivity()).insert(getDataSourceClient(dataSource), d);
                            setSettings();
                        } catch (DataKitException ignored) {
                            Log.d("abc", "error=" + ignored.getMessage());
                            getActivity().finish();
                        }

                    }
                }).show();
            }
        }).show();
    }

    void inputEditText(String title, String text, final DataSource dataSource) {
        Dialog.editboxText(getActivity(), "Enter " + title, null, text, new DialogCallback() {
            @Override
            public void onSelected(String value) {
                if (value == null || value.length() == 0) return;
                DataTypeString d = new DataTypeString(DateTime.getDateTime(), value);
                try {
                    Log.d("abc","insert...datasource="+dataSource.getType()+" connected="+DataKitAPI.getInstance(getActivity()).isConnected());
                    DataKitAPI.getInstance(getActivity()).insert(getDataSourceClient(dataSource), d);
                    setSettings();
                } catch (DataKitException ignored) {
                    Log.d("abc", "error=" + ignored.getMessage());
                    getActivity().finish();
                }
            }
        }).show();
    }

    void inputSingleSelect(String title, String[] options, String selected, final DataSource dataSource) {
        int s = -1;
        for (int i = 0; i < options.length && selected != null; i++) {
            if (selected.equals(options[i])) {
                s = i;
                break;
            }
        }
        Dialog.singleChoice(getActivity(), "Select " + title, options, s, new DialogCallback() {
            @Override
            public void onSelected(String value) {
                DataTypeString d = new DataTypeString(DateTime.getDateTime(), value);
                try {
                    Log.d("abc","insert...datasource="+dataSource.getType()+" connected="+DataKitAPI.getInstance(getActivity()).isConnected());
                    DataKitAPI.getInstance(getActivity()).insert(getDataSourceClient(dataSource), d);
                    setSettings();
                } catch (DataKitException ignored) {
                    Log.d("abc", "error=" + ignored.getMessage());
                    getActivity().finish();
                }
            }
        }).show();


    }

    void inputNumeric(String title, final long value, final DataSource dataSource) {
        String selectedValue = null;
        if (value == -1) selectedValue = String.valueOf(value);
        Dialog.editbox_numeric(getActivity(), "Enter " + title, null, selectedValue, new DialogCallback() {
            @Override
            public void onSelected(String value) {
                try {
                    DataTypeLong d = new DataTypeLong(DateTime.getDateTime(), Long.valueOf(value));
                    try {
                        Log.d("abc","insert...datasource="+dataSource.getType()+" connected="+DataKitAPI.getInstance(getActivity()).isConnected());
                        DataKitAPI.getInstance(getActivity()).insert(getDataSourceClient(dataSource), d);
                        setSettings();
                    } catch (DataKitException ignored) {
                    }
                } catch (Exception ignored) {
                    Log.d("abc", "error=" + ignored.getMessage());
                    getActivity().finish();

                }
            }
        }).show();
    }

    @Override
    public void onDestroy() {
        Log.d("abc","onDestroy()..disconnect...10");
        DataKitAPI.getInstance(getActivity()).disconnect();
        super.onDestroy();
    }

}