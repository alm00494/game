import game2D.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

// Game demonstrates how we can override the GameCore class
// to create our own 'game'. We usually need to implement at
// least 'draw' and 'update' (not including any local event handling)
// to begin the process. You should also add code to the 'init'
// method that will initialise event handlers etc.

// Student ID: ???????

@SuppressWarnings("serial")

public class Game extends GameCore {
    // Useful game constants
    static int screenWidth = 512;
    static int screenHeight = 400;
    float gravity = 0.001f;
    float jumpVelocity = -0.4f;
    float moveSpeed = 0.2f;
    boolean debug = true;
    long lastAttack = 10000;

    // Game resources
    Animation playerIdle, playerRunning, playerJumping, playerDeath, playerAttacking, stab, knifeRun;
    Animation thugIdle, thugRunning, thugDeath, thugAttacking;
    BufferedImage bgImage1, bgImage2, bgImage3, youDiedImage;
    Sprite player = null;
    Sprite thug = null;
    ArrayList<Sprite> clouds = new ArrayList<Sprite>();
    TileMap tmap = new TileMap();    // Our tile map, note that we load it in init()
    long total;                    // The score will be the total time elapsed since a crash
    private Sprite knife;

    /**
     * The obligatory main method that creates
     * an instance of our class and starts it running
     *
     * @param args The list of parameters this program might use (ignored)
     */
    public static void main(String[] args) {
        Game gct = new Game();
        gct.init();
        // Start in windowed mode with the given screen height and width
        gct.run(false, screenWidth, screenHeight);
    }

    /**
     * Initialise the class, e.g. set up variables, load images,
     * create animations, register event handlers.
     * <p>
     * This shows you the general principles but you should create specific
     * methods for setting up your game that can be called again when you wish to
     * restart the game (for example you may only want to load animations once
     * but you could reset the positions of sprites each time you restart the game).
     */
    public void init() {
        Sprite s;    // Temporary reference to a sprite
        // Load the tile map and print it out so we can check it is valid
        tmap.loadMap("maps", "map.txt");
        setSize(tmap.getPixelWidth() / 4, tmap.getPixelHeight());
        setVisible(true);

        // Create a set of background sprites that we can
        // rearrange to give the illusion of motion

        playerIdle = new Animation();
        playerIdle.loadAnimationFromSheet("images/player/idle.png", 4, 1, 60);

        playerAttacking = new Animation();
        playerAttacking.loadAnimationFromSheet("images/player/punch.png", 6, 1, 60);

        playerRunning = new Animation();
        playerRunning.loadAnimationFromSheet("images/player/run.png", 6, 1, 60);

        playerJumping = new Animation();
        playerJumping.loadAnimationFromSheet("images/player/jump.png", 4, 1, 60);

        playerDeath = new Animation();
        playerDeath.loadAnimationFromSheet("images/player/death.png", 6, 1, 60);
        playerDeath.setLoop(false);

        thugIdle = new Animation();
        thugIdle.loadAnimationFromSheet("images/thug/Thug_idle.png", 4, 1, 60);

        thugRunning = new Animation();
        thugRunning.loadAnimationFromSheet("images/thug/Thug_Walk.png", 6, 1, 60);

        thugAttacking = new Animation();
        thugAttacking.loadAnimationFromSheet("images/thug/Thug_attack1.png", 6, 1, 60);

        thugDeath = new Animation();
        thugDeath.loadAnimationFromSheet("images/thug/Death.png", 6, 1, 60);

        stab = new Animation();
        stab.loadAnimationFromSheet("images/player/knifeStab.png", 6, 1, 60);

        knifeRun = new Animation();
        knifeRun.loadAnimationFromSheet("images/player/knifeRun.png", 6, 1, 60);

        // Initialise the player with an animation
        player = new Sprite(playerIdle);

        knife = new Sprite(knifeRun);

        // Initialise the thug with an animation
        thug = new Sprite(thugRunning);
        thug.setFlipped(true);

        // Load a single cloud animation
        Animation ca = new Animation();
        ca.addFrame(loadImage("images/cloud.png"), 1000);

        // Create 3 clouds at random positions off the screen
        // to the right
        for (int i = 0; i < 3; i++) {
            s = new Sprite(ca);
            s.setPosition(screenWidth * 2 + (int) (Math.random() * 500), (int) (Math.random() * 300));
            s.setVelocityX(-0.1f);
            clouds.add(s);
        }

        initialiseGame();

        System.out.println(tmap);
    }

