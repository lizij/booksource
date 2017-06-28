package com.lizij.locationtest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public LocationClient locationClient;
    private BaiduMap baiduMap;
    private boolean isFirstLocate = true;

    private TextView positionText;
    private MapView mapView;
    private Button locateButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationClient = new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(new MyLocationListener());
        positionText = (TextView) findViewById(R.id.postion_text_view);
        mapView = (MapView) findViewById(R.id.bmap_view);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        locateButton = (Button) findViewById(R.id.locate_button);
        locateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFirstLocate = true;
            }
        });


        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }else {
            requestLocation();
        }
    }

    private void requestLocation(){
        initLocation();
        locationClient.start();
    }

    private void navigateTo(BDLocation bdLocation){
        if (isFirstLocate){
            LatLng latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }

        MyLocationData.Builder builder = new MyLocationData.Builder();
        builder.latitude(bdLocation.getLatitude());
        builder.longitude(bdLocation.getLongitude());
        MyLocationData myLocationData = builder.build();
        baiduMap.setMyLocationData(myLocationData);
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
//        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        locationClient.setLocOption(option);
    }

    class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            navigateTo(bdLocation);

            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("latitude:" + bdLocation.getLatitude() + "\n");
            currentPosition.append("longitude:" + bdLocation.getLongitude() + "\n");
            currentPosition.append("loctype:");
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation){
                currentPosition.append("GPS");
            }else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){
                currentPosition.append("Network");
            }else if (bdLocation.getLocType() == BDLocation.TypeCacheLocation){
                currentPosition.append("Cache");
            }else if (bdLocation.getLocType() == BDLocation.TypeOffLineLocation){
                currentPosition.append("Offline");
            }else if (bdLocation.getLocType() == BDLocation.TypeNone){
                currentPosition.append("None");
            }else{
                currentPosition.append("Error");
            }
            currentPosition.append("\n");
            currentPosition.append("Country:" + bdLocation.getCountry() + "\n");
            currentPosition.append("Province" + bdLocation.getProvince() + "\n");
            currentPosition.append("City:" + bdLocation.getCity() + "\n");
            currentPosition.append("District:" + bdLocation.getDistrict() + "\n");
            currentPosition.append("Street:" + bdLocation.getStreet() + "\n");

            final String currentPostionString = currentPosition.toString();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    positionText.setText(currentPostionString);
                }
            });
            Log.d(TAG, "onReceiveLocation: " + currentPosition.toString());
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {
            Log.d(TAG, "onConnectHotSpotMessage: " + s + "/" + i);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0){
                    for (int result: grantResults){
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(MainActivity.this, "Need to grant all permissions", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else{
                    Toast.makeText(this, "Unknown Error", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }
}
