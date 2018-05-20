package info.fandroid.example.augmentedreality;

// здесь просто набор переменных: имя и координаты, а также геттеры для них.
public class ArObject {
	private String mName;
	private double mLatitude;
	private double mLongitude;

	// конструктор класса
	public ArObject(String newName, double newLatitude, double newLongitude) {
		this.mName = newName;
        this.mLatitude = newLatitude;
        this.mLongitude = newLongitude;
	}
	

	public String getPoiName() {
		return mName;
	}
	public double getPoiLatitude() {
		return mLatitude;
	}
	public double getPoiLongitude() {
		return mLongitude;
	}
}
