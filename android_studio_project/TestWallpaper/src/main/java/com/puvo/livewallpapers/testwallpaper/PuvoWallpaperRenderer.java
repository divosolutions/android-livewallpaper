/*
 * Author:
 *   Thomas Siegmund Thomas.Siegmund@puvoproductions.com
 *
 * Copyright (c) 2015, Puvo Productions http://puvoproductions.com
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    * Redistributions of source code must retain the above copyright notice, this
 *      list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *    * Neither the name of the [ORGANIZATION] nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

public class PuvoWallpaperRenderer extends com.puvo.livewallpapers.puvowallpaperbase.PuvoWallpaperRenderer
{
	@Override
	public BaseObjectInterface initObject(Hashtable<String, String> rd, int[] res, int l)
	{
		BaseObjectInterface bo = null;

		final int left = Integer.parseInt(rd.get("left"));
		final int right = Integer.parseInt(rd.get("right"));

		if (rd.get("special").equals("0")) {
			bo = new BaseObject(context, res, left, right, virtual_scroll_speed_factor[l][0]);
		} else {
			// this is special for every wallpaper too
			String name = rd.get("fullName").toLowerCase();
			if (name.startsWith("character")) {
				bo = new Character(context, res, left, right, virtual_scroll_speed_factor[l][0]);
				inclinationObjects.add(bo);
			} else if (name.startsWith("poster")) {
				bo = new Poster(context, res, left, right, virtual_scroll_speed_factor[l][0]);
			} else if (name.startsWith("skyobject")) {
				bo = new SkyObject(context, res, left, right, virtual_scroll_speed_factor[l][0]);
			} else if (name.startsWith("settings")) {
				bo = new Settings(context, res, left, right, virtual_scroll_speed_factor[l][0]);
			} else if (name.startsWith("visit")) {
				bo = new Visit(context, res, left, right, virtual_scroll_speed_factor[l][0]);
			} else if (name.startsWith("particle")) {
				bo = new Particle(context, res, left, right, virtual_scroll_speed_factor[l][0]);
			} else if (name.startsWith("counter")) {
				bo = new Counter(context, res, left, right, virtual_scroll_speed_factor[l][0]);
			} else if (name.startsWith("balloon")) {
				bo = new Balloon(context, res, left, right, virtual_scroll_speed_factor[l][0]);
			} else if (name.startsWith("switch")) {
				bo = new Switch(context, res, left, right, virtual_scroll_speed_factor[l][0]);
			} else if (name.startsWith("onoff")) {
				bo = new OnOff(context, res, left, right, virtual_scroll_speed_factor[l][0]);
			} else if (name.startsWith("flower")) {
				bo = new Flower(context, res, left, right, virtual_scroll_speed_factor[l][0]);
			}
		}

		return bo;
	}
}
