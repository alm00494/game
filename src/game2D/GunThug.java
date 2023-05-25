package game2D;

/**
 * GunThug is a subclass of the Sprite class representing an enemy character
 * equipped with a gun. This class provides a specific implementation of a
 * Sprite with predetermined health and damage values.
 *
 * @author 2925642
 */
public class GunThug extends Sprite {

    /**
     * Creates a new GunThug object with the specified Animation.
     *
     * @param anim The Animation object associated with this GunThug
     */
    public GunThug(Animation anim) {
        super(anim);
        health = 1;
    }

}
