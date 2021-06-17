package edu.cg;

import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;

public class Controller{
    private Window window = null;
    private Viewer viewer = null;

    private class MouseHandler implements GLFWCursorPosCallbackI {
        Point fromPoint = null;

        @Override
        public void invoke(long window, double xpos, double ypos) {
            int mouseState = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT);
            if (mouseState == GLFW_PRESS){
                Point point = new Point((int)xpos, (int)ypos);
                if (fromPoint == null){
                    fromPoint=point;
                }else{
                    viewer.trackball(fromPoint, point);
                    fromPoint = point;
                }
            }
            if (mouseState == GLFW_RELEASE && fromPoint!=null){
                fromPoint=null;
            }
        }
    }

    private class ScrollHandler implements GLFWScrollCallbackI{
        @Override
        public void invoke(long window, double xoffset, double yoffset) {
            viewer.zoom(yoffset);
        }
    }
    
    private class KeyboardHandler implements GLFWKeyCallbackI{
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (key == GLFW_KEY_W && action == GLFW_RELEASE){
                viewer.toggleRenderMode();
            }
            if (key == GLFW_KEY_A && action == GLFW_RELEASE){
                viewer.toggleAxes();
            }
            if (key == GLFW_KEY_M && action == GLFW_RELEASE){
                viewer.nextModel();
            }
        }
    }

    private class WindowSizeHandler implements GLFWWindowSizeCallbackI{
        @Override
        public void invoke(long window, int width, int height) {
            viewer.reshape(0,0, width, height);
        }
    }

    public Controller(Window window, Viewer viewer){
           this.window=window;
           this.viewer=viewer;
           System.out.println(viewer);
    }

    public void init(){
        this.window.registerMouseCallback(new MouseHandler());
        this.window.registerScrollCallback(new ScrollHandler());
        this.window.registerKeyCallback(new KeyboardHandler());
        this.window.registerWindowSizeCallback(new WindowSizeHandler());
    }



}
