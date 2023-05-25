package game2D;

/**
 * Circle class represents a 2D circle shape with a center point (x, y) and a radius.
 * This class was created to implement bounding circles for collision detection but
 * couldn't get it working right.
 *
 * @author 2925642
 */
public class Circle {
    private int x;      // The x-coordinate of the circle's center
    private int y;      // The y-coordinate of the circle's center
    private int radius; // The radius of the circle

    /**
     * Constructs a new Circle with the specified center point (x, y) and radius.
     *
     * @param x      The x-coordinate of the circle's center
     * @param y      The y-coordinate of the circle's center
     * @param radius The radius of the circle
     */
    public Circle(int x, int y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    /**
     * Determines whether this circle intersects with another circle.
     *
     * @param other The other circle to check for intersection
     * @return True if the two circles intersect, false otherwise
     */
    public boolean intersects(Circle other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        int distance = (int) Math.sqrt(dx * dx + dy * dy);
        return distance < this.radius + other.radius;
    }

    /**
     * Returns the x-coordinate of the circle's center.
     *
     * @return The x-coordinate of the circle's center
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y-coordinate of the circle's center.
     *
     * @return The y-coordinate of the circle's center
     */
    public int getY() {
        return y;
    }

    /**
     * Returns the radius of the circle.
     *
     * @return The radius of the circle
     */
    public int getRadius() {
        return radius;
    }
}

