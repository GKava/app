package tomato_ar.battleroyale.fortnite.augmentedreality;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import java.io.IOException;
import tomato_ar.battleroyale.example.augmentedreality.R;


public class CameraViewActivity extends Activity implements SurfaceHolder.Callback {

	private Camera mCamera;
	private SurfaceHolder mSurfaceHolder;
	private boolean isCameraviewOn = false;
	ImageView image_action;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_UP: // отпускание
				image_action.setVisibility(ProgressBar.INVISIBLE);
				break;
			case MotionEvent.ACTION_DOWN:
				float x = event.getX();
				float y = event.getY();
				image_action.setX(x-200);
				image_action.setY(y-250);
				image_action.setVisibility(ProgressBar.VISIBLE);
				break;
			case MotionEvent.ACTION_MOVE:
				image_action.setVisibility(ProgressBar.INVISIBLE);
				break;

		}
		return true;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_view);
		image_action = findViewById(R.id.image_action);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setupLayout();

	}


	//метод setupLayout инициализирует все элементы экрана и создает surfaceView для отображения превью камеры
	private void setupLayout() {

		getWindow().setFormat(PixelFormat.UNKNOWN);
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.cameraview);
		mSurfaceHolder = surfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	/*вызывается сразу же после того, как были внесены любые структурные изменения (формат или размер)
        surfaceView. Здесь , в зависимости от условий, стартуем или останавливаем превью камеры*/

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
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
}
