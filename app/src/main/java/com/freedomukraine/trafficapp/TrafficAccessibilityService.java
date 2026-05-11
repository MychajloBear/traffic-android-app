package com.freedomukraine.trafficapp;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.*;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TrafficAccessibilityService extends AccessibilityService {

    private boolean handled = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (handled) return;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        String text = extractText(root);

        if (text.contains("red light")) {
            new Thread(() -> {
                try {
                    String answer = callBackend(text);
                    clickByText(root, answer);
                    handled = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @Override
    public void onInterrupt() {}

    private String extractText(AccessibilityNodeInfo node) {
        StringBuilder sb = new StringBuilder();
        traverse(node, sb);
        return sb.toString();
    }

    private void traverse(AccessibilityNodeInfo node, StringBuilder sb) {
        if (node == null) return;

        if (node.getText() != null) {
            sb.append(node.getText()).append("\n");
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            traverse(node.getChild(i), sb);
        }
    }

    private String callBackend(String text) throws Exception {

        URL url = new URL("http://192.168.1.7:8080/api/analysis");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        JSONObject body = new JSONObject();
        body.put("questionText", text);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        String response = new String(
                conn.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        JSONObject json = new JSONObject(response);
        return json.getString("answer");
    }

    private boolean clickByText(AccessibilityNodeInfo node, String text) {

        if (node == null) return false;

        if (node.getText() != null &&
            node.getText().toString().equalsIgnoreCase(text)) {

            if (node.isClickable()) {
                return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

            AccessibilityNodeInfo parent = node.getParent();
            while (parent != null) {
                if (parent.isClickable()) {
                    return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                parent = parent.getParent();
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            if (clickByText(node.getChild(i), text)) {
                return true;
            }
        }
        Log.d("TrafficApp", "Accessibility event received");
        return false;
    }
}