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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;

import com.puvo.livewallpapers.puvowallpaperbase.BaseObject;
import com.puvo.livewallpapers.puvowallpaperbase.Defines;

import javax.microedition.khronos.opengles.GL10;

class Character extends BaseObject
{
	private enum State
	{
		IDLE1,
		IDLE2,
		TAPPED,
		SLIDE1_LEFT,
		SLIDE1_RIGHT,
		SLIDE2_LEFT,
		SLIDE2_RIGHT,
	}

	private static final String LOG_TAG = "Character";
	private final static float MAX_SPEED = 35.0f;

	private CountDownTimer countdown;
	private IdleToSlide2CountdownTimer idleToSlide2Countdown;
	private Idle1ToIdle2CountdownTimer idle1ToIdle2Countdown;
	private TappedToIdle1CountdownTimer tappedToIdle1Countdown;

	private State state;
	private boolean tapped_override, character_tapable, inclination;
	private long sprint_millis, update_millis;

	private float pixelToInchesFactor;
	private float traveledInches;
	private float conversionFactor = 2.54f / 100f;

	private float traveledDistance;
	private int traveledDistanceInt;

	public Character(GL10 gl, final Context context, final int[] res,
					 final int left, final int right, final float virtual_scroll_speed_factor)
	{
		super(gl, context, res, left, right, virtual_scroll_speed_factor);
		state = State.TAPPED;
		tapped_override = false;
		inclination = true;
		character_tapable = true;
		update_millis = sprint_millis = System.currentTimeMillis();
		pixelToInchesFactor = 1.0f;
		traveledInches = 0f;
		traveledDistance = 0f;
		traveledDistanceInt = 0;

		manualAnimation(true);
		switchToFrame(state.ordinal());
		move_it = true;
	}

	@Override
	public void setPreferences(SharedPreferences sharedPreferences, String key)
	{
		if (key == null || key.equals(fullName + "_tappable")) {
			character_tapable = sharedPreferences.getBoolean(fullName + "_tappable", true);
		}

		if (key == null || key.equals("inclination")) {
			inclination = sharedPreferences.getBoolean("inclination", true);
			if (inclination) {
				if (countdown != null) {
					countdown.start();
				}
			} else {
				if (countdown != null) {
					countdown.cancel();
				}
				state = State.IDLE2;
				switchToFrame(state.ordinal());
			}
		}

		if (key == null || key.equals("switch_yd")) {
			conversionFactor = sharedPreferences.getBoolean("switch_yd", false) ? 1 / 36f : 2.54f / 100f;
		}

		if (key != null && key.equals(context.getResources().getString(R.string.extra_prefs_file))) {
			traveledInches = sharedPreferences.getFloat(fullName, 0f);

			traveledDistance = traveledInches * conversionFactor;
			traveledDistanceInt = (int) traveledDistance;
			sendCommand(Command.COUNTER, traveledDistanceInt);
		}
	}

	private State handleIdleState(final State new_state)
	{
		/* state != IDLE1 && state != IDLE2 => state > IDLE2 */
		if (state.ordinal() > State.IDLE2.ordinal()) {
			if (inclination) {
				if (countdown != null) {
					countdown.cancel();
				}
				if (idle1ToIdle2Countdown == null) {
					idle1ToIdle2Countdown = new Idle1ToIdle2CountdownTimer(2000, 2000);
				}
				countdown = idle1ToIdle2Countdown;
				countdown.start();
			}
			return new_state;
		}
		return state;
	}

	private State handleState(final float new_dir, final float old_dir, final State states[])
	{
		State ret_state = state;

		if (dir < -2.0) {
			ret_state = states[1];
			if (countdown != null) {
				countdown.cancel();
				countdown = null;
			}
		} else if (dir > 2.0) {
			ret_state = states[2];
			if (countdown != null) {
				countdown.cancel();
				countdown = null;
			}
		} else if (Math.abs(new_dir) <= 1.0) {
			ret_state = handleIdleState(states[0]);
			dir = old_dir;
		}
		return ret_state;
	}

	@Override
	public void setMetrics(float value)
	{
		pixelToInchesFactor = value;
	}

	@Override
	public void savePreferences(SharedPreferences sharedPreferences)
	{
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putFloat(fullName, traveledInches);
		editor.commit();
	}

