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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Hashtable;

public class Defines
{
	private static final String LOG_TAG = "Defines";
	private static Hashtable<Integer, Hashtable<String, String>> allResourceData;
	private static Hashtable<Integer, Hashtable<String, String>> resourceData;
	private static int number_of_layer;
	private static BitmapFactory.Options options;

	public static String packageName;

	public static BitmapFactory.Options getOptions()
	{
		if (options == null) {
			options = new BitmapFactory.Options();
			options.inSampleSize = 1;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			return options;
		}
		return options;
	}

	public static int getNumberOfLayer()
	{
		if (number_of_layer == 0) {
			getResourceData();
			for (Hashtable<String, String> tmpHT : resourceData.values()) {
				int l = Integer.parseInt(tmpHT.get("layer")) + 1;
				if (l > number_of_layer) {
					number_of_layer = l;
				}
			}
		}
		return number_of_layer;
	}

	/* every resource which has to be used in the wallpaper follows a naming scheme. Since resources in android
	 * can't start with a number every file starts with an 'r'
	 *
	 * layer__number__name__id__special__frameCount__fps__posX__posY__left__right
	 * e.g.
	 * r4__1__character_name__11__1__14__20__405__540__m138__1772.png
	 *
	 * layer: Integer
	 *       Number of the layer in which the resource should be displayed in. Starts with 0
	 * number: Integer
	 *       Order in which the resources within a layer will be drawn. Resource number 0 will be the first. In case of
	 *       a combined resource all resources must have the same number.
	 * fullName: String.
	 *       Name of the resource which will later be used to distinguish the special resources like SkyObject, Poster, ...
	 *       Resources with the same name but another id will be combined to one resource and every singe resource will
	 *       be treated as one frame (or number of frames if frameCount is bigger than 1). The fps for this combined
	 *       resource will be fsp value of the one with the lowest id.
	 * id: Integer
	 *       ID of this resource used for later connections between the objects.
	 * special: Integer
	 *       1 if this is a resource which needs its own class and 0 if the BaseObject class should be used.
	 * frameCount: Integer
	 *       Number of frames the resource consists of. The frames are arranged side by side like in a film strip and
	 *       all must be of the same size.
	 * fps: Integer
	 *       If the resource has more than one frame, it is an animation and this animation will be displayed with
	 *       'fps' frames per second
	 * posX: Integer
	 *       x coordinate in display-pixels of the top left corner of the resource.
	 *       An 'm' as prefix will negate the value.
	 * posY: Integer
	 *       y coordinate in display-pixels of the top left corner of the resource.
	 *       An 'm' as prefix will negate the value.
	 * left: Integer
	 *       Left (x) border for this resource.
	 *       An 'm' as prefix will negate the value.
	 * right: Integer
	 *       Right (x) border for this resource.
	 *       An 'm' as prefix will negate the value.
	 */
	private static Hashtable<Integer, Hashtable<String, String>> getAllResourceData(Class c)
	{
		if (allResourceData == null) {
			allResourceData = new Hashtable<>(c.getFields().length);

			for (Field f : c.getFields()) {
				try {
					if (!f.getName().contains("__")) {
						continue;
					}
					String[] tmpData = f.getName().split(("__"));
					if (tmpData.length < 11) {
						continue;
					}
					Hashtable<String, String> tmpHT = new Hashtable<>(11);
					tmpHT.put("layer", tmpData[0].substring(1));
					tmpHT.put("number", tmpData[1]);
					tmpHT.put("fullName", tmpData[2]);
					tmpHT.put("id", tmpData[3]);
					tmpHT.put("special", tmpData[4]);
					tmpHT.put("frameCount", tmpData[5]);
					tmpHT.put("fps", tmpData[6]);
					tmpHT.put("posX", tmpData[7].replace("m", "-"));
					tmpHT.put("posY", tmpData[8].replace("m", "-"));
					tmpHT.put("left", tmpData[9].replace("m", "-"));
					tmpHT.put("right", tmpData[10].replace("m", "-"));

					allResourceData.put(f.getInt(c), tmpHT);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return allResourceData;
	}

	private static Class getClassByName(final String name)
	{
		if (packageName == null) {
			Log.e(LOG_TAG, "packageName not set");
			return null;
		}

		try {
			return Class.forName(packageName + ".R$" + name);
		} catch (Exception ignoreE) {
			try {
				return Class.forName("com.puvo.livewallpapers.puvowallpaperbase.R$" + name);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public static Hashtable<Integer, Hashtable<String, String>> getResourceData()
	{
		if (resourceData == null) {
			Class c = getClassByName("drawable");

			if (c == null) {
				return null;
			}

			resourceData = new Hashtable<>(c.getFields().length);

			if (allResourceData == null) {
				getAllResourceData(c);
			}

			for (Integer key : allResourceData.keySet()) {
				Hashtable<String, String> ht = allResourceData.get(key);
				if (ht.containsKey("layer")) {
					resourceData.put(key, ht);
				}
			}
		}
		return resourceData;
	}

	public static int getResourceIDbyName(final String type, final String name)
	{
		return getResourceIDbyName(type, name, null);
	}

	public static int getResourceIDbyName(final String type, final String name, final String prefix)
	{
		Class c = getClassByName(type);
		if (c == null) {
			return -1;
		}

		for (Field f : c.getFields()) {
			if (prefix != null) {
				if (f.getName().equals(prefix + "_" + name)) {
					try {
						return f.getInt(c);
					} catch (Exception e) {
						e.printStackTrace();
						return -1;
					}
				}
			}
		}
		for (Field f : c.getFields()) {
			if (f.getName().equals(name)) {
				try {
					return f.getInt(c);
				} catch (Exception e) {
					e.printStackTrace();
					return -1;
				}
			}
		}
		return -1;
	}

	public static int signum(final float value)
	{
		return value < 0 ? -1 : 1;
	}
}
