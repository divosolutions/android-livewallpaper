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

	public ObjectSprite(final int[] res, final int fps, final Context context)
	{
		timer = 0;
		currentFrame = 0;
		spriteTexture = new PuvoTexture(res, context);
		spriteWidth = spriteTexture.getWidth();
		spriteHeight = spriteTexture.getHeight();

		frameCount = spriteTexture.getFrameCount();

		frameDuration = 1000 / fps;
		run = true;
		manualRun = toggleRun = false;

		startFrame = 0;
		endFrame = frameCount - 1;

		if (Defines.getResourceData() != null) {
			name = Defines.getResourceData().get(res[0]).get("fullName");
		} else {
			name = "no resourceData";
		}
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

	public void onDrawFrame(final long now, final float translation_x, final float translation_y,
							final float ratio, final float scale_x, final float scale_y, final float rotation,
							final PuvoColor c, final int direction)
	{
		if (!manualRun && run && frameCount > 1) {
			Update(now);
		}

		spriteTexture.draw(translation_x, translation_y, currentFrame, ratio, scale_x * direction, scale_y, rotation, c);
	}
}
