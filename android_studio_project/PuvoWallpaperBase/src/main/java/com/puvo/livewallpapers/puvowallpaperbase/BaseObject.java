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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.CountDownTimer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Hashtable;

@SuppressWarnings({"SameParameterValue", "CanBeFinal", "WeakerAccess"})
public class BaseObject implements BaseObjectInterface {
	private static final String LOG_TAG = "BaseObject";

	// trigger commands
	protected enum Command {
		NONE,
		OPACITY_CHANGE,
		MOVE,
		SET,
		DELETE,
		COUNTER,
		TAPPED,
		VISIBLE,
	}

	protected final String fullName;
	protected final int left_border, right_border;
	protected final ObjectSprite objectSprite;
	protected final PointF original_position, position;
	protected final Point lastTouch;
	protected float dir;
	protected float rotation;
	protected final float draw_width, draw_height;
	final ArrayList<BaseObjectInterface> listener;
	protected final Context context;
	protected CountDownTimer opacityCountdownUser;
	protected OpacityCountdownTimer opacityCountdown;

	protected int hour, minutes, seconds;
	protected long millis;
	protected int direction;
	protected PuvoColor color, drawColor;
	protected boolean draw, move_it;
	protected final PointF zeroPoint = new PointF(0f, 0f);

	public BaseObject(final Context context, final int[] res, final int left_border, final int right_border,
					  final float virtual_scroll_speed_factor)
	{
		this.context = context;
		Hashtable<Integer, Hashtable<String, String>> resourceData = Defines.getResourceData();

		if (resourceData == null) {
			Log.e(LOG_TAG, "resourceData is null");
			fullName = "no resourceData";
			this.left_border = this.right_border = 0;
			objectSprite = null;
			original_position = new PointF();
			position = new PointF();
			lastTouch = new Point();
			draw_width = draw_height = 0f;
			listener = new ArrayList<>();
			return;
		}


		fullName = resourceData.get(res[0]).get("fullName");

		final int fps = Integer.parseInt(resourceData.get(res[0]).get("fps"));
		original_position = new PointF(Float.parseFloat(resourceData.get(res[0]).get("posX")), Float.parseFloat(resourceData.get(res[0]).get("posY")));
		position = new PointF();
		position.set(original_position);
		lastTouch = new Point();

		objectSprite = new ObjectSprite(res, fps, context);

		this.left_border = (int) (left_border * virtual_scroll_speed_factor);
		this.right_border = (int) (right_border * virtual_scroll_speed_factor);
		dir = 0f;
		draw_width = objectSprite.getWidth();
		draw_height = objectSprite.getHeight();
		listener = new ArrayList<>();

		color = new PuvoColor(1f, 1f, 1f, 1f);
		drawColor = new PuvoColor();
		drawColor.set(color);
		direction = 1;
		rotation = 0;
		move_it = false;
		draw = true;
	}

	public void setNewPositionParallaxOffset(final float offset)
	{
		position.set(original_position);
		position.offset(offset, offset);
	}

	public void setDirection(final float value)
	{
		final int new_direction = Defines.signum(value);

		if (new_direction == direction) {
			return;
		}

		if (new_direction < 0f) {
			position.x += draw_width;
			original_position.x += draw_width;
		} else {
			position.x -= draw_width;
			original_position.x -= draw_width;
		}
		direction = new_direction;
	}

	public void setPreferences(SharedPreferences sharedPreferences, String key)
	{
	}

	public void addListener(BaseObjectInterface bo)
	{
		if (bo != null) {
			listener.add(bo);
		}
	}

	public void sendCommand(final Command cmd, final int value)
	{
		for (BaseObjectInterface bo : listener) {
			bo.receiveCommand(fullName, cmd, value);
		}
	}

	public void setMetrics(float value)
	{
	}

	public void savePreferences(SharedPreferences sharedPreferences)
	{
	}

	public boolean triggerAction()
	{
		final int value = ((0xFFFF & (int) (position.x + draw_width / 2f)) << 16) | (0xFFFF & (int) (position.y + draw_height / 2f));
		sendCommand(Command.MOVE, value);
		return false;
	}

	public void receiveCommand(final String remote_name, final Command cmd, final int value)
	{
		if (cmd == Command.OPACITY_CHANGE) {
			if (opacityCountdown == null) {
				opacityCountdown = new OpacityCountdownTimer(5000, 10);
			}
			if (opacityCountdownUser != null) {
				opacityCountdown.cancel();
				opacityCountdown.skipStart = true;
			}
			opacityCountdownUser = opacityCountdown;
			opacityCountdownUser.start();
		}
	}

