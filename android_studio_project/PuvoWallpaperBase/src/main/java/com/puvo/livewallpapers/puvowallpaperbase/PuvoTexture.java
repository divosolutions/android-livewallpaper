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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Hashtable;

/* This class will load one resource, save it as textures and draw it as requested */
public class PuvoTexture {
	private static final String LOG_TAG = "PuvoTexture";
	private FloatBuffer[][] vertexBuffer;    // buffer for the vertices
	private float[][][] vertices;

	private FloatBuffer textureBuffer;    // buffer for the texture coordinates
	private int[][] textures;

	private int frameCount;
	private int noTiles;
	private int textureWidth;
	private int textureHeight;

	public PuvoTexture(int[] res, Context context)
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
		Hashtable<Integer, Hashtable<String, String>> resourceData = Defines.getResourceData();

		if (resourceData == null) {
			Log.e(LOG_TAG, "resourceData is null");
			return;
		}

		int fc = 0, framePos = 0;
		for (int re : res) {
			fc += Integer.parseInt(resourceData.get(re).get("frameCount"));
		}
		frameCount = fc;
		vertices = new float[frameCount][][];

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(context.getResources(), res[0], options);
		textureWidth = options.outWidth / Integer.parseInt(resourceData.get(res[0]).get("frameCount"));
		textureHeight = options.outHeight;

		for (int re : res) {
			final int resFrameCount = Integer.parseInt(resourceData.get(re).get("frameCount"));
			final Bitmap resBitmap = BitmapFactory.decodeResource(context.getResources(), re, Defines.getOptions());

			if (resBitmap.getWidth() / resFrameCount != textureWidth) {
				Log.e(LOG_TAG, "frame width doesn't match textureWidth");
				continue;
			} else if (resBitmap.getHeight() != textureHeight) {
				Log.e(LOG_TAG, "frame height doesn't match textureHeight");
				continue;
			}

			//put the maximum texture size in the array.
			GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, max_texture_size, 0);

			for (int frame = 0; frame < resFrameCount; ++frame, ++framePos) {
				final Bitmap bm = getAnimationBitmap(resBitmap, frame);
				textureBitmap = splitBitmapIntoRectangles(bm, framePos, max_texture_size[0]);

				if (framePos == 0) {
					noTiles = textureBitmap.length;
					textures = new int[frameCount][noTiles];
					vertexBuffer = new FloatBuffer[frameCount][noTiles];
				}

				GLES20.glGenTextures(noTiles, textures[framePos], 0);
				for (int tile = 0; tile < noTiles; tile++) {
					// one float needs 4 byte => 4 byte for every coordinate
					byteBuffer = ByteBuffer.allocateDirect(vertices[framePos][tile].length * 4);
					byteBuffer.order(ByteOrder.nativeOrder());

					vertexBuffer[framePos][tile] = byteBuffer.asFloatBuffer();    // allocate memory
					vertexBuffer[framePos][tile].put(vertices[framePos][tile]);      // fill the vertexBuffer with the vertices
					vertexBuffer[framePos][tile].position(0);                     // set the cursor to the beginning of vertexBuffer

					// defines textures[frame][tile] as the texture to create/use
					GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[framePos][tile]);

					// filter for the texture. LINEAR makes them look more smooth and less pixelated
					GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
					GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

					// this is important, otherwise edges between the textures of a resource will appear
					GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
					GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

					// load the bitmap into the texture
					GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textureBitmap[tile], 0);
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

	private void translate(float[] model_matrix, float x, float y, float z)
	{
		Matrix.translateM(model_matrix, 0, x, y, z);
	}

	private void rotate(float[] model_matrix, float angle, float x, float y, float z)
	{
		Matrix.rotateM(model_matrix, 0, angle, x, y, z);
	}

	private void scale(float[] model_matrix, float xFactor, float yFactor, float zFactor)
	{
		Matrix.scaleM(model_matrix, 0, xFactor, yFactor, zFactor);
	}

	private float[] model_matrix = new float[16];
	private float[] mvp_matrix = new float[16];

	public void draw(final float translation_x, final float translation_y, final int curFrame,
					 final float ratio, final float scale_x, final float scale_y, final float rotation, final PuvoColor c)
	{
		Matrix.setIdentityM(model_matrix, 0);

		// it's pretty ease to handle the change of the aspect ratio of the display
		Matrix.scaleM(model_matrix, 0, ratio, ratio, 0);
		//Matrix.scaleM(model_matrix, 0, ratio * scale_x, ratio * scale_y, 0);
		// original_position the the resource
		Matrix.translateM(model_matrix, 0, translation_x, translation_y, 0);

		Matrix.scaleM(model_matrix, 0, scale_x, scale_y, 0);

		Matrix.translateM(model_matrix, 0, -textureWidth / 2, 0, 0);
		Matrix.rotateM(model_matrix, 0, rotation, 0, 0, 1);
		Matrix.translateM(model_matrix, 0, textureWidth / 2, 0, 0);

		Matrix.multiplyMM(mvp_matrix, 0, PuvoGLES20Tools.mProjectionAndView, 0, model_matrix, 0);
		GLES20.glUniformMatrix4fv(PuvoGLES20Tools.mvp_matrix_handle, 1, false, mvp_matrix, 0);

		GLES20.glBlendColor(c.r, c.g, c.b, c.o);


		// Prepare the texture coordinates
		GLES20.glVertexAttribPointer(PuvoGLES20Tools.texture_coordinates_handle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);

		for (int i = 0; i < noTiles; i++) {
			GLES20.glVertexAttribPointer(PuvoGLES20Tools.position_handle, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer[curFrame][i]);

			// Set the active texture unit to texture unit 0.
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

			// Bind the texture to this unit.
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[curFrame][i]);

			GLES20.glUniform1i(PuvoGLES20Tools.texture_handle, 0);


			// Draw the triangle

			GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
		}
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
}