	@Override
	public void setDirection(final float new_dir)
	{
		if (!tapped_override) {
			State new_state;
			State states[];
			final float old_dir = dir;
			dir += 0.05f * new_dir;

			switch (state) {
				case SLIDE2_LEFT:
				case SLIDE2_RIGHT:
					states = new State[]{State.IDLE2, State.SLIDE2_LEFT, State.SLIDE2_RIGHT};
					new_state = handleState(new_dir, old_dir, states);
					break;
				default:
					states = new State[]{State.IDLE1, State.SLIDE1_LEFT, State.SLIDE1_RIGHT};
					new_state = handleState(new_dir, old_dir, states);
					break;
			}

			if (dir > MAX_SPEED) {
				dir = MAX_SPEED;
			} else if (dir < -MAX_SPEED) {
				dir = -MAX_SPEED;
			}
			if (new_state != state) {
				state = new_state;
				switchToFrame(state.ordinal());
			}
		}
	}

	@Override
	public boolean triggerAction()
	{
		if (character_tapable) {
			tapped_override = true;
			switchToFrame(State.TAPPED.ordinal());

			if (countdown != null) {
				countdown.cancel();
			}

			if (tappedToIdle1Countdown == null) {
				tappedToIdle1Countdown = new TappedToIdle1CountdownTimer(2000, 2000);
			}
			countdown = tappedToIdle1Countdown;
			countdown.start();

			return true;
		}
		return false;
	}

	private void handleBorder(final State special_state, final int opposite_border)
	{
		if (inclination) {
			if (Math.random() < 0.4) {
				state = special_state;
			} else {
				state = State.IDLE2;
			}
			switchToFrame(state.ordinal());
		}

		original_position.x = position.x = opposite_border;
	}

	@Override
	protected void move(final long now, final int value)
	{
		if (!inclination) {
			if (now - sprint_millis > 20000) {
				if (Defines.signum(0.5f - (float) Math.random()) > 0) {
					dir = MAX_SPEED;
					state = State.SLIDE2_RIGHT;
				} else {
					dir = -MAX_SPEED;
					state = State.SLIDE2_LEFT;
				}
				switchToFrame(state.ordinal());
				sprint_millis = now;
			}
			if (now - update_millis > 100) {
				setDirection(0);
			}
		}

		dir *= 0.95;

		position.x += dir;
		original_position.x += dir;
		traveledInches += Math.abs(dir) * pixelToInchesFactor;
		traveledDistance = traveledInches * conversionFactor;
		if (traveledDistanceInt != (int) traveledDistance) {
			traveledDistanceInt = (int) traveledDistance;
			sendCommand(Command.COUNTER, traveledDistanceInt);
		}

		if (position.x < left_border) {
			handleBorder(State.SLIDE2_LEFT, right_border);
		} else if (position.x > right_border) {
			handleBorder(State.SLIDE2_RIGHT, left_border);
		}
	}

	class IdleToSlide2CountdownTimer extends CountDownTimer
	{
		IdleToSlide2CountdownTimer(long startTime, long interval)
		{
			super(startTime, interval);
		}

		@Override
		public void onTick(long millisUntilFinished) { }

		@Override
		public void onFinish()
		{
			if (state == State.IDLE2) {
				if (Defines.signum(0.5f - (float) Math.random()) > 0) {
					dir = MAX_SPEED;
					state = State.SLIDE2_RIGHT;
				} else {
					dir = -MAX_SPEED;
					state = State.SLIDE2_LEFT;
				}
				switchToFrame(state.ordinal());
			}
		}
	}

	class Idle1ToIdle2CountdownTimer extends CountDownTimer
	{
		Idle1ToIdle2CountdownTimer(long startTime, long interval)
		{
			super(startTime, interval);
		}

		@Override
		public void onTick(long millisUntilFinished) { }

		@Override
		public void onFinish()
		{
			if (state == State.IDLE1) {
				state = State.IDLE2;
			}
			switchToFrame(state.ordinal());
			if (inclination) {
				if (idleToSlide2Countdown == null) {
					idleToSlide2Countdown = new IdleToSlide2CountdownTimer(20000, 20000);
				}
				countdown = idleToSlide2Countdown;
				countdown.start();
			}
		}
	}

	class TappedToIdle1CountdownTimer extends CountDownTimer
	{
		TappedToIdle1CountdownTimer(long startTime, long interval)
		{
			super(startTime, interval);
		}

		@Override
		public void onTick(long millisUntilFinished) { }

		@Override
		public void onFinish()
		{
			tapped_override = false;
			state = State.IDLE1;
			switchToFrame(state.ordinal());

			if (idle1ToIdle2Countdown == null) {
				idle1ToIdle2Countdown = new Idle1ToIdle2CountdownTimer(2000, 2000);
			}
			countdown = idle1ToIdle2Countdown;
			countdown.start();
		}
	}
}
