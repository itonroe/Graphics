package edu.cg.models;

import static org.lwjgl.opengl.GL21.*;


/**
 * A simple 3D Box renderer. The box is centered at the origin in its local coordinate system.
 * The box can have different lengths along each of the main axes.
 */
public class Box implements IRenderable {
    private double rx, ry, rz;

    /**
     * Constructs an object that renders a 3D box centered at the origin, with lengths rx, ry and rz.
     * @param rx the length along the x-axis.
     * @param ry the length along the y-axis.
     * @param rz the length along the z-axis.
     */
    public Box(double rx, double ry, double rz) {
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
    }

    /**
     * Constructs an object that renders a 3D Square-box centered at the origin with length r.
     * @param r the edge length of the box.
     */
    public Box(double r) {
        this.rx = r;
        this.ry = r;
        this.rz = r;
    }

    @Override
    public void render() {
        glBegin(GL_QUADS);

        // TODO(1): draw the face that lies on the plane X=-rx/2
        // X=-rx/2:
        renderLeftFace();

        // X=rx/2:
        renderRightFace();

        // Y=-ry/2
        renderBottomFace();

        // TODO(1): draw the face that lies on the plane Y=ry/2
        // Y=ry/2
        renderUpperFace();

        // TODO(1): draw the face that lies on the plane Z=-rz/2
        // Z=-rz/2:
        renderBackFace();

        // Z=rz/2:
        renderFrontFace();

        glEnd();
    }

    private void renderLeftFace(){
        glNormal3d(-1, 0, 0);
        glVertex3d(-rx / 2, -ry / 2, -rz / 2);
        glVertex3d(-rx / 2, -ry / 2, rz / 2);
        glVertex3d(-rx / 2, ry / 2, rz / 2);
        glVertex3d(-rx / 2, ry / 2, -rz / 2);
    }

    private void renderRightFace(){
        glNormal3d(1, 0, 0);
        glVertex3d(rx / 2, -ry / 2, -rz / 2);
        glVertex3d(rx / 2, ry / 2, -rz / 2);
        glVertex3d(rx / 2, ry / 2, rz / 2);
        glVertex3d(rx / 2, -ry / 2, rz / 2);
    }

    private void renderBackFace(){
        glNormal3d(0, 0, -1);
        glVertex3d(-rx / 2, -ry / 2, -rz / 2);
        glVertex3d(-rx / 2, ry / 2, -rz / 2);
        glVertex3d(rx / 2, ry / 2, -rz / 2);
        glVertex3d(rx / 2, -ry / 2, -rz / 2);
    }

    private void renderFrontFace(){
        glNormal3d(0, 0, 1);
        glVertex3d(-rx / 2, -ry / 2, rz / 2);
        glVertex3d(rx / 2, -ry / 2, rz / 2);
        glVertex3d(rx / 2, ry / 2, rz / 2);
        glVertex3d(-rx / 2, ry / 2, rz / 2);
    }

    private void renderUpperFace(){
        glNormal3d(0, 1, 0);
        glVertex3d(rx / 2, ry / 2, rz / 2);
        glVertex3d(rx / 2, ry / 2, -rz / 2);
        glVertex3d(-rx / 2, ry / 2, -rz / 2);
        glVertex3d(-rx / 2, ry / 2, rz / 2);
    }

    private void renderBottomFace(){
        glNormal3d(0, -1, 0);
        glVertex3d(rx / 2, -ry / 2, rz / 2);
        glVertex3d(-rx / 2, -ry / 2, rz / 2);
        glVertex3d(-rx / 2, -ry / 2, -rz / 2);
        glVertex3d(rx / 2, -ry / 2, -rz / 2);
    }

    @Override
    public String toString() {
        return "Box";
    }

    @Override
    public void init() {
        // Relevant for HW6
    }

}
