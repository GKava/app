package info.fandroid.example.augmentedreality;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.location.Location;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CameraViewActivity extends Activity implements
		SurfaceHolder.Callback, OnLocationChangedListener, OnAzimuthChangedListener, View.OnClickListener {
	//объявляем необходимые переменные
	private Camera mCamera;
	private SurfaceHolder mSurfaceHolder;
	private boolean isCameraviewOn = false;
	private Pikachu mPoi;

	private double mAzimuthReal = 0;
	private double mAzimuthTeoretical = 0;

	/*константы для хранения допустимых отклонений дистанции и азимута
    устройства от целевых. Значения подобраны практически,  их можно менять, чтобы облегчить, или наоборот,
    усложнить задачу поиска AR указателя . Точность дистанции указана в условных единицах, равных примерно 0.9м,
    а точность азимута - в градусах*/
	private static final double DISTANCE_ACCURACY = 20;
	private static final double AZIMUTH_ACCURACY = 20;

	private double mMyLatitude = 0;
	private double mMyLongitude = 0;

	/*константы с координатами цели, это будет местоположение AR указателя. */
	public static double TARGET_LATITUDE = 53.219466;
	public static double TARGET_LONGITUDE = 44.792290;

	private MyCurrentAzimuth myCurrentAzimuth;
	private MyCurrentLocation myCurrentLocation;

	TextView descriptionTextView;
	ImageView pointerIcon;
	Button btnMap;

	// onCreate вызывается при создании или перезапуске активности
	// Задаём портретную ориентацию активити
	//1. setupListeners  инициализацирует слушателей местоположения и азимута, ь вызываем конструкторы классов MyCurrentLocation и MyCurrentAzimuth и выполняем их методы start
	//2. setupLayout инициализирует все элементы экрана и создает surfaceView для отображения превью камеры
	//3. Создаем экземпляр метки в дополненной реальности с указанием координат ее местоположения
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_view);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		pointerIcon = (ImageView) findViewById(R.id.icon);
		setupListeners();
		setupLayout();
		setAugmentedRealityPoint();
	}
	//создаем экземпляр покемона с указанием координат его местоположения
	private void setAugmentedRealityPoint() {
		mPoi = new Pikachu(
				getString(R.string.p_name),
				TARGET_LATITUDE, TARGET_LONGITUDE
		);
	}
	/*вычисляем дистанцию между устройством и покемоном по формуле, о которой я говорил в начале урока.
        Результат приходит в десятичных градусах, умножение его на 100000 дает некую условную единицу,
        приблизительно равную 0.9м. Чтобы перевести результат в метрическую систему, нужно применять
        сложные расчеты, и я решил не усложнять приложение.*/
	public double calculateDistance() {
		double dX = mPoi.getPoiLatitude() - mMyLatitude;
		double dY = mPoi.getPoiLongitude() - mMyLongitude;

		double distance = (Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2)) * 100000);

		return distance;
	}
	/*вычисляем теоретический азимут по формуле, о которой я говорил в начале урока.
    Вычисление азимута для разных четвертей производим на основе таблицы. */
	public double calculateTeoreticalAzimuth() {
		double dX = mPoi.getPoiLatitude() - mMyLatitude;
		double dY = mPoi.getPoiLongitude() - mMyLongitude;

		double phiAngle;
		double tanPhi;
		double azimuth = 0;

		tanPhi = Math.abs(dY / dX);
		phiAngle = Math.atan(tanPhi);
		phiAngle = Math.toDegrees(phiAngle);

		if (dX > 0 && dY > 0) { // I quater
			return azimuth = phiAngle;
		} else if (dX < 0 && dY > 0) { // II
			return azimuth = 180 - phiAngle;
		} else if (dX < 0 && dY < 0) { // III
			return azimuth = 180 + phiAngle;
		} else if (dX > 0 && dY < 0) { // IV
			return azimuth = 360 - phiAngle;
		}

		return phiAngle;
	}
	//расчитываем точность азимута, необходимую для отображения покемона
	private List<Double> calculateAzimuthAccuracy(double azimuth) {
		double minAngle = azimuth - AZIMUTH_ACCURACY;
		double maxAngle = azimuth + AZIMUTH_ACCURACY;
		List<Double> minMax = new ArrayList<Double>();

		if (minAngle < 0)
			minAngle += 360;

		if (maxAngle >= 360)
			maxAngle -= 360;

		minMax.clear();
		minMax.add(minAngle);
		minMax.add(maxAngle);

		return minMax;
	}
	//Метод isBetween определяет, находится ли азимут в целевом диапазоне с учетом допустимых отклонений
	private boolean isBetween(double minAngle, double maxAngle, double azimuth) {
		if (minAngle > maxAngle) {
			if (isBetween(0, maxAngle, azimuth) && isBetween(minAngle, 360, azimuth))
				return true;
		} else {
			if (azimuth > minAngle && azimuth < maxAngle)
				return true;
		}
		return false;
	}

	// выводим на экран основную информацию о местоположении цели и нашего устройства
	private void updateDescription() {

		long distance = (long) calculateDistance();
		int tAzimut = (int) mAzimuthTeoretical;
		int rAzimut = (int) mAzimuthReal;

		String text = mPoi.getPoiName()
				+ " location:"
				+ "\nlatitude: " + TARGET_LATITUDE + "  longitude: " + TARGET_LONGITUDE
				+ "\nCurrent location:"
				+ "\nLatitude: " + mMyLatitude	+ "  Longitude: " + mMyLongitude
				+ "\n"
				+ "\nTarget azimuth: " + tAzimut
				+ "\nCurrent azimuth: " + rAzimut
				+ "\nDistance: " + distance;

		descriptionTextView.setText(text);
	}


	/*переопределяем метод слушателя OnAzimuthChangeListener, который вызывается при изменении азимута
        устройства, расчитанного на основании показаний датчиков, получаемых в параметрах этого метода из
        класса MyCurrentAsimuth. Получаем данные азимута устройства, сравниваем их с целевыми параметрами -
        проверяем, если азимуты реальный и теоретический, а также дистанция до цели совпадают в пределах
        допустимых значений, отображаем картинку покемона на экране. Также вызываем метод обновления
        информации о местоположении на экране.*/
	@Override
	public void onAzimuthChanged(float azimuthChangedFrom, float azimuthChangedTo) {
		mAzimuthReal = azimuthChangedTo;
		mAzimuthTeoretical = calculateTeoreticalAzimuth();
		int distance = (int) calculateDistance();

		double minAngle = calculateAzimuthAccuracy(mAzimuthTeoretical).get(0);
		double maxAngle = calculateAzimuthAccuracy(mAzimuthTeoretical).get(1);

		if ((isBetween(minAngle, maxAngle, mAzimuthReal)) && distance <= DISTANCE_ACCURACY) {
			pointerIcon.setVisibility(View.VISIBLE);
		} else {
			pointerIcon.setVisibility(View.INVISIBLE);
		}

		updateDescription();
	}
	/*переопределяем метод onLocationChanged интерфейса слушателя OnLocationChangedListener, здесь
        при изменении местоположения отображаем тост с новыми координатами и вызываем метод, который
        выводит основную информацию на экран.*/
	@Override
	public void onLocationChanged(Location location) {
		mMyLatitude = location.getLatitude();
		mMyLongitude = location.getLongitude();
		mAzimuthTeoretical = calculateTeoreticalAzimuth();
		int distance = (int) calculateDistance();
		Toast.makeText(this,"latitude: "+location.getLatitude()+" longitude: "
				+location.getLongitude(), Toast.LENGTH_SHORT).show();

		if (mAzimuthReal == 0){
			if ( distance <= DISTANCE_ACCURACY) {
				pointerIcon.setVisibility(View.VISIBLE);
			} else {
				pointerIcon.setVisibility(View.INVISIBLE);
			}
		}
		updateDescription();
	}
	/*в методе жизненного цикла onStop мы вызываем методы отмены регистрации датчика азимута и
        закрытия подключения к службам Google Play*/
	@Override
	protected void onStop() {
		myCurrentAzimuth.stop();
		myCurrentLocation.stop();
		super.onStop();
	}
	//в методе onResume соответственно открываем подключение и регистрируем слушатель датчиков
	@Override
	protected void onResume() {
		super.onResume();
		myCurrentAzimuth.start();
		myCurrentLocation.start();
	}

	/*метод setupListeners служит для инициализации слушателей местоположения и азимута - здесь
    мы вызываем конструкторы классов MyCurrentLocation и MyCurrentAzimuth и выполняем их методы start*/
	private void setupListeners() {
		myCurrentLocation = new MyCurrentLocation(this);
		myCurrentLocation.buildGoogleApiClient(this);
		myCurrentLocation.start();

		myCurrentAzimuth = new MyCurrentAzimuth(this, this);
		myCurrentAzimuth.start();
	}
	//метод setupLayout инициализирует все элементы экрана и создает surfaceView для отображения превью камеры
	private void setupLayout() {
		descriptionTextView = (TextView) findViewById(R.id.cameraTextView);
		btnMap = (Button) findViewById(R.id.btnMap);
		btnMap.setVisibility(View.VISIBLE);
		btnMap.setOnClickListener(this);
		getWindow().setFormat(PixelFormat.UNKNOWN);
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.cameraview);
		mSurfaceHolder = surfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	/*вызывается сразу же после того, как были внесены любые структурные изменения (формат или размер)
        surfaceView. Здесь , в зависимости от условий, стартуем или останавливаем превью камеры*/
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {
		if (isCameraviewOn) {
			mCamera.stopPreview();
			isCameraviewOn = false;
		}

		if (mCamera != null) {
			try {
				mCamera.setPreviewDisplay(mSurfaceHolder);
				mCamera.startPreview();
				isCameraviewOn = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/*вызывается при первом создании surfaceView, здесь получаем доступ к камере и устанавливаем
        ориентацию дисплея превью*/
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
		mCamera.setDisplayOrientation(90);
	}
	//вызывается перед уничтожением surfaceView, останавливаем превью и освобождаем камеру
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
		isCameraviewOn = false;
	}
	//и последний метод - обработчик нажатия кнопки, здесь по нажатию открываем карту
	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, MapActivity.class);
		startActivity(intent);
	}
}
