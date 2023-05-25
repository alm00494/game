import game2D.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    static int screenWidth = 1280;      // define the size of the game window
    static int screenHeight = 720;      // define the size of the game window
    float gravity = 0.001f;             // acceleration of player's downward movement
    float jumpVelocity = -0.4f;         // initial velocity of player's upward jump movement
    float moveSpeed = 0.2f;             // speed of player's horizontal movement
    boolean debug = true;               // flag for printing debug information

    // Game resources
    Animation playerIdle, playerRunning, playerJumping, playerDeath, playerAttacking, stab, knifeRun,
            thugIdle, thugRunning, thugDeath, thugAttacking, thugAttacked, fountainIdle, chestOpen, cloud;
    BufferedImage bgImage1, bgImage2, bgImage3, youDiedImage, menu;
    MidiPlayer midiPlayer;
    private Sprite player, knife, chest, fountain;
    ArrayList<Sprite> clouds = new ArrayList<>();
    ArrayList<Sprite> sprites = new ArrayList<>();
    ArrayList<Sprite> enemies = new ArrayList<>();
    TileMap tmap = new TileMap();           // Our tile map, note that we load it in init()
    long total;                             // The score will be the total time elapsed since a crash;
    boolean paused = true;                  // Is the game paused?
    private boolean mainMenu = true;        // Is the main menu showing?
    private boolean bgMusicStarted = false; // Has the background music started?
    private boolean bgMusicPlaying = false; // Is the background music playing?


    /**
     * The obligatory main method that creates
     * an instance of our class and starts it running
     *
     * @param args The list of parameters this program might use (ignored)
     */
    public static void main(String[] args) {
        Game gct = new Game();      // Create a new Game object
        gct.init();                 // Initialize the game
        gct.run(false, screenWidth, screenHeight); // Start the game in windowed mode
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

        // Load the tile map and set the size of the game window to match the map's size
        tmap.loadMap("maps", "map.txt");
        setSize(screenWidth, screenHeight);
        setVisible(true);

        // Create a MIDI player to play background music
        midiPlayer = new MidiPlayer();
        midiPlayer.playMidi("sounds/biker_mice_from_mars.mid");

        // Load animation resources
        loadAnimations();

        // Create 3 clouds at random positions off the screen to the right
        // and add them to the list of sprites
        for (int i = 0; i < 3; i++) {
            s = new Sprite(cloud);
            s.setPosition(screenWidth * 2 + (int) (Math.random() * 500), (int) (Math.random() * 300));
            s.setVelocityX(-0.1f);
            clouds.add(s);
            sprites.add(s);
        }

        // Create 3 thugs at specific positions and add them to the list of sprites
        for(int i = 0; i < 3; i++) {
            s = new Sprite(thugRunning);
            s.setFlipped(true);
            s.setPosition(100 + (i * 100), 580);
            s.setVelocityX(-0.1f);
            enemies.add(s);
            sprites.add(s);
        }

        // Initialize the game by setting up the player, knife, chest, and fountain sprites,
        // adding them to the list of sprites, and setting the game state to "running"
        initialiseGame();

        // Print the tile map to the console for debugging purposes
        System.out.println(tmap);
    }


    /**
     * You will probably want to put code to restart a game in
     * a separate method so that you can call it when restarting
     * the game when the player loses.
     */
    public void initialiseGame() {
        total = 0;  // Reset the total elapsed time since a crash

        // Set the initial position and velocity of the player sprite
        player.setPosition(55, 230);
        player.setVelocity(0, 0);

        // Set the initial positions of the knife, chest, and fountain sprites
        knife.setPosition(player.getX(), player.getY());  // Set the knife position to be the same as the player's position
        chest.setPosition(600, 260);  // Set the chest position to a specific location
        fountain.setPosition(3030, 280);  // Set the fountain position to a specific location
    }

    // This method is responsible for loading all the animations used in the game
    public void loadAnimations() {
        // Load animations for the player's idle, attacking, running, jumping and death states
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

        // Load animations for the thug's idle, attacked, running, attacking and death states
        thugIdle = new Animation();
        thugIdle.loadAnimationFromSheet("images/thug/Thug_idle.png", 4, 1, 60);

        thugAttacked = new Animation();
        thugAttacked.loadAnimationFromSheet("images/thug/Hurt.png", 2, 1, 60);
        thugAttacked.setLoop(false);

        thugRunning = new Animation();
        thugRunning.loadAnimationFromSheet("images/thug/Thug_Walk.png", 6, 1, 60);

        thugAttacking = new Animation();
        thugAttacking.loadAnimationFromSheet("images/thug/Thug_attack1.png", 6, 1, 60);

        thugDeath = new Animation();
        thugDeath.loadAnimationFromSheet("images/thug/Death.png", 6, 1, 60);
        thugDeath.setLoop(false);

        // Load animations for the player's knife stab and running with the knife
        stab = new Animation();
        stab.loadAnimationFromSheet("images/player/knifeStab.png", 6, 1, 60);

        knifeRun = new Animation();
        knifeRun.loadAnimationFromSheet("images/player/knifeRun.png", 6, 1, 60);

        // Load animations for the chest and fountain objects
        chestOpen = new Animation();
        chestOpen.loadAnimationFromSheet("images/objects/Chest_open.png", 7, 1, 60);
        chestOpen.setLoop(false);
        chestOpen.pause();

        fountainIdle = new Animation();
        fountainIdle.loadAnimationFromSheet("images/objects/Fountain.png", 4, 1, 60);

        // Initialise the player and objects with the loaded animations
        player = new Sprite(playerIdle);
        sprites.add(player);

        chest = new Sprite(chestOpen);
        sprites.add(chest);

        knife = new Sprite(knifeRun);
        knife.setWeapon(true);
        sprites.add(knife);

        fountain = new Sprite(fountainIdle);
        sprites.add(fountain);

        // Load a single cloud animation
        cloud = new Animation();
        cloud.addFrame(loadImage("images/cloud.png"), 1000);
    } // End of loadAnimations method

    /**
     * Draw the current state of the game. Note the sample use of
     * debugging output that is drawn directly to the game screen.
     */
    public void draw(Graphics2D g) {

        // Create an AffineTransform to scale the graphics
        AffineTransform transform = new AffineTransform();
        transform.scale(1.5, 1.5);

        // Calculate the x and y offsets to center the view around the player
        int xo = -(int) player.getX() + 450;
        int yo = 0; //-(int)player.getY() + 200;

        // Draw the background using background.png
        g.drawImage(loadImage("images/background.png"), 0, 0, null);

        // Load the background and menu images
        try {
            bgImage1 = ImageIO.read(new File("images/buildings_light.png"));
            bgImage2 = ImageIO.read(new File("images/trees.png"));
            bgImage3 = ImageIO.read(new File("images/buildings_dark.png"));
            youDiedImage = ImageIO.read(new File("images/you_died.png"));
            menu = ImageIO.read(new File("images/menuxx.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This code scales up the background images
        // First, an AffineTransform object is created with the scale factor.
        AffineTransform at = new AffineTransform();
        at.scale(1.1, 1.1);
        // Then, the filter method is called on each background image to apply the transformation.
        bgImage1 = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR).filter(bgImage1, null);
        bgImage3 = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR).filter(bgImage3, null);

        // The you_died image is also scaled up, this time by a factor of 2.
        AffineTransform at2 = new AffineTransform();
        at2.scale(2.1, 2);
        youDiedImage = new AffineTransformOp(at2, AffineTransformOp.TYPE_BILINEAR).filter(youDiedImage, null);

        // The distance each background layer should move is calculated based on the x offset (xo).
        int bg1Move = -(xo / 4);
        int bg2Move = -(xo / 2);
        int bg3Move = -(xo / 3);

        // The background images are drawn multiple times to create a seamless loop.
        // The loop iterates from -1 to the width of the canvas divided by the background image width plus 1.
        // The x coordinate of each image is calculated based on the current iteration, the background layer move distance,
        // and the width of the background image.
        // The y coordinate is the height of the canvas minus the height of the background image.
        // Finally, the image is drawn using the Graphics object.
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

        // Set the offsets of each sprite based on the camera position and draw them
        for (Sprite s : clouds) {
            s.setOffsets(xo, yo);
            s.draw(g);
        }

        for (Sprite s : enemies) {
            s.setOffsets(xo, yo);
            // Flip the sprite horizontally if necessary before drawing
            if (s.isFlipped())
                s.drawTransformedFlip(g);
            else
                s.draw(g);
        }

        // Set the offsets of player, knife, chest and fountain based on the camera position
        player.setOffsets(xo, yo);
        knife.setOffsets(xo, yo);
        chest.setOffsets(xo, yo);
        fountain.setOffsets(xo, yo);

        // Apply offsets to the tile map and draw
        tmap.draw(g, xo, yo);

        // Flip player and knife horizontally if necessary before drawing
        if (player.isFlipped()){
            player.drawTransformedFlip(g);
            knife.drawTransformedFlip(g);
        }
        else{
            player.draw(g);
            knife.draw(g);
        }

        // Draw chest and fountain
        chest.draw(g);
        fountain.draw(g);

        // Draw the player's health hearts
        for(int i = 0; i<player.getHealth(); i++) {
            g.drawImage(loadImage("images/hearts.png"), 10 + (i * 30), 30, null);
        }

        // If the player is dead, display the "you died" image
        if (!player.isAlive()) {
            g.drawImage(youDiedImage, 0, 333, null);
        }

        // Debug mode drawing
        if (debug) {
            // Set color to red for bounding box drawing
            g.setColor(Color.RED);

            // Draw the player's bounding box
            player.drawBoundingBox(g);
            //player.drawBoundingCircle(g); // (Optional) Draw the player's bounding circle

            // Draw the knife's bounding box - it's using a different method to draw the bounding box
            knife.drawBounds(g);
            //knife.drawBoundingCircle(g); // (Optional) Draw the knife's bounding circle

            // Draw the enemies' bounding boxes
            for (Sprite s : enemies) {
                s.drawBoundingBox(g);
                //s.drawBoundingCircle(g); // (Optional) Draw the enemy's bounding circle
            }

            // Draw the debug image and text
            g.drawImage(loadImage("images/debug.png"), 30, 200, null);
            g.drawString("Space: attack", 30, 300);
            g.drawString("Esc: exit", 30, 320);
            g.drawString("WAD: movement", 30, 340);
        }

        // Check if the game is in the main menu state
        if (mainMenu) {

            // Draw the background black
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, screenWidth, screenHeight);

            // Draw the menu image at the center of the screen
            g.drawImage(menu, (screenWidth / 2) - (menu.getWidth() / 2), (screenHeight / 2) - (menu.getHeight() / 2), null);

            // Define the dimensions and spacing of the buttons
            int buttonWidth = 55;
            int buttonHeight = 30;
            int buttonSpacing = 12;

            // Define the position of the start button relative to the screen dimensions
            int startButtonX = (screenWidth / 2);
            int startButtonY = (screenHeight / 2) - 30;

            // Define the position of the exit button relative to the screen dimensions
            int exitButtonX = (screenWidth / 2);
            int exitButtonY = startButtonY + buttonHeight + buttonSpacing;

            // Add a mouse listener to the JFrame to detect button clicks
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int x = e.getX();
                    int y = e.getY();

                    // Check if the mouse click is within the bounds of the start button
                    if (x >= startButtonX && x <= startButtonX + buttonWidth
                            && y >= startButtonY && y <= startButtonY + buttonHeight) {

                        // Change the game state to playing
                        mainMenu = false;

                        // Unpause the game
                        paused = false;
                    }

                    // Check if the mouse click is within the bounds of the exit button
                    if (x >= exitButtonX && x <= exitButtonX + buttonWidth
                            && y >= exitButtonY && y <= exitButtonY + buttonHeight) {

                        // Stop the game
                        stop();
                    }
                }// End mouseClicked method
            });
        }// End if(mainMenu)
    }// End draw method

    /**
     * Update any sprites and check for collisions
     *
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */
    public void update(long elapsed) {

        // Check if the game is not in the main menu and background music has not started playing
        if (!mainMenu && !bgMusicStarted) {
            // Change the background music to "sounds/bt.mid"
            midiPlayer.changeMidi("sounds/bt.mid");
            // Set the background music started flag to true
            bgMusicStarted = true;
            // Set the background music playing flag to true
            bgMusicPlaying = true;
        }

        // Check if the game is not paused
        if (!paused) {

            // Make adjustments to the speed of the player sprite due to gravity
            player.setVelocityY(player.getVelocityY() + (gravity * elapsed));

            // Check for any collisions that may have occurred with tiles
            checkTileCollision(player, tmap);

            // Check if the player's bounding box has collided with the chest
            if (boundingBoxCollision(player, chest)) {
                // Play the chest open sound effect
                chestOpen.play();
                // Give the player a weapon
                player.setHasWeapon(true);
                // Set the player's damage to 3
                player.setDamage(3);
            }

            // Check if the player's bounding box has collided with the fountain
            if (boundingBoxCollision(player, fountain)) {
                // Restore the player's health to 4
                player.setHealth(4);
            }

            // Check for any collisions that may have occurred with the player's weapon and enemies
            checkWeaponCollision(knife, enemies, elapsed);

            // Check for any collisions that may have occurred with enemies and the player
            checkEnemyCollision(player, enemies, elapsed);

            // Reposition the clouds to the right of the screen if they have moved off the left side
            for (Sprite s : clouds) {
                if (s.getX() < player.getX() - (screenWidth)) {
                    // Set the new position of the cloud
                    s.setPosition((player.getX() + (screenWidth * 2)) + (int) (Math.random() * 500), (int) (Math.random() * 250));
                }
            }

            // Check the status of all enemies
            checkEnemyStatus();

            // Check the status of the player
            checkPlayerStatus();

            // Update the animation and position of all the sprites
            for (Sprite s : sprites) {
                s.update(elapsed);
            }

            // Handle the screen edge collision for the player sprite and tile map
            handleScreenEdge(player, tmap, elapsed);
        }
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
        // Calculate the difference between the sprite's bottom edge and the bottom of the tile map
        float difference = s.getY() + s.getHeight() - tmap.getPixelHeight();

        // Check if the sprite has gone off the bottom of the screen
        if (difference > 0) {
            // Reset the sprite's position to (100, 300)
            s.setPosition(100, 300);
        }
    }


    public void checkPlayerStatus() {

        // Check if the player is alive. If not, set the death animation and stop its movement
        if (!player.isAlive()) {
            player.setAnimation(playerDeath);
            player.setVelocityX(0);
            player.setVelocityY(0);
            return;
        }

        // Check the player's movement direction and call the appropriate method
        if (player.isMoveLeft()) {
            moveLeft();
        } else if (player.isMoveRight()) {
            moveRight();
        } else if (player.isOnGround()) {
            idle();
        }

        // Handle the player's knife
        handleKnife();

        // Check if the player has jumped and is on the ground. If so, perform a jump
        if (player.isJump() && player.isOnGround()) {
            jump();
        }
    }


    public void checkEnemyStatus() {
        // Loop through all the enemies and check their status
        for (Sprite enemy : enemies) {

            // Check if the enemy is alive
            if (enemy.isAlive()) {
                // Check if the enemy is attacking
                if (enemy.isAttack()){
                    enemy.setAnimation(thugAttacking);
                    if (System.currentTimeMillis() - enemy.getLastAttack() >= 360) {
                        enemy.setLastAttack(System.currentTimeMillis());
                        enemy.setAttack(false);
                        playerHit(enemy);
                    }
                }

                if(!enemy.isAttack())
                    enemy.setAnimation(thugRunning);

                // Check the enemy's horizontal velocity and set its movement direction and flip its image if necessary
                if (enemy.getVelocityX() > 0) {
                    enemy.setMoveRight(true);
                    if (enemy.isFlipped()) {
                        enemy.setFlipped(false);
                    }
                } else {
                    enemy.setMoveRight(false);
                }
                if (enemy.getVelocityX() < 0) {
                    enemy.setMoveLeft(true);
                    if (!enemy.isFlipped()) {
                        enemy.setFlipped(true);
                    }
                } else {
                    enemy.setMoveLeft(false);
                }
            }
            // If the enemy is not alive, set the death animation and stop its movement
            else {
                enemy.setAnimation(thugDeath);
                enemy.setVelocityX(0);
                enemy.setVelocityY(0);
            }
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
            case KeyEvent.VK_SPACE:
                // if player's last attack was more than 360ms ago
                if ((System.currentTimeMillis() - player.getLastAttack()) > 360) {
                    // set player's attack flag to true and update last attack time
                    player.setAttack(true);
                    player.setLastAttack(System.currentTimeMillis());
                }
                break;
            case KeyEvent.VK_W:
                // if player is on the ground, set jump flag to true
                if (player.isOnGround()) {
                    player.setJump(true);
                }
                break;
            case KeyEvent.VK_D:
                // set player's move right flag to true
                player.setMoveRight(true);
                break;
            case KeyEvent.VK_A:
                // set player's move left flag to true
                player.setMoveLeft(true);
                break;
            case KeyEvent.VK_S:
                // currently commented out, would play a sound if uncommented
                //Sound s = new Sound("sounds/caw.wav");
                //s.start();
                break;
            case KeyEvent.VK_ESCAPE:
                // stop the game
                stop();
                break;
            case KeyEvent.VK_B:
                // toggle debug flag
                debug = !debug;
                break;

            case KeyEvent.VK_P:
                // toggle pause flag
                if (paused) {
                    paused = false;
                } else {
                    paused = true;
                }
                break;
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
            case KeyEvent.VK_SPACE:
                //player.setAttack(false);
                break;
            case KeyEvent.VK_W:
                player.setJump(false);
                break;
            case KeyEvent.VK_M:
                if(bgMusicPlaying){
                    midiPlayer.stopMidi();
                    bgMusicPlaying = false;
                }
                else{
                    midiPlayer.playMidi("sounds/bt.mid");
                    bgMusicPlaying = true;
                }
                break;
            case KeyEvent.VK_BACK_SPACE:
                sprites.clear();
                enemies.clear();
                clouds.clear();
                player.stop();
                this.init();
                break;

            case KeyEvent.VK_D:
                player.setMoveRight(false);
                break;
            case KeyEvent.VK_A:
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
        if(s1.isWeapon()){
            r1 = s1.getKnifeBounds(); // if s1 is a weapon, get its knife bounding box
        }
        Rectangle r2 = s2.getBoundingBox();

        // Check if the bounding boxes intersect
        if (r1.intersects(r2)) {
            // If they do, then check if the individual pixels
            // also intersect
            return s1.collidesWith(s2); // check if the sprites actually collide
        }

        return false; // otherwise, they do not collide
    }

    public boolean boundingKnifeCollision(Sprite s1, Sprite s2) {
        // Get the bounding boxes for each sprite
        Rectangle r1 = s1.getKnifeBounds(); // get the knife bounding box for s1
        Rectangle r2 = s2.getBoundingBox();

        // Check if the bounding boxes intersect
        if (r1.intersects(r2)) {
            // If they do, then check if the individual pixels
            // also intersect
            return s1.collidesWith(s2); // check if the sprites actually collide
        }

        return false; // otherwise, they do not collide
    }

    public boolean boundingCircleCollision(Sprite s1, Sprite s2) {
        // Get the bounding circles for each sprite
        Circle r1 = s1.getBoundingCircle(); // get the circle bounding box for s1
        Circle r2 = s2.getBoundingCircle();

        // Check if the bounding boxes intersect
        if (r1.intersects(r2)) {
            // If they do, then check if the individual pixels
            // also intersect
            return s1.collidesWith(s2); // check if the sprites actually collide
        }

        return false; // otherwise, they do not collide
    }

    public void checkWeaponCollision(Sprite knife, ArrayList<Sprite> enemies, long elapsed) {
        for(Sprite s : enemies){
            if (boundingKnifeCollision(knife, s) && player.isAttack()) { // check if the knife and the enemy collide and the player is attacking
                s.setVelocityY(-0.4f); // set the enemy's y velocity
                if(player.isFlipped()){
                    s.setVelocityX(-0.1f); // set the enemy's x velocity if the player is facing left
                }
                else{
                    s.setVelocityX(0.1f); // set the enemy's x velocity if the player is facing right
                }
                s.setHealth(s.getHealth() - player.getDamage()); // decrease the enemy's health by the player's damage
                if (s.getHealth() <= 0) {
                    s.setAlive(false); // if the enemy's health is zero or less, set it as dead
                } else {
                    s.setAnimation(thugAttacked); // otherwise, set the enemy's animation to 'thugAttacked'
                }
            }
        }
    }

    public void checkEnemyCollision(Sprite player, ArrayList<Sprite> enemies, long elapsed) {
        for (Sprite enemy : enemies) {
            // Update the enemy's velocity due to gravity
            enemy.setVelocityY(enemy.getVelocityY() + (gravity * elapsed));

            // Check for collisions between the enemy and the tile map
            checkEnemyTileCollision(enemy, tmap);

            // If there is no collision between the player and the enemy, or if the enemy is already dead, continue to the next iteration of the loop
            if (!boundingBoxCollision(player, enemy) || !enemy.isAlive()) {
                continue;
            }

            // If a collision has occurred and the enemy is alive, set the enemy's attack state
            enemy.setAttack(true);
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
        // Get the width and height of each tile in the TileMap
        float tileWidth = tmap.getTileWidth();
        float tileHeight = tmap.getTileHeight();
        // Get the bounds of the Sprite
        Rectangle spriteBounds = new Rectangle((int) s.getX(), (int) s.getY(), (int) (s.getWidth() * 0.5f), s.getHeight());
        // Iterate through each tile in the TileMap
        for (int row = 0; row < tmap.getMapHeight(); row++) {
            for (int col = 0; col < tmap.getMapWidth(); col++) {
                // Get the character representing the current tile
                char tileChar = tmap.getTileChar(col, row);
                // If the tile is not empty (represented by a '.'), check for collision
                if (tileChar != '.') {
                    // Get the bounds of the current tile
                    Rectangle tileBounds = new Rectangle((int) (col * tileWidth), (int) (row * tileHeight), (int) tileWidth, (int) tileHeight);
                    // If the Sprite's bounds intersect with the current tile's bounds, handle the collision
                    if (spriteBounds.intersects(tileBounds)) {
                        // Determine which side of the Sprite collided with the tile
                        float xDiff = Math.abs((s.getX() + s.getWidth() / 2) - (col * tileWidth + tileWidth / 2));
                        float yDiff = Math.abs((s.getY() + s.getHeight() / 2) - (row * tileHeight + tileHeight / 2));
                        float w = s.getWidth() / 2 + tileWidth / 2;
                        float h = s.getHeight() / 2 + tileHeight / 2;
                        float dx = w - xDiff;
                        float dy = h - yDiff;
                        // Only move the Sprite back in the direction of the collision
                        if (dx < dy) {
                            if (s.getX() < col * tileWidth) {
                                s.setX(s.getX() - dx + 26.5f); // Move the Sprite back to the left
                            } else {
                                s.setX(s.getX() + dx); // Move the Sprite back to the right
                            }
                            s.setVelocity(0, s.getVelocityY()); // Stop the Sprite from moving horizontally
                        } else {
                            if (s.getY() < row * tileHeight) {
                                s.setY(s.getY() - dy); // Move the Sprite back up
                                s.setOnGround(true); // Set the "onGround" flag to true
                            } else {
                                s.setY(s.getY() + dy); // Move the Sprite back down
                            }
                            s.setVelocity(s.getVelocityX(), 0); // Stop the Sprite from moving vertically
                            // Set the "onGround" flag to true only if the Sprite is colliding with the ground
                            if (dy < dx && s.getY() == row * tileHeight) {
                                s.setOnGround(true);
                            }}}}}}
    }// end checkTileCollision method


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
    private void moveLeft() {
        player.setFlipped(true);
        player.setVelocityX(-moveSpeed);
        if (player.isOnGround()) {
            player.setAnimation(playerRunning);
        } else {
            player.setAnimation(playerJumping);
        }
    }

    private void moveRight() {
        player.setFlipped(false);
        player.setVelocityX(moveSpeed);
        if (player.isOnGround()) {
            player.setAnimation(playerRunning);
        } else {
            player.setAnimation(playerJumping);
        }
    }

    private void idle() {
        player.setVelocityX(0);
        player.setAnimation(playerIdle);
    }

    private void playerHit(Sprite enemy){
        // If the enemy is moving left, set the player's velocity to move left and up
        if (enemy.isMoveLeft()) {
            player.setVelocity(-0.2f, -0.2f);
        }
        // If the enemy is moving right, set the player's velocity to move right and up
        else if (enemy.isMoveRight()) {
            player.setVelocity(0.2f, -0.2f);
        }

        // Set the player's 'onGround' flag to false to prevent the player from jumping while being attacked
        player.setOnGround(false);

        // Decrease the player's health by the enemy's damage value
        player.setHealth(player.getHealth() - enemy.getDamage());
        // if the player's health is zero or less
        if(player.getHealth() <= 0){
            // play the death sound once and set the player as dead
            if(player.isAlive()){
                Sound deathSound = new Sound("sounds/deathh.wav");
                deathSound.start();
            }
            player.setAlive(false);
        }
        else {
            // play a random pain sound from pain1 to pain6
            int random = (int) (Math.random() * 6) + 1;
            Sound painSound = new Sound("sounds/pain" + random + ".wav");
            painSound.start();
        }
    }

    private void handleKnife() {
        if (!player.isHasWeapon()) {
            knife.hide();
            return;
        }

        knife.setFlipped(player.isFlipped());
        knife.setPosition(player.getX(), player.getY());

        if ((player.isOnGround() && (player.isMoveLeft() || player.isMoveRight())) || player.isAttack()) {
            knife.show();
        } else {
            knife.hide();
        }

        if (player.isAttack()) {
            for (Sprite enemy : enemies) {
                if (boundingKnifeCollision(knife, enemy)) {
                    if ((System.currentTimeMillis() - enemy.getLastAttack()) >= 360) {
                        // Full attack animation completed
                        enemy.setHealth(enemy.getHealth() - 1);
                        enemy.setLastAttack(System.currentTimeMillis());
                        // Handle knockback for enemy
                    } else {
                        // Partial attack animation
                        knife.setAnimation(stab);
                        player.setAnimation(playerAttacking);
                    }
                }
            }
            if ((System.currentTimeMillis() - player.getLastAttack()) >= 360) {
                // Full attack animation completed
                knife.hide();
                player.setAttack(false);
                player.setAnimation(playerIdle); // Change player animation to running
            } else {
                // Partial attack animation
                knife.setAnimation(stab);
                player.setAnimation(playerAttacking);
            }
        }
    }
    private void jump() {
        player.setAnimationSpeed(1.8f);
        player.setVelocityY(jumpVelocity);
        player.setAnimation(playerJumping);
        player.setOnGround(false);
    }

}
