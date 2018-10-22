package com.claire.mymaps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION = 2;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //利用getFragmentManager()方法取得管理器
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        //以非同步的方式取得GoogleMap物件
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //使用MyLocation功能，是讓地圖位置移至目前裝置所在位置的一個功能
        //先加入危險權險檢查
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            setupMyLocation();
        }

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    //接著覆寫onRequestPermissionsResult方法，判斷使用者是否允許權限
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //此段又會出現檢查權限的相關程式碼，但是這一行是在取得使用者允許的if判斷式中，
                    //可以加入忽略提醒的註解
                    //使用者允許權限
                    setupMyLocation();
                }else {
                    //使用者拒絕授權，停用MyLocation功能
                }
                break;
        }
    }

    //設定按下MyLocation按鈕時的事件傾聽setOnMyLocationButtonClickListener方法
    @SuppressLint("MissingPermission")
    private void setupMyLocation() {
        //啟用我的位置
        mMap.setMyLocationEnabled(true);
        //按下「我的位置」鈕時執行
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                //透過位置服務，取得目前裝置所在
                //gpsLocation();
                //若需要持續更新目前位置、存取更多資料時，改使用Location API 取得位置
                fuseLocation();
                return false;
            }
        });
    }

    //若需要持續更新目前位置、存取更多資料時，改使用Location API 取得位置
    @SuppressLint("MissingPermission")
    private void fuseLocation() {
        //首先，先取得FusedLocationProviderClient物件
        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(this);
        //再呼叫它的getLastLocation()方法取得目前位置，只是，
        //它是非同步呼叫(A synchronized)，所以需要再指派完成工作的傾聽介面
        client.getLastLocation().addOnCompleteListener(
                this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        //完成呼叫時，應先判斷工作是否成功，若成功，
                        //再取得Location物件後，將地圖移到該位置(Location)
                        if (task.isSuccessful()){
                            Location location = task.getResult();
                            if (location != null){
                                setMapAnimateCamera(location);
                            }

                        }
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void gpsLocation() {
        //取得位置服務管理器
        LocationManager locationManager =
                (LocationManager) getSystemService(LOCATION_SERVICE);
        //getLastKnownLocation可取得目前裝置的位置
        String provider = "gps"; //需要一個服務提供者的名稱字串

        //嚴謹一點的用法，使用Criteria類別向系統查詢最適合的服務提供者字串
        Criteria criteria = new Criteria();
        //設定標準為存取精確
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //向系統查詢最合適的服務提供者名稱(通常也是"gps')
        String provider2 = locationManager.getBestProvider(criteria, true);


        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null){
            setMapAnimateCamera(location);
        }
    }

    private void setMapAnimateCamera(Location location) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(),
                        location.getLongitude()),15));

        Log.i("LOCATION", location.getLatitude() + "/"
                + location.getLongitude());
    }

}
