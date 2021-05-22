package edu.cg.models.Locomotive;

import edu.cg.models.Box;
import edu.cg.models.IRenderable;

import static org.lwjgl.opengl.GL21.*;


/***
 * A 3D locomotive back body renderer. The back-body of the locomotive model is composed of a chassis, two back wheels,
 * , a roof, windows and a door.
 */
public class BackBody implements IRenderable {
    // The back body is composed of one box that represents the locomotive front body.
    private Box chassis = new Box(Specification.BACK_BODY_WIDTH, Specification.BACK_BODY_HEIGHT, Specification.BACK_BODY_DEPTH);
    // The back body is composed of two back wheels.
    private Wheel wheel = new Wheel();
    // The back body is composed of a roof that lies on-top of the locomotive chassis.
    private Roof roof = new Roof();
    // TODO(9): Define your window/door objects here. You are free to implement these models as you wish as long as you
    //           stick to the locomotive sketch.

    private Box door = new Box(Specification.EPS, Specification.BACK_BODY_HEIGHT - 0.05, Specification.WINDOW_WIDTH);

    private Box window = new Box(Specification.EPS, (Specification.BACK_BODY_HEIGHT) / 2, Specification.WINDOW_WIDTH);

    private Box windowBigFront = new Box(Specification.WINDOW_WIDTH * 2, (Specification.BACK_BODY_HEIGHT) / 2 - 0.05 ,Specification.EPS);
    private Box windowBigBack = new Box(Specification.WINDOW_WIDTH * 2, (Specification.BACK_BODY_HEIGHT) / 2 ,Specification.EPS);

    @Override
    public void render() {
        glPushMatrix();
        // TODO(8): render the back-body of the locomotive model. You need to combine the chassis, wheels and roof using
        //          affine transformations. In addition, you need to render the back-body windows and door. You can do
        //          that using simple QUADRATIC polygons (use GL_QUADS).

        Materials.setMaterialChassis();
        renderChassis();

        renderRoof();
        renderWheels();

        Materials.setMaterialWindow();
        renderDoor();
        renderWindows();
        renderFrontBackWindows();

        glPopMatrix();
    }

    private void renderRoof() {
        glTranslated(0, Specification.BACK_BODY_HEIGHT / 2,  - Specification.ROOF_DEPTH / 2);
        this.roof.render();
        glPopMatrix();
    }

    private void renderDoor() {
        glPushMatrix();

        glTranslated(-Specification.BACK_BODY_WIDTH / 2 - Specification.EPS, -0.025, Specification.BACK_BODY_DEPTH / 4);
        door.render();

        glPopMatrix();
    }

    private void renderWindows() {
        glPushMatrix();

        glTranslated(Specification.BACK_BODY_WIDTH / 2 + Specification.EPS, (Specification.BACK_BODY_HEIGHT) / 8 , Specification.BACK_BODY_DEPTH / 4);
        window.render();

        glTranslated(0, 0 , -Specification.BACK_BODY_DEPTH / 4);
        window.render();
        showWindowFlip();

        glTranslated(0, 0 , -Specification.BACK_BODY_DEPTH / 4);
        window.render();
        showWindowFlip();

        glPopMatrix();
    }

    private void renderFrontBackWindows() {
        glPushMatrix();
        glTranslated(0,(Specification.BACK_BODY_HEIGHT) / 4 - 0.025,Specification.BACK_BODY_DEPTH / 2 + Specification.EPS);
        windowBigFront.render();
        glPopMatrix();

        glPushMatrix();
        glTranslated(0,(Specification.BACK_BODY_HEIGHT) / 8 , -(Specification.BACK_BODY_DEPTH / 2 + Specification.EPS));
        windowBigBack.render();
        glPopMatrix();
    }

    private void showWindowFlip() {
        glPushMatrix();

        glTranslated(- Specification.BACK_BODY_WIDTH - (2 * Specification.EPS), 0,0);
        window.render();

        glPopMatrix();
    }

    private void renderChassis(){
        this.chassis.render();

        // Set chassis as base for the rest of the car
        glPushMatrix();
    }

    private void renderWheels(){
        // Move the wheels back 3/4 of the back body and down to touch the "surface".
        glTranslated(0, - Specification.BACK_BODY_HEIGHT / 2, - Specification.BACK_BODY_DEPTH / 4);
        glPushMatrix();

        glTranslated(Specification.BACK_BODY_WIDTH / 2, 0, 0);
        this.wheel.render();
        glPopMatrix();

        glTranslated(-Specification.BACK_BODY_WIDTH / 2, 0, 0);
        this.wheel.render();
        glPopMatrix();
    }

    @Override
    public void init() {

    }
}
