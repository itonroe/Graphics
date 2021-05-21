package edu.cg;

import edu.cg.algebra.Vec;
import edu.cg.models.Box;
import edu.cg.models.Empty;
import edu.cg.models.IRenderable;
import edu.cg.models.Locomotive.*;

import java.awt.*;

import static org.lwjgl.opengl.GL21.*;


/**
 * An OpenGL model viewer
 */
public class Viewer {
    private double zoom = 0.0; // How much to zoom in? >0 mean come closer, <0 means get back
    private Point mouseFrom, mouseTo; // From where to where was the mouse dragged between the last redraws?
    private int canvasWidth, canvasHeight;
    private boolean isWireframe = false; // Should we display wireframe or not?
    private boolean isAxes = true; // Should we display axes or not?
    private double rotationMatrix[] = {1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1};


    // The Models that the viewer is viewing
    // TODO(0): You can add (sub)-models here to see how they are rendered in their local coordinate system.
    private final IRenderable[] models = {new Box(0.1f), new Locomotive(), new FrontBody(), new BackBody(), new Chimney(), new Wheel(),
            new Roof(), new Empty()};
    private int currentModel = 0;


    public Viewer(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
    }


    public void render() {
        //clear the window before drawing
        glClearColor(1, 1, 1, 1);
        glClearDepth(1.0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glMatrixMode(GL_MODELVIEW);

        setupCamera();
        if (isAxes)
            renderAxes();

        if (isWireframe) {
            glLineWidth(4);
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        } else {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }

        models[currentModel].render();

    }

    private Vec mousePointToVec(Point pt) {
        double x = 2 * pt.x / (double) canvasWidth - 1;
        double y = 1 - 2 * pt.y / (double) canvasHeight;
        double z2 = 2 - x * x - y * y;
        if (z2 < 0)
            z2 = 0;
        double z = Math.sqrt(z2);
        return new Vec(x, y, z).normalize();
    }

    private void setupCamera() {
        //Calculate rotation matrix
        glLoadIdentity();
        if (mouseFrom != null && mouseTo != null) {

            Vec from = mousePointToVec(mouseFrom);
            Vec to = mousePointToVec(mouseTo);
            Vec axis = from.cross(to).normalize();
            if (axis.isFinite()) {
                double angle = 180 / Math.PI * Math.acos(from.dot(to));
                angle = Double.isFinite(angle) ? angle : 0;
                glRotated(angle, axis.x, axis.y, axis.z);
            }
        }
        glMultMatrixd(rotationMatrix);
        glGetDoublev(GL_MODELVIEW_MATRIX, rotationMatrix);
        glLoadIdentity();
        glTranslated(0, 0, -1.0);
        glTranslated(0, 0, -zoom);
        glMultMatrixd(rotationMatrix);


        // We should have already changed the point of view, now set these to null
        // so we don't change it again on the next redraw.
        mouseFrom = null;
        mouseTo = null;
    }

    public void init() {
        // TODO(*) In your final submission you need to make sure that BACK FACE culling is enabled.
        //      You may disable face culling while building your model, and only later return it.
        //      Note that doing so may require you to modify the way you present the vertices to OPENGL in order for the
        //      normal of all surface be facing outside. See recitation 8 for more information about face culling.
        glCullFace(GL_BACK);    // Set Culling Face To Back Face
        glEnable(GL_CULL_FACE); // Enable back face culling

        // Enable other flags for OPENGL.
        glEnable(GL_NORMALIZE);
        glEnable(GL_DEPTH_TEST);


        reshape(0, 0, canvasWidth, canvasHeight);

        // Init Models
        for (IRenderable model : models) {
            model.init();
        }
    }

    public void reshape(int x, int y, int width, int height) {
        // TODO(9) Set the projection matrix to be the perspective matrix in the final submission.
        canvasWidth = width;
        canvasHeight = height;
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        double w, h;
        w = 3;
        h = 3 * ((double) height / width);
        glOrtho(-w / 2, w / 2, -h / 2, h / 2, -2.0, 2.0);
    }

    /**
     * Rotate model in a way that corresponds with a virtual trackball.
     * This function is called whenever the mouse is dragged inside the window.
     * The window is refreshed 30 times/sec, and if there are more than 1 mouse
     * drag event between 2 refreshes, we just need to store the first and last
     * points.
     *
     * @param from 2D canvas point of drag beginning
     * @param to   2D canvas point of drag ending
     */
    public void trackball(Point from, Point to) {
        //The following lines store the rotation for use when next displaying the model.
        //After you redraw the model, you should set these variables back to null.
        if (null == mouseFrom)
            mouseFrom = from;
        mouseTo = to;
    }

    /**
     * Zoom in or out of object. s<0 - zoom out. s>0 zoom in.
     *
     * @param s Scalar
     */
    public void zoom(double s) {
        zoom += s * 0.01;
        zoom = Math.max(zoom, -5.0);
        zoom = Math.min(zoom, 10.0);
    }

    /**
     * Toggle rendering method. Either wireframes (lines) or fully shaded
     */
    public void toggleRenderMode() {
        isWireframe = !isWireframe;
    }

    /**
     * Toggle whether axes are shown.
     */
    public void toggleAxes() {
        isAxes = !isAxes;
    }


    private void renderAxes() {
        glLineWidth(2);
        boolean flag = glIsEnabled(GL_LIGHTING);
        glDisable(GL_LIGHTING);
        glBegin(GL_LINES);
        glColor3d(1, 0, 0);
        glVertex3d(0, 0, 0);
        glVertex3d(1, 0, 0);

        glColor3d(0, 1, 0);
        glVertex3d(0, 0, 0);
        glVertex3d(0, 1, 0);

        glColor3d(0, 0, 1);
        glVertex3d(0, 0, 0);
        glVertex3d(0, 0, 1);

        glEnd();
        if (flag)
            glEnable(GL_LIGHTING);
    }

    public void nextModel() {
        currentModel = (currentModel + 1) % models.length;
    }
}
