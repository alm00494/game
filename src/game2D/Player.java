package game2D;

/**
 * The Player class represents the player character in the game, extending the Sprite class.
 * It includes various attributes such as whether the player has a key card, is on a platform, or has a gun.
 */
public class Player extends Sprite {

    private boolean hasKeyCard = false;
    private boolean onPlatform = false;
    private boolean hasGun = false;

    /**
     * Creates a new Sprite object with the specified Animation.
     *
     * @param anim the Animation object representing the player character's animation
     */
    public Player(Animation anim) {
        super(anim);
        health = getMAX_HEALTH();
    }

    public void setHasKeyCard(boolean b) {
        hasKeyCard = b;
    }

    public boolean hasKeyCard() {
        return hasKeyCard;
    }

    public boolean isOnPlatform() {
        return onPlatform;
    }

    public void setOnPlatform(boolean onPlatform) {
        this.onPlatform = onPlatform;
    }

    public boolean isHasGun() {
        return hasGun;
    }

    public void setHasGun(boolean hasGun) {
        this.hasGun = hasGun;
    }
}
