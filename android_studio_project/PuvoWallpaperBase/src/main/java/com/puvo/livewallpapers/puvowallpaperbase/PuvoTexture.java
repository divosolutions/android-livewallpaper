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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

/* This class will load one resource, save it as textures and draw it as requested */
public class PuvoTexture
{
	private static final String LOG_TAG = "PuvoTexture";
	private FloatBuffer[][] vertexBuffer;    // buffer for the vertices
	private final float[][][] vertices;

	private final FloatBuffer textureBuffer;    // buffer for the texture coordinates
	private int[][] textures;

	private final int frameCount;
	private int noTiles;
	private int textureWidth;
	private int textureHeight;

	public PuvoTexture(GL10 gl, int[] res, Context context)
	{
		Bitmap[] textureBitmap;
		ByteBuffer byteBuffer;
		int[] max_texture_size = new int[1];
		float[] textureCoordinates = {
				// mapping for the vertices
				0.0f, 1.0f,        // top left		(V2)
				0.0f, 0.0f,        // bottom left	(V1)
				1.0f, 1.0f,        // top right		(V4)
				1.0f, 0.0f         // bottom right	(V3)
		};

		int fc = 0, framePos = 0;
		for (int re : res) {
			fc += Integer.parseInt(Defines.getResourceData().get(re).get("frameCount"));
		}
		frameCount = fc;
		vertices = new float[frameCount][][];

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(context.getResources(), res[0], options);
		textureWidth = options.outWidth / Integer.parseInt(Defines.getResourceData().get(res[0]).get("frameCount"));
		textureHeight = options.outHeight;

		for (int re : res) {
			final int resFrameCount = Integer.parseInt(Defines.getResourceData().get(re).get("frameCount"));
			final Bitmap resBitmap = BitmapFactory.decodeResource(context.getResources(), re, Defines.getOptions());

			if (resBitmap.getWidth() / resFrameCount != textureWidth) {
				Log.e(LOG_TAG, "frame width doesn't match textureWidth");
				continue;
			} else if (resBitmap.getHeight() != textureHeight) {
				Log.e(LOG_TAG, "frame height doesn't match textureHeight");
				continue;
			}

			gl.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, max_texture_size, 0); //put the maximum texture size in the array.

			for (int frame = 0; frame < resFrameCount; ++frame, ++framePos) {
				final Bitmap bm = getAnimationBitmap(resBitmap, frame);
				textureBitmap = splitBitmapIntoRectangles(bm, framePos, max_texture_size[0]);

				if (framePos == 0) {
					noTiles = textureBitmap.length;
					textures = new int[frameCount][noTiles];
					vertexBuffer = new FloatBuffer[frameCount][noTiles];
				}

				gl.glGenTextures(noTiles, textures[framePos], 0);
				for (int tile = 0; tile < noTiles; tile++) {
					// one float needs 4 byte => 4 byte for every coordinate
					byteBuffer = ByteBuffer.allocateDirect(vertices[framePos][tile].length * 4);
					byteBuffer.order(ByteOrder.nativeOrder());

					vertexBuffer[framePos][tile] = byteBuffer.asFloatBuffer();    // allocate memory
					vertexBuffer[framePos][tile].put(vertices[framePos][tile]);      // fill the vertexBuffer with the vertices
					vertexBuffer[framePos][tile].position(0);                     // set the cursor to the beginning of vertexBuffer

					// defines textures[frame][tile] as the texture to create/use
					gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[framePos][tile]);

					// filter for the texture. LINEAR makes them look more smooth and less pixelated
					gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
					gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

					// this is important, otherwise edges between the textures of a resource will appear
					gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
					gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

					// load the bitmap into the texture
					GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, textureBitmap[tile], 0);
					textureBitmap[tile].recycle();
					System.gc();
				}
			}

			resBitmap.recycle();
			System.gc();
		}
		byteBuffer = ByteBuffer.allocateDirect(textureCoordinates.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());

		textureBuffer = byteBuffer.asFloatBuffer();
		textureBuffer.put(textureCoordinates);
		textureBuffer.position(0);
	}

	/* split the original resource into frames */
	Bitmap getAnimationBitmap(final Bitmap resBitmap, final int index)
	{
		final int w = textureWidth;
		final int h = textureHeight;
		return Bitmap.createBitmap(resBitmap, index * w, 0, w, h);
	}

	/* This function will split 'value' into a list of single values, everyone with the size of a power of 2 and not
	 * bigger than MAX_TEXTURE_SIZE
	 * Look for the biggest power of 2 which is '<=' than the remaining value. Take this value, subtract it from the
	 * remaining value and look for the next biggest power of 2.
	 * To avoid running into fractals the smallest possible value is 64.
	 */
	private ArrayList<Integer> splitIntoSections(int value, final int max_size)
	{
		ArrayList<Integer> sections = new ArrayList<>();
		int start = 0, end = 0;
		int sign = 1;


		if (value < 0) {
			sign = -1;
			value *= sign;
		}

		if (value < 2) {
			sections.add(0);
			sections.add(1);
			return sections;
		}

		while (end < value) {
			if (value - end < 2) {
				sections.add(start * sign);
				start = end + 2;
				break;
			}
			for (int sectionSize = 2; sectionSize + start <= value; sectionSize *= 2) {
				// diff is the value which would not fit into sectionSize
				int diff = value - (sectionSize + start);

				if (2 * sectionSize > max_size) {
					end = sectionSize + start;
					break;
				}
				if (sectionSize / 2 <= diff) {
					if (diff <= sectionSize) {
						// sectionSize / 2 <= diff <= sectionSize
						// In this case just take the bigger value (2 * sectionSize), otherwise we would at least create
						// two more values

						end = 2 * sectionSize + start;
						break;
					}
				} else {
					// value ends 'short' behind sectionSize so it is a good idea to split it into one part of sectionSize
					// and one for the rest, but only if sectionSize is bigger than 64. Otherwise consume all by using
					// 2 * sectionSize to prevent a situation (e.g. 63) in which the section would be split into a lot
					// more sections (32, 16, 8, 4, 2, 1). Nobody wants that.

					if (sectionSize <= 64) {
						end = 2 * sectionSize + start;
					} else {
						end = sectionSize + start;
					}
					break;
				}
			}

			sections.add(start * sign);
			start = end;
		}
		sections.add(start * sign);

		return sections;
	}

	/* This function will split a given bitmap into single rectangles, each with edge lengths of a power of 2.
	 * Older OpenGLES versions aren't able to handle arbitrary edge lengths.
	 * The function splitIntoSections will be used to create a list of single values which in total will be equal or
	 * slightly bigger then the initial value. Every single value will be a power of 2.
	 * I think this is called a texture atlas. */
	private Bitmap[] splitBitmapIntoRectangles(final Bitmap bitmap, final int frame, final int max_size)
	{
		int index = 0;
		ArrayList<Integer> widthSections = splitIntoSections(bitmap.getWidth(), max_size);
		ArrayList<Integer> heightSections = splitIntoSections(bitmap.getHeight(), max_size);
		int _noTiles = (widthSections.size() - 1) * (heightSections.size() - 1);
		Bitmap[] bitmaps = new Bitmap[_noTiles];

		vertices[frame] = new float[_noTiles][];
		Bitmap workingBitmap = Bitmap.createBitmap(widthSections.get(widthSections.size() - 1), heightSections.get(heightSections.size() - 1), bitmap.getConfig());
		Canvas c = new Canvas(workingBitmap);
		c.drawBitmap(bitmap, 0, 0, null);

		for (int i = 0; i < widthSections.size() - 1; i++) {
			for (int j = 0; j < heightSections.size() - 1; j++) {
				int x = widthSections.get(i);
				int y = heightSections.get(j);
				int w = widthSections.get(i + 1) - x;
				int h = heightSections.get(j + 1) - y;

				bitmaps[index] = Bitmap.createBitmap(workingBitmap, x, y, w, h);
				float v[] = {
						x, y + h,        // V1 - bottom left
						x, y,            // V2 - top left
						x + w, y + h,    // V3 - bottom right
						x + w, y         // V4 - top right
				};
				vertices[frame][index] = v;
				index++;
			}
		}

		if (index > 1) {
			workingBitmap.recycle();
			System.gc();
		}
		return bitmaps;
	}

	public void draw(GL10 gl, final float translation_x, final float translation_y, final int curFrame,
					 final float ratio, final float scale_x, final float scale_y, final float rotation, final PuvoColor c)
	{
		gl.glPushMatrix();
		// it's pretty ease to handle the change of the aspect ratio of the display
		gl.glScalef(ratio, ratio, 0);
		// original_position the the resource
		gl.glTranslatef(translation_x, translation_y, 0f);

		gl.glScalef(scale_x, scale_y, 0);

		gl.glTranslatef(textureWidth/2, 0, 0);
		gl.glRotatef(rotation, 0, 0, 1);
		gl.glTranslatef(-textureWidth/2, 0, 0);


		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

		gl.glColor4f(c.r, c.g, c.b, c.o);
		for (int i = 0; i < noTiles; i++) {
			// Point to our buffers
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[curFrame][i]);

			// Point to our vertex buffer
			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer[curFrame][i]);

			// Draw the vertices as triangle strip
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
		}

		gl.glPopMatrix();
	}

	public int getWidth()
	{
		return textureWidth;
	}

	public int getHeight()
	{
		return textureHeight;
	}

	public int getFrameCount()
	{
		return frameCount;
	}

	static class FlushedInputStream extends FilterInputStream
	{
		public FlushedInputStream(InputStream inputStream)
		{
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException
		{
			Log.v(LOG_TAG, "try to skip: " + n);
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int b = read();
					if (b < 0) {
						break;  // we reached EOF
					} else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}
}
