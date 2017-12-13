package com.chartbeat.androidsdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by Mike Dai Wang on 2016-02-10.
 */
final class ChartbeatServiceHandler extends Handler {
    private static final String TAG = ChartbeatServiceHandler.class.getSimpleName();

    private static final String KEY_LAST_USED_ACCOUNT_ID = "KEY_LAST_USED_ACCOUNT_ID";
    private static final String KEY_LAST_USED_DOMAIN = "KEY_LAST_USED_DOMAIN";

    private WeakReference<Context> context;
    private static ChartBeatTracker singleton;
    private static String userAgent;

    public ChartbeatServiceHandler(WeakReference<Context> context, Looper looper, String systemUserAgent) {
        super(looper);
        this.context = context;
        userAgent = systemUserAgent;
    }

    @Override
    public void handleMessage(Message msg) {
        Bundle bundle = msg.getData();

        processMessage(bundle);
    }

    private void processMessage(Bundle bundle) {
        String actionType = bundle.getString(Tracker.KEY_SDK_ACTION_TYPE);

        if (actionType.equals(Tracker.ACTION_INIT_TRACKER)) {
            handleSDKInit(bundle);
        }

        if (!isSDKInitialized()) {
            reInitSDKFromBackground();
        }

        handleMessageType(actionType, bundle);
    }

    private static boolean isSDKInitialized() {
        return singleton != null;
    }

    private void reInitSDKFromBackground() {
        SharedPreferences prefs = context.get().getSharedPreferences(ChartBeatTracker.CHARTBEAT_PREFS, Context.MODE_PRIVATE);

        String accountID = prefs.getString(KEY_LAST_USED_ACCOUNT_ID, null);

        if (accountID != null) {
            String domain = prefs.getString(KEY_LAST_USED_DOMAIN, null);

            initSDK(accountID, domain);
        }
    }

    private void initSDK(String accountID, String domain) {
        singleton = new ChartBeatTracker(context, accountID, domain, userAgent, getLooper());
    }

    private void handleMessageType(String actionType, Bundle bundle) {

        if (!isSDKInitialized()) {
            Logger.e(TAG, "Chartbeat SDK has not been initialized");
            return;
        }

        switch (actionType) {
            case Tracker.ACTION_SET_APP_REFERRER:
                setAppReferrer(bundle);
                break;
            case Tracker.ACTION_STOP_TRACKER:
                stopTracker();
                break;
            case Tracker.ACTION_TRACK_VIEW:
                trackView(bundle);
                break;
            case Tracker.ACTION_LEFT_VIEW:
                userLeftView(bundle);
                break;
            case Tracker.ACTION_USER_INTERACTED:
                userInteracted();
                break;
            case Tracker.ACTION_USER_TYPED:
                userTyped();
                break;
            case Tracker.ACTION_SET_SUBDOMAIN:
                setSubdomain(bundle);
                break;
            case Tracker.ACTION_SET_DOMAIN:
                setDomain(bundle);
                break;
            case Tracker.ACTION_SET_ZONES:
                setZones(bundle);
                break;
            case Tracker.ACTION_SET_AUTHORS:
                setAuthors(bundle);
                break;
            case Tracker.ACTION_SET_SECTIONS:
                setSections(bundle);
                break;
            case Tracker.ACTION_SET_VIEW_LOADING_TIME:
                setViewLoadTime(bundle);
                break;
            case Tracker.ACTION_SET_POSITION:
                setPosition(bundle);
                break;
            case Tracker.ACTION_PAUSE_TRACKER:
                pauseTracker();
            default:
                return;
        }
    }

    private void handleSDKInit(Bundle bundle) {
        if (!isSDKInitialized()) {
            String accountID = bundle.getString(Tracker.KEY_ACCOUNT_ID);
            String domain = bundle.getString(Tracker.KEY_DOMAIN);

            initSDK(accountID, domain);
            cacheSDKDetailForReinit(accountID, domain);
        }
    }

