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
import android.os.Build;
import android.os.CountDownTimer;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class PuvoGestureDetector implements View.OnTouchListener
{
	static final String LOG_TAG = "PuvoGestureDetector";
	private final Object swipeGuard = new Object();
	private final GestureDetector fling_detector;
	private final int max_offset_path, min_swipe_distance;
	private final float swipe_velocity_threshold;
	private CountDownTimer swipeTimer;
	private final PuvoGLRenderer puvoRenderer;
	private float swipeStartXOffset = -1, down_x;

	public PuvoGestureDetector(Context context, PuvoGLRenderer renderer)
	{
		float density = context.getResources().getDisplayMetrics().density;
		ViewConfiguration view_conf = ViewConfiguration.get(context);

		swipe_velocity_threshold = view_conf.getScaledMinimumFlingVelocity();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			min_swipe_distance = (int) (view_conf.getScaledPagingTouchSlop() * density);
		} else {
			min_swipe_distance = (int) (view_conf.getScaledTouchSlop() * 2 * density);
		}

		max_offset_path = min_swipe_distance * 2;
		fling_detector = new GestureDetector(context, new FlingGestureDetector());
		puvoRenderer = renderer;
	}

	private void moveOffset(final float fromOffset, final float toOffset)
	{
		// Good default time: 280ms
		if (swipeTimer != null) {
			swipeTimer.cancel();
		}
		if (puvoRenderer.isVisible()) {
			final long moveTime = (long) (Math.abs(fromOffset - toOffset) * 1300);
			final float toOffsetSafe = toOffset < 0 ? 0 : (toOffset > 1 ? 1 : toOffset);
			swipeTimer = new CountDownTimer(moveTime, 20)
			{
				@Override
				public void onTick(long millisUntilFinished)
				{
					if (puvoRenderer.isVisible()) {
						float fractionTodo = (float) millisUntilFinished / (float) moveTime;
						puvoRenderer.setXOffset(fractionTodo * (fromOffset - toOffsetSafe) + toOffsetSafe);
					}
				}

				@Override
				public void onFinish()
				{
					if (puvoRenderer.isVisible()) {
						puvoRenderer.setXOffset(toOffsetSafe);
					}
				}
			}.start();
		}
	}

	private float calcOffsetAdvance()
	{
		return 1f / (float) puvoRenderer.getNumberOfScreens();
	}

	private void handleOnSwipe(boolean left_to_right)
	{
		if (puvoRenderer.isVisible()) {
			final float fromXOffset = puvoRenderer.getXOffset();
			final float toXOffset = swipeStartXOffset < 0 ? fromXOffset : swipeStartXOffset;
			moveOffset(fromXOffset, toXOffset + calcOffsetAdvance() * (left_to_right ? -1 : 1));
		}
	}

	void onHorizontalSwipe(boolean left_to_right)
	{
		if (swipeTimer != null) {
			swipeTimer.cancel();
		}
		synchronized (swipeGuard) {
			handleOnSwipe(left_to_right);
			swipeStartXOffset = -1;
		}
	}

	void onHorizontalMove(float horizontalMove)
	{
		if (swipeTimer != null) {
			swipeTimer.cancel();
		}

		synchronized (swipeGuard) {
			if (puvoRenderer.isVisible()) {
				if (swipeStartXOffset < 0) {
					swipeStartXOffset = puvoRenderer.getXOffset();
				}
				puvoRenderer.setXOffset(horizontalMove / (float) puvoRenderer.getRenderWidth() * calcOffsetAdvance() + swipeStartXOffset);
			}
		}
	}

	void onCanceledHorizontalMove()
	{
		if (swipeTimer != null) {
			swipeTimer.cancel();
		}

		synchronized (swipeGuard) {
			if (puvoRenderer.isVisible() && swipeStartXOffset >= 0) {
				final float act = puvoRenderer.getXOffset();
				// Move back.
				if (act != swipeStartXOffset) {
					moveOffset(act, swipeStartXOffset);
				}
			}
			// Reset swipe move motion.
			swipeStartXOffset = -1;
		}
	}

	public float getOffsetDelta()
	{
		return swipeStartXOffset - puvoRenderer.getXOffset();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		return onTouchEvent(event);
	}

	public boolean onTouchEvent(MotionEvent event)
	{
		if (event == null) { return false; }

		if (fling_detector.onTouchEvent(event)) { return true; }

		switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
				onHorizontalMove(down_x - event.getX());

				break;
			case MotionEvent.ACTION_DOWN:
				down_x = event.getX();
				break;
			case MotionEvent.ACTION_UP:
				final float delta_x = down_x - event.getX();

				// This is done in FlingGestureDetector now but since the onFling
				// thing seems to be not 100% reliable, we do the traditional
				// version here, again. Just in case.
				// swipe horizontal?
				if (Math.abs(delta_x) > min_swipe_distance * 5) {
					// x5 because of missing velocity check here!
					// left or right
					onHorizontalSwipe(delta_x < 0);
					return true;
				}

				// No horizontal swipe found (otherwise we would already have returned above), cancel.
				onCanceledHorizontalMove();
				break;
		}

		// No swipe found, so we do not consume the event.
		return false;
	}

	private class FlingGestureDetector extends GestureDetector.SimpleOnGestureListener
	{
		@Override
		public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY)
		{
			final float x1 = e1.getX();
			final float x2 = e2.getX();
			final float y1 = e1.getY();
			final float y2 = e2.getY();

			try {
				if (Math.abs(y1 - y2) > max_offset_path) {
					return false;
				}
				final float diff = x1 - x2;

				if (Math.abs(velocityX) > swipe_velocity_threshold && Math.abs(diff) > min_swipe_distance) {
					onHorizontalSwipe(diff < 0);
					return true;
				}
			} catch (Exception e) {
				// nothing
			}
			return false;
		}
	}
}
