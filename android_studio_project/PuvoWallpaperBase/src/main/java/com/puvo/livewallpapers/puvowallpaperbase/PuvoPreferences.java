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
				//noinspection deprecation
				addPreferencesFromResource(Defines.getResourceIDbyName("xml", "prefs"));
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
			addPreferencesFromResource(Defines.getResourceIDbyName("xml", "prefs"));
			ps = this.getPreferenceScreen();
			setNumberOfScreensEnabled(number_of_screens_enabled);
			setParallaxEnabled(parallax_enabled);
			setInclinationEnabled(inclination_enabled);
		}
	}
}
