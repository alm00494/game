package game2D;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * This class provides the functionality for a moving animated image or Sprite.
 *
 * @author David Cairns
 * @author 2925642
 */
public class Sprite {

    // The draw offset associated with this sprite. Used to draw it
    // relative to specific on screen position (usually the player)
    private int xoff = 0;
    private int yoff = 0;

    // The current Animation to use for this sprite
    private Animation anim;

    // Position (pixels)
    private float x;
    private float y;

    // Velocity (pixels per millisecond)
    private float dx;
    private float dy;

    // Dimensions of the sprite
    private float height;
    private float width;
    private float radius;

    // The scale to draw the sprite at where 1 equals normal size
    private double xscale;
    private double yscale;
    private long lastAttack = -1;

    private double rotation; // The rotation to apply to the sprite image

    private boolean render; // If render is 'true', the sprite will be drawn when requested

    // The following variables are used to control the sprite's movement and actions
    private boolean flipped, flippedVertically, onGround, jump, moveRight, moveLeft, attack, hasWeapon, stationary;
    private boolean alive = true; // If the sprite is alive or not
    int health = 1; // base health of default sprite
    final int MAX_HEALTH = 50; // maximum health of any sprite
    int damage = 1; // base damage of default sprite

    /**
     * Creates a new Sprite object with the specified Animation.
     *
     * @param // animation to use for the sprite.
     */
    public Sprite(Animation anim) {
        this.anim = anim;
        render = true;
        xscale = 1.0f;
        yscale = 1.0f;
        rotation = 0.0f;
    }

    /**
     * Change the animation for the sprite to 'a'.
     *
     * @param a The animation to use for the sprite.
     */
    public void setAnimation(Animation a) {
        anim = a;
    }

    /**
     * Set the current animation to the given 'frame'
     *
     * @param frame The frame to set the animation to
     */
    public void setAnimationFrame(int frame) {
        anim.setAnimationFrame(frame);
    }

    /**
     * Pauses the animation at its current frame. Note that the
     * sprite will continue to move, it just won't animate
     */
    public void pauseAnimation() {
        anim.pause();
    }

    /**
     * Pause the animation when it reaches frame 'f'.
     *
     * @param f The frame to stop the animation at
     */
    public void pauseAnimationAtFrame(int f) {
        anim.pauseAt(f);
    }

    /**
     * Change the speed at which the current animation runs. A
     * speed of 1 will result in a normal animation,
     * 0.5 will be half the normal rate and 2 will double it.
     * <p>
     * Note that if you change animation, it will run at whatever
     * speed it was previously set to.
     *
     * @param speed The speed to set the current animation to.
     */
    public void setAnimationSpeed(float speed) {
        anim.setAnimationSpeed(speed);
    }

    /**
     * Starts an animation playing if it has been paused.
     */
    public void playAnimation() {
        anim.play();
    }

    /**
     * Returns a reference to the current animation
     * assigned to this sprite.
     *
     * @return A reference to the current animation
     */
    public Animation getAnimation() {
        return anim;
    }

    /**
     * Updates this Sprite's Animation and its position based
     * on the elapsedTime.
     *
     * @param // time that has elapsed since the last call to update
     */
    public void update(long elapsedTime) {
        if (!render) return;
        x += dx * elapsedTime;
        y += dy * elapsedTime;
        anim.update(elapsedTime);
        width = getWidth();
        height = getHeight();
        if (width > height)
            radius = width / 2.0f;
        else
            radius = height / 2.0f;
    }

    /**
     * Stops the sprites movement at the current position
     */
    public void stop() {
        dx = 0;
        dy = 0;
    }

    /**
     * Gets this Sprite's current image.
     */
    public Image getImage() {
        return anim.getImage();
    }

    /**
     * Draws the sprite with the graphics object 'g' at
     * the current x and y co-ordinates. Scaling and rotation
     * transforms are NOT applied.
     */
    public void draw(Graphics2D g) {
        if (!render) return;
        g.drawImage(getImage(), (int) x + xoff + getWidth() / 6, (int) y + yoff, null);
    }

    /**
     * Draws the sprite with the graphics object 'g' at
     * the current x and y co-ordinates. Scaling and rotation
     * transforms are applied to flip the sprite horizontally.
     */
    public void drawTransformedFlip(Graphics2D g) {
        if (!render) return;

        AffineTransform transform = new AffineTransform();

        // Apply scaling to current x and y positions to
        // ensure shifted left and up when flipped due to scaling.
        float shiftx = 0;
        float shifty = 0;
        if (xscale < 0) shiftx = getWidth();
        if (yscale < 0) shifty = getHeight();

        transform.translate(Math.round(x) + shiftx + xoff + getWidth() * 0.75, Math.round(y) + shifty + yoff);
        transform.scale(getScaleX(), getScaleY());
        transform.rotate(rotation, getImage().getWidth(null) / 6, getImage().getHeight(null) / 2);
        transform.scale(-1, 1); // flip horizontally
        // Apply transform to the image and draw it
        g.drawImage(getImage(), transform, null);
    }

