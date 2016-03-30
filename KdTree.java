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

        Node(Point2D p) {
            this.p = p;
        }

        Node(Point2D p, boolean isVertical) {
            this(p);
            this.isVertical = isVertical;
        }
    }


    private int N;
    private Node sentinel;
    private Node root;

    public KdTree()                               // construct an empty set of points
    {
        sentinel = new Node(new Point2D(0.0, 0.0), false); //  sentinel is horizontal since root is vertical
    }

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
        root = put(root, sentinel, p);
        N++;
    }

    private Node put(Node x, Node parent, Point2D p) {
        if (x == null)
            return new Node(p, !parent.isVertical);

        double cmp;
        if (x.isVertical)
            cmp = x.p.x() - p.x();
        else
            cmp = x.p.y() - p.y();

        if (cmp > 0)
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

        double cmp;
        if (x.isVertical)
            cmp = x.p.x() - p.x();
        else
            cmp = x.p.y() - p.y();

        if (cmp > 0)
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
            drawVerticalNode(x, zone);
            draw(x.lb, new RectHV(zone.xmin(), zone.ymin(), x.p.x(), zone.ymax()));
            draw(x.rt, new RectHV(x.p.x(), zone.ymin(), zone.xmax(), zone.ymax()));
        } else {
            drawHorizontalNode(x, zone);
            draw(x.lb, new RectHV(zone.xmin(), zone.ymin(), zone.xmax(), x.p.y()));
            draw(x.rt, new RectHV(zone.xmin(), x.p.y(), zone.xmax(), zone.ymax()));
        }
    }


    private void drawVerticalNode(Node x, RectHV range) {
        StdDraw.setPenColor(StdDraw.RED);
        StdDraw.setPenRadius(.001);
        StdDraw.line(x.p.x(), range.ymin(), x.p.x(), range.ymax());
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setPenRadius(.01);
        x.p.draw();
    }

    private void drawHorizontalNode(Node x, RectHV range) {
        StdDraw.setPenColor(StdDraw.BLUE);
        StdDraw.setPenRadius(.001);
        StdDraw.line(range.xmin(), x.p.y(), range.xmax(), x.p.y());
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

        if (hasInLB(x, rect))
            range(x.lb, rect, set);
        if (hasInRT(x, rect))
            range(x.rt, rect, set);
        if (lieInRect(x, rect))
            set.add(x.p);
    }


    private boolean hasInLB(Node x, RectHV rect) {
        return x.isVertical && rect.xmin() < x.p.x() || !x.isVertical && rect.ymin() < x.p.y();
    }

    private boolean hasInRT(Node x, RectHV rect) {
        return x.isVertical && rect.xmax() >= x.p.x() || !x.isVertical && rect.ymax() >= x.p.y();
    }

    private boolean lieInRect(Node x, RectHV rect) {
        double px = x.p.x();
        double py = x.p.y();
        return px >= rect.xmin() &&  py>= rect.ymin() && px <= rect.xmax() && py <= rect.ymax();
    }

    public Point2D nearest(Point2D p)                 // a nearest neighbor in the set to point p; null if the set is empty
    {
        verify(p);
        return nearest(root, p, Double.MAX_VALUE, null);  // nearest point is null before recursion
    }

    private Point2D nearest(Node x, Point2D p, double minDistance, Point2D champion) {
        if (x == null)
            return champion;

        double distanceSquared = x.p.distanceSquaredTo(p);
        if (distanceSquared < minDistance) {
            minDistance = distanceSquared;
            champion = x.p;
        }

        double cmp;
        if (x.isVertical)
            cmp = x.p.x() - p.x();
        else
            cmp = x.p.y() - p.y();

        if (cmp > 0) {
            champion = nearest(x.lb, p, minDistance, champion);
            //  pruning rule
            if (x.isVertical && minDistance > p.distanceSquaredTo(new Point2D(x.p.x(), p.y()))
                    || !x.isVertical && minDistance > p.distanceSquaredTo(new Point2D(p.x(), x.p.y()))) {
                champion = nearest(x.rt, p, minDistance, champion);
            }
        } else {
            champion = nearest(x.rt, p, minDistance, champion);
            //  pruning rule
            if (x.isVertical && minDistance > p.distanceSquaredTo(new Point2D(x.p.x(), p.y()))
                    || !x.isVertical && minDistance > p.distanceSquaredTo(new Point2D(p.x(), x.p.y()))) {
                champion = nearest(x.lb, p, minDistance, champion);
            }
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
        Point2D nearest = kd.nearest(new Point2D(-0.33036709507768675, 0.6317533239316923));
        Iterable<Point2D> set = kd.range(new RectHV(-0.34634662072206124, -0.017699115044247815 , -0.1869632182107709, 0.9410029498525073));
    }

}
