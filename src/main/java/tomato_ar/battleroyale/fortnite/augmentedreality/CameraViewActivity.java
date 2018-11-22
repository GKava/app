package tomato_ar.battleroyale.fortnite.augmentedreality;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.IOException;
import java.io.InputStream;

import pl.droidsonroids.gif.GifImageView;
import tomato_ar.battleroyale.example.augmentedreality.R;


public class CameraViewActivity extends Activity implements SurfaceHolder.Callback , View.OnClickListener{

	private Camera mCamera;
	private SurfaceHolder mSurfaceHolder;
	private boolean isCameraviewOn = false;
	ImageView image_action;
	private AdView mAdView;
	Button tomato;
	Button beef;
	GifImageView gifka;
	boolean lol;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_view);
		image_action = findViewById(R.id.image_action);
		tomato = findViewById(R.id.tomato);
		beef = findViewById(R.id.beef);
        gifka = findViewById(R.id.gifka);
		tomato.setOnClickListener(this);
		beef.setOnClickListener(this);

		mAdView = findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setupLayout();

	}


	@Override
	public void onClick(View v) {
switch (v.getId()){
	case R.id.tomato:
		lol=false;
		image_action.setVisibility(ProgressBar.VISIBLE);
		gifka.setVisibility(View.INVISIBLE);
		break;
	case R.id.beef:
		lol=true;
        gifka.setVisibility(View.VISIBLE);
		image_action.setVisibility(ProgressBar.INVISIBLE);
		break;
}
	}



	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_UP: // отпускание
				if (lol==false) {
					image_action.setVisibility(ProgressBar.INVISIBLE);
				}
				if (lol==true) {
					gifka.setVisibility(View.INVISIBLE);
				}
				break;
			case MotionEvent.ACTION_DOWN:
				float x = event.getX();
				float y = event.getY();
				if (lol==false) {
				image_action.setX(x-200);
				image_action.setY(y-250);
				image_action.setVisibility(ProgressBar.VISIBLE);
				}
				if (lol==true) {
					gifka.setX(x-200);
					gifka.setY(y-250);
					gifka.setVisibility(GifImageView.VISIBLE);
				}
				break;

			case MotionEvent.ACTION_MOVE:
				if (lol==false) {
					image_action.setVisibility(ProgressBar.INVISIBLE);
				}
				if (lol==true) {
					gifka.setVisibility(GifImageView.INVISIBLE);
				}
				break;

		}
		return true;
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
