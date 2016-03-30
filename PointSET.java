import edu.princeton.cs.algs4.*;

public class PointSET {
    SET<Point2D> points;

    public PointSET()                               // construct an empty set of points
    {
        points = new SET<Point2D>();
    }

    public boolean isEmpty()                      // is the set empty?
    {
        return points.isEmpty();
    }

    public int size()                         // number of points in the set
    {
        return points.size();
    }

    private void verify(Object o) {
        if (o == null)
            throw new java.lang.NullPointerException();
    }

    public void insert(Point2D p)              // add the point to the set (if it is not already in the set)
    {
        verify(p);
        points.add(p);
    }

    public boolean contains(Point2D p)            // does the set contain point p?
    {
        verify(p);
        return points.contains(p);
    }

    public void draw()                         // draw all points to standard draw
    {
        for (Point2D p : points) {
            StdDraw.setPenRadius(.01);
            p.draw();
        }
    }

    public Iterable<Point2D> range(RectHV rect)             // all points that are inside the rectangle
    {
        verify(rect);
        Stack<Point2D> near = new Stack<Point2D>();
        double xmin = rect.xmin();
        double xmax = rect.xmax();
        double ymin = rect.ymin();
        double ymax = rect.ymax();
        for (Point2D p : points) {
            double px = p.x();
            double py = p.y();
            if (px > xmin && px < xmax && py > ymin && py < ymax)
                near.push(p);
        }
        return near;
    }

    public Point2D nearest(Point2D p)             // a nearest neighbor in the set to point p; null if the set is empty
    {
        verify(p);
        double width = 0.4;
        double height = 0.3;
        double xmin = Math.floor(p.x() / width) * width;
        double ymin = Math.floor(p.y() / height) * height;
        RectHV rect = new RectHV(xmin, ymin, xmin + width, ymin + height);
        double minDistanceSquared = Double.MAX_VALUE;
        Point2D champion = null;
        for (Point2D k : range(rect)) {
            double distance = k.distanceSquaredTo(p);
            if (distance < minDistanceSquared) {
                minDistanceSquared = distance;
                champion = k;
            }
        }
        return champion;
    }

    public static void main(String[] args)                  // unit testing of the methods (optional)
    {

    }
}