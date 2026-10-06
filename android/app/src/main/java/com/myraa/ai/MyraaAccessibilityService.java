package com.myraa.ai;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.ArrayList;
import java.util.List;

public class MyraaAccessibilityService extends AccessibilityService {
    private static volatile MyraaAccessibilityService instance;
    @Override public void onServiceConnected() { instance = this; }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) { /* Native event bridge is intentionally lightweight. */ }
    @Override public void onInterrupt() { }
    @Override public void onDestroy() { if (instance == this) instance = null; super.onDestroy(); }

    public static boolean performGlobalBack() { return instance != null && instance.performGlobalAction(GLOBAL_ACTION_BACK); }
    public static boolean performHome() { return instance != null && instance.performGlobalAction(GLOBAL_ACTION_HOME); }
    public static boolean performRecents() { return instance != null && instance.performGlobalAction(GLOBAL_ACTION_RECENTS); }

    public static boolean clickText(String text) {
        if (instance == null) return false;
        AccessibilityNodeInfo root = instance.getRootInActiveWindow();
        if (root == null) return false;
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(text);
        for (AccessibilityNodeInfo n : nodes) {
            if (n.isClickable() && n.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true;
            AccessibilityNodeInfo p = n.getParent();
            while (p != null) { if (p.isClickable() && p.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true; p = p.getParent(); }
        }
        return false;
    }

    public static boolean typeText(String text) {
        if (instance == null) return false;
        AccessibilityNodeInfo root = instance.getRootInActiveWindow();
        if (root == null) return false;
        AccessibilityNodeInfo focus = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if (focus == null) return false;
        android.os.Bundle args = new android.os.Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        return focus.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
    }

    public static boolean tap(float x, float y) {
        if (instance == null) return false;
        Path path = new Path(); path.moveTo(x, y);
        GestureDescription g = new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path, 0, 50)).build();
        return instance.dispatchGesture(g, null, null);
    }
}
