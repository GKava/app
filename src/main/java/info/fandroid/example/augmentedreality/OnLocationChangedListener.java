package info.fandroid.example.augmentedreality;

import android.location.Location;

// Cлушатель изменения местоположения
public interface OnLocationChangedListener {
    void onLocationChanged(Location currentLocation);
}
