package utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2018/4/17.
 * 获取用户的地理位置
 */
public class GPSUtils {
    private static GPSUtils instance = null;
    private LocationManager locationManager = null;
    public static final int LOCATION_CODE = 1000;
    public static final int OPEN_GPS_CODE = 1001;
    private GPSUtils() {}

    public static GPSUtils getInstance() {
        if (instance == null)
            instance = new GPSUtils();
        return instance;
    }

    @SuppressLint("MissingPermission")
    public Location getLatLon(Context context) {
        Log.i("GPS: ", "getLatLon");
        if(locationManager == null)
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);        // 默认Android GPS定位实例

        Location location = null;

        // 检查权限
        setPermission(context);

//        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);      // GPS芯片定位 需要开启GPS
//        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);      // 利用网络定位 需要开启GPS
        location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);      // 其他应用使用定位更新了定位信息 需要开启GPS
        return location;
    }

    @SuppressLint("MissingPermission")
    public Pair<String, String> getCountryProvince(Context context) {
        Log.i("GPS: ", "getProvince");
        if(locationManager == null)
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);        // 默认Android GPS定位实例

        Location location = null;
        // 检查权限
        setPermission(context);

//        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);      // GPS芯片定位 需要开启GPS
//        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);      // 利用网络定位 需要开启GPS
        location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);      // 其他应用使用定位更新了定位信息 需要开启GPS

        Pair<String, String> p = null;
        if(location != null) {
            Log.i("GPS: ", "获取位置信息成功");
            Log.i("GPS: ","经度：" + location.getLatitude());
            Log.i("GPS: ","纬度：" + location.getLongitude());

            // 获取地址信息
            p = getAddress(context, location.getLatitude(), location.getLongitude());
            Log.i("GPS: ","location：" + p.toString());
        } else
            Log.e("GPS: ", "获取位置信息失败，请检查是够开启GPS,是否授权");
        return p;
    }

    /*
     * 根据经度纬度 获取国家，省份
     * */
    public Pair<String, String> getAddress(Context context, double latitude, double longitude) {
        Pair<String, String> location = null;
        List<Address> addList = null;
        Geocoder ge = new Geocoder(context);
        try {
            addList = ge.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
            return location;
        }
        for (Address ad : addList)
            location = new Pair<>(ad.getCountryName(), ad.getLocality());
        return location;
    }
    private void setPermission(Context context) {
        // 是否已经授权
        for(int i=0;i<5;i++) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //判断GPS是否开启，没有开启，则开启
                if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    //跳转到手机打开GPS页面
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    //设置完成后返回原来的界面
                    ((Activity) context).startActivityForResult(intent,OPEN_GPS_CODE);
                    continue;
                }
                return;
            } else {
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }
        ((Activity) context).finish();
        System.exit(-1);
    }
}

