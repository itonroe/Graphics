package edu.cg.scene.objects;

import edu.cg.algebra.Hit;
import edu.cg.algebra.Ray;

public interface Intersectable {
	/**
	 * Checks if the ray hits the object. If the ray hits the object,
	 * then the hit point is returned. 
	 * 
	 * NOTE: 
	 * The implementation should also indicate whether the ray is within the object or not - which is needed for the
	 * refraction bonus.
	 * 
	 * @param ray the specified ray
	 * @return The hit point of the ray with the object if exist and null otherwise. 
	 */
	public Hit intersect(Ray ray);
}
