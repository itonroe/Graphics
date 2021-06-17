package edu.cg.models.Locomotive;

import edu.cg.models.IRenderable;

import static org.lwjgl.opengl.GL21.*;

/**
 * A 3D Renderable Locomotive model.
 */
public class Locomotive implements IRenderable {

    FrontBody frontBody = new FrontBody();
    BackBody backBody = new BackBody();

    public void render() {
        glPushMatrix();
        glPushMatrix();
        glTranslated(0., -Specification.BASE_UNIT, Specification.FRONT_BODY_DEPTH / 2);
        frontBody.render();
        glPopMatrix();
        glTranslated(0., 0., -Specification.BACK_BODY_DEPTH / 2);
        backBody.render();
        glPopMatrix();
    }


    @Override
    public String toString() {
        return new String("Locomotive 3D model");
    }


    @Override
    public void init() {
        // Relevant for HW6
    }

}
