package org.md2k.studywithema.menu;
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

import android.graphics.Color;
import android.view.View;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import org.md2k.mcerebrum.system.update.Update;
import org.md2k.studywithema.MyApplication;
import org.md2k.studywithema.R;
import org.md2k.studywithema.configuration.CMenu;


public class MyMenu {
    public static final int MENU_HOME = 0;
    public static final int MENU_SETTINGS = 1;
    public static final int MENU_START_STOP_DATA_COLLECTION =2;
    public static final int MENU_RESET=3;
    public static final int MENU_UPDATE=4;
    public static final int MENU_HELP = 5;
    public static final int MENU_CONTACT_US=6;
    public static final String MENU_HOME_STR="HOME";
    public static final String MENU_SETTINGS_STR="SETTINGS";
    public static final String MENU_START_STOP_DATA_COLLECTION_STR="START_STOP_DATA_COLLECTION";
    public static final String MENU_RESET_STR="RESET";
    public static final String MENU_UPDATE_STR ="UPDATE";
    public static final String MENU_HELP_STR="HELP";
    public static final String MENU_CONTACT_US_STR="CONTACT_US";

//    abstract IProfile[] getHeaderContentType(final Context context, UserInfo userInfo, StudyInfo studyInfo, final ResponseCallBack responseCallBack);

    public IProfile[] getHeaderContent(String userTitle, /*UserInfo userInfo, StudyInfo studyInfo, */final ResponseCallBack responseCallBack) {
        IProfile[] iProfiles=new IProfile[1];
        iProfiles[0]=new ProfileDrawerItem().withName(userTitle).withIcon(R.mipmap.ic_launcher);
        return iProfiles;
    }
    private static MenuContent[] getMenuContent(CMenu[] cMenu){
        MenuContent[] menuContents=new MenuContent[cMenu.length];
        for(int i=0;i<cMenu.length;i++){
            if(cMenu[i].id.equalsIgnoreCase(MENU_HOME_STR))
                menuContents[i]=new MenuContent(cMenu[i].title, FontAwesome.Icon.faw_home, MenuContent.PRIMARY_DRAWER_ITEM, MENU_HOME,0);
            else if(cMenu[i].id.equalsIgnoreCase(MENU_SETTINGS_STR))
                menuContents[i]=new MenuContent(cMenu[i].title, FontAwesome.Icon.faw_cog, MenuContent.PRIMARY_DRAWER_ITEM, MENU_SETTINGS,0);
            else if(cMenu[i].id.equalsIgnoreCase(MENU_START_STOP_DATA_COLLECTION_STR))
                menuContents[i]=new MenuContent("Start Data Collection", FontAwesome.Icon.faw_play_circle_o, MenuContent.PRIMARY_DRAWER_ITEM, MENU_START_STOP_DATA_COLLECTION,0);
            else if(cMenu[i].id.equalsIgnoreCase(MENU_RESET_STR))
                menuContents[i]=new MenuContent(cMenu[i].title, FontAwesome.Icon.faw_repeat, MenuContent.PRIMARY_DRAWER_ITEM, MENU_RESET,0);
            else if(cMenu[i].id.equalsIgnoreCase(MENU_UPDATE_STR))
                menuContents[i]=new MenuContent(cMenu[i].title, FontAwesome.Icon.faw_refresh, MenuContent.PRIMARY_DRAWER_ITEM, MENU_UPDATE,Update.hasUpdate(MyApplication.getContext()));
            else if(cMenu[i].id.equalsIgnoreCase(MENU_HELP_STR))
                menuContents[i]=new MenuContent(cMenu[i].title, FontAwesome.Icon.faw_question, MenuContent.PRIMARY_DRAWER_ITEM, MENU_HELP,0);
            else if(cMenu[i].id.equalsIgnoreCase(MENU_CONTACT_US_STR))
                menuContents[i]=new MenuContent(cMenu[i].title, FontAwesome.Icon.faw_envelope_o, MenuContent.PRIMARY_DRAWER_ITEM, MENU_CONTACT_US,0);

        }
        return menuContents;
    }

    public static IDrawerItem[] getMenuContent(CMenu[] cMenu, final ResponseCallBack responseCallBack) {
        MenuContent[] menuContent=getMenuContent(cMenu);
        IDrawerItem[] iDrawerItems = new IDrawerItem[menuContent.length];
        for (int i = 0; i < menuContent.length; i++) {
            switch (menuContent[i].type) {
                case MenuContent.PRIMARY_DRAWER_ITEM:
                    iDrawerItems[i] = new PrimaryDrawerItem().withName(menuContent[i].name).withIcon(menuContent[i].icon).withIdentifier(menuContent[i].identifier).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            responseCallBack.onResponse(drawerItem, (int) drawerItem.getIdentifier());
                            return false;
                        }
                    });
                    if(menuContent[i].badgeValue>0){
                        ((PrimaryDrawerItem)(iDrawerItems[i])).withBadge(String.valueOf(menuContent[i].badgeValue)).withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.md_red_700));;
                    }
                    break;
                case MenuContent.SECONDARY_DRAWER_ITEM:
                    iDrawerItems[i] = new SecondaryDrawerItem().withName(menuContent[i].name).withIcon(menuContent[i].icon).withIdentifier(menuContent[i].identifier).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            responseCallBack.onResponse(drawerItem, (int) drawerItem.getIdentifier());
                            return false;
                        }
                    });
                    if(menuContent[i].badgeValue>0){
                        ((SecondaryDrawerItem)(iDrawerItems[i])).withBadge(String.valueOf(menuContent[i].badgeValue)).withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.md_red_700));;
                    }
                    break;
                case MenuContent.SECTION_DRAWER_ITEM:
                    iDrawerItems[i] = new SectionDrawerItem().withName(menuContent[i].name).withIdentifier(menuContent[i].identifier).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            responseCallBack.onResponse(drawerItem, (int) drawerItem.getIdentifier());
                            return false;
                        }
                    });
            }
        }
        return iDrawerItems;
    }

    public static boolean hasMenuItem(CMenu[] menu, int menuUpdate) {
        String id;
        switch(menuUpdate){
            case MENU_HOME: id=MENU_HOME_STR;break;
            case MENU_SETTINGS: id=MENU_SETTINGS_STR;break;
            case MENU_START_STOP_DATA_COLLECTION: id=MENU_START_STOP_DATA_COLLECTION_STR;break;
            case MENU_RESET: id = MENU_RESET_STR;break;
            case MENU_UPDATE: id = MENU_UPDATE_STR;break;
            case MENU_HELP: id = MENU_HELP_STR;break;
            case MENU_CONTACT_US: id=MENU_CONTACT_US_STR;break;
            default: id="";
        }
        for(int i=0;i<menu.length;i++){
            if(menu[i].id.equalsIgnoreCase(id)) return true;
        }
        return false;
    }
}

