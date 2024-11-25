package com.example.wifitester;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TARGET_SSID = "Tanishq_5GHz"; // Replace with actual SSID
    private static final String TARGET_BSSID = "04:25:e0:e3:66:4d"; // Replace with actual BSSID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check permissions based on the Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.NEARBY_WIFI_DEVICES}, 2);
            }
        } else if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // Ensure location services are enabled
        if (!isLocationEnabled()) {
            Toast.makeText(this, "Please enable location services for Wi-Fi verification.", Toast.LENGTH_LONG).show();
        }

        TextView tvStatus = findViewById(R.id.tv_status);
        Button btnVerify = findViewById(R.id.btn_verify);

        btnVerify.setOnClickListener(v -> {
            if (isConnectedToTargetWifi()) {
                tvStatus.setText("Presence Confirmed: Connected to the target Wi-Fi.");
                Toast.makeText(this, "You are connected to the required Wi-Fi!", Toast.LENGTH_SHORT).show();
            } else {
                tvStatus.setText("Verification Failed: Please connect to the target Wi-Fi.");
                Toast.makeText(this, "Not connected to the required Wi-Fi.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isConnectedToTargetWifi() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                Network network = connectivityManager.getActiveNetwork();
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);

                if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    String currentSsid = wifiInfo.getSSID();
                    String currentBssid = wifiInfo.getBSSID();

                    // Debugging logs
                    Log.d("WiFiInfo", "SSID: " + currentSsid);
                    Log.d("WiFiInfo", "BSSID: " + currentBssid);

                    return TARGET_SSID.equals(currentSsid.replace("\"", "")) && TARGET_BSSID.equals(currentBssid);
                } else {
                    Toast.makeText(this, "No active Wi-Fi connection detected.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }

            // For Android 9 and below
            String currentSsid = wifiInfo.getSSID();
            String currentBssid = wifiInfo.getBSSID();
            return TARGET_SSID.equals(currentSsid.replace("\"", "")) && TARGET_BSSID.equals(currentBssid);
        }

        Toast.makeText(this, "Wi-Fi Manager unavailable.", Toast.LENGTH_SHORT).show();
        return false;
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 2) { // Nearby Wi-Fi Devices permission
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Nearby Wi-Fi permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied. Wi-Fi verification won't work.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 1) { // Location permission
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied. Wi-Fi verification won't work.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
