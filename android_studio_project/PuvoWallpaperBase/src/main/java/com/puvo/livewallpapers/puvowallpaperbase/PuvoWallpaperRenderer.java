// Author:
//   Thomas Siegmund Thomas.Siegmund@puvoproductions.com
//
// Copyright (c) 2015, Puvo Productions http://puvoproductions.com
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
//    * Redistributions of source code must retain the above copyright notice, this
//      list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice,
//      this list of conditions and the following disclaimer in the documentation
//      and/or other materials provided with the distribution.
//    * Neither the name of the [ORGANIZATION] nor the names of its contributors
//      may be used to endorse or promote products derived from this software
//      without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
// PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
// LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package com.puvo.livewallpapers.puvowallpaperbase;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PuvoWallpaperRenderer implements GLSurfaceView.Renderer, PuvoGLRenderer {
	private static final String LOG_TAG = "PuvoWallpaperRenderer";
	protected ArrayList<BaseObjectInterface> inclinationObjects = null;
	private BaseObjectInterface[] inclinationObjects_array = null;
	private BaseObjectInterface[][] layer_array = null;
	private PointF currentPositionSF[];
	private PointF currentParallaxSF[];
	private PointF currentRenderPosition[];
	protected Context context;
	private float scroll_speed_factor[], x_pixels, current_offset_x_position, scroll_factor, current_x_position;
	protected float virtual_scroll_speed_factor[][], scroll_response, current_ratio, max_offset;
	private float x_parallax = 0f, y_parallax = 0f, min_parallax = -5f, max_parallax = 5f;
	private float current_x_parallax = 0f, current_y_parallax = 0f;
	private boolean visible = true, create_done = false;
	private float x_offset;
	private int render_width;
	private int number_of_screens;
	private float pixelToInchesFactor = 0f;
	private final PointF defaultScale = new PointF(1f, 1f);
	private boolean apply_preferences = false;

	private int frameCounter = 300;

	private void setCurrentPositionSF()
	{
		for (int i = 0; i < currentPositionSF.length; i++) {
			currentPositionSF[i].x = current_x_position * scroll_speed_factor[i];
		}
	}

	private void setCurrentParallaxSF()
	{
		for (int i = 0; i < currentParallaxSF.length; i++) {
			currentParallaxSF[i].x = x_parallax * (virtual_scroll_speed_factor[i][1]);
			currentParallaxSF[i].y = y_parallax * (virtual_scroll_speed_factor[i][1]);
		}
		current_x_parallax = x_parallax;
		current_y_parallax = y_parallax;
	}

	private void setCurrentRenderPosition()
	{
		for (int i = 0; i < currentRenderPosition.length; i++) {
			currentRenderPosition[i].x = currentPositionSF[i].x + currentParallaxSF[i].x;
			currentRenderPosition[i].y = currentPositionSF[i].y + currentParallaxSF[i].y;
		}
	}

	private float[] calcSpeedFactor(final float background_width, final float background_offset)
	{
		final float tmp[] = new float[Defines.getNumberOfLayer()];
		final float background_offset_ratio = background_offset * current_ratio;

		for (int l = 0; l < Defines.getNumberOfLayer(); l++) {
			tmp[l] = ((virtual_scroll_speed_factor[l][1]) * background_width + background_offset) / background_offset_ratio;
		}
		return tmp;
	}

	/* Adapts the wallpaper to the new width and height
	 * It is always assumed that the background resource is twice as wide as high
	 */
	private void handleRatioChange(final int width, final int height)
	{
		WallpaperManager wm = WallpaperManager.getInstance(context);
		// width of the background as is has been created
		final float background_width = (float) context.getResources().getInteger(Defines.getResourceIDbyName("integer", "background_width"));
		// the amount of pixels which will be 'scrolled', 'width' pixels will always be visible
		max_offset = wm.getDesiredMinimumWidth() - width;
		// ratio between the width which will be used to display the wallpaper and the width of the width of the
		// background resource
		current_ratio = (float) (2 * height) / background_width;
		max_parallax = context.getResources().getInteger(Defines.getResourceIDbyName("integer", "parallax")) * current_ratio;
		min_parallax = -max_parallax;

		scroll_factor = (background_width * current_ratio - width) / max_offset;
		scroll_speed_factor = calcSpeedFactor(2 * height, max_offset * scroll_factor);

		setCurrentPositionSF();
		for (int l = 0; l < layer_array.length; l++) {
			final float offset = -(virtual_scroll_speed_factor[l][1]) * max_parallax;
			for (int i = 0; i < layer_array[l].length; i++) {
				layer_array[l][i].setNewPositionParallaxOffset(offset);
			}
		}
		resetParallax();
		render_width = width;
		setXOffset(x_offset);
		setMetrics(pixelToInchesFactor);
	}

	/* This function creates an array which holds for every layer the resources in the right order.
	 * structure: construct[layer][original_position][0=left, 1=right]
	 */
	private int[][][] createLayerConstruct()
	{
		final int number_of_layers = Defines.getNumberOfLayer();
		Hashtable<Integer, Hashtable<String, String>> resourceData = Defines.getResourceData();
		int[][][] construct = new int[number_of_layers][][];
		ArrayList<Hashtable<String, ArrayList<Integer>>> tmpVec = new ArrayList<>(number_of_layers);
		Hashtable<String, ArrayList<Integer>> byName;

		if (resourceData == null) {
			Log.e(LOG_TAG, "resourceData is null");
			return construct;
		}

		for (int l = 0; l < number_of_layers; l++) {
			tmpVec.add(l, new Hashtable<String, ArrayList<Integer>>());
		}

		for (int res : resourceData.keySet()) {
			String number = resourceData.get(res).get("number");
			byName = tmpVec.get(Integer.parseInt(resourceData.get(res).get("layer")));

			if (!byName.containsKey(number)) {
				byName.put(number, new ArrayList<Integer>());
			}

			byName.get(number).add(res);
		}

		for (int l = 0; l < number_of_layers; l++) {
			Hashtable<String, ArrayList<Integer>> layerContent = tmpVec.get(l);
			construct[l] = new int[layerContent.keySet().size()][];

			for (String number : layerContent.keySet()) {
				try {
					ArrayList<Integer> resources = layerContent.get(number);
					int pos = Integer.parseInt(resourceData.get(resources.get(0)).get("number"));
					construct[l][pos] = new int[resources.size()];

					for (int r = 0; r < resources.size(); ++r) {
						int res = resources.get(r);

						if (Integer.parseInt(resourceData.get(res).get("fps")) != 0) {
							construct[l][pos][0] = res;
						} else {
							int index = Integer.parseInt((resourceData.get(res).get("right")));
							construct[l][pos][index] = res;
						}
					}
				} catch (Exception e) {
					ArrayList<Integer> resources = layerContent.get(number);
					int pos = Integer.parseInt(resourceData.get(resources.get(0)).get("number"));
					Log.w(LOG_TAG, "number: " + number + ", pos: " + pos);
				}

			}
		}
		return construct;
	}

	public BaseObjectInterface initObject(Hashtable<String, String> rd, int[] res, int l)
	{
		return null;
	}

	/* initializes all necessary values and creates for every layer a list of renderable objects  */
	private void create()
	{
		int[][][] layer_construct = createLayerConstruct();
		String[] scrollSpeedStrings = new String[]{"1.0", "1.1", "1.2", "1.3"}, cd = new String[0];
		int resID;

		currentPositionSF = new PointF[layer_construct.length];
		currentParallaxSF = new PointF[layer_construct.length];
		currentRenderPosition = new PointF[layer_construct.length];
		scroll_response = 0.55f;

		resID = Defines.getResourceIDbyName("array", "virtual_scroll_speed_factor");
		if (resID != -1) {
			scrollSpeedStrings = context.getResources().getStringArray(resID);
		}
		// responsible for the moving speed of a layer in relation to the background (Disney pseudo 3D)
		virtual_scroll_speed_factor = new float[scrollSpeedStrings.length][2];

		for (int l = 0; l < scrollSpeedStrings.length; l++) {
			virtual_scroll_speed_factor[l][0] = Float.parseFloat(scrollSpeedStrings[l]);
			virtual_scroll_speed_factor[l][1] = virtual_scroll_speed_factor[l][0] - 1f;
		}
		// is responsible for how fast the background will adapt to the new x_pixels value, because onOffsetsChanged
		// (in PuvoWallpaperService) isn't called that often
		resID = Defines.getResourceIDbyName("string", "scroll_response");
		if (resID != -1) {
			scroll_response = Float.parseFloat(context.getResources().getString(resID));
		}

		Hashtable<Integer, BaseObjectInterface> all = new Hashtable<>();
		BaseObjectInterface bo;
		Hashtable<String, String> rd;
		Hashtable<Integer, Hashtable<String, String>> resourceData = Defines.getResourceData();

		if (resourceData == null) {
			Log.e(LOG_TAG, "resourceData is null");
		} else {
			inclinationObjects = new ArrayList<>();
			layer_array = new BaseObjectInterface[layer_construct.length][];
			for (int l = 0; l < layer_construct.length; l++) {
				currentPositionSF[l] = new PointF(0, 0);
				currentParallaxSF[l] = new PointF(0, 0);
				currentRenderPosition[l] = new PointF(0, 0);
				layer_array[l] = new BaseObjectInterface[layer_construct[l].length];
				for (int r = 0; r < layer_construct[l].length; r++) {
					rd = resourceData.get(layer_construct[l][r][0]);

					bo = initObject(rd, layer_construct[l][r], l);
					layer_array[l][r] = bo;
					if (bo != null) {
						all.put(Integer.parseInt(rd.get("id")), bo);
					} else {
						Log.e(LOG_TAG, "bo must not be null");
						throw new RuntimeException("unable to initialize resource " + rd.get("fullName"));
					}
				}
			}
			inclinationObjects_array = new BaseObjectInterface[inclinationObjects.size()];
			for (int i = 0; i < inclinationObjects.size(); i++) {
				inclinationObjects_array[i] = inclinationObjects.get(i);
			}
		}


		resID = Defines.getResourceIDbyName("array", "connections");
		if (resID != -1) {
			cd = context.getResources().getStringArray(resID);
		}

		for (int i = 0; i < cd.length; i++) {
			if (cd[i].equals("none")) {
				continue;
			}
			String[] signal_receiver = cd[i].split((" "));

			BaseObjectInterface sender = all.get(i);
			for (String id : signal_receiver) {
				sender.addListener(all.get(Integer.parseInt(id)));
			}
		}

		// set the wallpaper to the middle
		setXOffset(0.5f);
		// -1 to make the if condition in onDrawFrame for at least one time true
		current_offset_x_position = x_pixels - 1;
		onSharedPreferenceChanged(PreferenceManager.getDefaultSharedPreferences(context), null);

		String tmp = "extra_prefs";
		resID = Defines.getResourceIDbyName("string", "extra_prefs_file");
		if (resID != -1) {
			tmp = context.getResources().getString(resID);
		}
		SharedPreferences sp = context.getSharedPreferences(tmp, Context.MODE_PRIVATE);
		onSharedPreferenceChanged(sp, tmp);

		create_done = true;
	}

	public void triggerAction(final int x, final int y)
	{
		if (layer_array == null) {
			return;
		}
		final float x_ratio = x / current_ratio;
		final float y_ratio = y / current_ratio;

		for (int i = layer_array.length - 1; i >= 0; i--) {
			final float _x = x_ratio - currentRenderPosition[i].x;
			final float _y = y_ratio - currentRenderPosition[i].y;

			for (int j = layer_array[i].length - 1; j >= 0; j--) {
				final BaseObjectInterface bo = layer_array[i][j];
				if (bo.touched(_x, _y)) {
					if (bo.triggerAction()) {
						return;
					}
				}
			}
		}
	}

	public void setContext(Context value)
	{
		context = value;
	}

	public float getXOffset()
	{
		return x_offset;
	}

	public void setXOffset(final float value)
	{
		x_offset = value;
		if (x_offset < 0) {
			x_offset = 0;
		} else if (x_offset > 1) {
			x_offset = 1;
		}
		x_pixels = -x_offset * max_offset;
	}

	public void resetParallax()
	{
		x_parallax = y_parallax = current_x_parallax = current_y_parallax = 0;
		if (create_done) {
			setCurrentParallaxSF();
			setCurrentRenderPosition();
		}
	}

	public void setParallaxX(float x)
	{
		x_parallax += x;

		if (x_parallax < min_parallax) {
			x_parallax = min_parallax;
		} else if (x_parallax > max_parallax) {
			x_parallax = max_parallax;
		}
	}

	public void setParallaxY(float y)
	{
		y_parallax -= y;

		if (y_parallax < min_parallax) {
			y_parallax = min_parallax;
		} else if (y_parallax > max_parallax) {
			y_parallax = max_parallax;
		}
	}

	public void setInclination(final float value)
	{
		if (inclinationObjects_array == null) {
			return;
		}

		for (int i = 0; i < inclinationObjects_array.length; i++) {
			inclinationObjects_array[i].setDirection(value);
		}
	}

	public void setMetrics(float value)
	{
		pixelToInchesFactor = value;

		if (layer_array == null) {
			return;
		}

		value *= current_ratio;

		for (int i = layer_array.length - 1; i >= 0; i--) {
			for (int j = layer_array[i].length - 1; j >= 0; j--) {
				layer_array[i][j].setMetrics(value);
			}
		}
	}

	public void savePreferences(SharedPreferences sharedPreferences)
	{
		if (layer_array == null) {
			return;
		}

		for (int i = layer_array.length - 1; i >= 0; i--) {
			for (int j = layer_array[i].length - 1; j >= 0; j--) {
				layer_array[i][j].savePreferences(sharedPreferences);
			}
		}
	}

	public void setVisibility(boolean value)
	{
		visible = value;
	}

	public void onDestroy()
	{
		for (int i = layer_array.length - 1; i >= 0; i--) {
			for (int j = layer_array[i].length - 1; j >= 0; j--) {
				layer_array[i][j].onDestroy();
			}
		}
	}

	@Override
	public void onSurfaceCreated(GL10 gl_unused, EGLConfig config)
	{
		GLES20.glEnable(GLES20.GL_TEXTURE_2D);                // enable texture mapping
		GLES20.glClearColor(0.416f, 0.624f, 0.702f, 1.0f);
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		GLES20.glFrontFace(GLES20.GL_CCW);                    // Set the face rotation

		PuvoGLES20Tools.create_program();

		if (PuvoGLES20Tools.program_handle != 0) {
			GLES20.glUseProgram(PuvoGLES20Tools.program_handle);
		}

		// Set the camera position (View matrix)
		Matrix.setLookAtM(PuvoGLES20Tools.mView, 0, 0f, 0f, 1f, 0f, 0f, -1f, 0f, 1.0f, 0.0f);

		create();
	}

	@Override
	public void onSurfaceChanged(GL10 gl_unused, int width, int height)
	{
		if (height == 0) {
			height = 1;
		}
		// adapt the wallpaper to the new aspect ratio
		handleRatioChange(width, height);

		// adapt the OpenGL context
		GLES20.glViewport(0, 0, width, height);

		// Setup our screen width and height for normal sprite translation.
		Matrix.orthoM(PuvoGLES20Tools.mProjection, 0, 0, width, height, 0, -1f, 1f);

		// Calculate the projection and view transformation
		Matrix.multiplyMM(PuvoGLES20Tools.mProjectionAndView, 0, PuvoGLES20Tools.mProjection, 0, PuvoGLES20Tools.mView, 0);
	}

	@Override
	public void onDrawFrame(GL10 gl)
	{
		long now = System.currentTimeMillis();

		if (visible) {
			boolean changed = false;

			frameCounter++;
			if (frameCounter > 300) {
				frameCounter = 0;

				final int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
				final int minutes = Calendar.getInstance().get(Calendar.MINUTE);
				final int seconds = Calendar.getInstance().get(Calendar.SECOND);

				for (int i = 0; i < layer_array.length; i++) {
					for (int j = 0; j < layer_array[i].length; j++) {
						layer_array[i][j].setTime(now, hour, minutes, seconds);
					}
				}
			}

			if (x_pixels != current_offset_x_position) {
				// as long as after an offset change the wallpaper hasn't reached its destination the current x-original_position
				// has to be adapted
				current_offset_x_position = ((x_pixels - current_offset_x_position) * scroll_response) + current_offset_x_position;
				current_x_position = current_offset_x_position * scroll_factor;

				// the same for the positions of the layers
				setCurrentPositionSF();
				changed = true;
			}

			if (x_parallax != current_x_parallax || y_parallax != current_y_parallax) {
				setCurrentParallaxSF();
				changed = true;
			}

			if (changed) {
				setCurrentRenderPosition();
			}

			// clear everything
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
			GLES20.glUseProgram(PuvoGLES20Tools.program_handle);

			// Enable generic vertex attribute array
			GLES20.glEnableVertexAttribArray(PuvoGLES20Tools.texture_coordinates_handle);
			// Enable generic vertex attribute array
			GLES20.glEnableVertexAttribArray(PuvoGLES20Tools.position_handle);

			int layer_index = 0;

			// draw the objects
			for (int i = 0; i < layer_array.length; i++) {
				PointF p = currentRenderPosition[i];

				for (int j = 0; j < layer_array[i].length; j++) {
					layer_array[i][j].onDrawFrame(now, p, current_ratio, defaultScale);
				}
			}

			// Disable vertex array
			GLES20.glDisableVertexAttribArray(PuvoGLES20Tools.position_handle);
			GLES20.glDisableVertexAttribArray(PuvoGLES20Tools.texture_coordinates_handle);

			// sleep for a while to get approximately 30 fps
			now = System.currentTimeMillis() - now;
			if (now < 33) {
				// for 30 fps we have to wait 33.33 ms
				try {
					Thread.sleep(33 - now);
				} catch (InterruptedException ignored) {
				}
			}
		} else {
			frameCounter = 300;
		}
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (apply_preferences) {
			return;
		}
		apply_preferences = true;
		number_of_screens = PuvoPreferences.getIntPreferenceByName(sharedPreferences, "number_of_screens", 5, 1, 20) - 1;

		if (layer_array == null) {
			apply_preferences = false;
			return;
		}

		for (int i = 0; i < layer_array.length; i++) {
			for (int j = 0; j < layer_array[i].length; j++) {
				layer_array[i][j].setPreferences(sharedPreferences, key);
			}
		}
		apply_preferences = false;
	}

	@Override
	public boolean isVisible()
	{
		return visible;
	}

	@Override
	public int getRenderWidth()
	{
		return render_width;
	}

	@Override
	public int getNumberOfScreens()
	{
		return number_of_screens;
	}
}
