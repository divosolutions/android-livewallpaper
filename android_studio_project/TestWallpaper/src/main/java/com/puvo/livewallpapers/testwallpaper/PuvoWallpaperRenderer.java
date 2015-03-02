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

import com.puvo.livewallpapers.puvowallpaperbase.Balloon;
import com.puvo.livewallpapers.puvowallpaperbase.BaseObject;
import com.puvo.livewallpapers.puvowallpaperbase.BaseObjectInterface;
import com.puvo.livewallpapers.puvowallpaperbase.Counter;
import com.puvo.livewallpapers.puvowallpaperbase.Flower;
import com.puvo.livewallpapers.puvowallpaperbase.OnOff;
import com.puvo.livewallpapers.puvowallpaperbase.Particle;
import com.puvo.livewallpapers.puvowallpaperbase.Poster;
import com.puvo.livewallpapers.puvowallpaperbase.Settings;
import com.puvo.livewallpapers.puvowallpaperbase.SkyObject;
import com.puvo.livewallpapers.puvowallpaperbase.Switch;
import com.puvo.livewallpapers.puvowallpaperbase.Visit;

import java.util.Hashtable;

import javax.microedition.khronos.opengles.GL10;

public class PuvoWallpaperRenderer extends com.puvo.livewallpapers.puvowallpaperbase.PuvoWallpaperRenderer
{
	@Override
	public BaseObjectInterface initObject(GL10 gl, Hashtable<String, String> rd, int[] res, int l)
	{
		BaseObjectInterface bo = null;

		final int left = Integer.parseInt(rd.get("left"));
		final int right = Integer.parseInt(rd.get("right"));

		if (rd.get("special").equals("0")) {
			bo = new BaseObject(gl, context, res, left, right, virtual_scroll_speed_factor[l][0]);
		} else {
			// this is special for every wallpaper too
			String name = rd.get("fullName").toLowerCase();
			if (name.startsWith("character")) {
				bo = new Character(gl, context, res, left, right, virtual_scroll_speed_factor[l][0]);
				inclinationObjects.add(bo);
			} else if (name.startsWith("poster")) {
				bo = new Poster(gl, context, res, left, right, virtual_scroll_speed_factor[l][0]);
			} else if (name.startsWith("skyobject")) {
				bo = new SkyObject(gl, context, res, left, right, virtual_scroll_speed_factor[l][0]);
			} else if (name.startsWith("settings")) {
				bo = new Settings(gl, context, res, left, right, virtual_scroll_speed_factor[l][0]);
			} else if (name.startsWith("visit")) {
				bo = new Visit(gl, context, res, left, right, virtual_scroll_speed_factor[l][0]);
			} else if (name.startsWith("particle")) {
				bo = new Particle(gl, context, res, left, right, virtual_scroll_speed_factor[l][0]);
			} else if (name.startsWith("counter")) {
				bo = new Counter(gl, context, res, left, right, virtual_scroll_speed_factor[l][0]);
			} else if (name.startsWith("balloon")) {
				bo = new Balloon(gl, context, res, left, right, virtual_scroll_speed_factor[l][0]);
			} else if (name.startsWith("switch")) {
				bo = new Switch(gl, context, res, left, right, virtual_scroll_speed_factor[l][0]);
			} else if (name.startsWith("onoff")) {
				bo = new OnOff(gl, context, res, left, right, virtual_scroll_speed_factor[l][0]);
			} else if (name.startsWith("flower")) {
				bo = new Flower(gl, context, res, left, right, virtual_scroll_speed_factor[l][0]);
			}
		}

		return bo;
	}
}
