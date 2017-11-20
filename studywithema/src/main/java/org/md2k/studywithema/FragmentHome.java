package org.md2k.studywithema;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.md2k.mcerebrum.commons.storage.Storage;
import org.md2k.mcerebrum.commons.ui.buttons.ViewButtons;
import org.md2k.mcerebrum.commons.ui.data_quality.CDataQuality;
import org.md2k.mcerebrum.commons.ui.data_quality.ResultCallback;
import org.md2k.mcerebrum.commons.ui.data_quality.UserViewDataQuality;
import org.md2k.mcerebrum.commons.ui.data_quality.ViewDataQuality;
import org.md2k.mcerebrum.commons.ui.day.UserViewDay;
import org.md2k.mcerebrum.commons.ui.day.ViewDay;
import org.md2k.mcerebrum.commons.ui.privacy.UserViewPrivacyControl;
import org.md2k.mcerebrum.commons.ui.privacy.ViewPrivacy;
import org.md2k.studywithema.configuration.CButton;
import org.md2k.studywithema.configuration.CHomeScreen;
import org.md2k.studywithema.configuration.ConfigManager;

public class FragmentHome extends Fragment {
    UserViewDataQuality userViewDataQuality;
    UserViewPrivacyControl userViewPrivacyControl;
    UserViewDay userViewDay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_home, parent, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        CHomeScreen cHomeScreen = ((ActivityMain)getActivity()).cConfig.ui.home_screen;
        if(cHomeScreen.data_quality!=null) {
            loadDataQuality(view, cHomeScreen.data_quality);
        }
        if(cHomeScreen.privacy!=null){
            loadPrivacy(view);
        }
        if(cHomeScreen.button!=null)
            loadButtons(view);
        if(cHomeScreen.day!=null)
            loadDay(view);
    }
    void loadButtons(View view){
        final CButton cButton = ((ActivityMain)getActivity()).cConfig.ui.home_screen.button;
        if(cButton==null || cButton.list==null || cButton.list.length==0) return;
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.linear_layout_add);
        final ViewButtons viewButtons=new ViewButtons(getActivity());
        LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        viewButtons.setLayoutParams(LLParams);
        linearLayout.addView(viewButtons);
        for(int i=0;i<cButton.list.length;i++) {
            Drawable d = Storage.readDrawable(ConfigManager.getConfigDirectory()+cButton.list[i].icon);
            if(d==null) d=ContextCompat.getDrawable(getActivity(), R.drawable.ic_error_outline_white_48dp);
            final int finalI = i;
            viewButtons.addButton(i+1,cButton.list[i].title, d, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(cButton.list[finalI].run.package_name, cButton.list[finalI].run.activity));
                    startActivity(intent);
                    Log.d("abc", "selected");
                }
            });
        }
/*
        userViewDataQuality=new UserViewDataQuality(((ActivityMain)getActivity()).dataQualityManager);
        userViewDataQuality.set(new ResultCallback() {
            @Override
            public void onResult(int[] result) {
                viewButtons.setDataQuality(result);
            }
        });
*/

    }
    void loadDataQuality(View view, CDataQuality[] cDataQualities){
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.linear_layout_add);
        final ViewDataQuality viewDataQuality=new ViewDataQuality(getActivity(), cDataQualities);
        LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        viewDataQuality.setLayoutParams(LLParams);
        linearLayout.addView(viewDataQuality);
        userViewDataQuality=new UserViewDataQuality(((ActivityMain)getActivity()).dataQualityManager);
        userViewDataQuality.set(new ResultCallback() {
            @Override
            public void onResult(int[] result) {
                viewDataQuality.setDataQuality(result);
            }
        });
    }
    void loadPrivacy(View view){
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.linear_layout_add);
        ViewPrivacy viewPrivacy=new ViewPrivacy(getActivity());
        LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        viewPrivacy.setLayoutParams(LLParams);
        linearLayout.addView(viewPrivacy);
        userViewPrivacyControl=new UserViewPrivacyControl(viewPrivacy);
        userViewPrivacyControl.set();
    }
    void loadDay(View view){
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.linear_layout_add);
        ViewDay viewDay = new ViewDay(getActivity());
        LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        viewDay.setLayoutParams(LLParams);
        linearLayout.addView(viewDay);
        userViewDay=new UserViewDay(viewDay);
        userViewDay.set();
    }

    @Override
    public void onDestroyView() {
        CHomeScreen cHomeScreen = ((ActivityMain)getActivity()).cConfig.ui.home_screen;
        if(cHomeScreen.data_quality!=null) {
            userViewDataQuality.clear();
        }
        if(cHomeScreen.privacy!=null)
            userViewPrivacyControl.clear();
        super.onDestroyView();
    }
}
