package com.ridgebotics.ridgescout.utility;

import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class settingsManager {
    public static SharedPreferences prefs;
    public static SharedPreferences.Editor editor;

    public static final String UnameKey     = "username";
    public static final String SelEVCodeKey = "selected_event_code";
    public static final String WifiModeKey  = "wifi_mode";
    public static final String TeamNumKey   = "team_num";
    public static final String MatchNumKey  = "match_num";
    public static final String AllyPosKey   = "alliance_pos";
    public static final String DataModeKey  = "data_view_mode";
    public static final String BtUUIDKey    = "bt_uuid";
    public static final String FTPEnabled   = "ftp_enabled";
    public static final String FTPServer    = "ftp_server";
    public static final String FTPSendMetaFiles    = "ftp_send_meta_files";

    public static Map defaults = getDefaults();
    private static Map getDefaults(){
        Map<String, Object> hm = new HashMap<>();

        hm.put(UnameKey,     "Username");
        hm.put(SelEVCodeKey, "unset");
        hm.put(WifiModeKey,  false);
        hm.put(TeamNumKey,   4388);
        hm.put(MatchNumKey,  0);
        hm.put(AllyPosKey,   "red-1");
        hm.put(DataModeKey,  0);
        hm.put(BtUUIDKey,    UUID.randomUUID().toString());
        hm.put(FTPEnabled,   false);
        hm.put(FTPServer,    "0.0.0.0");
        hm.put(FTPSendMetaFiles, false);

        return hm;
    }

    public static SharedPreferences.Editor getEditor(){
        if(editor == null) editor = prefs.edit();
        return editor;
    }

    public static void resetSettings(){
        getEditor() .putString(UnameKey,    (String)  defaults.get( UnameKey    )).apply();
        getEditor() .putString(SelEVCodeKey,(String)  defaults.get( SelEVCodeKey)).apply();
        getEditor().putBoolean(WifiModeKey, (boolean) defaults.get( WifiModeKey )).apply();

        getEditor()    .putInt(TeamNumKey,  (int)     defaults.get( TeamNumKey  )).apply();
        getEditor()    .putInt(MatchNumKey, (int)     defaults.get( MatchNumKey )).apply();
        getEditor() .putString(AllyPosKey,  (String)  defaults.get( AllyPosKey  )).apply();
        getEditor()    .putInt(DataModeKey, (int)     defaults.get( DataModeKey )).apply();

        getEditor() .putString(BtUUIDKey,   (String)  defaults.get( BtUUIDKey   )).apply();

        getEditor().putBoolean(FTPEnabled,  (boolean) defaults.get( FTPEnabled  )).apply();
        getEditor() .putString(FTPServer,   (String)  defaults.get( FTPServer   )).apply();
        getEditor().putBoolean(FTPSendMetaFiles,   (boolean)  defaults.get( FTPSendMetaFiles   )).apply();
    }

    // IDK why I decided to format these functions like this. It looks cool though.
    public static String  getUsername(){return            prefs.getString(  UnameKey,           (String)  defaults.get(UnameKey));}
    public static void    setUsername(String str){  getEditor().putString(  UnameKey,str).apply();}

    public static String  getEVCode(){return              prefs.getString(  SelEVCodeKey,       (String)  defaults.get(SelEVCodeKey));}
    public static void    setEVCode(String str){    getEditor().putString(  SelEVCodeKey,str).apply();}

    public static boolean getWifiMode(){return            prefs.getBoolean( WifiModeKey,        (boolean) defaults.get(WifiModeKey));}
    public static void    setWifiMode(boolean bool){getEditor().putBoolean( WifiModeKey,bool).apply();}

    public static int     getTeamNum(){return             prefs.getInt(     TeamNumKey,         (int)     defaults.get(TeamNumKey));}
    public static void    setTeamNum(int num){      getEditor().putInt(     TeamNumKey,num).apply();}

    public static int     getMatchNum(){return            prefs.getInt(     MatchNumKey,        (int)     defaults.get(MatchNumKey));}
    public static void    setMatchNum(int num){     getEditor().putInt(     MatchNumKey,num).apply();}

    public static String  getAllyPos(){return             prefs.getString(  AllyPosKey,         (String)  defaults.get(AllyPosKey));}
    public static void    setAllyPos(String str){   getEditor().putString(  AllyPosKey,str).apply();}

    public static int     getDataMode(){return            prefs.getInt(     DataModeKey,        (int)     defaults.get(DataModeKey));}
    public static void    setDataMode(int num){     getEditor().putInt(     DataModeKey,num).apply();}

    public static String  getBtUUID(){return              prefs.getString(  BtUUIDKey,          (String)  defaults.get(BtUUIDKey));}
    public static void    setBtUUID(String str){    getEditor().putString(  BtUUIDKey,str).apply();}



    public static boolean getFTPEnabled(){return            prefs.getBoolean( FTPEnabled,       (boolean) defaults.get(FTPEnabled));}
    public static void    setFTPEnabled(boolean bool){getEditor().putBoolean( FTPEnabled,bool).apply();}

    public static String  getFTPServer(){return             prefs.getString(  FTPServer,          (String)  defaults.get(FTPServer));}
    public static void    setFTPServer(String str){   getEditor().putString(  FTPServer,str).apply();}

    public static boolean  getFTPSendMetaFiles(){return prefs.getBoolean(FTPSendMetaFiles, (boolean) defaults.get(FTPSendMetaFiles));}
    public static void    setFTPSendMetaFiles(String str){getEditor().putString(FTPSendMetaFiles,str).apply();}



}
