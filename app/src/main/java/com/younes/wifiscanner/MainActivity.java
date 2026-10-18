package com.younes.wifiscanner;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.location.LocationManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.graphics.Color;

import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private WifiManager wifi;
    private TextView results;
    private Button scanButton;
    private boolean receiverRegistered = false;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
                    .equals(intent.getAction())) {
                showNetworks();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wifi = (WifiManager) getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 30, 24, 20);
        layout.setBackgroundColor(Color.rgb(16, 24, 39));

        TextView title = new TextView(this);
        title.setText("📡 WiFi Scanner");
        title.setTextSize(26);
        title.setTextColor(Color.CYAN);
        layout.addView(title);

        scanButton = new Button(this);
        scanButton.setText("فحص الشبكات القريبة");
        layout.addView(scanButton);

        results = new TextView(this);
        results.setTextColor(Color.WHITE);
        results.setTextSize(16);
        results.setPadding(0, 20, 0, 0);
        layout.addView(results);

        setContentView(layout);

        scanButton.setOnClickListener(v -> startScan());

        IntentFilter filter = new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(receiver, filter,
                    Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(receiver, filter);
        }

        receiverRegistered = true;

        startScan();
    }

    private void startScan() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                    }, 100);

            return;
        }

        if (wifi == null) {
            results.setText("خدمة Wi-Fi غير متاحة على هذا الجهاز.");
            return;
        }

        if (!wifi.isWifiEnabled()) {
            results.setText(
                    "يرجى تفعيل Wi-Fi من إعدادات الهاتف، ثم أعد الفحص.");
            return;
        }

        LocationManager locationManager =
                (LocationManager) getSystemService(
                        Context.LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 28
                && !locationManager.isLocationEnabled()) {
            results.setText(
                    "يرجى تفعيل خدمة الموقع من إعدادات الهاتف.");
            return;
        }

        results.setText("جارٍ فحص الشبكات...");

        boolean started = wifi.startScan();

        if (!started) {
            results.setText(
                    "تعذر بدء فحص جديد. سأحاول عرض آخر النتائج المتاحة.");
            showNetworks();
        }
    }

    private void showNetworks() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            List<ScanResult> networks = wifi.getScanResults();

            if (networks == null || networks.isEmpty()) {
                results.setText(
                        "لم تظهر شبكات. تأكد من تفعيل Wi-Fi والموقع، "
                        + "ثم حاول مجددًا.");
                return;
            }

            StringBuilder text = new StringBuilder();
            text.append("عدد الشبكات: ")
                    .append(networks.size())
                    .append("\n\n");

            for (ScanResult network : networks) {
                text.append("📶 ")
                    .append(network.SSID.isEmpty()
                            ? "(شبكة مخفية)" : network.SSID)
                    .append("\n");

                text.append("قوة الإشارة: ")
                    .append(network.level)
                    .append(" dBm\n");

                text.append("التردد: ")
                    .append(netwo
