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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PointF;

@SuppressWarnings("ConstantConditions")
public class Counter extends BaseObject
{
	private static final String LOG_TAG = "Counter";

	public Counter(Context context, int[] res, int left_border, int right_border, float virtual_scroll_speed_factor)
	{
		super(context, res, left_border, right_border, virtual_scroll_speed_factor);

		manualAnimation(true);
		objectSprite.switchToFrame(0);
	}

	@Override
	public boolean triggerAction() { return false; }

	@Override
	public void receiveCommand(final String name, final BaseObject.Command cmd, final int value)
	{
		if (cmd == Command.COUNTER) {
			objectSprite.switchToFrame(value % 10);
			sendCommand(Command.COUNTER, value / 10);
		}
	}

	@Override
	public void setPreferences(SharedPreferences sharedPreferences, String key)
	{
		if (key == null || key.equals("counter")) {
			draw = sharedPreferences.getBoolean("counter", true);
		}
	}

	@Override
	public void onDrawFrame(final long now, PointF baseTranslation, final float ratio, final PointF scale)
	{
		super.onDrawFrame(now, zeroPoint, ratio, scale);
	}
}
