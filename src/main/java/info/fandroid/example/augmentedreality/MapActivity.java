package info.fandroid.example.augmentedreality;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Arrays;

//Здесь мы просто создаем карту Google Map и добавляем маркер в точку с заранее определенными координатами
public class MapActivity extends AppCompatActivity implements GoogleMap.OnMarkerDragListener , OnLocationChangedListener {

    GoogleMap googleMap;

    private Polyline mPolyline;

    private Marker mMarkerA;
    private Marker mMarkerB;
    private Marker mMarkerС;
    private Marker mMarkerD;
    private Marker mMarkerE;

    // объявляем переменные которые будут содержать информацию и широте и долготе устройства и обнуляем их
    private double mMyLatitude = 0;
    private double mMyLongitude = 0;

    // переменные для хранения и передачи параметров
    private double marALat;
    private double marALnt;

    private double marBLat;
    private double marBLnt;

    private double marCLat;
    private double marCLnt;

    private double marDLat;
    private double marDLnt;

    private double marELat;
    private double marELnt;

    //переменная для определения того есть ли что то на карте
    private int clearValue=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        createMapView();
        addMarker();
        setTitle(getString(R.string.app_name));
        googleMap.setMyLocationEnabled(true);
        getMap().setOnMarkerDragListener(this); // перетаскивание маркера


    }
    /***************************** Сохраняем состояние ******************************/
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        getPosition();

        if(outState == null)
            outState = new Bundle();
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
    protected void getPosition() {
        marALat = mMarkerA.getPosition().latitude;
        marALnt = mMarkerA.getPosition().longitude;

        marBLat = mMarkerB.getPosition().latitude;
        marBLnt = mMarkerB.getPosition().longitude;

        marCLat = mMarkerС.getPosition().latitude;
        marCLnt = mMarkerС.getPosition().longitude;

        marDLat = mMarkerD.getPosition().latitude;
        marDLnt = mMarkerD.getPosition().longitude;

        marELat = mMarkerE.getPosition().latitude;
        marELnt = mMarkerE.getPosition().longitude;

    }

    private void createMapView() {
        try {
            if (null == googleMap) {
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();

                if (null == googleMap) {
                    Toast.makeText(getApplicationContext(),
                            "Ошибка при создании карты", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException exception) {
            Log.e("mapApp", exception.toString());
        }

    }

    //В методе добавления маркера устанавливаем позицию камеры и масштаб карты
    private void addMarker() {

        double lat = CameraViewActivity.TARGET_LATITUDE;
        double lng = CameraViewActivity.TARGET_LONGITUDE;

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(lat, lng))
                .zoom(15)
                .build();


        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.animateCamera(cameraUpdate);

        if (null != googleMap) {
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lng))
                    .title(getString(R.string.app_name))
                    .draggable(false)
            );
        }
    }


    /***************************** Строим маршрут ******************************/
    protected GoogleMap getMap() {
        return googleMap;
    }


    @Override
    public void onLocationChanged(Location location) {
        mMyLatitude = location.getLatitude();
        mMyLongitude = location.getLongitude();


    }

    //Добавляем маркеры на карту
    protected void addWay() {
        double razbros = 0.003000;
        double latitude = CameraViewActivity.TARGET_LATITUDE;
        double longitude = CameraViewActivity.TARGET_LONGITUDE;


        mMarkerA = getMap().addMarker(new MarkerOptions().position(new LatLng(latitude + razbros, longitude)).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.romb1)));
        mMarkerA.setTitle("A");
        mMarkerB = getMap().addMarker(new MarkerOptions().position(new LatLng(latitude + razbros * 2, longitude)).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.romb2)));
        mMarkerB.setTitle("B");
        mMarkerС = getMap().addMarker(new MarkerOptions().position(new LatLng(latitude + razbros * 3, longitude)).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.romb3)));
        mMarkerС.setTitle("C");
        mMarkerD = getMap().addMarker(new MarkerOptions().position(new LatLng(latitude + razbros * 4, longitude)).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.romb4)));
        mMarkerD.setTitle("D");
        mMarkerE = getMap().addMarker(new MarkerOptions().position(new LatLng(latitude + razbros * 5, longitude)).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.romb5)));
        mMarkerE.setTitle("E");

        mPolyline = getMap().addPolyline(new PolylineOptions().geodesic(true));
    }


    private void updatePolyline() {
        mPolyline.setPoints(Arrays.asList(mMarkerA.getPosition(), mMarkerB.getPosition(), mMarkerС.getPosition(), mMarkerD.getPosition(), mMarkerE.getPosition()));

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
        getPosition();
        updatePolyline();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        updatePolyline();
    }

    // переход на активити с камерой и передача параметров точек на карте ( неактиная пока не посмотришь маршрут)
    public void onClick(View view) {

        if (clearValue ==1 ) {

            Intent intent = new Intent(this, CameraViewActivity.class);

            intent.putExtra("latMarA", mMarkerA.getPosition().latitude);
            intent.putExtra("lngMarA", mMarkerA.getPosition().longitude);

            intent.putExtra("latMarB", mMarkerB.getPosition().latitude);
            intent.putExtra("lngMarB", mMarkerB.getPosition().longitude);

            intent.putExtra("latMarC", mMarkerС.getPosition().latitude);
            intent.putExtra("lngMarC", mMarkerС.getPosition().longitude);

            intent.putExtra("latMarD", mMarkerD.getPosition().latitude);
            intent.putExtra("lngMarD", mMarkerD.getPosition().longitude);

            intent.putExtra("latMarE", mMarkerE.getPosition().latitude);
            intent.putExtra("lngMarE", mMarkerE.getPosition().longitude);

            startActivity(intent);
        }else {
            Toast.makeText(this, R.string.t_marsh, Toast.LENGTH_SHORT).show();
        }
    }

    //добавляем маркеры для построения маршрута на карту
    public void onClickWay(View view) {
        if (clearValue ==0) {
            addWay();
            updatePolyline();
            clearValue++;
        }

    }

    //отчищаем карту
    public void clean(View view) {
        if (clearValue ==1) {
            mMarkerA.remove();
            mMarkerB.remove();
            mMarkerС.remove();
            mMarkerD.remove();
            mMarkerE.remove();
            mPolyline.remove();
                clearValue--;
        }else {
            Toast.makeText(this, R.string.t_clear, Toast.LENGTH_SHORT).show();
        }
    }
}
