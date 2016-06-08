package com.chartbeat.androidsdk;

import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * Created by Mike Dai Wang on 2016-02-04.
 */
final class ViewTracker {
    private static final String TAG = ViewTracker.class.getSimpleName();

    private static final double SECOND_IN_DOUBLE = 1000.0;

    private final long viewInitTime;

    private ViewInfo viewInfo;
    private ViewDimension dimension;
    private ViewContent content;

    ViewTracker(String viewID, String viewTitle, String internalReferrer, String token, ViewDimension dimension) {
        this.viewInfo = new ViewInfo(viewID, viewTitle, internalReferrer, token);
        this.viewInitTime = System.currentTimeMillis();
        if (dimension == null) {
            this.dimension = new ViewDimension();
        } else {
            this.dimension = dimension;
        }
        this.content = new ViewContent();
    }

    boolean isSameView(String viewID) {
        String currentViewID = viewInfo.getViewID();
        return currentViewID != null && currentViewID.equals(viewID);
    }

    boolean wasReferredFromAnotherView() {
        return viewInfo.getInternalReferrer() != null;
    }

    String getViewID() {
        return viewInfo.getViewID();
    }

    String getToken() {
        return viewInfo.getViewID();
    }

    double getViewingTimeInMinutes() {
        long timeInView = System.currentTimeMillis() - this.viewInitTime;

        if (timeInView < 0) {// could happen if time is adjusting
            timeInView = 0;
        }

        // calculate time in minutes:
        double secondsInView = timeInView / SECOND_IN_DOUBLE;
        double minutesInView = secondsInView / 60;
        // print with one decimal precision:
        return minutesInView;
    }

    LinkedHashMap<String, String> toPingParams() {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();

        if (viewInfo != null) {
            params.putAll(viewInfo.toPingParams());
        }

        double minutesInView = getViewingTimeInMinutes();
        params.put(QueryKeys.TIME_ON_VIEW_IN_MINUTES, String.format(Locale.US, "%.1f", minutesInView));

        if (dimension != null) {
            params.putAll(dimension.toPingParams());
        }

        if (content != null) {
            params.putAll(content.toPingParams());
        }

        return params;
    }

    void updateDimension(int scrollPositionTop, int scrollWindowHeight, int totalContentHeight, int fullyRenderedDocWidth) {
        int currentMaxDepth = dimension.getMaxScrollDepth();

        dimension = new ViewDimension(scrollPositionTop,
                scrollWindowHeight,
                totalContentHeight,
                fullyRenderedDocWidth,
                Math.max(currentMaxDepth, scrollPositionTop));
    }

    void updateSections(String sections) {
        content = new ViewContent(sections, content.getAuthors(), content.getZones(), content.getPageLoadTime());
    }

    void updateAuthors(String authors) {
        content = new ViewContent(content.getSections(), authors, content.getZones(), content.getPageLoadTime());
    }

    void updateZones(String zones) {
        content = new ViewContent(content.getSections(), content.getAuthors(), zones, content.getPageLoadTime());
    }

    void updatePageLoadingTime(float pageLoadTime) {
        content = new ViewContent(content.getSections(), content.getAuthors(), content.getZones(), pageLoadTime);
    }
}