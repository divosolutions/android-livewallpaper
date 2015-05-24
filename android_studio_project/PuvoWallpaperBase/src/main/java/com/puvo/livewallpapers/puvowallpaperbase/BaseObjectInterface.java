// Author:
//   xaero puvo-productions@freenet.de
//
// Copyright (c) 2014, Puvo Productions http://puvoproductions.com/
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


import android.content.SharedPreferences;
import android.graphics.PointF;

@SuppressWarnings({"SameParameterValue", "EmptyMethod"})
public interface BaseObjectInterface
{
	void onDrawFrame(final long now, final PointF baseTranslation, final float ratio, final PointF scale);

	boolean touched(float x, float y);

	boolean triggerAction();

	void setNewPositionParallaxOffset(final float offset);

	void setPreferences(SharedPreferences sharedPreferences, String key);

	void setDirection(final float new_dir);

	void addListener(BaseObjectInterface bo);

	void sendCommand(final BaseObject.Command cmd, final int value);

	void receiveCommand(final String remote_name, final BaseObject.Command cmd, final int value);

	String getName();

	void setMetrics(float value);

	void savePreferences(SharedPreferences sharedPreferences);

	void setColor(final PuvoColor c);

	void setTime(final long millis, final int hour, final int minutes, final int seconds);

	void onDestroy();
}
