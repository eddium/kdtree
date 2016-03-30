import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.SET;
import edu.princeton.cs.algs4.StdDraw;

public class KdTree {

    private static class Node {
        private Point2D p;      // the point
        private Node lb;        // the left/bottom subtree
        private Node rt;        // the right/top subtree
        private boolean isVertical;

        Node(Point2D p, boolean isVertical) {
            this.p = p;
            this.isVertical = isVertical;
        }

        //  return a positive number if Point should go to left or bottom, and vice versa.
        int compareTo(Point2D that) {
            double cmp;
            if (this.isVertical)
                cmp = this.p.x() - that.x();
            else
                cmp = this.p.y() - that.y();

            if (cmp > 0)
                return 1;
            else if (cmp < 0)
                return -1;
            else
                return 0;
        }

        int compareTo(RectHV that) {
            if (this.isVertical) {
                if (this.p.x() > that.xmax())
                    return 1;
                else if (this.p.x() < that.xmin())
                    return -1;
                else
                    return 0;
            } else {
                if (this.p.y() > that.ymax())
                    return 1;
                else if (this.p.y() < that.ymin())
                    return -1;
                else
                    return 0;
            }
        }
    }

    private int N;
    private Node sentinel = new Node(new Point2D(0.0, 0.0), false); //  sentinel is horizontal since root is vertical

    private Node root;

    public KdTree() {
    }                           // construct an empty set of points

    public boolean isEmpty()                      // is the set empty?
    {
        return root == null;
    }

    public int size()                         // number of points in the set
    {
        return N;
    }

    private void verify(Object o) {
        if (o == null)
            throw new java.lang.NullPointerException();
    }


    public void insert(Point2D p)              // add the point to the set (if it is not already in the set)
    {
        verify(p);
        if (!contains(p)) N++;
        root = put(root, sentinel, p);
    }

    private Node put(Node x, Node parent, Point2D p) {
        if (x == null)
            return new Node(p, !parent.isVertical);

        if (x.p.equals(p)) {
            return x;
        } else if (x.compareTo(p) > 0)
            x.lb = put(x.lb, x, p);
        else
            x.rt = put(x.rt, x, p);

        return x;
    }

    public boolean contains(Point2D p)            // does the set contain point p?
    {
        verify(p);
        return get(root, p) != null;
    }

    private Node get(Node x, Point2D p) {
        if (x == null)
            return null;

        if (x.p.equals(p))
            return x;

        if (x.compareTo(p) > 0)
            return get(x.lb, p);
        else
            return get(x.rt, p);
    }


    public void draw()                         // draw all points to standard draw
    {
        draw(root, new RectHV(0, 0, 1, 1));
    }

    private void draw(Node x, RectHV zone) {
        if (x == null) return;
        if (x.isVertical) {
            drawNode(x, zone);
            draw(x.lb, new RectHV(zone.xmin(), zone.ymin(), x.p.x(), zone.ymax()));
            draw(x.rt, new RectHV(x.p.x(), zone.ymin(), zone.xmax(), zone.ymax()));
        } else {
            drawNode(x, zone);
            draw(x.lb, new RectHV(zone.xmin(), zone.ymin(), zone.xmax(), x.p.y()));
            draw(x.rt, new RectHV(zone.xmin(), x.p.y(), zone.xmax(), zone.ymax()));
        }
    }

    private void drawNode(Node x, RectHV range) {
        StdDraw.setPenRadius(.001);

        if (x.isVertical) {
            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.line(x.p.x(), range.ymin(), x.p.x(), range.ymax());
        } else {
            StdDraw.setPenColor(StdDraw.BLUE);
            StdDraw.line(range.xmin(), x.p.y(), range.xmax(), x.p.y());
        }

        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setPenRadius(.01);
        x.p.draw();
    }


    public Iterable<Point2D> range(RectHV rect)             // all points that are inside the rectangle
    {
        verify(rect);
        SET<Point2D> set = new SET<>();
        range(root, rect, set);
        return set;
    }

    private void range(Node x, RectHV rect, SET<Point2D> set) {
        if (x == null) return;

        if (x.compareTo(rect) >= 0)
            range(x.lb, rect, set);
        if (x.compareTo(rect) <= 0)
            range(x.rt, rect, set);
        if (rect.contains(x.p))
            set.add(x.p);
    }

    private double minDistance;

    public Point2D nearest(Point2D p)                 // a nearest neighbor in the set to point p; null if the set is empty
    {
        verify(p);
        minDistance = Double.MAX_VALUE;
        return nearest(root, p, null, new RectHV(0, 0, 1, 1));
    }

    private Point2D nearest(Node x, Point2D p, Point2D champion, RectHV division) {
        if (x == null)
            return champion;

        double distance = x.p.distanceSquaredTo(p);
        if (distance < minDistance) {
            minDistance = distance;
            champion = x.p;
        }

        if (minDistance == 0)
            return champion;

        RectHV LB, RT;
        if (x.isVertical) {
            LB = new RectHV(division.xmin(), division.ymin(), x.p.x(), division.ymax());
            RT = new RectHV(x.p.x(), division.ymin(), division.xmax(), division.ymax());
        } else {
            LB = new RectHV(division.xmin(), division.ymin(), division.xmax(), x.p.y());
            RT = new RectHV(division.xmin(), x.p.y(), division.xmax(), division.ymax());
        }

        if (x.compareTo(p) > 0) {
            champion = nearest(x.lb, p, champion, LB);
            if (RT.distanceSquaredTo(p) <= minDistance)
                champion = nearest(x.rt, p, champion, RT);
        } else {
            champion = nearest(x.rt, p, champion, RT);
            if (LB.distanceSquaredTo(p) <= minDistance)
                champion = nearest(x.rt, p, champion, RT);
        }

        return champion;
    }

    public static void main(String[] args)                  // unit testing of the methods (optional)
    {
        KdTree kd = new KdTree();
        kd.insert(new Point2D(0.27353848617158194, 0.3609799055319829));
        kd.insert(new Point2D(0.6289020402382021, 0.14183750762087155));
        kd.insert(new Point2D(0.003799658831281527, 0.565878201535875));
        kd.insert(new Point2D(-0.14644390114728767, -0.4674565687294856));
        kd.insert(new Point2D(-0.2546941490180623, 0.3844121613715721));
        kd.insert(new Point2D(0.18264227166636893, 0.9002666535290024));
        kd.insert(new Point2D(0.36195907207035216, 0.00751926002106118));
        kd.insert(new Point2D(1.1736142330170032, 1.2246971665753188));
        kd.insert(new Point2D(1.0280263328057737, 0.9613013677540136));
        kd.insert(new Point2D(0.5047449548290154, -0.3869185813785929));
//        Point2D nearest = kd.nearest(new Point2D(-0.33036709507768675, 0.6317533239316923));
//        Iterable<Point2D> set = kd.range(new RectHV(-0.34634662072206124, -0.017699115044247815,
//                                                    -0.1869632182107709,  0.9410029498525073));
    }

}
