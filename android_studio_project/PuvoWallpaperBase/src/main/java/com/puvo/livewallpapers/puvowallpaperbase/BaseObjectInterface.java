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


import android.content.SharedPreferences;
import android.graphics.PointF;

import javax.microedition.khronos.opengles.GL10;

@SuppressWarnings({"SameParameterValue", "EmptyMethod"})
public interface BaseObjectInterface
{
	void onDrawFrame(GL10 gl, final long now, final PointF baseTranslation, final float ratio, final PointF scale);

	public boolean touched(float x, float y);

	public boolean triggerAction();

	public void setNewPositionParallaxOffset(final float offset);

	public void setPreferences(SharedPreferences sharedPreferences, String key);

	public void setDirection(final float new_dir);

	public void addListener(BaseObjectInterface bo);

	public void sendCommand(final BaseObject.Command cmd, final int value);

	public void receiveCommand(final String remote_name, final BaseObject.Command cmd, final int value);

	public String getName();

	public void setMetrics(float value);

	public void savePreferences(SharedPreferences sharedPreferences);

	public void setColor(final PuvoColor c);

	public void setTime(final long millis, final int hour, final int minutes, final int seconds);

    public void onDestroy();
}
