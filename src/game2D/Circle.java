package game2D;

public class Circle {
    private int x;
    private int y;
    private int radius;

    public Circle(int x, int y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public boolean intersects(Circle other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        int distance = (int) Math.sqrt(dx * dx + dy * dy);
        return distance < this.radius + other.radius;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRadius() {
        return radius;
    }
}

