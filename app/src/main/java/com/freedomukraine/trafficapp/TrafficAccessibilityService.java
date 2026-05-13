package com.freedomukraine.trafficapp;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TrafficAccessibilityService extends AccessibilityService {

    private static final String TAG = "TrafficAccessibility";
    private boolean handled = false;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Accessibility service connected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "Event received: type=" + event.getEventType()
                + ", package=" + event.getPackageName()
                + ", class=" + event.getClassName());

        if (handled) {
            Log.d(TAG, "Already handled, skipping");
            return;
        }

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            Log.d(TAG, "Root window is null");
            return;
        }

        String text = extractText(root);
        Log.d(TAG, "Extracted text: " + text);

        if (text.toLowerCase().contains("red light")) {
            Log.d(TAG, "Matched question text, calling backend");

            new Thread(() -> {
                try {
                    String answer = callBackend(text);
                    Log.d(TAG, "Backend returned answer: " + answer);

                    boolean clicked = clickByText(root, answer);
                    Log.d(TAG, "Click result: " + clicked);

                    handled = true;
                } catch (Exception e) {
                    Log.e(TAG, "Backend call or click failed", e);
                }
            }).start();
        } else {
            Log.d(TAG, "No matching question text found");
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted");
    }

    private String extractText(AccessibilityNodeInfo node) {
        StringBuilder sb = new StringBuilder();
        traverse(node, sb);
        return sb.toString();
    }

    private void traverse(AccessibilityNodeInfo node, StringBuilder sb) {
        if (node == null) {
            return;
        }

        CharSequence nodeText = node.getText();
        if (nodeText != null) {
            sb.append(nodeText).append("\n");
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            traverse(node.getChild(i), sb);
        }
    }

    private String callBackend(String text) throws Exception {
        URL url = new URL("http://192.168.1.7:8080/api/analysis");
        Log.d(TAG, "Calling backend: " + url);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setDoOutput(true);

        JSONObject body = new JSONObject();
        body.put("questionText", text);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        Log.d(TAG, "Backend HTTP status: " + status);

        String response = new String(
                conn.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        Log.d(TAG, "Backend response body: " + response);

        JSONObject json = new JSONObject(response);
        return json.getString("answer");
    }

    private boolean clickByText(AccessibilityNodeInfo node, String text) {
        if (node == null) return false;

        CharSequence nodeText = node.getText();

        if (nodeText != null) {
            Log.d(TAG, "Checking node text: " + nodeText);
        }

        if (nodeText != null && nodeText.toString().equalsIgnoreCase(text)) {
            Log.d(TAG, "Found matching node: " + nodeText);

            if (node.isClickable()) {
                Log.d(TAG, "Node is clickable, clicking directly");
                return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

            AccessibilityNodeInfo parent = node.getParent();
            while (parent != null) {
                if (parent.isClickable()) {
                    Log.d(TAG, "Parent is clickable, clicking parent");
                    return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                parent = parent.getParent();
            }

            Log.d(TAG, "Matching node found but no clickable parent");
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            if (clickByText(node.getChild(i), text)) {
                return true;
            }
        }

        return false;
    }
}