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

import android.content.Context;
import android.content.SharedPreferences;

import javax.microedition.khronos.opengles.GL10;

public class OnOff extends BaseObject
{
	private static final String LOG_TAG = "OnOff";

	public OnOff(GL10 gl, Context context, int[] res, int left_border, int right_border, float virtual_scroll_speed_factor)
	{
		super(gl, context, res, left_border, right_border, virtual_scroll_speed_factor);
	}

	@Override
	public void setPreferences(SharedPreferences sharedPreferences, String key)
	{
		if (key == null || key.equals(fullName)) {
			draw = sharedPreferences.getBoolean(fullName, false);
		}
	}
}
