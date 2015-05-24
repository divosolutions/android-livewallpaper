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

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Build;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public abstract class PuvoGLWallpaperService extends WallpaperService {
	@Override
	public Engine onCreateEngine()
	{
		return new PuvoGLEngine();
	}

	public abstract Renderer getNewRenderer();

	@SuppressWarnings("SameParameterValue")
	public class PuvoGLEngine extends Engine {
		private PuvoGLSurfaceView puvoGLSurfaceView;
		private boolean preserveContext = false;

		@Override
		public void onCreate(SurfaceHolder surfaceHolder)
		{
			super.onCreate(surfaceHolder);
			final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
			final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

			if (supportsEs2) {
				puvoGLSurfaceView = new PuvoGLSurfaceView(PuvoGLWallpaperService.this);

				setEGLContextClientVersion(2);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					setPreserveEGLContextOnPause(true);
					setEGConfigChooser();
				}
				setRenderer(getNewRenderer());
				setRenderMode();
			} else {
				throw new RuntimeException("system doesn't support OpenGL ES 2.0");
			}
		}

		@Override
		public void onVisibilityChanged(boolean visible)
		{
			if (preserveContext) {
				if (visible) {
					puvoGLSurfaceView.onResume();
				} else {
					puvoGLSurfaceView.onPause();
				}
			}
		}

		@Override
		public void onDestroy()
		{
			super.onDestroy();
			puvoGLSurfaceView.onDestroy();
		}

		void setRenderMode()
		{
			puvoGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		}

		void setRenderer(Renderer renderer)
		{
			puvoGLSurfaceView.setRenderer(renderer);
			preserveContext = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB);
		}

		void setEGLContextClientVersion(int version)
		{
			puvoGLSurfaceView.setEGLContextClientVersion(version);
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		void setPreserveEGLContextOnPause(boolean value)
		{
			puvoGLSurfaceView.setPreserveEGLContextOnPause(value);
		}

		void setEGConfigChooser()
		{
			puvoGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		}

		class PuvoGLSurfaceView extends GLSurfaceView {
			PuvoGLSurfaceView(Context context)
			{
				super(context);
			}

			@Override
			public SurfaceHolder getHolder()
			{
				return getSurfaceHolder();
			}

			public void onDestroy()
			{
				super.onDetachedFromWindow();
			}
		}
	}
}
