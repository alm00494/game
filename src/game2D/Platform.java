package game2D;

/**
 * Platform is a subclass of the Sprite class representing an elevator.
 * This class on reflection is just used so I can use instanceof elsewhere in the code
 * I could have just used the Sprite class but I went this way instead, I think it's
 * clearer for me using instanceof.
 *
 * @author 2925642
 */
public class Platform extends Sprite {
    /**
     * Creates a new Sprite object with the specified Animation.
     *
     * @param anim
     */
    public Platform(Animation anim) {
        super(anim);
    }
}
