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
    static int screenWidth = 1280;
    static int screenHeight = 720;
    float gravity = 0.001f;
    float jumpVelocity = -0.4f;
    float moveSpeed = 0.2f;
    boolean debug = true;
    long lastAttack = 10000;

    // Game resources
    Animation playerIdle, playerRunning, playerJumping, playerDeath, playerAttacking, stab, knifeRun;
    Animation thugIdle, thugRunning, thugDeath, thugAttacking, thugAttacked;
    Animation fountainIdle, chestOpen, cloud;
    BufferedImage bgImage1, bgImage2, bgImage3, youDiedImage, menu;
    Sound soundtrack;
    MidiPlayer midiPlayer;
    Sprite player = null;
    private Sprite knife, chest, fountain;
    ArrayList<Sprite> clouds = new ArrayList<Sprite>();
    ArrayList<Sprite> sprites = new ArrayList<Sprite>();
    ArrayList<Sprite> enemies = new ArrayList<Sprite>();
    ArrayList<Sprite> items = new ArrayList<Sprite>();
    TileMap tmap = new TileMap();    // Our tile map, note that we load it in init()
    long total;                    // The score will be the total time elapsed since a crash;
    float counter = 0;
    boolean paused = true;
    private boolean mainMenu = true;
    private boolean bgMusicPlaying = false;
    private boolean bgMusicStarted = false;


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

        midiPlayer = new MidiPlayer();
        midiPlayer.playMidi("sounds/biker_mice_from_mars.mid");

        // Create a set of background sprites that we can
        // rearrange to give the illusion of motion
        loadAnimations();

        // Create 3 clouds at random positions off the screen
        // to the right
        for (int i = 0; i < 3; i++) {
            s = new Sprite(cloud);
            s.setPosition(screenWidth * 2 + (int) (Math.random() * 500), (int) (Math.random() * 300));
            s.setVelocityX(-0.1f);
            clouds.add(s);
            sprites.add(s);
        }

        // Create 3 thugs at specific positions
        for(int i = 0; i < 3; i++) {
        	s = new Sprite(thugRunning);
            s.setFlipped(true);
        	s.setPosition(100 + (i * 100), 580);
        	s.setVelocityX(-0.1f);
        	enemies.add(s);
        	sprites.add(s);
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

        player.setPosition(55, 230);
        player.setVelocity(0, 0);

        knife.setPosition(player.getX(), player.getY());
        chest.setPosition(600, 260);
        fountain.setPosition(3030, 280);

    }

    public void loadAnimations() {
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

        thugAttacked = new Animation();
        thugAttacked.loadAnimationFromSheet("images/thug/Hurt.png", 2, 1, 60);
        thugAttacked.setLoop(false);

        thugRunning = new Animation();
        thugRunning.loadAnimationFromSheet("images/thug/Thug_Walk.png", 6, 1, 60);

        thugAttacking = new Animation();
        thugAttacking.loadAnimationFromSheet("images/thug/Thug_attack1.png", 6, 1, 60);
        thugAttacking.setLoop(false);

        thugDeath = new Animation();
        thugDeath.loadAnimationFromSheet("images/thug/Death.png", 6, 1, 60);

        thugDeath.setLoop(false);

        stab = new Animation();
        stab.loadAnimationFromSheet("images/player/knifeStab.png", 6, 1, 60);

        knifeRun = new Animation();
        knifeRun.loadAnimationFromSheet("images/player/knifeRun.png", 6, 1, 60);

        chestOpen = new Animation();
        chestOpen.loadAnimationFromSheet("images/objects/Chest_open.png", 7, 1, 60);
        chestOpen.setLoop(false);
        chestOpen.pause();

        fountainIdle = new Animation();
        fountainIdle.loadAnimationFromSheet("images/objects/Fountain.png", 4, 1, 60);

        // Initialise the player and one-off objects with animations
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
    }

    /**
     * Draw the current state of the game. Note the sample use of
     * debugging output that is drawn directly to the game screen.
     */
    public void draw(Graphics2D g) {

        AffineTransform transform = new AffineTransform();
        transform.scale(1.5, 1.5);

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
            menu = ImageIO.read(new File("images/menuxx.png"));
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

        for (Sprite s : clouds) {
            s.setOffsets(xo, yo);
            s.draw(g);
        }

        for (Sprite s : enemies) {
            s.setOffsets(xo, yo);
            if (s.isFlipped())
                s.drawTransformedFlip(g);
            else
                s.draw(g);
        }

        player.setOffsets(xo, yo);
        knife.setOffsets(xo, yo);
        chest.setOffsets(xo, yo);
        fountain.setOffsets(xo, yo);

        // Apply offsets to tile map and draw  it
        tmap.draw(g, xo, yo);

        if (player.isFlipped())
            player.drawTransformedFlip(g);
        else
            player.draw(g);

        if (knife.isFlipped())
            knife.drawTransformedFlip(g);
        else
            knife.draw(g);

        chest.draw(g);

        fountain.draw(g);

        for(int i = 0; i<player.getHealth(); i++) {
            g.drawImage(loadImage("images/hearts.png"), 10 + (i * 30), 30, null);
        }

        if (!player.isAlive()) {
            g.drawImage(youDiedImage, 0, 333, null);
        }

        if (debug) {
            g.setColor(Color.RED);
            player.drawBoundingBox(g);
            //player.drawBoundingCircle(g);
            knife.drawBounds(g);
            //knife.drawBoundingCircle(g);
            for (Sprite s : enemies) {
                s.drawBoundingBox(g);
                //s.drawBoundingCircle(g);
            }
            //g.drawString(String.valueOf(getFPS()).concat(": FPS (clearly doesn't work"), 30, 200);
            g.drawImage(loadImage("images/debug.png"), 30, 200, null);
        }

        if(mainMenu) {
            // draw the screen black
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, screenWidth, screenHeight);
            g.drawImage(menu, (screenWidth / 2) - (menu.getWidth() / 2), (screenHeight / 2) - (menu.getHeight() / 2), null);

            // Define the bounds of the start and exit buttons
            int buttonWidth = 55;
            int buttonHeight = 30;
            int buttonSpacing = 12;

            int startButtonX = (screenWidth / 2);
            int startButtonY = (screenHeight / 2)-30;                ;

            int exitButtonX = (screenWidth / 2);
            int exitButtonY = startButtonY + buttonHeight + buttonSpacing;

            // Add a mouse listener to the JFrame
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int x = e.getX();
                    int y = e.getY();

                    // Check if the mouse click is within the bounds of the start button
                    if (x >= startButtonX && x <= startButtonX + buttonWidth
                            && y >= startButtonY && y <= startButtonY + buttonHeight) {
                        // Perform the action for the start button
                        mainMenu = false;
                        paused = false;
                    }

                    // Check if the mouse click is within the bounds of the exit button
                    if (x >= exitButtonX && x <= exitButtonX + buttonWidth
                            && y >= exitButtonY && y <= exitButtonY + buttonHeight) {
                        // Perform the action for the exit button
                        stop();
                    }
                }
            });
        }
    }

    /**
     * Update any sprites and check for collisions
     *
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */
    public void update(long elapsed) {
        if(!mainMenu && !bgMusicStarted){
            midiPlayer.changeMidi("sounds/bt.mid");
            bgMusicStarted = true;
            bgMusicPlaying = true;
        }

        if (!paused) {
            // Make adjustments to the speed of the sprite due to gravity
            player.setVelocityY(player.getVelocityY() + (gravity * elapsed));

            // Then check for any collisions that may have occurred
            checkTileCollision(player, tmap);

            if (boundingBoxCollision(player, chest)) {
                chestOpen.play();
                player.setHasWeapon(true);
            }

            if (boundingBoxCollision(player, fountain)) {
                player.setHealth(4);
            }

            checkWeaponCollision(knife, enemies, elapsed);
            checkEnemyCollision(player, enemies, elapsed);

            // reposition the clouds to the right of the screen if they have moved off the left side
            for (Sprite s : clouds) {
                if (s.getX() < player.getX() - (screenWidth)) {
                    s.setPosition((player.getX() + (screenWidth * 2)) + (int) (Math.random() * 500), (int) (Math.random() * 250));
                }
            }

            checkPlayerStatus();
            checkEnemyStatus();

            // Update the animation and position of all the sprites
            for (Sprite s : sprites)
                s.update(elapsed);

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
        // This method just checks if the sprite has gone off the bottom screen.
        // Ideally you should use tile collision instead of this approach

        float difference = s.getY() + s.getHeight() - tmap.getPixelHeight();
        if (difference > 0) {
            s.setPosition(100, 300);
        }
    }

    public void checkPlayerStatus() {
        // Check the state of the player and update their animation and position accordingly
        if (player.isAlive()) {
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
            } else if (!player.isMoveLeft() && !player.isMoveRight() && player.isOnGround()) {
                player.setVelocityX(0);
                player.setAnimation(playerIdle);
            }

            if (player.isAttack()) {
                //if it has been 360ms since last attack
                if (System.currentTimeMillis() - lastAttack > 360) {
                    player.setAttack(false);
                }
                if (player.isHasWeapon()) {
                    knife.setAnimation(stab);
                    player.setAnimation(playerAttacking);
                } else {
                    player.setAnimation(playerAttacking);
                }
            } else {
                knife.setAnimation(knifeRun);
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

            if (player.isHasWeapon()) {
                knife.setFlipped(player.isFlipped());
                knife.setPosition(player.getX(), player.getY());
                if (((player.isMoveLeft() || player.isMoveRight()) && player.isOnGround()) || player.isAttack()) {
                    knife.show();
                } else {
                    knife.hide();
                }
            } else {
                knife.hide();
            }
        }
        else {
            player.setAnimation(playerDeath);
            player.setVelocityX(0);
            player.setVelocityY(0);
        }

    }

    public void checkEnemyStatus(){
        for (Sprite s : enemies) {
            if (s.isAlive()){
                if (s.getVelocityX()>0){
                    s.setMoveRight(true);
                    if(s.isFlipped()){
                        s.setFlipped(false);
                    }
                }
                else{
                    s.setMoveRight(false);
                }
                if (s.getVelocityX()<0){
                    s.setMoveLeft(true);
                    if(!s.isFlipped()){
                        s.setFlipped(true);
                    }
                }
                else{
                    s.setMoveLeft(false);
                }
            }
            else {
                s.setAnimation(thugDeath);
                s.setVelocityX(0);
                s.setVelocityY(0);
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

            case KeyEvent.VK_W:
                if (player.isOnGround()) {
                    player.setJump(true);
                }
                break;
            case KeyEvent.VK_D:
                player.setMoveRight(true);
                break;
            case KeyEvent.VK_A:
                player.setMoveLeft(true);
                break;
            case KeyEvent.VK_S:
                //Sound s = new Sound("sounds/caw.wav");
                //s.start();
                break;
            case KeyEvent.VK_ESCAPE:
                stop();
                break;
            case KeyEvent.VK_B:
                debug = !debug;
                break; // Flip the debug state

            case KeyEvent.VK_P:
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
                // set timer so that the player can only attack once every animation cycle
                if (System.currentTimeMillis() - lastAttack > 360) {
                    lastAttack = System.currentTimeMillis();
                    player.setAttack(true);
                }
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
            r1 = s1.getKnifeBounds();
        }
        Rectangle r2 = s2.getBoundingBox();

        // Check if the bounding boxes intersect
        if (r1.intersects(r2)) {
            // If they do, then check if the individual pixels
            // also intersect
            return s1.collidesWith(s2);
        }

        return false;
    }

    public boolean boundingKnifeCollision(Sprite s1, Sprite s2) {
        // Get the bounding boxes for each sprite
        Rectangle r1 = s1.getKnifeBounds();
        Rectangle r2 = s2.getBoundingBox();

        // Check if the bounding boxes intersect
        if (r1.intersects(r2)) {
            // If they do, then check if the individual pixels
            // also intersect
            return s1.collidesWith(s2);
        }

        return false;
    }

    public boolean boundingCircleCollision(Sprite s1, Sprite s2) {
        // Get the bounding circles for each sprite
        Circle r1 = s1.getBoundingCircle();
        Circle r2 = s2.getBoundingCircle();

        // Check if the bounding boxes intersect
        if (r1.intersects(r2)) {
            // If they do, then check if the individual pixels
            // also intersect
            return s1.collidesWith(s2);
        }

        return false;
    }

    public void checkWeaponCollision(Sprite knife, ArrayList<Sprite> enemies, long elapsed) {
        for(Sprite s : enemies){
            if (boundingKnifeCollision(knife, s) && player.isAttack()) {
                s.setVelocityY(-0.4f);
                if(player.isFlipped()){
                    s.setVelocityX(-0.1f);
                }
                else{
                    s.setVelocityX(0.1f);
                }
                s.setHealth(s.getHealth() - player.getDamage());
                if (s.getHealth() <= 0) {
                    s.setAlive(false);
                } else {
                    s.setAnimation(thugAttacked);
                }
            }
        }
    }

    public void checkEnemyCollision(Sprite player, ArrayList<Sprite> enemies, long elapsed) {
        for (Sprite s : enemies) {
            s.setVelocityY(s.getVelocityY() + (gravity * elapsed));
            checkEnemyTileCollision(s, tmap);
            if (boundingBoxCollision(player, s)) {
                    // if the player is not attacking, kill the player
                    if (s.isAlive()) {
                        s.setAnimation(thugAttacking);
                        if(s.isMoveLeft()){
                            player.setOnGround(false);
                            player.setVelocityY(-0.2f);
                            player.setVelocityX(-0.2f);
                        }
                        else if(s.isMoveRight()){
                            player.setOnGround(false);
                            player.setVelocityY(-0.2f);
                            player.setVelocityX(0.2f);
                        }
                        player.setHealth(player.getHealth()-s.getDamage());
                    }}}
        }




    public void handleCollision(Sprite s1, Sprite s2) {
        // Get the bounding boxes for each sprite
        Rectangle r1 = s1.getBoundingBox();
        Rectangle r2 = s2.getBoundingBox();

        // Check if the bounding boxes intersect
        if (r1.intersects(r2)) {
            // If they do, then check if the individual pixels
            // also intersect
            if (s1.collidesWith(s2)) {
                // If they do, then we have a collision
                // We can now handle the collision
                s1.setVelocityX(0);
                if (s2.getVelocityX() > 0) {
                    s2.setAnimation(thugAttacking);
                } else {
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
                            }
                        }
                    }
                }
            }
        }
    }

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

}
