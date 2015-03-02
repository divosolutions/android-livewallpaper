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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import javax.microedition.khronos.opengles.GL10;

public class Settings extends BaseObject
{
	private final Intent intent;
	private boolean use_settings;

	public Settings(GL10 gl, final Context context, final int[] res, final int left, final int right,
					final float virtual_scroll_speed_factor)
	{
		super(gl, context, res, left, right, virtual_scroll_speed_factor);
		intent = new Intent(context, PuvoPreferences.class);
		intent.addCategory(Intent.CATEGORY_PREFERENCE);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		use_settings = true;
	}

	@Override
	public void setPreferences(SharedPreferences sharedPreferences, String key)
	{
		if (key == null || key.equals(fullName)) {
			use_settings = sharedPreferences.getBoolean(fullName, true);
		}
	}

	@Override
	public boolean triggerAction()
	{
		super.triggerAction();
		if (draw && use_settings && drawColor.o == 1f) {
			try {
				context.startActivity(intent);
			} catch (ActivityNotFoundException ignored) { }

			return true;
		}
		return false;
	}
}
