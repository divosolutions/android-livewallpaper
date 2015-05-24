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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

public class Visit extends BaseObject
{
	private static final String LOG_TAG = "Visit";
	private final Intent intent;
	private boolean enabled = true;

	public Visit(final Context context, final int[] res, final int left, final int right,
				 final float virtual_scroll_speed_factor)
	{
		super(context, res, left, right, virtual_scroll_speed_factor);

		int index = Integer.parseInt(fullName.substring(fullName.indexOf("_") + 1));

		intent = new Intent(Intent.ACTION_VIEW, Uri.parse(context.getResources().getStringArray(Defines.getResourceIDbyName("array", "visit_url"))[index]));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	}

	@Override
	public void setPreferences(SharedPreferences sharedPreferences, String key)
	{
		if (key == null || key.equals(fullName + "_enabled") || key.equals(fullName + "_visible")) {
			enabled = sharedPreferences.getBoolean(fullName + "_enabled", true);
			draw = sharedPreferences.getBoolean(fullName + "_visible", true);
		}
	}

	@Override
	public boolean triggerAction()
	{
		super.triggerAction();
		if (draw && enabled && drawColor.o == 1f) {
			try {
				context.startActivity(intent);
			} catch (ActivityNotFoundException ignored) { }
			return true;
		}
		return false;
	}
}
