package edu.cg.scene.camera;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;

public class PinholeCamera {
	// 
	Point cameraPosition;
	Point centerPoint;
	Vec towardsVec;
	Vec upVec;
	Vec rightVec;
	double planeWidth;
	double distanceToPlain;
	double resX;
	double resY;

	/**
	 * Initializes a pinhole camera model with default resolution 200X200 (RxXRy) and view angle 90.
	 * @param cameraPosition - The position of the camera.
	 * @param towardsVec - The towards vector of the camera (not necessarily normalized).
	 * @param upVec - The up vector of the camera.
	 * @param distanceToPlain - The distance of the camera (position) to the center point of the image-plain.
	 * 
	 */
	public PinholeCamera(Point cameraPosition, Vec towardsVec, Vec upVec, double distanceToPlain) {
		this.cameraPosition = cameraPosition;
		this.towardsVec = towardsVec.normalize();
		this.rightVec = this.towardsVec.cross(upVec).normalize();
		this.upVec = this.rightVec.cross(this.towardsVec).normalize();
		this.distanceToPlain = distanceToPlain;
		this.centerPoint = new Ray(cameraPosition, towardsVec).add(distanceToPlain);
		resX = 200;
		resY = 200;
		planeWidth = 200;
	}

	/**
	 * Initializes the resolution and width of the image.
	 * @param height - the number of pixels in the y direction.
	 * @param width - the number of pixels in the x direction.
	 * @param planeWidth - the width of the image plane in the model coordinate system.
	 */
	public void initResolution(int height, int width, double planeWidth) {
		this.planeWidth = planeWidth;
		resX = width;
		resY = height;
	}

	/**
	 * Transforms from pixel coordinates to the center point of the corresponding pixel in model coordinates.
	 * @param x - the pixel index in the x direction.
	 * @param y - the pixel index in the y direction.
	 * @return the middle point of the pixel (x,y) in the model coordinates.
	 */
	public Point transform(int x, int y) {
		// Assuming that the pixel is square -> pixelWidth == pixelHeight
		// Get the pixel ratio in that plane
		double ratio = this.planeWidth / this.resX;

		// Finding center pixel
		int centerPixelY = (int)(this.resY / 2.0D);
		int centerPixelX = (int)(this.resX / 2.0D);

		// Calculate the distance from center
		double upDistanceFromCenter = (double)(y - centerPixelY) * ratio;
		double rightDistanceFromCenter = (double)(x - centerPixelX) * ratio;

		// Calculating distance from center multiply direction vector
		Vec up = this.upVec.mult(upDistanceFromCenter);
		Vec right = this.rightVec.mult(rightDistanceFromCenter);

		// P = Pcenter + (DistanceRight)*Vright - (DistanceUp)*Vup
		return this.centerPoint.add(right).add(up.neg());
	}
	
	/**
	 * Returns the camera position
	 * @return a new point representing the camera position.
	 */
	public Point getCameraPosition() {
		return new Point(cameraPosition.x, cameraPosition.y, cameraPosition.z);
	}

	/**
	 * @return The vector which points in the right direction of the camera
	 */
	public Vec getRightVec(){
		return rightVec;
	}


	/**
	 * @return The vector which poins in the up direction of the camera
	 */
	public Vec getUpVec(){
		return upVec;
	}

	/**
	 *
	 * @return The length of a pixel on the image plane (Pixels are assume to be square pixels).
	 */
	public double getPixelLength(){
		if(resX == 0){
			throw new ExceptionInInitializerError("The field resX is invalid");
		}
		return (double) planeWidth/resX;
	}
}
