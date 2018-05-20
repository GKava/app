package info.fandroid.example.augmentedreality;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

// В классе MyCurrentLocation определяется местоположение устройства

public class MyCurrentLocation implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private OnLocationChangedListener onLocationChangedListener;

    //передаем интерфейс OnLocationChangedListener в конструкторе для организации
//прослушивания события смены местоположения

    public MyCurrentLocation(OnLocationChangedListener onLocationChangedListener) {
        this. onLocationChangedListener = onLocationChangedListener;
    }
    /**
     * Создает GoogleApiClient. Использует метод { @code #addApi} для запроса
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks( this)
                .addOnConnectionFailedListener( this )
                .addApi(LocationServices. API)
                .build();
//создаем запрос и устанавливаем интервал для его отправки
        mLocationRequest = LocationRequest. create()
                .setPriority(LocationRequest. PRIORITY_HIGH_ACCURACY )
                .setInterval( 10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000 ); // 1 second, in milliseconds
    }

    public void start(){
//Подключает клиента к службам Google Play.
        mGoogleApiClient.connect();
    }

    public void stop(){
        //Закрывает подключение к службам Google Play.
        mGoogleApiClient.disconnect();
    }
    //После вызова connect(), этот метод будет вызываться асинхронно после успешного завершения запроса подключения.
    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi .requestLocationUpdates( mGoogleApiClient, mLocationRequest , this );
        mLastLocation = LocationServices.FusedLocationApi .getLastLocation(
                mGoogleApiClient);

        if ( mLastLocation != null ) {
            onLocationChangedListener.onLocationChanged( mLastLocation );
        }
    }
    //Вызывается, когда клиент временно в отключенном состоянии.
    @Override
    public void onConnectionSuspended( int i) {

    }
    //Вызывается, когда произошла ошибка при подключении клиента к службе.
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e( "MyApp" , "Location services connection failed with code " + connectionResult.getErrorCode());
    }
    /*
        * Реализуем метод onLocationChanged интерфейса LocationListener. Обратный вызов,
   который возникает, когда изменяется местоположение.
        * Здесь создаем объект mLastLocation, который хранит последнее местоположение и передаем его в методе интерфейса.
        */
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = LocationServices.FusedLocationApi .getLastLocation(
                mGoogleApiClient);
        if ( mLastLocation != null ) {
            onLocationChangedListener.onLocationChanged( mLastLocation );

        }
    }
}