	public String getName()
	{
		return fullName;
	}

	public void setColor(final PuvoColor c)
	{
		color.set(c);
		if (opacityCountdownUser == null) {
			drawColor.set(color);
		}
	}

	public void setTime(final long millis, final int hour, final int minutes, final int seconds)
	{
		this.millis = millis;
		this.hour = hour;
		this.minutes = minutes;
		this.seconds = seconds;
	}

	protected void move(final long now, final int value)
	{
	}

	public void onDrawFrame(final long now, PointF baseTranslation, final float ratio, final PointF scale)
	{
		if (objectSprite != null && draw && drawColor.o != 0f) {
			objectSprite.onDrawFrame(
										now,
										position.x + baseTranslation.x,
										position.y + baseTranslation.y,
										ratio,
										scale.x,
										scale.y,
										rotation,
										drawColor,
										direction);
			if (move_it) {
				move(now, 0);
			}
		}
	}

	public boolean touched(final float x, final float y)
	{
		lastTouch.set((int) x, (int) y);
		if (direction > 0f) {
			return !((position.x > x) || (position.x + draw_width < x)) && !((position.y > y) || (position.y + draw_height < y));
		} else {
			return !((position.x < x) || (position.x - draw_width > x)) && !((position.y > y) || (position.y + draw_height < y));
		}
	}

	public void setFrameDuration(final int duration)
	{
		if (objectSprite != null) {
			objectSprite.setFrameDuration(duration);
		}
	}

	public void setFrameRange(final int start, final int end)
	{
		if (objectSprite != null) {
			objectSprite.setFrameRange(start, end);
		}
	}

	public void manualAnimation(final boolean value)
	{
		if (objectSprite != null) {
			objectSprite.manualAnimation(value);
		}
	}

	public void toggleAnimationRun(final boolean now)
	{
		if (objectSprite != null) {
			objectSprite.toggleAnimationRun(now);
		}
	}

	public void switchToNextFrame()
	{
		if (objectSprite != null) {
			objectSprite.switchToNextFrame();
		}
	}

	public void switchToFrame(final int f)
	{
		if (objectSprite != null) {
			objectSprite.switchToFrame(f);
		}
	}

	public void onDestroy()
	{
	}

	protected class OpacityCountdownTimer extends CountDownTimer {
		static final long BLEND_DELTA = 500;
		long startTime;
		public boolean skipStart = false;
		public boolean fadeIn = false;
		private boolean dest = false;

		public OpacityCountdownTimer(long startTime, long interval)
		{
			super(startTime, interval);
			this.startTime = startTime;
		}

		@Override
		public void onTick(long millisUntilFinished)
		{
			long delta = startTime - millisUntilFinished;
			if (fadeIn) {
				if (delta < OpacityCountdownTimer.BLEND_DELTA && !skipStart) {
					drawColor.set(color);
					drawColor.multiply((float) delta / (float) OpacityCountdownTimer.BLEND_DELTA);
					dest = false;
				} else if (millisUntilFinished < OpacityCountdownTimer.BLEND_DELTA) {
					drawColor.set(color);
					drawColor.multiply((float) millisUntilFinished / (float) OpacityCountdownTimer.BLEND_DELTA);
					dest = false;
				} else {
					if (!dest) {
						drawColor.set(color);
						dest = true;
					}
				}
			} else {
				if (delta < OpacityCountdownTimer.BLEND_DELTA && !skipStart) {
					drawColor.set(color);
					drawColor.multiply(1f - (float) delta / (float) OpacityCountdownTimer.BLEND_DELTA);
					dest = false;
				} else if (millisUntilFinished < OpacityCountdownTimer.BLEND_DELTA) {
					drawColor.set(color);
					drawColor.multiply(1f - (float) millisUntilFinished / (float) OpacityCountdownTimer.BLEND_DELTA);
					dest = false;
				} else {
					if (!dest) {
						drawColor.set(0f, 0f);
						dest = true;
					}
				}
			}
		}

		@Override
		public void onFinish()
		{
			if (fadeIn) {
				drawColor.set(0f, 0f);
			} else {
				drawColor.set(color);
			}
			skipStart = false;
			opacityCountdownUser = null;
		}
	}
}
