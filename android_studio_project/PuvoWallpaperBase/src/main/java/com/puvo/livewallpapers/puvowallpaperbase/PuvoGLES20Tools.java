//
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

import android.opengl.GLES20;

public class PuvoGLES20Tools {
	public static int program_handle;
	public static int vertex_shader_handle;
	public static int fragment_shader_handle;

	public static int mvp_matrix_handle;
	public static int position_handle;
	public static int texture_coordinates_handle;
	public static int texture_handle;

	// GLES20 stuff
	// Our matrices
	public static final float[] mProjection = new float[16];
	public static final float[] mView = new float[16];
	public static final float[] mProjectionAndView = new float[16];



	public static final String image_vertex_shader =
		"uniform mat4 u_mvp_matrix;" +

			"attribute vec4 a_position;" +
			"attribute vec2 a_texture_coordinates;" +

			"varying vec2 v_texture_coordinates;" +

			"void main() {" +
			"  gl_Position = u_mvp_matrix * a_position;" +
			"  v_texture_coordinates = a_texture_coordinates;" +
			"}";

	public static final String image_fragment_shader =
		"precision mediump float;" +
			"varying vec2 v_texture_coordinates;" +

			"uniform sampler2D s_texture;" +

			"void main() {" +
			"  gl_FragColor = texture2D( s_texture, v_texture_coordinates );" +
			"}";


	private static int loadShader(int shader_type, String shader_source)
	{
		int shader = GLES20.glCreateShader(shader_type);
		String error_info = "unable to create shader";

		if (shader != 0) {
			final int[] compileStatus = new int[1];

			GLES20.glShaderSource(shader, shader_source);
			GLES20.glCompileShader(shader);
			GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			if (compileStatus[0] == 0) {
				error_info = GLES20.glGetShaderInfoLog(shader);
				GLES20.glDeleteShader(shader);
				shader = 0;
			}
		}

		if (shader == 0) {
			throw new RuntimeException("Error creating shader: " + error_info);
		}

		return shader;
	}

	public static int create_program()
	{
		String error_info = "unable to create program";

		program_handle = vertex_shader_handle = fragment_shader_handle = mvp_matrix_handle = position_handle = texture_coordinates_handle = 0;

		vertex_shader_handle = loadShader(GLES20.GL_VERTEX_SHADER, image_vertex_shader);
		fragment_shader_handle = loadShader(GLES20.GL_FRAGMENT_SHADER, image_fragment_shader);

		program_handle = GLES20.glCreateProgram();

		if (program_handle != 0) {
			final int[] linkStatus = new int[1];
			GLES20.glAttachShader(program_handle, vertex_shader_handle);
			GLES20.glAttachShader(program_handle, fragment_shader_handle);

			GLES20.glBindAttribLocation(program_handle, 0, "a_position");
			GLES20.glBindAttribLocation(program_handle, 1, "a_texture_coordinates");

			GLES20.glLinkProgram(program_handle);

			GLES20.glGetProgramiv(program_handle, GLES20.GL_LINK_STATUS, linkStatus, 0);

			if (linkStatus[0] == 0) {
				error_info = GLES20.glGetProgramInfoLog(program_handle);
				GLES20.glDeleteProgram(program_handle);
				program_handle = 0;
			}
		}

		if (program_handle == 0) {
			throw new RuntimeException("Error creating program: " + error_info);
		}

		mvp_matrix_handle = GLES20.glGetUniformLocation(program_handle, "u_mvp_matrix");
		position_handle = GLES20.glGetAttribLocation(program_handle, "a_position");
		texture_coordinates_handle = GLES20.glGetAttribLocation(program_handle, "a_texture_coordinates");
		texture_handle = GLES20.glGetUniformLocation(program_handle, "s_texture");


		return program_handle;
	}
}