    /**
     * Draws the sprite with the graphics object 'g' at
     * the current x and y co-ordinates. Scaling and rotation
     * transforms are applied to flip the sprite vertically.
     */
    public void drawTransformedFlipVertical(Graphics2D g) {
        if (!render) return;

        AffineTransform transform = new AffineTransform();

        // Apply scaling to current x and y positions to
        // ensure shifted left and up when flipped due to scaling.
        float shiftx = 0;
        float shifty = 0;
        if (xscale < 0) shiftx = getWidth();
        if (yscale < 0) shifty = getHeight();

        // Add the image height to the vertical translation
        transform.translate(Math.round(x) + shiftx + xoff + getWidth() / 6, Math.round(y) + shifty + yoff + getImage().getHeight(null));
        transform.scale(getScaleX(), getScaleY());
        transform.rotate(getRotation(), getImage().getWidth(null) / 6, getImage().getHeight(null) / 2);
        transform.scale(1, -1); // flip vertically
        // Apply transform to the image and draw it
        g.drawImage(getImage(), transform, null);
    }

    /**
     * Draws the bounding box of this sprite using the graphics object 'g' and
     * the currently selected foreground colour.
     */
    public void drawBoundingBox(Graphics2D g) {
        if (!render) return;
        Image img = getImage();
        g.drawRect((int) x + xoff + getWidth() / 4, (int) y + yoff, (img.getWidth(null) / 2), img.getHeight(null));
    }

    /**
     * Draws the bounding circle of this sprite using the graphics object 'g' and
     * the currently selected foreground colour.
     */
    public void drawBoundingCircle(Graphics2D g) {
        if (!render) return;

        Image img = getImage();

        g.drawArc((int) x + xoff - (img.getWidth(null) / 4), (int) y + yoff, img.getWidth(null), img.getHeight(null), 0, 360);
    }

    /**
     * Gets the bounding circle of this sprite.
     * Tried to get bounding circle / oval collision working but it didn't work.
     */
    public Circle getBoundingCircle() {
        int radius = getImage().getWidth(null) / 4;
        int x = (int) (this.x + xoff - radius);
        int y = (int) (this.y + yoff - radius);
        return new Circle(x, y, radius);
    }

    /**
     * Hide the sprite.
     */
    public void hide() {
        render = false;
    }

    /**
     * Show the sprite
     */
    public void show() {
        render = true;
    }

    /**
     * Set an x & y offset to use when drawing the sprite.
     * Note this does not affect its actual position, just
     * moves the drawn position.
     */
    public void setOffsets(int x, int y) {
        xoff = x;
        yoff = y;
    }

    /**
     * Get onGround boolean
     */
    public boolean isOnGround() {
        return onGround;
    }

    /**
     * Set onGround boolean
     */
    public void setOnGround(boolean b) {
        onGround = b;
    }

    /**
     * Get the bounding box of this sprite.
     */
    public Rectangle getBoundingBox() {
        return new Rectangle((int) x + xoff + getWidth() / 2, (int) y + yoff, ((getWidth() / 2)), getHeight());
    }

    /**
     * Get the boolean of whether the sprite is colliding with another sprite.
     */
    public boolean collidesWith(Sprite s2) {
        return getBoundingBox().intersects(s2.getBoundingBox());
    }

    /**
     * Get the boolean of whether the sprite is stationary.
     */
    public boolean isStationary() {
        return stationary;
    }

    /**
     * Set the boolean of whether the sprite is stationary.
     */
    public void setStationary(boolean b) {
        stationary = b;
    }

    /**
     * set the boolean of whether the sprite is flipped vertically.
     */
    public void setFlippedVertically(boolean b) {
        flippedVertically = b;
    }

    /**
     * get the boolean of whether the sprite is flipped vertically.
     */
    public boolean isFlippedVertically() {
        return flippedVertically;
    }

    /**
     * Get the boolean of whether the sprite has a weapon
     */
    public boolean isHasWeapon() {
        return hasWeapon;
    }

    /**
     * Set the boolean of whether the sprite has a weapon
     */
    public void setHasWeapon(boolean hasWeapon) {
        this.hasWeapon = hasWeapon;
    }

    /**
     * Get the boolean of whether the sprite is alive
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * Set the boolean of whether the sprite is alive
     */
    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    /**
     * Get the boolean of whether the sprite is jumping
     */
    public boolean isJump() {
        return jump;
    }

    /**
     * Set the boolean of whether the sprite is jumping
     */
    public void setJump(boolean jump) {
        this.jump = jump;
    }

    /**
     * Get the boolean of whether the sprite is moving right
     */
    public boolean isMoveRight() {
        return moveRight;
    }

    /**
     * Set the boolean of whether the sprite is moving right
     */
    public void setMoveRight(boolean moveRight) {
        this.moveRight = moveRight;
    }

    /**
     * Get the boolean of whether the sprite is moving left
     */
    public boolean isMoveLeft() {
        return moveLeft;
    }

