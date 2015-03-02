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


package com.puvo.livewallpapers.testwallpaper;

import android.service.wallpaper.WallpaperService;
import android.util.Log;

import com.puvo.livewallpapers.puvowallpaperbase.Defines;

public class PuvoWallpaperService extends com.puvo.livewallpapers.puvowallpaperbase.PuvoWallpaperService
{
	private final static String LOG_TAG = "PuvoWallpaperService";

	@Override
	public WallpaperService.Engine onCreateEngine()
	{
		Log.v(LOG_TAG, "package_name: " + getPackageName());
		Defines.packageName = getPackageName();
		puvoWallpaperRenderer = new PuvoWallpaperRenderer();
		return new PuvoWallpaperEngine(puvoWallpaperRenderer);
	}
}