    private void cacheSDKDetailForReinit(String accountID, String domain) {
        SharedPreferences prefs = context.get().getSharedPreferences(ChartBeatTracker.CHARTBEAT_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(KEY_LAST_USED_ACCOUNT_ID, accountID);
        editor.putString(KEY_LAST_USED_DOMAIN, domain);
        editor.commit();
    }

    public void setAppReferrer(Bundle bundle) {
        if (!isSDKInitialized()) {
            return;
        }

        String appReferrer = bundle.getString(Tracker.KEY_APP_REFERRER);
        singleton.setExternalReferrer(appReferrer);
    }

    public void stopTracker() {
        if (!isSDKInitialized()) {
            return;
        }

        singleton.stopTracker();
        clearCachedSDKDetail();
        singleton = null;
    }

    public void pauseTracker() {
        if (!isSDKInitialized()) {
            return;
        }

        singleton.stopTracker();
    }

    private void clearCachedSDKDetail() {
        SharedPreferences prefs = context.get().getSharedPreferences(ChartBeatTracker.CHARTBEAT_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(KEY_LAST_USED_ACCOUNT_ID, null);
        editor.putString(KEY_LAST_USED_DOMAIN, null);
        editor.commit();
    }

    public void trackView(Bundle bundle) {
        if (!isSDKInitialized()) {
            return;
        }

        String viewId = bundle.getString(Tracker.KEY_VIEW_ID);
        String viewTitle = bundle.getString(Tracker.KEY_VIEW_TITLE);

        int scrollPositionTop = bundle.getInt(Tracker.KEY_POSITION_TOP, -1);
        int scrollWindowHeight = bundle.getInt(Tracker.KEY_WINDOW_HEIGHT, -1);
        int totalContentHeight = bundle.getInt(Tracker.KEY_CONTENT_HEIGHT, -1);
        int fullyRenderedDocWidth = bundle.getInt(Tracker.KEY_DOC_WIDTH, -1);

        singleton.trackViewImpl(viewId, viewTitle,
                scrollPositionTop, scrollWindowHeight, totalContentHeight, fullyRenderedDocWidth);
    }

    public static void userLeftView(Bundle bundle) {
        if (!isSDKInitialized()) {
            return;
        }

        String viewId = bundle.getString(Tracker.KEY_VIEW_ID);
        singleton.userLeftViewImpl(viewId);
    }

    public static void userInteracted() {
        if (!isSDKInitialized()) {
            return;
        }

        singleton.userInteractedImpl();
    }

    public static void userTyped() {
        if (!isSDKInitialized()) {
            return;
        }

        singleton.userTypedImpl();
    }

    public static void setDomain(Bundle bundle) {
        if (!isSDKInitialized()) {
            return;
        }

        if (singleton.isNotTrackingAnyView()) {
            Logger.e(TAG, "View tracking hasn't started, please call Tracker.trackView() first");
            return;
        }
        String domain = bundle.getString(Tracker.KEY_DOMAIN);
        singleton.updateDomain(domain);
    }

    public static void setSubdomain(Bundle bundle) {
        if (!isSDKInitialized()) {
            return;
        }

        if (singleton.isNotTrackingAnyView()) {
            Logger.e(TAG, "View tracking hasn't started, please call Tracker.trackView() first");
            return;
        }
        String subdomain = bundle.getString(Tracker.KEY_SUBDOMAIN);
        singleton.updateSubdomain(subdomain);
    }

    public static void setZones(Bundle bundle) {
        if (!isSDKInitialized()) {
            return;
        }

        if (singleton.isNotTrackingAnyView()) {
            Logger.e(TAG, "View tracking hasn't started, please call Tracker.trackView() first");
            return;
        }

        String zones = bundle.getString(Tracker.KEY_ZONES);
        singleton.updateZones(zones);
    }

    public static void setAuthors(Bundle bundle) {
        if (!isSDKInitialized()) {
            return;
        }

        if (singleton.isNotTrackingAnyView()) {
            Logger.e(TAG, "View tracking hasn't started, please call Tracker.trackView() first");
            return;
        }
        String authors = bundle.getString(Tracker.KEY_AUTHORS);
        singleton.updateAuthors(authors);
    }

    public static void setSections(Bundle bundle) {
        if (!isSDKInitialized()) {
            return;
        }

        if (singleton.isNotTrackingAnyView()) {
            Logger.e(TAG, "View tracking hasn't started, please call Tracker.trackView() first");
            return;
        }
        String sections = bundle.getString(Tracker.KEY_SECTIONS);
        singleton.updateSections(sections);
    }

    public static void setViewLoadTime(Bundle bundle) {
        if (!isSDKInitialized()) {
            return;
        }

        if (singleton.isNotTrackingAnyView()) {
            Logger.e(TAG, "View tracking hasn't started, please call Tracker.trackView() first");
            return;
        }
        float viewLoadTime = bundle.getFloat(Tracker.KEY_VIEW_LOADING_TIME, 0.0f);
        singleton.updatePageLoadingTime(viewLoadTime);
    }

    public static void setPosition(Bundle bundle) {
        if (!isSDKInitialized()) {
            Logger.e(TAG, "Chartbeat SDK has not been initialized");
            return;
        }

        int scrollPositionTop = bundle.getInt(Tracker.KEY_POSITION_TOP, -1);
        int scrollWindowHeight = bundle.getInt(Tracker.KEY_WINDOW_HEIGHT, -1);
        int totalContentHeight = bundle.getInt(Tracker.KEY_CONTENT_HEIGHT, -1);
        int fullyRenderedDocWidth = bundle.getInt(Tracker.KEY_DOC_WIDTH, -1);

        singleton.updateViewDimensions(scrollPositionTop,
                scrollWindowHeight,
                totalContentHeight,
                fullyRenderedDocWidth);
    }
}