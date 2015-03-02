//
//  Author:
//    Thomas Siegmund thomassiegmund@gmx.de
//
//  Copyright (c) 2014, Thomas Siegmund
//
//  All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//
//     * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in
//       the documentation and/or other materials provided with the distribution.
//     * Neither the name of the [ORGANIZATION] nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
//  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
//  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
//  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
//  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
//  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
//  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
//  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

using UnityEngine;

namespace puvo.utils
{
	public enum Extent : int
	{
		WIDTH,
		HEIGHT}
	;
	
	public class ScreenCameraTools
	{
		public static Vector2 getSceneDimensionAtPoint(Camera cam, Vector3 point)
		{
			return (new Vector2(getSceneDimensionAtPoint(cam, point, Extent.WIDTH), 
							 getSceneDimensionAtPoint(cam, point, Extent.HEIGHT)));
		}

		private static int getCameraRays(Camera cam, out Ray r1, out Ray r2, Extent dimension)
		{
			switch (dimension) {
			case Extent.WIDTH:
				float halfHeight = cam.pixelHeight / 2f;
				r1 = cam.ScreenPointToRay(new Vector3(0, halfHeight, 0));
				r2 = cam.ScreenPointToRay(new Vector3(cam.pixelWidth, halfHeight, 0));
				break;
			case Extent.HEIGHT:
				float halfWidth = cam.pixelWidth / 2f;
				r1 = cam.ScreenPointToRay(new Vector3(halfWidth, 0f, 0f));
				r2 = cam.ScreenPointToRay(new Vector3(halfWidth, cam.pixelHeight, 0));
				break;
			default:
				r1 = new Ray();
				r2 = new Ray();
				return 1;
			}
			return 0;
		}

		public static float getSceneDimensionAtPoint(Camera cam, Vector3 point, Extent dimension)
		{
			Ray r1, r2;
			float angle;

			if (getCameraRays(cam, out r1, out r2, dimension) != 0) {
				return (-1);
			}

			angle = Vector3.Angle(r1.direction, r2.direction) / 2;

			float outerDistance = getDistanceFromCameraPlane(cam, point) / 
				Mathf.Cos(angle * Mathf.PI / 180f);
			outerDistance -= cam.nearClipPlane;
		
			return ((r1.GetPoint(outerDistance) - r2.GetPoint(outerDistance)).magnitude);
		}

		public static float getDistanceFromCameraPlane(Component cam, Vector3 point)
		{
			Vector3 normal = Quaternion.Euler(cam.transform.rotation.eulerAngles) * Vector3.forward;
			Vector3 diffPoint = point - cam.transform.position;
			return (Mathf.Abs(Vector3.Dot(normal, diffPoint)));
		}

		public static float getDistanceAtWidth(Camera cam, float width, Extent dimension)
		{
			if (cam.isOrthoGraphic) {
				return (getSceneDimensionAtPoint(cam, cam.transform.position, dimension));
			} else {
				Ray r1, r2;
				if (getCameraRays(cam, out r1, out r2, dimension) != 0) {
					return (-1);
				}
				
				return (width / 2) / Mathf.Tan((Vector3.Angle(r1.direction, r2.direction) / 2f) * Mathf.PI / 180f);
			}
		}
	}
}
