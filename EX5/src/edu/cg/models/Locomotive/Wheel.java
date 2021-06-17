package edu.cg.models.Locomotive;

import edu.cg.models.IRenderable;
import edu.cg.util.glu.Cylinder;
import edu.cg.util.glu.Disk;

import static org.lwjgl.opengl.GL21.*;

/***
 * A simple 3D wheel renderer. The 3D wheel is centered at the origin, and its oriented along the x-axis.
 * This means that the wheel is parallel to the YZ-axis.
 */
public class Wheel implements IRenderable {

    final int slices = 20;
    final int stacks = 1;

    @Override
    public void render() {
        glPushMatrix();
        // TODO(3) : Render the wheel using a Cylinder, and disks that about the cylinder.

        // Rotate wheel around the y - wheel towards Z axis
        glRotated(90, 0, 1, 0);
        glTranslated(0, 0, -(Specification.WHEEL_DEPTH / 2));

        // Set sizes of wheel as a Cylinder
        Materials.setMaterialWheelTire();
        (new Cylinder()).draw((float)Specification.WHEEL_RADIUS, (float)Specification.WHEEL_RADIUS, (float)Specification.WHEEL_DEPTH, slices, stacks);

        this.drawRims();

        // Draw Rim on the other side of the wheel
        glTranslated(0, 0, Specification.WHEEL_DEPTH);
        glRotated(180, 0, 1, 0);
        this.drawRims();

        glPopMatrix();
    }

    public void drawRims() {
        glPushMatrix();
        glRotated(180, 0, 1, 0);
        Materials.setMaterialWheelRim();
        new Disk().draw(0f, (float) Specification.WHEEL_RIM_RADIUS, slices, stacks);
        Materials.setMaterialWheelTire();
        new Disk().draw((float) Specification.WHEEL_RIM_RADIUS, (float) Specification.WHEEL_RADIUS, slices, stacks);
        glPopMatrix();
    }

    @Override
    public void init() {
        // HW6 Related
    }
}
