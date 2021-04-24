package edu.cg.scene.objects;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.*;

// TODO Implement this class which represents a sphere
public class Sphere extends Shape {
	private Point center;
	private double radius;

	public Sphere(Point center, double radius) {
		this.center = center;
		this.radius = radius;
	}

	public Sphere() {
		this(new Point(0, -0.5, -6), 0.5);
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Sphere:" + endl +
				"Center: " + center + endl +
				"Radius: " + radius + endl;
	}


	public Sphere initCenter(Point center) {
		this.center = center;
		return this;
	}

	public Sphere initRadius(double radius) {
		this.radius = radius;
		return this;
	}

	@Override
	public Hit intersect(Ray ray) {
		// Calculating a,b,c parameters
		double a = calcA(ray);
		double b = calcB(ray);
		double c = calcC(ray);

		double insideSqrt = (Math.pow(b, 2) - (4.0D * a * c));

		// Check if discriminant is negative
		if (insideSqrt < 0) return null;

		// Check number of solutions = number of intersections
		double t1 = (-b - Math.sqrt(insideSqrt)) / (2.0D * a);
		double t2 = (-b + Math.sqrt(insideSqrt)) / (2.0D * a);

		// t1 must be smaller then t2 therefore
		if (t2 < Ops.epsilon) return null;

		double min = t1;

		// Normal towards the center of the sphere
		Vec N = calcNormal(t1, ray);

		// t2 is bigger then the lower threshold
		if (t1 < Ops.epsilon)
		{
			min = t2;
			N = calcNormal(t2, ray).neg();
		}

		// Upper Threshold
		if (min > Ops.infinity) return null;

		return new Hit (min, N);
	}

	private Vec calcNormal (double t, Ray ray){
		return ray.add(t).sub(this.center).normalize();
	}

	// a = Vx^2 + Vy^2 + Vz^2
	private double calcA (Ray ray){
		return ray.direction().dot(ray.direction());
	}

	// b = 2 * (V(P - Q))
	private double calcB (Ray ray) {
		return ray.direction().mult(2.0D).dot(ray.source().sub(this.center));
	}

	// c = (P - Q)^2 - r^2
	private double calcC (Ray ray) {
		return ray.source().distSqr(this.center) - Math.pow(this.radius, 2);
	}
}
