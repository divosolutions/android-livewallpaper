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

public class SkyObject extends Particle
{
	private static final String LOG_TAG = "SkyObject";

	public SkyObject(final Context context, final int[] res, final int left, final int right,
					 final float virtual_scroll_speed_factor)
	{
		super(context, res, left, right, virtual_scroll_speed_factor);
		setMoveDirection(MOVE_DIRECTION.X, left, right, (int) original_position.x, (int) original_position.y,
				virtual_scroll_speed_factor);

		particle_size_factor = 1;
	}

	@Override
	protected void initDefaults()
	{
		super.initDefaults();
		move_it = false;
	}

	@Override
	protected void setSize(final int index)
	{
		final float t = particle_size_factor * 0.5f;
		particle_sizes[index].x = ((float) Math.random() + 1) * t;
		particle_sizes[index].y = ((float) Math.random() + 1) * t;

		if (particle_sizes[index].x < particle_sizes[index].y) {
			final float s = particle_sizes[index].x;
			particle_sizes[index].x = particle_sizes[index].y;
			particle_sizes[index].y = s;
		}
	}

	@Override
	protected void setSpeed(final int index)
	{
		particle_speeds[index].set(0.3f * (float) Math.random() + particle_sizes[index].x * particle_speed / (2 * max_particle_speed), 0f);
	}

	@Override
	public void setPreferences(SharedPreferences sharedPreferences, String key)
	{
		super.setPreferences(sharedPreferences, key);
		if (key == null || key.equals("move_sky_objects")) {
			move_it = sharedPreferences.getBoolean("move_sky_objects", true);
		}
	}
}
