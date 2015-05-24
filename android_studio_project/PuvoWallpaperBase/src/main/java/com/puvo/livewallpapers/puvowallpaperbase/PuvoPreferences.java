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

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class PuvoPreferences extends PreferenceActivity
{
	private final static String LOG_TAG = "PuvoPreferences";
	private static PreferenceScreen ps;
	private static boolean number_of_screens_enabled = true;
	private static boolean parallax_enabled = false, inclination_enabled = false;

	public static void setNumberOfScreensEnabled(boolean value)
	{
		number_of_screens_enabled = value;
		if (ps != null) {
			Preference p = ps.findPreference("number_of_screens");

			if (p != null) {
				p.setEnabled(number_of_screens_enabled);
				p.setSelectable(number_of_screens_enabled);
			}
		}
	}

	public static void setParallaxEnabled(boolean value)
	{
		parallax_enabled = value;
		if (ps != null) {
			Preference p = ps.findPreference("parallax");

			if (p != null) {
				p.setEnabled(parallax_enabled);
				p.setSelectable(parallax_enabled);
			}
		}
	}

	public static void setInclinationEnabled(boolean value)
	{
		inclination_enabled = value;
		if (ps != null) {
			Preference p = ps.findPreference("inclination");

			if (p != null) {
				p.setEnabled(inclination_enabled);
				p.setSelectable(inclination_enabled);
			}
		}
	}

	public static int getIntPreferenceByName(SharedPreferences sharedPreferences, String name, int default_value, int min, int max)
	{
		if (ps != null) {
			Preference p = ps.findPreference(name);

			if (p != null) {
				if (p instanceof EditTextPreference) {
					int ret = default_value;
					try {
						ret = Integer.parseInt(((EditTextPreference) p).getText());
					} catch (NumberFormatException ignore) {}

					if (ret < min) {
						ret = min;
						((EditTextPreference) p).setText(String.valueOf(ret));
					} else if (ret > max) {
						ret = max;
						((EditTextPreference) p).setText(String.valueOf(ret));
					}
					return ret;
				}
			}
			return default_value;
		} else {
			int ret = default_value;
			try {
				ret = Integer.parseInt(sharedPreferences.getString(name, String.valueOf(default_value)));
			} catch (NumberFormatException ignore) {}

			if (ret < min) {
				ret = min;
			} else if (ret > max) {
				ret = max;
			}
			return ret;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
				int resID = Defines.getResourceIDbyName("xml", "prefs");
				if (resID == -1) {
					return;
				}
				//noinspection deprecation
				addPreferencesFromResource(resID);
				//noinspection deprecation
				ps = getPreferenceScreen();

				setNumberOfScreensEnabled(number_of_screens_enabled);
				setParallaxEnabled(parallax_enabled);
				setInclinationEnabled(inclination_enabled);
			} else {
				getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class PrefsFragment extends PreferenceFragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			int resID = Defines.getResourceIDbyName("xml", "prefs");
			if (resID == -1) {
				return;
			}
			addPreferencesFromResource(resID);
			ps = this.getPreferenceScreen();
			setNumberOfScreensEnabled(number_of_screens_enabled);
			setParallaxEnabled(parallax_enabled);
			setInclinationEnabled(inclination_enabled);
		}
	}
}