    /**
     * You will probably want to put code to restart a game in
     * a separate method so that you can call it when restarting
     * the game when the player loses.
     */
    public void initialiseGame() {
        total = 0;

        player.setPosition(100, 250);
        player.setVelocity(0, 0);
        player.show();

        knife.setPosition(player.getX(), player.getY());
        knife.show();

        thug.setPosition(500, 200);
        thug.setVelocity(-0.1f, 0);
        thug.show();
    }

    /**
     * Draw the current state of the game. Note the sample use of
     * debugging output that is drawn directly to the game screen.
     */
    public void draw(Graphics2D g) {

        AffineTransform transform = new AffineTransform();
        transform.scale(1.5,1.5);

        // Be careful about the order in which you draw objects - you
        // should draw the background first, then work your way 'forward'

        // First work out how much we need to shift the view in order to
        // see where the player is. To do this, we adjust the offset so that
        // it is relative to the player's position along with a shift
        int xo = -(int) player.getX() + 450;
        int yo = 0; //-(int)player.getY() + 200;

        //draw the background using background.png
        g.drawImage(loadImage("images/background.png"), 0, 0, null);

        // variables to hold the background images
        try {
            bgImage1 = ImageIO.read(new File("images/buildings_light.png"));
            bgImage2 = ImageIO.read(new File("images/trees.png"));
            bgImage3 = ImageIO.read(new File("images/buildings_dark.png"));
            youDiedImage = ImageIO.read(new File("images/you_died.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //scale up the background images
        AffineTransform at = new AffineTransform();
        at.scale(1.1, 1.1);
        bgImage1 = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR).filter(bgImage1, null);
        bgImage3 = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR).filter(bgImage3, null);

        //scale the you_died image
        AffineTransform at2 = new AffineTransform();
        at2.scale(2, 2);
        youDiedImage = new AffineTransformOp(at2, AffineTransformOp.TYPE_BILINEAR).filter(youDiedImage, null);

        // Calculate the distance each background layer should move
        int bg1Move = -(xo / 4);
        int bg2Move = -(xo / 2);
        int bg3Move = -(xo / 3);

        // Draw the background images
        // Note that we draw the images multiple times to create a seamless loop
        int bgWidth = bgImage1.getWidth();
        int bgHeight = bgImage1.getHeight();
        for (int i = -1; i <= getWidth() / bgWidth + 1; i++) {
            int x = i * bgWidth - bg1Move % bgWidth;
            int y = getHeight() - bgHeight;
            g.drawImage(bgImage1, x, y, null);
        }

        bgWidth = bgImage3.getWidth();
        bgHeight = bgImage3.getHeight();
        for (int i = -1; i <= getWidth() / bgWidth + 1; i++) {
            int x = i * bgWidth - bg3Move % bgWidth;
            int y = getHeight() - bgHeight;
            g.drawImage(bgImage3, x, y, null);
        }

        bgWidth = bgImage2.getWidth();
        bgHeight = bgImage2.getHeight();
        for (int i = -1; i <= getWidth() / bgWidth + 1; i++) {
            int x = i * bgWidth - bg2Move % bgWidth;
            int y = getHeight() - bgHeight;
            g.drawImage(bgImage2, x, y, null);
        }

        // Apply offsets to sprites then draw them
        for (Sprite s : clouds) {
            s.setOffsets(xo, yo);
            s.draw(g);
        }

        // Apply offsets to tile map and draw  it
        tmap.draw(g, xo, yo);

        // Apply offsets to player and draw
        player.setOffsets(xo, yo);

        // Apply offsets to knife and draw
        knife.setOffsets(xo, yo);

        // apply offsets to thug and draw
        thug.setOffsets(xo, yo);
        if (thug.isFlipped()) {
            thug.drawTransformedFlip(g);
        } else {
            thug.draw(g);
        }

        if((player.isMoveLeft() || player.isMoveRight()) && player.isOnGround())
        {
            knife.setPosition(player.getX()+5, player.getY()+5);
            if(player.isFlipped())
                knife.drawTransformedFlip(g);
            else
                knife.draw(g);
        }

        if(player.isFlipped()){
            player.drawTransformedFlip(g);
        }
        else {
            player.draw(g);
        }

        if(!player.isAlive())
        {
            g.drawImage(youDiedImage, 0, 333, null);
        }
    }

    /**
     * Update any sprites and check for collisions
     *
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */
    public void update(long elapsed) {
        // Make adjustments to the speed of the sprite due to gravity
        player.setVelocityY(player.getVelocityY() + (gravity * elapsed));
        thug.setVelocityY(thug.getVelocityY() + (gravity * elapsed));

        // Then check for any collisions that may have occurred
        checkTileCollision(player, tmap);
        checkEnemyTileCollision(thug, tmap);

        // Set the animation speed of the player and thug to 1.0f
        player.setAnimationSpeed(1.0f);
        thug.setAnimationSpeed(1.0f);

        //check for collision between player and thug
        if (boundingBoxCollision(player, thug)) {
            // if the player is attacking, kill the thug
            if (player.isAttack()) {
                thug.setVelocityX(0);
                thug.setVelocityY(0);
                thug.setAlive(false);
                thugDeath.setLoop(false);
                thug.setAnimation(thugDeath);
                thug.setAnimationSpeed(0.5f);
            } else {
                // if the player is not attacking, kill the player
                thugAttacking.setLoop(false);
                thug.setAnimation(thugAttacking);
                thug.setVelocityX(0);
                player.setVelocityX(0);
                player.setAlive(false);
                playerDeath.setLoop(false);
                player.setAnimation(playerDeath);
            }
        }

        // redraw the clouds to the right of the screen if they have moved off the left side
        for (Sprite s : clouds) {
            if (s.getX() < player.getX() - (screenWidth)) {
                s.setPosition((player.getX() + (screenWidth * 2)) + (int) (Math.random() * 500), (int) (Math.random() * 250));
            }
        }

        // Check the state of the player and update their animation and position accordingly
        if(player.isAlive()){
            if (player.isMoveRight()) {
                if (player.isOnGround()) {
                    player.setVelocityX(moveSpeed);
                    player.setAnimation(playerRunning);
                    player.setFlipped(false);
                } else {
                    player.setVelocityX(moveSpeed);
                    player.setAnimation(playerJumping);
                }
            } else if (player.isMoveLeft()) {
                player.setFlipped(true);
                player.setVelocityX(-moveSpeed);
                if (player.isOnGround()) {
                    player.setAnimation(playerRunning);
                } else {
                    player.setAnimation(playerJumping);
                }
            } else if(!player.isMoveLeft() && !player.isMoveRight() && player.isOnGround()){
                player.setVelocityX(0);
                player.setAnimation(playerIdle);
            }

            if (player.isAttack()) {
                player.setAnimation(playerAttacking);
            }
            if (player.isJump()) {
                // if the player is standing on a tile, then jump
                if (player.isOnGround()) {
                    player.setAnimationSpeed(1.8f);
                    player.setVelocityY(jumpVelocity);
                    player.setAnimation(playerJumping);
                    player.setOnGround(false);
                }
            }
        }


        knife.setVelocity(player.getVelocityX(), player.getVelocityY());

        // Update the animation and position of all the clouds
        for (Sprite s : clouds)
            s.update(elapsed);

        // Now update the sprites animation and position
        player.update(elapsed);
        thug.update(elapsed);
        knife.update(elapsed);
    }

    /**
     * Checks and handles collisions with the edge of the screen. You should generally
     * use tile map collisions to prevent the player leaving the game area. This method
     * is only included as a temporary measure until you have properly developed your
     * tile maps.
     *
     * @param s       The Sprite to check collisions for
     * @param tmap    The tile map to check
     * @param elapsed How much time has gone by since the last call
     */
    public void handleScreenEdge(Sprite s, TileMap tmap, long elapsed) {
        // This method just checks if the sprite has gone off the bottom screen.
        // Ideally you should use tile collision instead of this approach

        float difference = s.getY() + s.getHeight() - tmap.getPixelHeight();
        if (difference > 0) {
            // Put the player back on the map according to how far over they were
            s.setY(tmap.getPixelHeight() - s.getHeight() - (int) (difference));

            // and make them bounce
            s.setVelocityY(-s.getVelocityY() * 0.75f);
        }
    }

    /**
     * Override of the keyPressed event defined in GameCore to catch our
     * own events
     *
     * @param e The event that has been generated
     */
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        switch (key) {
            case KeyEvent.VK_UP:
                if (player.isOnGround()) {
                    player.setJump(true);
                }
                break;
            case KeyEvent.VK_RIGHT:
                player.setMoveRight(true);
                break;
            case KeyEvent.VK_LEFT:
                player.setMoveLeft(true);
                break;
            case KeyEvent.VK_S:
                Sound s = new Sound("sounds/caw.wav");
                s.start();
                break;
            case KeyEvent.VK_ESCAPE:
                stop();
                break;
            case KeyEvent.VK_B:
                debug = !debug;
                break; // Flip the debug state
            default:
                break;
        }

    }

