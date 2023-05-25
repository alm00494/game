package game2D;

/**
 * The Boss class extends the {@link Sprite} class and represents a boss character in the game.
 * It has the following attributes:
 * An {@link Animation} object that defines the boss's appearance and movement.
 * A maximum health that is set as its damage value when it is created.
 * A fixed horizontal velocity and no vertical velocity.
 *
 * @author 2925642
 */
public class Boss extends Sprite {

    /**
     * Creates a new Boss object with the specified Animation.
     *
     * @param anim The {@link Animation} object that defines the appearance and movement of the boss.
     */
    public Boss(Animation anim) {
        super(anim);
        setDamage(getMAX_HEALTH());
        setHealth(getMAX_HEALTH()/5);
    }// end constructor
}// end class Boss
