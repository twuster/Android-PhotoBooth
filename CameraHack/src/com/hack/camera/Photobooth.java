package com.hack.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.hack.camera.R.id;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.support.v4.app.NavUtils;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class Photobooth extends Activity implements SensorEventListener{

	public static final int MEDIA_TYPE_IMAGE = 1;
	private final static String DEBUG_TAG = "MakePhotoActivity";
	private Camera camera;
	private CameraPreview mPreview;
	private Camera.Parameters p;
	private int cameraId = -1;
	private final static int SENSITIVITY = 12;
	
	private boolean mInitialized;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mInitialized = false;
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		
		// Check for a front-facing camera.
		if (!getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
			.show();
		} else {
			camera = Camera.open(0);
		}
		mPreview = new CameraPreview(this, camera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
		p = camera.getParameters();
		if(p.getSupportedColorEffects()== null){
			System.out.println("NOOOOOOO");
		}
		else{
			System.out.println(p.getSupportedColorEffects());
		}
	}

	

	@Override
	protected void onPause() {
		if (camera != null) {
			camera.release();
			camera = null;
		}
		super.onPause();
	}
	
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void onCaptureClick(View view) {
		camera.takePicture(null, null,
				new PhotoHandler(getApplicationContext()));
	}
	
	public void onNoneClick(View view){
		p = camera.getParameters();
		p.setColorEffect("none");
		camera.setParameters(p);
	}
	
	public void onMonoClick(View view){
		p = camera.getParameters();
		p.setColorEffect("mono");
		camera.setParameters(p);
	}
	
	public void onSepiaClick(View view){
		p = camera.getParameters();
		p.setColorEffect("sepia");
		camera.setParameters(p);
	}
	public void onSolarizeClick(View view){
		p = camera.getParameters();
		p.setColorEffect("solarize");
		camera.setParameters(p);
	}
	public void onNegativeClick(View view){
		p = camera.getParameters();
		p.setColorEffect("negative");
		camera.setParameters(p);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if (!mInitialized) {
			mInitialized = true;
		} else {
			float[] gravity = new float[3];
			float[] linear_acceleration = new float[3];

			final float alpha = (float) 0.8;

			gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
			gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
			gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

			linear_acceleration[0] = event.values[0] - gravity[0];
			linear_acceleration[1] = event.values[1] - gravity[1];
			linear_acceleration[2] = event.values[2] - gravity[2];

			if(/*linear_acceleration[0] > SENSITIVITY || linear_acceleration[1] > SENSITIVITY ||*/ linear_acceleration[2] > SENSITIVITY && linear_acceleration[2]>0){
				System.out.println("FLIIIPP");
				int num = Camera.getNumberOfCameras();
				
				if (cameraId < 0) {
					camera.release();
					camera  = Camera.open(1);
					cameraId = 1;
					mPreview = new CameraPreview(this, camera);
					FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
					preview.removeAllViews();
					preview.addView(mPreview);
					camera.startPreview();
				}
				else if(cameraId >0){
					camera.release();
					camera = Camera.open(0);
					cameraId = -1;
					mPreview = new CameraPreview(this, camera);
					FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
					preview.removeAllViews();
					preview.addView(mPreview);
					camera.startPreview();
				}
			}
		}
		
	}
	
	@SuppressLint("NewApi")
	private int findFrontFacingCamera() {
		int cameraId = -1;
		// Search for the front facing camera
		int numberOfCameras = Camera.getNumberOfCameras();
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
				Log.d(DEBUG_TAG, "Camera found");
				cameraId = i;
				break;
			}
		}
		return cameraId;
	}
	

}
