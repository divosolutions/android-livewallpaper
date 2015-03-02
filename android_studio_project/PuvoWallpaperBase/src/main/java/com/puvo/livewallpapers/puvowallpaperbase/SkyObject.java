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
import android.content.SharedPreferences;

import javax.microedition.khronos.opengles.GL10;

public class SkyObject extends Particle
{
	private static final String LOG_TAG = "SkyObject";

	public SkyObject(GL10 gl, final Context context, final int[] res, final int left, final int right,
					 final float virtual_scroll_speed_factor)
	{
		super(gl, context, res, left, right, virtual_scroll_speed_factor);
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
