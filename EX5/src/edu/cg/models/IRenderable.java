package edu.cg.models;


/**
 * A renderable model
 */
public interface IRenderable {

    //Constants for types passed to the control function.
    //You can add more here, or add model-specific constants in the model class.
    public static final int TOGGLE_LIGHT_SPHERES = 0;

    /**
     * Render the model
     */
    public void render();

    /**
     * Initialize the model
     */
    public void init();

}
