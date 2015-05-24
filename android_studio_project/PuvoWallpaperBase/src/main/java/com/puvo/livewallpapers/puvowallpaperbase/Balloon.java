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
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.util.ArrayList;

public class Balloon extends Particle
{
	private static final String LOG_TAG = "Balloon";
	private static final int MAX_BALLOONS = 100;

	private ArrayList<Integer> toRemove;
	private int old_value, last_index;

	public Balloon(Context context, int[] res, int left_border, int right_border, float virtual_scroll_speed_factor)
	{
		super(context, res, left_border, right_border, virtual_scroll_speed_factor);

		last_index = 0;
		old_value = -1;
	}

	@Override
	protected void initDefaults()
	{
		final int frameCount = objectSprite.getFrameCount();

		min_particle_count = 5;
		max_particle_count = MAX_BALLOONS;
		default_particle_count = 10;
		min_particle_speed = 1;
		max_particle_speed = 10;
		default_particle_speed = 5;

		toRemove = new ArrayList<>(max_particle_count);

		particle_indices = new ArrayList<>(max_particle_count);
		particle_positions = new PointF[max_particle_count];
		particle_speeds = new PointF[max_particle_count];
		particle_sizes = new PointF[max_particle_count];
		particle_frames = new int[max_particle_count];

		for (int i = 0; i < max_particle_count; i++) {
			particle_positions[i] = new PointF();
			particle_speeds[i] = new PointF();
			particle_sizes[i] = new PointF();
			particle_frames[i] = (int) (Math.random() * frameCount);
		}
	}

	@Override
	public boolean triggerAction()
	{
		return false;
	}

	@Override
	public void receiveCommand(final String name, final Command cmd, final int value)
	{
		int number_of_balloons = value % MAX_BALLOONS;

		if (old_value == -1) {
			old_value = number_of_balloons;
		} else if (old_value != number_of_balloons) {
			old_value = number_of_balloons;
			if (particle_indices.size() == 0) {
				startConfiguration();

				DisplayMetrics metrics = new DisplayMetrics();
				((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);

				width = metrics.widthPixels - (int) objectSprite.getWidth() / 2;
				height = metrics.heightPixels - (int) objectSprite.getWidth() / 2;


				for (int i = 0; i < number_of_balloons; i++) {
					final float vp1 = spawn_border;
					final float vp2 = (float) Math.random();

					final float s = 1f + 0.2f * (0.5f - (float) Math.random());

					final float vs1 = 4 * Defines.signum(particle_factor) + (float) Math.random() * s * particle_factor * 4;
					final float vs2 = 0.3f * (float) Math.random();

					particle_sizes[last_index].set(s, s);

					if (moveDirection == MOVE_DIRECTION.X) {
						particle_positions[last_index].set(vp1, vp2 * height);
						particle_speeds[last_index].set(vs1, vs2);
					} else {
						particle_positions[last_index].set(vp2 * width, vp1);
						particle_speeds[last_index].set(vs2, vs1);
					}

					particle_indices.add(last_index);

					last_index = (last_index + 1) % MAX_BALLOONS;
				}
				endConfiguration();
			}
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
	protected void move(final long now, final int index)
	{
		PointF pos = particle_positions[index];
		PointF spd = particle_speeds[index];

		pos.x += spd.x;
		pos.y += spd.y;

		final boolean destroy = particle_factor_positive ? (moveDirection == MOVE_DIRECTION.X ? pos.x : pos.y) > destroy_border : (moveDirection == MOVE_DIRECTION.X ? pos.x : pos.y) < destroy_border;

		if (destroy) {
			toRemove.add(index);
		}
	}

	@Override
	public void onDrawFrame(final long now, PointF baseTranslation, final float ratio, final PointF scale)
	{
		toRemove.clear();

		super.onDrawFrame(now, zeroPoint, ratio, scale);

		if (toRemove.size() != 0) {
			particle_indices.removeAll(toRemove);
		}
	}


}
