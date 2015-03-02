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

import javax.microedition.khronos.opengles.GL10;

public class ObjectSprite
{
	private static final String LOG_TAG = "ObjectSprite";
	private final PuvoTexture spriteTexture;
	private final int frameCount;
	private final float spriteWidth, spriteHeight;
	private int currentFrame, frameDuration, startFrame, endFrame;
	private long timer;
	private boolean run, toggleRun, manualRun;
	private final String name;

	public ObjectSprite(GL10 gl, final int[] res, final int fps, final Context context)
	{
		timer = 0;
		currentFrame = 0;
		spriteTexture = new PuvoTexture(gl, res, context);
		spriteWidth = spriteTexture.getWidth();
		spriteHeight = spriteTexture.getHeight();

		frameCount = spriteTexture.getFrameCount();

		frameDuration = 1000 / fps;
		run = true;
		manualRun = toggleRun = false;

		startFrame = 0;
		endFrame = frameCount - 1;

		name = Defines.getResourceData().get(res[0]).get("fullName");
	}

	float getWidth()
	{
		return spriteWidth;
	}

	float getHeight()
	{
		return spriteHeight;
	}

	/* controls the animation's speed */
	private void Update(final long now)
	{
		if (now > (timer + frameDuration)) {
			timer = now;
			currentFrame += 1;

			if (currentFrame > endFrame) {
				if (toggleRun) {
					run = toggleRun = false;
				}
				currentFrame = startFrame;
			}
		}
	}

	public void setFrameDuration(final int duration)
	{
		frameDuration = duration;
	}

	public void setFrameRange(final int start, final int end)
	{
		if (start >= 0 && start < frameCount) {
			startFrame = start;
		}
		if (end >= 0 && end < frameCount) {
			endFrame = end;
		}
		currentFrame = startFrame;
	}

	public void manualAnimation(final boolean value)
	{
		manualRun = run = value;
	}

	public void toggleAnimationRun(final boolean now)
	{
		if (now || !run) {
			run = !run;
		} else {
			toggleRun = true;
		}
	}

	public void switchToNextFrame()
	{
		currentFrame += 1;

		if (currentFrame > endFrame) {
			currentFrame = startFrame;
		}
	}

	public void switchToFrame(final int new_frame)
	{
		if (new_frame >= 0 && new_frame < frameCount) {
			currentFrame = new_frame;
		}
	}

	public void switchToFrameUnchecked(final int new_frame)
	{
		currentFrame = new_frame;
	}

	public int getFrameCount()
	{
		return frameCount;
	}

	public void onDrawFrame(final GL10 gl, final long now, final float translation_x, final float translation_y,
							final float ratio, final float scale_x, final float scale_y, final float rotation,
							final PuvoColor c, final int direction)
	{
		if (!manualRun && run && frameCount > 1) {
			Update(now);
		}

		spriteTexture.draw(gl, translation_x, translation_y, currentFrame, ratio, scale_x * direction, scale_y, rotation, c);
	}
}
