package game2D;

/**
 * The Thug class represents an enemy character, extending the Sprite class.
 * Again I thought I would do more with subclasses of Sprite, but I didn't
 * I do find it useful for the instanceof check in the Game class and it allows for extension in the future.
 *
 * @author 2925642
 */
public class Thug extends Sprite {
    /**
     * Creates a new Sprite object with the specified Animation.
     *
     * @param anim
     */
    public Thug(Animation anim) {
        super(anim);
    }
}
