package info.fandroid.example.augmentedreality;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CameraViewActivity extends Activity implements
		SurfaceHolder.Callback, OnLocationChangedListener, OnAzimuthChangedListener, View.OnClickListener,SensorEventListener {
	//объявляем необходимые переменные
	private Camera mCamera;
	private SurfaceHolder mSurfaceHolder;
	private boolean isCameraviewOn = false;
	private ArObject mPoi;

	private double mAzimuthReal = 0;
	private double mAzimuthTeoretical = 0;

	/*константы для хранения допустимых отклонений дистанции и азимута
    устройства от целевых. Значения подобраны практически,  их можно менять, чтобы облегчить, или наоборот,
    усложнить задачу поиска AR указателя . Точность дистанции указана в условных единицах, равных примерно 0.9м,
    а точность азимута - в градусах*/
	private static final double DISTANCE_ACCURACY = 20;
	private static final double AZIMUTH_ACCURACY = 20;

	// объявляем переменные которые будут содержать информацию и широте и долготе устройства и обнуляем их
	private double mMyLatitude = 0;
	private double mMyLongitude = 0;

	/*константы с координатами цели, это будет местоположение AR указателя. */
	public static double TARGET_LATITUDE = 53.219446;
	public static double TARGET_LONGITUDE = 44.792207;

	private MyCurrentAzimuth myCurrentAzimuth;
	private MyCurrentLocation myCurrentLocation;

	//компас анимация вращения
	private float RotateDegree = 0f;
	private SensorManager mSensorManager;

	TextView descriptionTextView;
	ImageView pointerIcon,imageView, icons_map;

	//переменные с AR объектами
	private double TargetLatMarA;
	private double TargetLngMarA;

	private double TargetLatMarB;
	private double TargetLngMarB;

	private double TargetLatMarC;
	private double TargetLngMarC;

	private double TargetLatMarD;
	private double TargetLngMarD;

	private double TargetLatMarE;
	private double TargetLngMarE;

	// onCreate вызывается при создании или перезапуске активности
	// Задаём портретную ориентацию активити
	//Инициализируем возможность работать с сенсором устройства:
	//1. setupListeners  инициализацирует слушателей местоположения и азимута,  вызываем конструкторы классов MyCurrentLocation и MyCurrentAzimuth и выполняем их методы start
	//2. setupLayout инициализирует все элементы экрана и создает surfaceView для отображения превью камеры
	//3. Создаем экземпляр метки в дополненной реальности с указанием координат ее местоположения
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_view);


		// получение данных точек из активити с картой
            Intent intent = getIntent();
            double latMarA = intent.getExtras().getDouble("latMarA");
            double lngMarA = intent.getExtras().getDouble("lngMarA");
		    double latMarB = intent.getExtras().getDouble("latMarB");
		    double lngMarB = intent.getExtras().getDouble("latMarB");
		    double latMarC = intent.getExtras().getDouble("latMarC");
		    double lngMarC = intent.getExtras().getDouble("lngMarC");
		    double latMarD = intent.getExtras().getDouble("latMarD");
		    double lngMarD = intent.getExtras().getDouble("lngMarD");
		    double latMarE = intent.getExtras().getDouble("latMarE");
		    double lngMarE = intent.getExtras().getDouble("lngMarE");

		TargetLatMarA = latMarA;
		TargetLngMarA = lngMarA;
		TargetLatMarB = latMarB;
		TargetLngMarB = lngMarB;
		TargetLatMarC = latMarC;
		TargetLngMarC = lngMarC;
		TargetLatMarD = latMarD;
		TargetLngMarD = lngMarD;
		TargetLatMarE = latMarE;
		TargetLngMarE = lngMarE;

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		pointerIcon = (ImageView) findViewById(R.id.icon);
		imageView = (ImageView) findViewById(R.id.imageView);
		setupListeners(); // подключение к службам
		setupLayout(); //метод setupLayout инициализирует все элементы экрана и создает surfaceView для отображения превью камеры
		setAugmentedRealityPoint();
	}

	//Получаем градус поворота от оси, которая направлена на север, север = 0 градусов:
	@Override
	public void onSensorChanged(SensorEvent event) {
		double rotationAzimuthTeoretical = calculateTeoreticalAzimuth();
		int rotSen = (int) rotationAzimuthTeoretical;
		float degree = Math.round(event.values[0]);
		degree = degree-rotSen;
		//Создаем анимацию вращения:
		RotateAnimation rotateAnimation = new RotateAnimation(
				RotateDegree,
				-degree,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF,
				0.5f);
		//Продолжительность анимации в миллисекундах:
		rotateAnimation.setDuration(200);
		//Настраиваем анимацию после завершения подсчетных действий датчика:
		rotateAnimation.setFillAfter(true);
		//Запускаем анимацию:
		imageView.startAnimation(rotateAnimation);
		RotateDegree = -degree;
	}
	//Этот метод необходим для работы датчика сенсора
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
	//создаем экземпляр объекта который будет выводиться в указанной метке на карте с указанием координат его местоположения
	private void setAugmentedRealityPoint() {
		mPoi = new ArObject(getString(R.string.ar_obj), TARGET_LATITUDE, TARGET_LONGITUDE);


		// сначала точка 1
		// после точка 2
		// после точка 3




	}
	/*вычисляем дистанцию между устройством и объектом по формуле.
        Результат приходит в десятичных градусах, умножение его на 100000 дает некую условную единицу,
        приблизительно равную 0.9м. Чтобы перевести результат в метрическую систему, нужно применять
        сложные расчеты, и я решил не усложнять приложение.*/
	public double calculateDistance() {
		double dX = mPoi.getPoiLatitude() - mMyLatitude;
		double dY = mPoi.getPoiLongitude() - mMyLongitude;

		double distance = (Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2)) * 100000);

		return distance;
	}
	/*вычисляем теоретический азимут по формуле.
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

		if (dX > 0 && dY > 0) { // I четверть
			return azimuth = phiAngle;
		} else if (dX < 0 && dY > 0) { // II четверть
			return azimuth = 180 - phiAngle;
		} else if (dX < 0 && dY < 0) { // III четверть
			return azimuth = 180 + phiAngle;
		} else if (dX > 0 && dY < 0) { // IV четверть
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

		String text =
				 "Локация AR объекта:"
				+ "\nШирота: " + TARGET_LATITUDE + "  Долгота: " + TARGET_LONGITUDE
				+ "\nТекущая лекация:"
				+ "\nШирота: " + mMyLatitude	+ "  Долгота: " + mMyLongitude
				+ "\n"
				+ "\nТеоретический: " + tAzimut
				+ "\nРеальный азимут: " + rAzimut
				+ "\nДистанция до объекта: " + distance
				+ "\nMarker A: " + TargetLatMarA+ " "+ TargetLngMarA;


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

		// тост с текущей локацией
		Toast.makeText(this,"Ваша локация latitude: "+location.getLatitude()+" longitude: "
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

    //Останавливаем при надобности слушателя ориентации
    //сенсора с целью сбережения заряда батареи:
	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
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
		//Устанавливаем слушателя ориентации сенсора
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_GAME);
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
		icons_map = (ImageView) findViewById(R.id.icons_map);
		icons_map.setVisibility(View.VISIBLE);
		icons_map.setOnClickListener(this);
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
	// обработчик нажатия кнопки, здесь по нажатию открываем карту
	@Override
	public void onClick(View v) {
		Intent intentToMap = new Intent(this, MapActivity.class);
/*
		intentToMap.putExtra("latMarA", TargetLatMarA);
		intentToMap.putExtra("lngMarA", TargetLngMarA);
*/
		startActivity(intentToMap);
	}


}
