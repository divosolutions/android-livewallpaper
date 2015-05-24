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
