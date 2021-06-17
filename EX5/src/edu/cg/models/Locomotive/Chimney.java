package edu.cg.models.Locomotive;

import edu.cg.models.IRenderable;
import edu.cg.util.glu.Cylinder;
import edu.cg.util.glu.Disk;

import static org.lwjgl.opengl.GL21.*;

/**
 * A 3D Chimney model renderer.
 * TODO(5) : read this code and understand how do we render the chimney on the front chassis.
 */
public class Chimney implements IRenderable {
    @Override
    public void render() {
        double totalLength = Specification.CHIMNEY_SECOND_TUBE_HEIGHT + Specification.CHIMNEY_FIRST_TUBE_HEIGHT;
        Materials.setMaterialChassis();
        glPushMatrix();
        glRotated(-90, 1, 0, 0);
        glTranslated(0, 0, -totalLength / 2);

        new Cylinder().draw((float) Specification.CHIMNEY_FIRST_TUBE_RADIUS,
                (float) Specification.CHIMNEY_FIRST_TUBE_RADIUS, (float) Specification.CHIMNEY_FIRST_TUBE_HEIGHT, 20, 1);


        glTranslated(0, 0, Specification.CHIMNEY_FIRST_TUBE_HEIGHT);
        glPushMatrix();
        glRotated(180, 0, 1, 0);
        new Disk().draw(0, (float) Specification.CHIMNEY_SECOND_TUBE_RADIUS, 20, 1);
        glPopMatrix();
        new Cylinder().draw((float) Specification.CHIMNEY_SECOND_TUBE_RADIUS,
                (float) Specification.CHIMNEY_SECOND_TUBE_RADIUS, (float) Specification.CHIMNEY_SECOND_TUBE_HEIGHT, 20, 1);
        glTranslated(0, 0, Specification.CHIMNEY_SECOND_TUBE_HEIGHT);
        new Disk().draw(0, (float) Specification.CHIMNEY_SECOND_TUBE_RADIUS, 20, 1);

        glPopMatrix();
    }

    @Override
    public void init() {
        // Relevant for HW6
    }
}
