/*
* Author:
*   xaero xaero@exil.org
*
* Copyright (c) 2014, Puvo Productions http://puvoproductions.com/
*
* Permission to use, copy, modify, and distribute this software for any
* purpose with or without fee is hereby granted, provided that the above
* copyright notice and this permission notice appear in all copies.
*
* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
* WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
* MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
* ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
* WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
* ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
* OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/


package com.puvo.livewallpapers.puvowallpaperbase;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

public class PuvoWallpaperService extends PuvoGLWallpaperService
{
	private final static String LOG_TAG = "PuvoWallpaperService";
	protected PuvoWallpaperRenderer puvoWallpaperRenderer = null;

	@Override
	public GLSurfaceView.Renderer getNewRenderer()
	{
		return puvoWallpaperRenderer;
	}

	@Override
	public Engine onCreateEngine()
	{
		Log.e(LOG_TAG, "this is an empty function which has to be implemented by every wallpaper");
		return null;
	}

	public class PuvoWallpaperEngine extends PuvoGLEngine
			implements SensorEventListener, SharedPreferences.OnSharedPreferenceChangeListener
	{
		private final float PARALLAX_FACTOR_P = 1f / 20f;
		private final float PARALLAX_FACTOR_N = -1f / 20f;
		private final PuvoWallpaperRenderer puvoWallpaperRenderer;
		private final PuvoGestureDetector puvoGestureDetector;
		private boolean no_onOffsetChanged = true;
		private SensorManager mSensorManager = null;
		private Sensor gyroscopeSensor = null, accelSensor = null;
		private CountDownTimer sensorCountdown;
		private boolean isLandscape, isVisible, isUpside_down;
		private int get_rotation_counter = 0;
		private SavePreferencesCountdownTimer preferencesCountdown;

		public PuvoWallpaperEngine(PuvoWallpaperRenderer puvoWallpaperRenderer)
		{
			Context context = getBaseContext();
			isVisible = true;
			isLandscape = true;
			isUpside_down = false;
			get_rotation_counter = 0;

			this.puvoWallpaperRenderer = puvoWallpaperRenderer;
			this.puvoWallpaperRenderer.setContext(context);
			puvoGestureDetector = new PuvoGestureDetector(PuvoWallpaperService.this, puvoWallpaperRenderer);
			init();
		}

		private void init()
		{
			PreferenceManager.setDefaultValues(getBaseContext(), Defines.getResourceIDbyName("xml", "prefs"), false);
			SharedPreferences default_preferences = PreferenceManager.getDefaultSharedPreferences(PuvoWallpaperService.this);
			default_preferences.registerOnSharedPreferenceChangeListener(this);

			PackageManager packageManager = getPackageManager();
			PuvoPreferences.setParallaxEnabled(false);
			if (packageManager != null) {
				if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE)) {
					mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
					if (mSensorManager != null) {
						gyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
						PuvoPreferences.setParallaxEnabled(true);
					}
				}
				if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)) {
					if (mSensorManager == null) {
						mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
					}
					if (mSensorManager != null) {
						accelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
						PuvoPreferences.setInclinationEnabled(true);
					}
				}
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
				this.setOffsetNotificationsEnabled(true);
			}

			onSharedPreferenceChanged(default_preferences, null);
			PuvoPreferences.setNumberOfScreensEnabled(no_onOffsetChanged);
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder)
		{
			super.onCreate(surfaceHolder);

			if (!isPreview()) {
				// It seems to be impossible to set or get the offset of a wallpaper from within a service
				// (can't get the necessary IBinder)
				// So we cheat and call the home button to have at least a well defined starting point.
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getBaseContext().startActivity(intent);

				if (preferencesCountdown == null) {
					preferencesCountdown = new SavePreferencesCountdownTimer(60000, 60000);
					preferencesCountdown.start();
				}
			}
			if (sensorCountdown != null) {
				sensorCountdown.cancel();
				sensorCountdown = null;
			}
			_onSurfaceChanged();
		}

		@Override
		public void onDestroy()
		{
			if (preferencesCountdown != null) {
				puvoWallpaperRenderer.savePreferences(getSharedPreferences(getResources().getString(Defines.getResourceIDbyName("string", "extra_prefs_file")), MODE_PRIVATE));
				preferencesCountdown.cancel();
				preferencesCountdown = null;
			}
			if (sensorCountdown != null) {
				sensorCountdown.cancel();
				sensorCountdown = null;
			}
			switchSensor(null, false, false);
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
		{
			if (key == null || key.equals("inclination") || key.equals("parallax")) {
				switchSensor(sharedPreferences, isVisible(), true);
			}
			puvoWallpaperRenderer.onSharedPreferenceChanged(sharedPreferences, key);
		}

		@Override
		public void onVisibilityChanged(boolean visible)
		{
			super.onVisibilityChanged(visible);
			isVisible = visible;
			puvoWallpaperRenderer.setVisibility(visible);

			/* this CountDownTimer stuff look stupid but since the visibility also changes for a short period when
			 * rotating the screen in a 90 degree angle, we don't what the resulting flickering when switching the
			 * sensor listener off and on
			 */
			if (isVisible) {
				if (sensorCountdown != null) {
					sensorCountdown.cancel();
					sensorCountdown = null;
				}
				switchSensor(PreferenceManager.getDefaultSharedPreferences(PuvoWallpaperService.this), true, false);
			} else {
				if (sensorCountdown != null) {
					sensorCountdown.cancel();
					sensorCountdown = null;
				}

				sensorCountdown = new CountDownTimer(1000, 1000)
				{
					@Override
					public void onTick(long millisUntilFinished) { }

					@Override
					public void onFinish()
					{
						if (sensorCountdown != null) {
							switchSensor(null, false, false);
							sensorCountdown = null;
						}
					}
				}.start();
			}
		}

		@Override
		public void onOffsetsChanged(final float xOffset, final float yOffset, final float xOffsetStep,
		                             final float yOffsetStep, final int xPixelOffset, final int yPixelOffset)
		{
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
			if (isVisible) {
				if (no_onOffsetChanged) {
					if (xOffset != 0.0f && xOffset != 0.5f) {
						PuvoPreferences.setNumberOfScreensEnabled(false);
						no_onOffsetChanged = false;
						puvoWallpaperRenderer.setXOffset(xOffset);
					}
				} else {
					puvoWallpaperRenderer.setXOffset(xOffset);
				}
			}
		}

		@Override
		public Bundle onCommand(String action, int x, int y, int z,
		                        Bundle extras, boolean resultRequested)
		{
			if (action.equals(WallpaperManager.COMMAND_TAP)) {
				puvoWallpaperRenderer.triggerAction(x, y);
			}

			return null;
		}

		@Override
		public void onTouchEvent(MotionEvent event)
		{
			super.onTouchEvent(event);
			if (no_onOffsetChanged) {
				puvoGestureDetector.onTouchEvent(event);
			}
		}

		private void _onSurfaceChanged()
		{
			isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

			DisplayMetrics metrics = new DisplayMetrics();
			((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);

			if (isLandscape) {
				puvoWallpaperRenderer.setMetrics(1f / metrics.xdpi);
			} else {
				puvoWallpaperRenderer.setMetrics(1f / metrics.ydpi);
			}
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height)
		{
			super.onSurfaceChanged(holder, format, width, height);
			_onSurfaceChanged();
		}

		/////////////////////////////////////////////
		//             sensor interface            //
		/////////////////////////////////////////////
		@Override
		public void onSensorChanged(SensorEvent event)
		{
			if (get_rotation_counter == 0) {
				Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

				isUpside_down = display.getRotation() > Surface.ROTATION_90;

				_onSurfaceChanged();
				get_rotation_counter = 100;
			}
			get_rotation_counter--;
			switch (event.sensor.getType()) {
				case Sensor.TYPE_GYROSCOPE: {
					final int x_int = (int) (event.values[0] * 10);
					final int y_int = (int) (event.values[1] * 10);
					final float x, y;


					if (isLandscape) {
						if (isUpside_down) {
							x = x_int * PARALLAX_FACTOR_N;
							y = y_int * PARALLAX_FACTOR_N;
						} else {
							x = x_int * PARALLAX_FACTOR_P;
							y = y_int * PARALLAX_FACTOR_P;
						}
					} else {
						if (isUpside_down) {
							x = y_int * PARALLAX_FACTOR_N;
							y = x_int * PARALLAX_FACTOR_P;
						} else {
							x = y_int * PARALLAX_FACTOR_P;
							y = x_int * PARALLAX_FACTOR_N;
						}
					}
					puvoWallpaperRenderer.setParallaxX(x);
					puvoWallpaperRenderer.setParallaxY(y);

					break;
				}
				case Sensor.TYPE_ACCELEROMETER: {
					final float v;

					if (isLandscape) {
						if (isUpside_down) {
							v = event.values[1] * -2f;
						} else {
							v = event.values[1] * 2f;
						}
					} else {
						if (isUpside_down) {
							v = event.values[0] * 2f;
						} else {
							v = event.values[0] * -2f;
						}
					}

					puvoWallpaperRenderer.setInclination(v);

					break;
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy)
		{
		}

		private void switchSensor(SharedPreferences sharedPreferences, final boolean on, final boolean reset)
		{
			if (mSensorManager != null) {
				mSensorManager.unregisterListener(this);

				if (on) {
					if (sharedPreferences.getBoolean("inclination", false)) {
						mSensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_FASTEST);
					}

					if (sharedPreferences.getBoolean("parallax", false)) {
						mSensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
					} else {
						if (reset) {
							puvoWallpaperRenderer.resetParallax();
						}
					}
				} else {
					if (reset) {
						puvoWallpaperRenderer.resetParallax();
					}
				}
			}
		}

		class SavePreferencesCountdownTimer extends CountDownTimer
		{
			SavePreferencesCountdownTimer(long startTime, long interval)
			{
				super(startTime, interval);
			}

			@Override
			public void onTick(long millisUntilFinished) { }

			@Override
			public void onFinish()
			{
				if (isVisible) {
					puvoWallpaperRenderer.savePreferences(getSharedPreferences(getResources().getString(Defines.getResourceIDbyName("string", "extra_prefs_file")), MODE_PRIVATE));
				}
				preferencesCountdown.start();
			}
		}
	}
}