    /**
     * Set the boolean of whether the sprite is moving left
     */
    public void setMoveLeft(boolean moveLeft) {
        this.moveLeft = moveLeft;
    }

    /**
     * Get the boolean of whether the sprite is attacking
     */
    public boolean isAttack() {
        return attack;
    }

    /**
     * Set the boolean of whether the sprite is attacking
     */
    public void setAttack(boolean attack) {
        this.attack = attack;
    }

    /**
     * Gets this Sprite's current x position.
     */
    public float getX() {
        return x;
    }

    /**
     * Gets this Sprite's current y position.
     */
    public float getY() {
        return y;
    }

    /**
     * Sets this Sprite's current x position.
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * Sets this Sprite's current y position.
     */
    public void setY(float y) {
        this.y = y;
    }

    /**
     * Sets this Sprite's new x and y position.
     */
    public void setPosition(float x, float y) {
        setX(x);
        setY(y);
    }

    /**
     * Sets the flipped state of this sprite.
     */
    public void setFlipped(boolean f) {
        flipped = f;
    }

    /**
     * Gets the flipped state of this sprite.
     */
    public boolean isFlipped() {
        return flipped;
    }

    /**
     * Gets the MAX_HEALTH of this sprite.
     */
    public int getMAX_HEALTH() {
        return MAX_HEALTH;
    }

    /**
     * Gets the last attack time of this sprite in milliseconds.
     */
    public long getLastAttack() {
        return lastAttack;
    }

    /**
     * Sets the last attack time of this sprite in milliseconds.
     */
    public void setLastAttack(long lastAttack) {
        this.lastAttack = lastAttack;
    }

    /**
     * gets the current health of the sprite
     *
     * @return health
     */
    public int getHealth() {
        return health;
    }

    /**
     * sets the current health of the sprite
     *
     * @param health
     */
    public void setHealth(int health) {
        this.health = health;
    }

    /**
     * gets the damage of the sprite
     *
     * @return damage
     */
    public int getDamage() {
        return damage;
    }

    /**
     * sets the damage of the sprite
     *
     * @param damage
     */
    public void setDamage(int damage) {
        this.damage = damage;
    }

    /**
     * Gets this Sprite's width, based on the size of the
     * current image.
     */
    public int getWidth() {
        return (int) (anim.getImage().getWidth(null) * Math.abs(xscale));
    }

    /**
     * Gets this Sprite's height, based on the size of the
     * current image.
     */
    public int getHeight() {
        return (int) (anim.getImage().getHeight(null) * Math.abs(yscale));
    }

    /**
     * Gets the sprites radius in pixels
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Gets the horizontal velocity of this Sprite in pixels
     * per millisecond.
     */
    public float getVelocityX() {
        return dx;
    }

    /**
     * Gets the vertical velocity of this Sprite in pixels
     * per millisecond.
     */
    public float getVelocityY() {
        return dy;
    }


    /**
     * Sets the horizontal velocity of this Sprite in pixels
     * per millisecond.
     */
    public void setVelocityX(float dx) {
        this.dx = dx;
    }

    /**
     * Sets the vertical velocity of this Sprite in pixels
     * per millisecond.
     */
    public void setVelocityY(float dy) {
        this.dy = dy;
    }

    /**
     * Sets the horizontal and vertical velocity of this Sprite in pixels
     * per millisecond.
     */
    public void setVelocity(float dx, float dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Set the x and y scale of the sprite to 'scx' and 'scy' respectively.
     * If scx and scy are 1, the sprite will be drawn at normal size. If they
     * are 0.5 it will be drawn at half size. If scx is -1, the sprite will be
     * flipped along its vertical axis (it will face left instead of right).
     * Negative values of scy will flip along the horizontal axis. The flipping
     * and scaling of the sprite are now accounted for when setting a sprite
     * position and getting its width and height (you will always reference
     * the top left of the sprite irrespective of the scaling).
     * Note that scaling and rotation are only applied when
     * using the drawTransformed method.
     */
    public void setScale(float scx, float scy) {
        xscale = scx;
        yscale = scy;
    }

    /**
     * Set the scale of the sprite to 's'. If s is 1
     * the sprite will be drawn at normal size. If 's'
     * is 0.5 it will be drawn at half size. Note that
     * scaling and rotation are only applied when
     * using the drawTransformed method.
     */
    public void setScale(float s) {
        xscale = s;
        yscale = s;
    }


    /**
     * Get the current value of the x scaling attribute.
     * See 'setScale' for more information.
     */
    public double getScaleX() {
        return xscale;
    }

    /**
     * Get the current value of the y scaling attribute.
     * See 'setScale' for more information.
     */
    public double getScaleY() {
        return yscale;
    }

    /**
     * Set the rotation angle for the sprite in degrees.
     * Note that scaling and rotation are only applied when
     * using the drawTransformed method.
     */
    public void setRotation(double r) {
        rotation = Math.toRadians(r);
    }

    /**
     * Get the current value of the rotation attribute.
     * in degrees. See 'setRotation' for more information.
     */
    public double getRotation() {
        return Math.toDegrees(rotation);
    }


}
