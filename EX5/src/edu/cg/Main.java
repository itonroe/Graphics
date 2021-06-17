package edu.cg;

import org.lwjgl.opengl.GLCapabilities;

import static org.lwjgl.opengl.GL.createCapabilities;

public class Main {

    private Window window = new Window();
    private Viewer viewer = new Viewer(Window.WINDOW_INIT_WIDTH, Window.WINDOW_INIT_HEIGHT);
    private Controller controller = new Controller(window, viewer);
    private Timer timer = new Timer();

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GLCapabilities caps = createCapabilities();


        // Run the rendering loop until the user has attempted to clos e
        // the window or has pressed the ESCAPE key.

        while (!window.shouldClose()) {
            viewer.render();
            window.refresh();
            sync(30);
        }
    }

    private void sync(int fps) {
        double lastLoopTime = timer.getLastLoopTime();
        double now = timer.getTime();
        float targetTime = 1f / fps;

        while (now - lastLoopTime < targetTime) {
            Thread.yield();

            /* This is optional if you want your game to stop consuming too much
             * CPU but you will loose some accuracy because Thread.sleep(1)
             * could sleep longer than 1 millisecond */
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {

            }

            now = timer.getTime();
        }
    }

    public void run() {
        window.init();
        viewer.init();
        controller.init();
        loop();
        window.dispose();
    }

    public static void main(String[] args) {
        new Main().run();
    }


}