    public void keyReleased(KeyEvent e) {

        int key = e.getKeyCode();

        switch (key) {
            case KeyEvent.VK_ESCAPE:
                stop();
                break;
            case KeyEvent.VK_UP:
                player.setJump(false);
                break;

            case KeyEvent.VK_DOWN:
                // set timer so that the player can only attack once every half second
                if (System.currentTimeMillis() - lastAttack > 500) {
                    lastAttack = System.currentTimeMillis();
                    player.setAttack(true);
                } else {
                    player.setAttack(false);
                }
                player.setAnimation(playerAttacking);
                // ensure animation plays for its full length
                try {
                    Thread.sleep(375);
                    player.setAttack(false);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                break;
            case KeyEvent.VK_RIGHT:
                player.setMoveRight(false);
                break;
            case KeyEvent.VK_LEFT:
                player.setMoveLeft(false);
                break;
            default:
                break;
        }
    }

    /**
     * Use the sample code in the lecture notes to properly detect
     * a bounding box collision between sprites s1 and s2.
     *
     * @return true if a collision may have occurred, false if it has not.
     */
    public boolean boundingBoxCollision(Sprite s1, Sprite s2) {
        // Get the bounding boxes for each sprite
        Rectangle r1 = s1.getBoundingBox();
        Rectangle r2 = s2.getBoundingBox();

        // Check if the bounding boxes intersect
         if (r1.intersects(r2))
        {
            // If they do, then check if the individual pixels
            // also intersect
            return s1.collidesWith(s2);
        }

        return false;
    }

    public void handleCollision(Sprite s1, Sprite s2) {
        // Get the bounding boxes for each sprite
        Rectangle r1 = s1.getBoundingBox();
        Rectangle r2 = s2.getBoundingBox();

        // Check if the bounding boxes intersect
        if (r1.intersects(r2))
        {
            // If they do, then check if the individual pixels
            // also intersect
            if (s1.collidesWith(s2))
            {
                // If they do, then we have a collision
                // We can now handle the collision
                s1.setVelocityX(0);
                if(s2.getVelocityX()>0){
                    s2.setAnimation(thugAttacking);
                }
                else{
                    //flip the sprite
                    s2.setAnimation(thugAttacking);
                }
                s2.setVelocityX(0);
            }
        }
    }

    /**
     * Check and handles collisions with a tile map for the
     * given sprite 's'. Initial functionality is limited...
     *
     * @param s    The Sprite to check collisions for
     * @param tmap The tile map to check
     */
    public void checkTileCollision(Sprite s, TileMap tmap) {
        float tileWidth = tmap.getTileWidth();
        float tileHeight = tmap.getTileHeight();
        Rectangle spriteBounds = new Rectangle((int) s.getX(), (int) s.getY(), (int) (s.getWidth() * 0.5f), s.getHeight());
        for (int row = 0; row < tmap.getMapHeight(); row++) {
            for (int col = 0; col < tmap.getMapWidth(); col++) {
                char tileChar = tmap.getTileChar(col, row);
                if (tileChar != '.') {
                    Rectangle tileBounds = new Rectangle((int) (col * tileWidth), (int) (row * tileHeight), (int) tileWidth, (int) tileHeight);
                    if (spriteBounds.intersects(tileBounds)) {
                        // Determine which side of the sprite collided with the tile
                        float xDiff = Math.abs((s.getX() + s.getWidth() / 2) - (col * tileWidth + tileWidth / 2));
                        float yDiff = Math.abs((s.getY() + s.getHeight() / 2) - (row * tileHeight + tileHeight / 2));
                        float w = s.getWidth() / 2 + tileWidth / 2;
                        float h = s.getHeight() / 2 + tileHeight / 2;
                        float dx = w - xDiff;
                        float dy = h - yDiff;
                        // Only move the sprite back in the direction of the collision
                        if (dx < dy) {
                            if (s.getX() < col * tileWidth) {
                                s.setX(s.getX() - dx + 26.5f);
                            } else {
                                s.setX(s.getX() + dx);
                            }
                            s.setVelocity(0, s.getVelocityY());
                        } else {
                            if (s.getY() < row * tileHeight) {
                                s.setY(s.getY() - dy);
                                s.setOnGround(true);
                            } else {
                                s.setY(s.getY() + dy);
                            }
                            s.setVelocity(s.getVelocityX(), 0);
                            // Set the 'onGround' flag only if the sprite is colliding with the ground
                            if (dy < dx && s.getY() == row * tileHeight) {
                                s.setOnGround(true);
                            }}}}}}}

    public void checkEnemyTileCollision(Sprite s, TileMap tmap) {
        float tileWidth = tmap.getTileWidth();
        float tileHeight = tmap.getTileHeight();
        Rectangle spriteBounds = new Rectangle((int) s.getX(), (int) s.getY(), (int) (s.getWidth() * 0.5f), s.getHeight());
        for (int row = 0; row < tmap.getMapHeight(); row++) {
            for (int col = 0; col < tmap.getMapWidth(); col++) {
                char tileChar = tmap.getTileChar(col, row);
                if (tileChar != '.') {
                    Rectangle tileBounds = new Rectangle((int) (col * tileWidth), (int) (row * tileHeight), (int) tileWidth, (int) tileHeight);
                    if (spriteBounds.intersects(tileBounds)) {
                        // Determine which side of the sprite collided with the tile
                        float xDiff = Math.abs((s.getX() + s.getWidth() / 2) - (col * tileWidth + tileWidth / 2));
                        float yDiff = Math.abs((s.getY() + s.getHeight() / 2) - (row * tileHeight + tileHeight / 2));
                        float w = s.getWidth() / 2 + tileWidth / 2;
                        float h = s.getHeight() / 2 + tileHeight / 2;
                        float dx = w - xDiff;
                        float dy = h - yDiff;
                        // Only move the sprite back in the direction of the collision
                        if (dx < dy) {
                            if (s.getX() < col * tileWidth) {
                                s.setX(s.getX() - dx + 26.5f);
                            } else {
                                s.setX(s.getX() + dx);
                            }
                            s.setVelocity(-s.getVelocityX(), s.getVelocityY());
                            s.setFlipped(!s.isFlipped());
                        } else {
                            if (s.getY() < row * tileHeight) {
                                s.setY(s.getY() - dy);
                            } else {
                                s.setY(s.getY() + dy);
                            }
                            s.setVelocity(s.getVelocityX(), 0);
                        }
                    }
                }
            }
        }
    }

}
