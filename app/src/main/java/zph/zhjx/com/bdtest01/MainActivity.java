package zph.zhjx.com.bdtest01;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private TextView tvWGS84, tvNmea;
    private LocationListener gpsListener;
    private LocationManager mLocationManager;
    private GeomagneticField gmfield;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //显示wgs84数据
        tvWGS84 = (TextView) findViewById(R.id.tv_rgs84);
        //显示nmea协议中数据
        tvNmea = (TextView) findViewById(R.id.tv_nmea);
        mLocationManager = ((LocationManager) getSystemService(Context.LOCATION_SERVICE));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.addNmeaListener(new GpsStatus.NmeaListener() {

            @Override
            public void onNmeaReceived(long timestamp, String nmea) {
                tvNmea.invalidate();
                //此处以GPGGA为例
                //$GPGGA,232427.000,3751.1956,N,11231.1494,E,1,6,1.20,824.4,M,-23.0,M,,*7E
                if (nmea.contains("GPGGA")) {
                    String info[] = nmea.split(",");
                    //GPGGA中altitude是MSL altitude(平均海平面)
                    tvWGS84.setText(nmea);
                    Log.i("GPGGA","获取的GPGGA数据是："+nmea);
                    Log.i("GPGGA","获取的GPGGA数据length："+info.length);
                    Log.i("GPGGA","GPS定位数据："+info[0]);
                    Log.i("GPGGA","UTC时间："+info[1]);

                    Log.i("GPGGA","纬度："+info[2]);
                    Log.i("GPGGA","纬度半球："+info[3]);
                    Log.i("GPGGA","经度："+info[4]);
                    Log.i("GPGGA","经度半球："+info[5]);
                    Log.i("GPGGA","GPS状态："+info[6]);
                    Log.i("GPGGA","使用卫星数量："+info[7]);
                    Log.i("GPGGA","HDOP-水平精度因子："+info[8]);
                    Log.i("GPGGA","椭球高："+info[9]);
                    Log.i("GPGGA","大地水准面高度异常差值："+info[10]);
                    Log.i("GPGGA","差分GPS数据期限："+info[11]);
                    Log.i("GPGGA","差分参考基站标号："+info[12]);
                    Log.i("GPGGA","ASCII码的异或校验："+info[info.length-1]);
                    //UTC + (＋0800) = 本地（北京）时间
                    int a= Integer.parseInt(info[1].substring(0,2));
                    a+=8;
                    String time="";
                    String time1="";
                    if(a<10){
                        time="0"+a+info[1].substring(2,info[1].length()-1);
                    }
                    else{
                        time=a+info[1].substring(2,info[1].length()-1);
                    }
                    time1=time.substring(0,2)+":"+time.substring(2,4)+":"+time.substring(4,6);
                    tvNmea.setText("获取的GPGGA数据length："+info.length+"\nUTC时间："+info[1]+"\n北京时间: "+time1
                    +"\n纬度："+info[2]+"\n纬度半球："+info[3]+"\n经度："+info[4]+"\n经度半球："+info[5]
                    +"\nGPS状态："+info[6]+"\n使用卫星数量："+info[7]+"\nHDOP-水平精度因子："+info[8]+"\n椭球高："+info[9]
                    +"\n大地水准面高度异常差值："+info[10]+"\n差分GPS数据期限："+info[11]+"\n差分参考基站标号："+info[12]
                    +"\nASCII码的异或校验："+info[info.length-1]);
                }
            }
        });
        gpsListener = new MyLocationListner();
    }

    private class MyLocationListner implements LocationListener {

        @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
        @Override
        public void onLocationChanged(Location location) {
            tvWGS84.invalidate();
            tvNmea.invalidate();
            Double longitude = location.getLongitude();
            float accuracy = location.getAccuracy();
            Double latitude = location.getLatitude();
            Double altitude = location.getAltitude();// WGS84
            float bearing = location.getBearing();
            gmfield = new GeomagneticField((float) location.getLatitude(),
                    (float) location.getLongitude(), (float) location.getAltitude(),
                    System.currentTimeMillis());
            tvWGS84.setText("Altitude=" + altitude + "\nLongitude=" + longitude + "\nLatitude="
                    + latitude + "\nDeclination=" + gmfield.getDeclination() + "\nBearing="
                    + bearing + "\nAccuracy=" + accuracy);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //退出Activity后不再定位
        mLocationManager.removeUpdates(gpsListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //判断gps是否可用
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "gps可用", Toast.LENGTH_LONG).show();
            //开始定位
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, gpsListener);
        }else{
            Toast.makeText(this, "请打开gps或者选择gps模式为准确度高", Toast.LENGTH_LONG).show();
            //前往设置GPS页面
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }
}