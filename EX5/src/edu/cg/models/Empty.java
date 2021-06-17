package edu.cg.models;

/**
 * A simple axes dummy 
 *
 */
public class Empty implements IRenderable {	
	
	private boolean isLightSpheres;

	@Override
	public void render() {
	}
	

	@Override
	public String toString() {
		return "Empty";
	}

	@Override
	public void init() {
		
	}
}
