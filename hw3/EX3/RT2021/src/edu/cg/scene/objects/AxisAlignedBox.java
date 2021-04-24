package edu.cg.scene.objects;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.*;


// TODO Implement this class which represents an axis aligned box
public class AxisAlignedBox extends Shape{
    private final static int NDIM=3; // Number of dimensions
    private Point a = null;
    private Point b = null;
    private double[] aAsArray;
    private double[] bAsArray;

    public AxisAlignedBox(Point a, Point b){
        this.a = a;
        this.b = b;
        // We store the points as Arrays - this could be helpful for more elegant implementation.
        aAsArray = a.asArray();
        bAsArray = b.asArray();
        assert (a.x <= b.x && a.y<=b.y && a.z<=b.z);
    }

    @Override
    public String toString() {
        String endl = System.lineSeparator();
        return "AxisAlignedBox:" + endl +
                "a: " + a + endl +
                "b: " + b + endl;
    }

    public AxisAlignedBox initA(Point a){
        this.a = a;
        aAsArray = a.asArray();
        return this;
    }

    public AxisAlignedBox initB(Point b){
        this.b = b;
        bAsArray = b.asArray();
        return this;
    }

    @Override
    public Hit intersect(Ray ray) {
        double[][] intervals = new double[2][3];
        boolean[] shouldNegateNormal = new boolean[3];
        Point p = ray.source();
        Vec v = ray.direction();

        int dim0;
        for(dim0 = 0; dim0 < 3; ++dim0) {
            double vdim = v.getCoordinate(dim0);
            double pdim = p.getCoordinate(dim0);
            if (Math.abs(vdim) <= 1.0E-5D) {
                if (pdim <= this.aAsArray[dim0] || pdim >= this.bAsArray[dim0]) {
                    return null;
                }

                intervals[0][dim0] = -1.0D / 0.0;
                intervals[1][dim0] = 1.0D / 0.0;
            } else {
                double t1 = (this.aAsArray[dim0] - pdim) / vdim;
                double t2 = (this.bAsArray[dim0] - pdim) / vdim;
                if (!(t1 <= 1.0E-5D) && (!(t2 > 1.0E-5D) || !(t2 < t1))) {
                    shouldNegateNormal[dim0] = false;
                } else {
                    shouldNegateNormal[dim0] = true;
                }

                intervals[0][dim0] = Math.min(t1, t2);
                intervals[1][dim0] = Math.max(t1, t2);
            }
        }

        dim0 = this.findMaxDim(intervals[0]);
        int dim1 = this.findMinDim(intervals[1]);
        double minT = intervals[0][dim0];
        double maxT = intervals[1][dim1];
        if (!(minT > maxT) && !(maxT <= 1.0E-5D)) {
            boolean isWithin;
            Vec normal;
            if (minT > 1.0E-5D) {
                isWithin = false;
                normal = this.getDimNormal(dim0).neg();
                if (shouldNegateNormal[dim0]) {
                    normal = normal.neg();
                }
            } else {
                minT = maxT;
                isWithin = true;
                normal = this.getDimNormal(dim1);
                if (shouldNegateNormal[dim1]) {
                    normal = normal.neg();
                }
            }

            return (new Hit(minT, normal)).setIsWithin(isWithin);
        } else {
            return null;
        }
    }

    private Vec getDimNormal(int dim) {
        switch(dim) {
            case 0:
                return new Vec(1.0D, 0.0D, 0.0D);
            case 1:
                return new Vec(0.0D, 1.0D, 0.0D);
            case 2:
                return new Vec(0.0D, 0.0D, 1.0D);
            default:
                return null;
        }
    }

    private int findMaxDim(double[] vals) {
        double minVal = vals[0];
        int minDim = 0;

        for(int dim = 1; dim < 3; ++dim) {
            if (vals[dim] > minVal) {
                minDim = dim;
                minVal = vals[dim];
            }
        }

        return minDim;
    }

    private int findMinDim(double[] vals) {
        double minVal = vals[0];
        int minDim = 0;

        for(int dim = 1; dim < 3; ++dim) {
            if (vals[dim] < minVal) {
                minDim = dim;
                minVal = vals[dim];
            }
        }

        return minDim;
    }
}

