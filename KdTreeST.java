import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.SET;

public class KdTreeST<Value> {

    public static final boolean VERTICAL   = true;
    public static final boolean HORIZONTAL = false;

    private int N;
    private Node sentinel;
    private Node root;

    //  KdTreeST helper node data type
    private class Node {
        private Point2D p;      // the point
        private Node lb;        // the left/bottom subtree
        private Node rt;        // the right/top subtree
        private Value val;
        private boolean orientation;

        public boolean perpendicular() {
            return !orientation;
        }

        Node(Point2D p, Value val, boolean orientation) {
            this.p = p;
            this.val = val;
            this.orientation = orientation;
        }

        //  return a positive number if Point should go to left or bottom, and vice versa.
        int compareTo(Point2D that) {
            double cmp;
            if (this.orientation == VERTICAL)
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
            if (this.orientation == VERTICAL) {
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


    /**
     * Construct an empty set of points
     */
    public KdTreeST() {
        // sentinel is horizontal since root is vertical
        sentinel = new Node(new Point2D(0.0, 0.0), null, false);
    }


    /**
     * Is this symbol table empty?
     * @return <tt>true</tt> if this symbol table is empty and <tt>false</tt> otherwise
     */
    public boolean isEmpty()
    {
        return root == null;
    }


    /**
     * Returns the number of point-value pairs in this symbol table.
     * @return the number of point-value pairs in this symbol table
     */
    public int size()
    {
        return N;
    }


    private void verify(Object o) {
        if (o == null)
            throw new java.lang.NullPointerException();
    }

    /**
     * Inserts the specified point-value pair into the symbol table, overwriting the old
     * value with the new value if the symbol table already contains the specified point.
     *
     * @param p the point
     * @param val the value
     * @throws NullPointerException if <tt>point</tt> is <tt>null</tt>
     */
    public void put(Point2D p, Value val)
    {
        verify(p);
        if (!contains(p)) N++;
        root = put(root, sentinel, p, val);
    }

    // insert the point-value pair in the subtree rooted at x
    private Node put(Node x, Node parent, Point2D p, Value val) {
        if (x == null)
            return new Node(p, val, parent.perpendicular());

        if (x.p.equals(p))
            x.val = val;
        else if (x.compareTo(p) > 0)
            x.lb = put(x.lb, x, p, val);
        else
            x.rt = put(x.rt, x, p, val);

        return x;
    }


    /**
     * Does this symbol table contain the given point?
     * @param p the point
     * @return <tt>true</tt> if this symbol table contains <tt>point</tt> and
     *     <tt>false</tt> otherwise
     * @throws NullPointerException if <tt>p</tt> is <tt>null</tt>
     */
    public boolean contains(Point2D p)            // does the set contain point p?
    {
        verify(p);
        return get(root, p) != null;
    }


    /**
     * Returns the value associated with the given point.
     * @param p the point
     * @return the value associated with the given point if the point is in the symbol table
     *     and <tt>null</tt> if the point is not in the symbol table
     * @throws NullPointerException if <tt>point</tt> is <tt>null</tt>
     */
    public Value get(Point2D p) {
        verify(p);
        return get(root, p).val;
    }

    // value associated with the given point; null if no such key
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


    /***************************************************************************
     *  Range search
     ***************************************************************************/

    /**
     * Returns all points inside the rectangle.
     * @return all keys inside the rectangle <tt>rect</tt> as
     * an <tt>Iterable</tt>
     * @throws NullPointerException if <tt>rect</tt> is <tt>null</tt>
     */
    public Iterable<Point2D> range(RectHV rect)
    {
        verify(rect);
        SET<Point2D> set = new SET<>();
        range(root, rect, set);
        return set;
    }

    // add the points inside the rect in the subtree rooted at x
    // to the set recursively
    private void range(Node x, RectHV rect, SET<Point2D> set) {
        if (x == null)
            return;

        if (x.compareTo(rect) >= 0)
            range(x.lb, rect, set);

        if (x.compareTo(rect) <= 0)
            range(x.rt, rect, set);

        if (rect.contains(x.p))
            set.add(x.p);
    }



    /***************************************************************************
     *  Nearest neighbor search
     ***************************************************************************/

    /**
     * Returns a nearest neighbor in the symbol table to point p
     * @return a nearest neighbor in the symbol table to point <tt>p</tt>
     * @throws NullPointerException if <tt>p</tt> is <tt>null</tt>
     */
    private double minDistance;
    public Point2D nearest(Point2D p)
    {
        verify(p);
        minDistance = Double.MAX_VALUE;
        return nearest(root, p, null, new RectHV(0, 0, 1, 1));
    }

    //
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
        if (x.orientation == VERTICAL) {
            LB = new RectHV(division.xmin(), division.ymin(), x.p.x(), division.ymax());
            RT = new RectHV(x.p.x(), division.ymin(), division.xmax(), division.ymax());
        } else {
            LB = new RectHV(division.xmin(), division.ymin(), division.xmax(), x.p.y());
            RT = new RectHV(division.xmin(), x.p.y(), division.xmax(), division.ymax());
        }

        if (x.compareTo(p) > 0) {
            //  pruning rule
            champion = nearest(x.lb, p, champion, LB);
            if (RT.distanceSquaredTo(p) <= minDistance)
                champion = nearest(x.rt, p, champion, RT);
        } else {
            champion = nearest(x.rt, p, champion, RT);
            if (LB.distanceSquaredTo(p) <= minDistance)
                champion = nearest(x.lb, p, champion, LB);
        }

        return champion;
    }


    // Unit Testing
    public static void main(String[] args)
    {
        KdTreeST<Integer> kd = new KdTreeST<>();
        kd.put(new Point2D(0.27353848617158194, 0.3609799055319829), 0);
        kd.put(new Point2D(0.6289020402382021, 0.14183750762087155), 0);
        kd.put(new Point2D(0.003799658831281527, 0.565878201535875), 0);
        kd.put(new Point2D(-0.14644390114728767, -0.4674565687294856), 0);
        kd.put(new Point2D(-0.2546941490180623, 0.3844121613715721), 0);
        kd.put(new Point2D(0.18264227166636893, 0.9002666535290024), 0);
        kd.put(new Point2D(0.36195907207035216, 0.00751926002106118), 0);
        kd.put(new Point2D(1.1736142330170032, 1.2246971665753188), 0);
        kd.put(new Point2D(1.0280263328057737, 0.9613013677540136), 0);
        kd.put(new Point2D(0.5047449548290154, -0.3869185813785929), 0);
        Point2D nearest = kd.nearest(new Point2D(-0.33036709507768675, 0.6317533239316923));
        Iterable<Point2D> set = kd.range(new RectHV(-0.34634662072206124, -0.017699115044247815,
                                                    -0.1869632182107709,  0.9410029498525073));
    }

}
