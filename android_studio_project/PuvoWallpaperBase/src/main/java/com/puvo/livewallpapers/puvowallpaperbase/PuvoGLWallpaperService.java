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

import android.annotation.TargetApi;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Build;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public abstract class PuvoGLWallpaperService extends WallpaperService
{
	@Override
	public Engine onCreateEngine()
	{
		return new PuvoGLEngine();
	}

	public abstract Renderer getNewRenderer();

	@SuppressWarnings("SameParameterValue")
	public class PuvoGLEngine extends Engine
	{
		private PuvoGLSurfaceView puvoGLSurfaceView;
		private boolean preserveContext = false;

		@Override
		public void onCreate(SurfaceHolder surfaceHolder)
		{
			super.onCreate(surfaceHolder);
			puvoGLSurfaceView = new PuvoGLSurfaceView(PuvoGLWallpaperService.this);

			setEGLContextClientVersion(2);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				setPreserveEGLContextOnPause(true);
				setEGConfigChooser();
			}
			setRenderer(getNewRenderer());
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

		class PuvoGLSurfaceView extends GLSurfaceView
		{
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
