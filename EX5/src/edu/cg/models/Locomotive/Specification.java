package edu.cg.models.Locomotive;

/**
 * TODO(2) : Read the instructions below for further information.
 * This class contains constants that could be used when building our 3D Model.
 * BASE_UNIT is our unit measure and it represents a square in the provided sketch (see assignment instructions).
 */
public final class Specification {
    public static final double BASE_UNIT = .1;
    public static final double EPS = 1e-5;

    // Back Body
    public static final double BACK_BODY_DEPTH = 8 * BASE_UNIT;
    public static final double BACK_BODY_HEIGHT = 4 * BASE_UNIT;
    public static final double BACK_BODY_WIDTH = 4 * BASE_UNIT;

    // Front body:
    public static final double FRONT_BODY_DEPTH = 5 * BASE_UNIT;
    public static final double FRONT_BODY_HEIGHT = 2 * BASE_UNIT;
    public static final double FRONT_BODY_WIDTH = BACK_BODY_WIDTH;

    // Wheel:
    public static final double WHEEL_RIM_RADIUS = 0.75 * BASE_UNIT;
    public static final double WHEEL_RADIUS = BASE_UNIT;
    public static final double WHEEL_WIDTH = BASE_UNIT;

    // Chimney:
    public static final double CHIMNEY_FIRST_TUBE_HEIGHT = 2 * BASE_UNIT;
    public static final double CHIMNEY_FIRST_TUBE_RADIUS = BASE_UNIT;
    public static final double CHIMNEY_SECOND_TUBE_HEIGHT = 1 * BASE_UNIT;
    public static final double CHIMNEY_SECOND_TUBE_RADIUS = 1.25 * BASE_UNIT;

    // Car Light:
    public static final double CAR_LIGHT_DEPTH = 0.5 * BASE_UNIT;
    public static final double CAR_LIGHT_INNER_RADIUS = 0.4 * BASE_UNIT;
    public static final double CAR_LIGHT_OUTER_RADIUS = 0.5 * BASE_UNIT;

    // Roof:
    public static final double ROOF_DEPTH = BACK_BODY_DEPTH - 2 * EPS;
    public static final double ROOF_HEIGHT = 0.6 * BASE_UNIT;
    public static final double ROOF_WIDTH = BACK_BODY_WIDTH;

}
