package edu.cg.models.Locomotive;

import edu.cg.models.IRenderable;
import edu.cg.util.glu.Cylinder;
import edu.cg.util.glu.Disk;

import static org.lwjgl.opengl.GL21.*;

/***
 * A 3D CarLight renderer. The locomotive light is oriented such that it is parallel to the XY plane.
 * TODO(4) : read this code and understand how do we render the locomotive light.
 */
public class CarLight implements IRenderable {
    @Override
    public void render() {
        glPushMatrix();
        Materials.setMaterialLightCase();
        new Cylinder().draw((float) Specification.CAR_LIGHT_OUTER_RADIUS,
                (float) Specification.CAR_LIGHT_OUTER_RADIUS,
                (float) Specification.CAR_LIGHT_DEPTH, 20, 1);
        glTranslated(0., 0., Specification.CAR_LIGHT_DEPTH);
        new Disk().draw((float) Specification.CAR_LIGHT_INNER_RADIUS, (float) Specification.CAR_LIGHT_OUTER_RADIUS, 20, 1);
        Materials.setMaterialFrontLight();
        new Disk().draw(0, (float) Specification.CAR_LIGHT_INNER_RADIUS, 20, 1);
        glPopMatrix();
    }

    @Override
    public void init() {

    }
}
