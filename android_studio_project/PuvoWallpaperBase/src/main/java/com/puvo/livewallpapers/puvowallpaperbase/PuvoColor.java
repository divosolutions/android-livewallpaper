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

public class PuvoColor
{
	public float r, g, b, o;

	public PuvoColor(final float r, final float g, final float b, final float o)
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.o = o;

		PuvoColor.checkValues(this);
	}

	public PuvoColor()
	{
		this(0f, 0f, 0f, 1f);
	}

	public PuvoColor(final float r, final float g, final float b)
	{
		this(r, g, b, 1f);
	}

	public PuvoColor(final PuvoColor c)
	{
		this(c.r, c.g, c.b, c.o);
	}

	public void set(final float r, final float g, final float b, final float o)
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.o = o;

		PuvoColor.checkValues(this);
	}

	public void set(final float r, final float g, final float b)
	{
		this.set(r, g, b, this.o);
	}

	public void set(final float v, final float o)
	{
		this.set(v, v, v, o);
	}

	public void set(final PuvoColor c)
	{
		this.set(c.r, c.g, c.b, c.o);
	}

	public void add(final float r, final float g, final float b, final float o)
	{
		this.r += r;
		this.g += g;
		this.b += b;
		this.o += o;

		PuvoColor.checkValues(this);
	}

	public void add(final float v, final float o)
	{
		this.add(v, v, v, o);
	}

	public void add(final PuvoColor c)
	{
		this.add(c.r, c.g, c.b, c.o);
	}

	public void multiply(final float r, final float g, final float b, final float o)
	{
		this.r *= r;
		this.g *= g;
		this.b *= b;
		this.o *= o;

		PuvoColor.checkValues(this);
	}

	public void multiply(final float v)
	{
		this.multiply(v, v, v, v);
	}

	public void multiply(final PuvoColor c)
	{
		this.multiply(c.r, c.g, c.b, c.o);
	}

	private static void checkValues(PuvoColor c)
	{
		if (c.r < 0f) {
			c.r = 0f;
		} else if (c.r > 1f) {
			c.r = 1f;
		}
		if (c.g < 0f) {
			c.g = 0f;
		} else if (c.g > 1f) {
			c.g = 1f;
		}
		if (c.b < 0f) {
			c.b = 0f;
		} else if (c.b > 1f) {
			c.b = 1f;
		}
		if (c.o < 0f) {
			c.o = 0f;
		} else if (c.o > 1f) {
			c.o = 1f;
		}
	}

	public String toString()
	{
		return String.format("PuvoColor: red=%f (%d), green=%f (%d), blue=%f (%d), opacity=%f (%d)", r, (int) r * 255, g, (int) g * 255, b, (int) b * 255, o, (int) o * 255);
	}
}
