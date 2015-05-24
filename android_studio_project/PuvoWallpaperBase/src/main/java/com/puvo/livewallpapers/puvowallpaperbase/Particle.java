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
import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.Rect;

import java.util.ArrayList;

@SuppressWarnings("ConstantConditions")
public class Particle extends BaseObject
{
	private static final String LOG_TAG = "Particle";

	protected enum MOVE_DIRECTION
	{
		X, Y
	}

	/* 0 means X, 1 means Y */
	protected MOVE_DIRECTION moveDirection;

	protected int min_particle_count, max_particle_count, default_particle_count;
	protected int min_particle_speed, max_particle_speed, default_particle_speed;

	protected int spawn_border, destroy_border;
	protected float particle_factor, particle_size_factor;

	protected int width, height;
	protected final Rect area;
	protected float particle_speed;
	protected PointF[] particle_positions, particle_speeds, particle_sizes;
	protected int[] particle_frames;
	protected ArrayList<Integer> particle_indices;
	protected boolean config_in_progress, rendering, particle_factor_positive;

	private int spawn_length, spawn_offset;

	public Particle(Context context, int[] res, int left, int right, float virtual_scroll_speed_factor)
	{
		super(context, res, left, right, virtual_scroll_speed_factor);

		area = new Rect();

		if (left <= right) {
			area.top = left;
			area.bottom = right;
		} else {
			area.top = right;
			area.bottom = left;
		}

		if (original_position.x <= original_position.y) {
			area.left = (int) original_position.x;
			area.right = (int) original_position.y;
		} else {
			area.left = (int) original_position.y;
			area.right = (int) original_position.x;
		}

		width = (int) (area.width() * virtual_scroll_speed_factor);
		height = area.height();

		setMoveDirection(MOVE_DIRECTION.Y, left, right, (int) original_position.x, (int) original_position.y,
				virtual_scroll_speed_factor);

		particle_size_factor = 8 * (virtual_scroll_speed_factor - 1);

		initDefaults();
		manualAnimation(true);
	}

	protected void initDefaults()
	{
		final int frameCount = objectSprite.getFrameCount();
		Resources res = context.getResources();

		min_particle_count = res.getInteger(Defines.getResourceIDbyName("integer", "min_particle_count", fullName));
		max_particle_count = res.getInteger(Defines.getResourceIDbyName("integer", "max_particle_count", fullName));
		default_particle_count = (max_particle_count - min_particle_count) / 2 + min_particle_count;
		min_particle_speed = res.getInteger(Defines.getResourceIDbyName("integer", "min_particle_speed", fullName));
		max_particle_speed = res.getInteger(Defines.getResourceIDbyName("integer", "max_particle_speed", fullName));
		default_particle_speed = (max_particle_speed - min_particle_speed) / 2 + min_particle_speed;

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

		move_it = true;
		draw = false;
	}

	protected void setMoveDirection(MOVE_DIRECTION m, int l, int r, int x, int y, float v)
	{
		moveDirection = m;

		if (moveDirection == MOVE_DIRECTION.X) {
			spawn_border = x;
			destroy_border = y;

			spawn_length = height;
			spawn_offset = area.top;
		} else {
			spawn_border = l;
			destroy_border = r;

			spawn_length = width;
			spawn_offset = area.left;
		}

		particle_factor = v * Defines.signum(destroy_border - spawn_border);
		particle_factor_positive = particle_factor > 0f;
	}

	@Override
	public void setPreferences(SharedPreferences sharedPreferences, String key)
	{
		if (key == null || key.equals(fullName) || key.equals(fullName + "_count")) {
			startConfiguration();

			final int particle_count = (int) (PuvoPreferences.getIntPreferenceByName(sharedPreferences, fullName + "_count", default_particle_count, min_particle_count, max_particle_count) / Math.abs(particle_factor));
			final int frameCount = objectSprite.getFrameCount();

			if (particle_indices.size() < particle_count) {
				particle_speed = PuvoPreferences.getIntPreferenceByName(sharedPreferences, fullName + "_speed", default_particle_speed, min_particle_speed, max_particle_speed) * particle_factor;
				for (int i = particle_indices.size(); i < particle_count; i++) {

					setSize(i);
					setPositionInit(i);
					setSpeed(i);

					particle_frames[i] = (int) (Math.random() * frameCount);
					particle_indices.add(i);
				}
			} else if (particle_indices.size() > particle_count) {
				while (particle_indices.size() > particle_count) {
					particle_indices.remove(particle_indices.size() - 1);
				}
			}

			draw = sharedPreferences.getBoolean(fullName, true);
			endConfiguration();
		}
		if (key == null || key.equals(fullName + "_speed")) {
			particle_speed = PuvoPreferences.getIntPreferenceByName(sharedPreferences, fullName + "_speed", default_particle_speed, min_particle_speed, max_particle_speed) * particle_factor;
			for (Integer index : particle_indices) {
				setSpeed(index);
			}
		}

	}

	void setPositionInit(final int index)
	{
		particle_positions[index].set((float) Math.random() * width + area.left, (float) Math.random() * height + area.top);
	}

	void setPosition(final int index, final float spawn, final float free_and_random)
	{
		if (moveDirection == MOVE_DIRECTION.X) {
			particle_positions[index].set(spawn - (particle_sizes[index].x - 1) * draw_width, free_and_random);
		} else {
			particle_positions[index].set(free_and_random, spawn - (particle_sizes[index].y - 1) * draw_height);
		}
	}

	protected void setSize(final int index)
	{
		particle_sizes[index].x = particle_sizes[index].y = particle_size_factor * (float) Math.random();
	}

	protected void setSpeed(final int index)
	{
		if (moveDirection == MOVE_DIRECTION.X) {
			particle_speeds[index].set(0.3f * (float) Math.random() + particle_sizes[index].x * particle_speed, 0.3f * (float) Math.random());
		} else {
			particle_speeds[index].set(0.3f * (float) Math.random(), 0.3f * (float) Math.random() + particle_sizes[index].y * particle_speed);
		}
	}

	protected void startConfiguration()
	{
		while (config_in_progress) {
			try {
				Thread.sleep(1);
			} catch (Exception ignored) {
			}
		}

		config_in_progress = true;
		while (rendering) {
			try {
				Thread.sleep(1);
			} catch (Exception ignored) {
			}
		}
	}

	protected void endConfiguration()
	{
		config_in_progress = false;
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
			setSize(index);
			setPosition(index, spawn_border, (float) Math.random() * spawn_length + spawn_offset);
			setSpeed(index);
		}
	}

	@Override
	public void onDrawFrame(final long now, PointF baseTranslation, final float ratio, final PointF scale)
	{
		if (draw && !config_in_progress && drawColor.o != 0f) {
			rendering = true;
			for (int index = 0; index < particle_indices.size(); index++) {
				final int i = particle_indices.get(index);
				objectSprite.switchToFrameUnchecked(particle_frames[i]);
				objectSprite.onDrawFrame(
						now,
						particle_positions[i].x + baseTranslation.x,
						particle_positions[i].y + baseTranslation.y,
						ratio,
						particle_sizes[i].x,
						particle_sizes[i].y,
						rotation,
						drawColor,
						direction);
				if (move_it) {
					move(now, i);
				}
			}
			rendering = false;
		}
	}

}
