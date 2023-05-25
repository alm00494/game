package game2D;

/**
 * The Projectile class represents a projectile / bullet, extending the Sprite class.
 * Similar to Platform, on reflection the only use of this class is to allow me to use instanceof
 * I thought I might need to add more functionality to the Projectile / Platform classes, but I didn't.
 */
public class Projectile extends Sprite {
    /**
     * Creates a new Sprite object with the specified Animation.
     *
     * @param anim
     */
    public Projectile(Animation anim) {
        super(anim);
    }
}
