import game2D.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Game demonstrates how we can override the GameCore class
 * to create our own 'game'. We usually need to implement at
 * least 'draw' and 'update' (not including any local event handling)
 * to begin the process. You should also add code to the 'init'
 * method that will initialise event handlers etc.
 * <p>
 * Student ID: 2925642
 */
public class Game extends GameCore implements MouseListener {
    // Useful game constants
    private float alpha = 0.0f;         // Was going to use for a fade in/out effect
    static int screenWidth = 1240;      // define the size of the game window
    static int screenHeight = 720;      // define the size of the game window
    float gravity = 0.00025f;           // acceleration of player's downward movement
    float jumpVelocity = -0.15f;         // initial velocity of player's upward jump movement
    float moveSpeed = 0.18f;             // speed of player's horizontal movement (0.18f works well)
    boolean debug = false;               // flag for printing debug information
    private static Font customFont;


    // Game resources
    Animation playerIdle, playerRunning, playerJumping, playerDeath, playerAttacking, stab, knifeRun,
            thugIdle, thugRunning, thugDeath, thugAttacking, thugHurt, fountainIdle, chestOpen, bird,
            bullet, gunThugIdle, gunThugAttack, gunThugDeath, gunThugHurt, gunThugWalk, spikes, keycard,
            screen, entry, lift, crusher, bossWalk, bossDeath, playerGun;
    BufferedImage bgImage1, bgImage2, bgImage3, youDiedImage, menu; // background / still images
    MidiPlayer midiPlayer; // to control background music
    private Sprite knife, chest, fountain, key, console, door, gun; // one off sprites
    private Player player; // the player sprite
    private Boss boss; // the boss sprite

    // ArrayLists to hold the sprites in the game (for collision detection etc.)
    // These get initialised here but are populated at game setup and dynamically during the game
    ArrayList<Sprite> birds = new ArrayList<>();
    ArrayList<Sprite> sprites = new ArrayList<>();
    ArrayList<Sprite> enemies = new ArrayList<>();
    ArrayList<Sprite> objects = new ArrayList<>();
    ArrayList<Sprite> shots = new ArrayList<>();
    ArrayList<Sprite> playerShots = new ArrayList<>();
    ArrayList<Thug> thugs = new ArrayList<>();
    ArrayList<GunThug> gunThugs = new ArrayList<>();
    ArrayList<Sprite> traps = new ArrayList<>();
    ArrayList<Platform> platforms = new ArrayList<>();
    ArrayList<Tile> collidedTiles = new ArrayList<Tile>();
    TileMap tmap = new TileMap();           // Our tile map, note that we load it in init()
    long total;                             // The score will be the total time elapsed since a crash;
    boolean paused = true;                  // Is the game paused?
    private boolean mainMenu = true;        // Is the main menu showing?
    private boolean bgMusicStarted = false; // Has the background music started?
    private boolean bgMusicPlaying = false; // Is the background music playing?
    private boolean mute = false;            // Is the game muted?
    private boolean clear = false;          // Is the game cleared?
    private boolean doorOpened = false;     // Has the door been opened?
    private boolean levelTwo = false;       // Is the player on level two?
    private boolean gameOver = false;       // Is the game over?
    private boolean bossSpawned;            // Has the boss spawned?

    // I didn't realise how many of these I would need, in hindsight I should have used an enum or array
    private boolean printInstructions;      // Should the instructions be printed?
    private boolean messageZero = false;    // Should messageZero be printed?
    private boolean messageOne = false;     // Should messageOne be printed?
    private boolean messageTwo = false;     // Should messageTwo be printed?
    private boolean messageThree = false;   // Should messageThree be printed?
    private boolean messageFour = false;    // Should messageFour be printed?
    private boolean messageFive = false;    // Should messageFive be printed?


    /**
     * The obligatory main method that creates
     * an instance of our class and starts it running
     *
     * @param args The list of parameters this program might use (ignored)
     */
    public static void main(String[] args) {
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("images/Orbitron-Bold.ttf")).deriveFont(30f);
        } catch (IOException|FontFormatException e) {
            System.err.println("Error loading font: " + e);
            customFont = new Font("Arial", Font.PLAIN, 24);
        }
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
        setSize(screenWidth, screenHeight);
        setVisible(true);
        // Create a MIDI player to play background music
        midiPlayer = new MidiPlayer();
        // Load animation resources
        loadAnimations();
        handleMusic();
        addMouseListener(this);

        if (!levelTwo) {
            // Load the tile map and set the size of the game window to match the map's size
            setupLevelOne();
        } else {
            setupLevelTwo();
        }

        // Print the tile map to the console for debugging purposes
        System.out.println(tmap);
    }//end init

    /**
     * Method for setting up level one
     * tilemap, sprites, etc.
     * sets important variables to appropriate values
     * sets position of sprite spawns to locations based on key tiles
     * adds sprites to their respective ArrayLists etc.
     */
    private void setupLevelOne() {
        tmap.loadMap("maps", "map1.txt");
        total = 0;  // Reset the total elapsed level time
        Sprite s;    // Temporary reference to a sprite
        // Create 5 thugs at specific positions and add them to the list of sprites
        for (int i = 0; i < 5; i++) {
            s = new Thug(thugRunning);
            s.setFlipped(true);
            s.setPosition(findLocationByTile(tmap, 'T').get(0) + (i * 150), findLocationByTile(tmap, 'T').get(1) - tmap.getTileHeight());

            // if i is odd
            if (i % 2 == 1)
                s.setVelocityX(0.1f);
            else
                s.setVelocityX(-0.1f);

            enemies.add(s);
            thugs.add((Thug) s);
            sprites.add(s);
        }

        for (int i = 0; i < 3; i++) {
            s = new Sprite(spikes);
            s.setPosition(findLocationByTile(tmap, 'D').get(0) + (i * tmap.getTileWidth()), findLocationByTile(tmap, 'D').get(1) - tmap.getTileHeight());
            s.setVelocityX(0);
            sprites.add(s);
            traps.add(s);
        }

        // Create a gun thugs at specific positions and add them to the list of sprites
        for (int i = 0; i < 3; i++) {
            s = new GunThug(gunThugIdle);
            s.setFlipped(true);
            if (i == 0) {
                s.setStationary(true);
                s.setPosition(findLocationByTile(tmap, 'G').get(0), findLocationByTile(tmap, 'G').get(1) - tmap.getTileHeight());
                s.setVelocityX(0);
            } else if (i == 1) {
                s.setPosition(findLocationByTile(tmap, 'H').get(0), findLocationByTile(tmap, 'H').get(1) - tmap.getTileHeight());
                s.setVelocityX(moveSpeed / 2);
            } else if (i == 2) {
                s.setPosition(findLocationByTile(tmap, 'I').get(0), findLocationByTile(tmap, 'I').get(1) - tmap.getTileHeight());
                s.setVelocityX(moveSpeed / 2);
            }
            enemies.add(s);
            gunThugs.add((GunThug) s);
            sprites.add(s);
        }

        // Set the initial position and velocity of the player sprite
        player.setPosition(findLocationByTile(tmap, 'S').get(0), findLocationByTile(tmap, 'S').get(1) - tmap.getTileHeight());
        player.setVelocity(0, 0);


        // Set the initial positions of the knife, chest, and fountain sprites
        knife.setPosition(player.getX(), player.getY());  // Set the knife position to be the same as the player's position
        chest.setPosition(findLocationByTile(tmap, 'C').get(0), findLocationByTile(tmap, 'C').get(1) - tmap.getTileHeight() / 2);  // Set the chest position to a specific location
        fountain.setPosition(findLocationByTile(tmap, 'L').get(0), findLocationByTile(tmap, 'L').get(1) - tmap.getTileHeight());  // Set the fountain position to a specific location
        key.setPosition(findLocationByTile(tmap, 'K').get(0), findLocationByTile(tmap, 'K').get(1) + tmap.getTileHeight() / 5);  // Set the fountain position to a specific location
        door.setPosition(findLocationByTile(tmap, 'o').get(0) + (tmap.getTileWidth() / 2), findLocationByTile(tmap, 'o').get(1) - tmap.getTileHeight());  // Set the door position to a specific location
        console.setPosition(findLocationByTile(tmap, 'o').get(0), findLocationByTile(tmap, 'o').get(1) - tmap.getTileHeight() + (tmap.getTileHeight() * 0.4f));  // Set the door position to a specific location

        objects.add(key);
        objects.add(door);
        objects.add(console);
        objects.add(fountain);
        objects.add(chest);
        objects.add(knife);

//         Create 3 birds at random positions off the screen to the right
//         and add them to the list of sprites
        for (int i = 0; i < 3; i++) {
            s = new Sprite(bird);
            s.setPosition(screenWidth * 2 + (int) (Math.random() * 500), (int) (Math.random() * 300));
            s.setVelocityX(-0.1f);
            s.setFlipped(true);
            birds.add(s);
            sprites.add(s);
        }

    }// end setLevelOne method

    /**
     * Method for setting up level two
     * tilemap, sprites, etc.
     * sets important variables to appropriate values
     * sets position of sprite spawns to locations based on key tiles
     * adds sprites to their respective ArrayLists etc.
     */
    private void setupLevelTwo() {
        tmap.loadMap("maps", "map2.txt");
        total = 0;  // Reset the total elapsed level time
        Sprite s;    // Temporary reference to a sprite

        // Set the initial position and velocity of the player sprite
        player.setPosition(findLocationByTile(tmap, 'S').get(0), findLocationByTile(tmap, 'S').get(1) - tmap.getTileHeight());
        player.setVelocity(0, 0);
        player.setHasWeapon(true);
        player.setHasKeyCard(false);

        knife.setPosition(player.getX(), player.getY());  // Set the knife position to be the same as the player's position

        chest.setPosition(findLocationByTile(tmap, '£').get(0), findLocationByTile(tmap, '£').get(1) - tmap.getTileHeight() / 2);
        objects.add(chest);
        objects.add(knife);

        // Create 3 Crusher at specific positions and add them to the list of sprites
        for (int i = 0; i < 4; i++) {
            s = new Sprite(crusher);
            s.setPosition(findLocationByTile(tmap, '3').get(0) + tmap.getTileWidth() / 3 + (i * tmap.getTileWidth()), findLocationByTile(tmap, '3').get(1) - tmap.getTileHeight());
            s.setVelocity(0, 0);
            sprites.add(s);
            traps.add(s);
        }

        // Create 3 Platform at specific positions and add them to the list of sprites
        for (int i = 0; i < 3; i++) {
            s = new Platform(lift);

            if (i == 0) {
                s.setPosition(findLocationByTile(tmap, '0').get(0) + tmap.getTileWidth() / 10, findLocationByTile(tmap, '0').get(1) - tmap.getTileHeight());
            } else if (i == 1) {
                s.setPosition(findLocationByTile(tmap, '1').get(0) + tmap.getTileWidth() / 10, findLocationByTile(tmap, '1').get(1) - tmap.getTileHeight());
            } else if (i == 2) {
                s.setPosition(findLocationByTile(tmap, '2').get(0) + tmap.getTileWidth() / 10, findLocationByTile(tmap, '2').get(1) - tmap.getTileHeight());
            }

            s.setVelocity(0, -moveSpeed);
            sprites.add(s);
            platforms.add((Platform) s);
        }

        // Create 3 Thug at specific positions and add them to the list of sprites
        for (int i = 0; i < 3; i++) {
            s = new Thug(thugRunning);
            s.setFlipped(true);
            s.setPosition(findLocationByTile(tmap, 'T').get(0) + (i * 150), findLocationByTile(tmap, 'T').get(1) - tmap.getTileHeight());

            // if i is odd
            if (i % 2 == 1)
                s.setVelocityX(0.1f);
            else
                s.setVelocityX(-0.1f);

            enemies.add(s);
            thugs.add((Thug) s);
            sprites.add(s);
        }

        //Create 9 spikes at specific positions and add them to the list of sprites
        for (int i = 0; i < 3; i++) {
            s = new Sprite(spikes);
            s.setPosition(findLocationByTile(tmap, '6').get(0) + tmap.getTileWidth() / 6 + (i * 58), findLocationByTile(tmap, '6').get(1));
            s.setFlippedVertically(true);
            sprites.add(s);
            traps.add(s);
            s = new Sprite(spikes);
            s.setPosition(findLocationByTile(tmap, '7').get(0) + tmap.getTileWidth() / 6 + (i * 58), findLocationByTile(tmap, '7').get(1));
            s.setFlippedVertically(true);
            sprites.add(s);
            traps.add(s);
            s = new Sprite(spikes);
            s.setPosition(findLocationByTile(tmap, '8').get(0) + tmap.getTileWidth() / 6 + (i * 58), findLocationByTile(tmap, '8').get(1));
            s.setFlippedVertically(true);
            sprites.add(s);
            traps.add(s);
        }

        // create one gunthug at the + position, flipped
        s = new GunThug(gunThugIdle);
        s.setPosition(findLocationByTile(tmap, '+').get(0), findLocationByTile(tmap, '+').get(1) - tmap.getTileHeight());
        s.setStationary(true);
        enemies.add(s);
        gunThugs.add((GunThug) s);
        sprites.add(s);

    }// end setupLevelTwo method

    /**
     * Method for handling the music in the game
     * plays the appropriate music based on the level
     * and whether or not the music is muted
     */
    public void handleMusic() {
        if(!mute) {
            if (mainMenu) {
                midiPlayer.stopMidi();
                midiPlayer.playMidi("sounds/01_-_Hacker_Scum_The_Resistance.mid");
            } else if (!mainMenu && !levelTwo) {
                midiPlayer.stopMidi();
                midiPlayer.playMidi("sounds/02_Neon-Flavored_Starvation.mid");
            } else if (levelTwo) {
                midiPlayer.stopMidi();
                midiPlayer.playMidi("sounds/03_Cyberspace_Lurkers.mid");
            }
        }
        else {
            midiPlayer.stopMidi();
        }
    }

    /**
     * Method for loading all the animations used in the game
     * animations are loaded from the images folder and added to the appropriate animation object
     * certain animations are loaded from individual images instead of an animation sheet
     * because I couldnt find an animation sheet that I liked
     * some animations have their values tweaked to make them look better
     * one-off sprites are assigned their animations here
     */
    public void loadAnimations() {
        // Load animations for the player's idle, attacking, running, jumping and death states
        playerIdle = new Animation();
        playerIdle.loadAnimationFromSheet("images/player/idle.png", 4, 1, 60);

        playerGun = new Animation();
        playerGun.loadAnimationFromSheet("images/player/gun.png", 1, 1, 60);

        keycard = new Animation();
        keycard.loadAnimationFromSheet("images/objects/Card.png", 8, 1, 60);

        // I couldnt find an animation sheet for the spike trap that I wanted to use, so I use individual images instead
        String[] files = {"images/objects/long_metal_spike_01.png", "images/objects/long_metal_spike_02.png",
                "images/objects/long_metal_spike_03.png", "images/objects/long_metal_spike_04.png", "images/objects/long_metal_spike_03.png",
                "images/objects/long_metal_spike_02.png", "images/objects/long_metal_spike_01.png"};

        spikes = new Animation();
        spikes.loadFramesFromFiles(files, 60);
        spikes.setAnimationSpeed(0.1f);

        bossWalk = new Animation();
        bossWalk.loadAnimationFromSheet("images/bosses/BossWalkL.png", 4, 1, 60);
        bossWalk.setAnimationSpeed(0.1f);

        bossDeath = new Animation();
        bossDeath.loadAnimationFromSheet("images/bosses/BossDeathL.png", 6, 1, 60);
        bossDeath.setAnimationSpeed(0.1f);


        crusher = new Animation();
        crusher.loadAnimationFromSheet("images/objects/Crusher.png", 8, 1, 60);
        crusher.setAnimationSpeed(0.1f);

        lift = new Animation();
        lift.loadAnimationFromSheet("images/objects/Elevator.png", 4, 1, 60);
        lift.setAnimationSpeed(0.1f);

        screen = new Animation();
        screen.loadAnimationFromSheet("images/objects/Screen2.png", 4, 1, 60);

        entry = new Animation();
        entry.loadAnimationFromSheet("images/objects/Entry.png", 8, 1, 60);
        entry.pause();

        playerAttacking = new Animation();
        playerAttacking.loadAnimationFromSheet("images/player/punch.png", 6, 1, 60);

        playerRunning = new Animation();
        playerRunning.loadAnimationFromSheet("images/player/run.png", 6, 1, 60);

        playerJumping = new Animation();
        playerJumping.loadAnimationFromSheet("images/player/jump.png", 4, 1, 60);

        playerDeath = new Animation();
        playerDeath.loadAnimationFromSheet("images/player/death.png", 6, 1, 60);

        // Load animations for the thug's idle, attacked, running, attacking and death states
        thugIdle = new Animation();
        thugIdle.loadAnimationFromSheet("images/thug/Thug_idle.png", 4, 1, 60);

        thugHurt = new Animation();
        thugHurt.loadAnimationFromSheet("images/thug/Hurt.png", 2, 1, 60);

        thugRunning = new Animation();
        thugRunning.loadAnimationFromSheet("images/thug/Thug_Walk.png", 6, 1, 60);

        thugAttacking = new Animation();
        thugAttacking.loadAnimationFromSheet("images/thug/Thug_attack1.png", 6, 1, 60);
        thugAttacking.setAnimationSpeed(1.5f);

        thugDeath = new Animation();
        thugDeath.loadAnimationFromSheet("images/thug/Death.png", 6, 1, 60);

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

        // Load animations for the gunthug's idle, attacking, walking, and death states

        gunThugIdle = new Animation();
        gunThugIdle.loadAnimationFromSheet("images/gunthug/Idle.png", 4, 1, 60);

        gunThugAttack = new Animation();
        gunThugAttack.loadAnimationFromSheet("images/gunthug/Attack.png", 9, 1, 30);

        gunThugWalk = new Animation();
        gunThugWalk.loadAnimationFromSheet("images/gunthug/Walk.png", 6, 1, 60);

        gunThugDeath = new Animation();
        gunThugDeath.loadAnimationFromSheet("images/gunthug/Death.png", 6, 1, 60);

        gunThugHurt = new Animation();
        gunThugHurt.loadAnimationFromSheet("images/gunthug/Hurt.png", 2, 1, 60);

        bird = new Animation();
        bird.loadAnimationFromSheet("images/Bird.png", 6, 1, 60);

        // Initialise the player and objects with the loaded animations
        player = new Player(playerIdle);
        sprites.add(player);

        chest = new Sprite(chestOpen);
        sprites.add(chest);

        knife = new Sprite(knifeRun);
        //knife.setWeapon(true);
        sprites.add(knife);

        fountain = new Sprite(fountainIdle);
        sprites.add(fountain);

        key = new Sprite(keycard);
        sprites.add(key);

        console = new Sprite(screen);
        sprites.add(console);

        door = new Sprite(entry);
        sprites.add(door);

        gun = new Sprite(playerGun);
        sprites.add(gun);

    } // End of loadAnimations method

    /**
     * Draw the current state of the game. Note the sample use of
     * debugging output that is drawn directly to the game screen.
     * Draws the background, player, enemies, and objects.
     * Draws based on various booleans and conditions such as
     * whether the main menu / credits screen is being displayed,
     * whether the player is dead, if objects are flipped etc.
     *
     * @param g The graphics context on which to draw
     */
    public void draw(Graphics2D g) {

        if (gameOver) {
            drawCreditsScreen(g);
        } else {

            // Create an AffineTransform to scale the graphics
            AffineTransform transform = new AffineTransform();
            transform.scale(1.5, 1.5);

            // Calculate the x and y offsets to center the view around the player
            int xo = -(int) player.getX() + 450;
            int yo = -(int) player.getY() + 400;

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

            // Apply offsets to the tile map and draw
            tmap.draw(g, xo, yo);

            // Draw the objects - one time so far a ConcurrentModificationException has been thrown
            // when the player's shots hit the boss enemy
            // I used an Iterator when removing the shots to prevent this but it still happened once
            // I haven't been able to reproduce it since
            for (Sprite s : sprites) {
                s.setOffsets(xo, yo);
                if (s.isFlipped()) {
                    s.drawTransformedFlip(g);
                } else if (s.isFlippedVertically()) {
                    s.drawTransformedFlipVertical(g);
                } else {
                    s.draw(g);
                }
            }

            if (player.isFlipped()) {
                player.drawTransformedFlip(g);
            } else {
                player.draw(g);
            }


            // Draw the player's health hearts
            try {
                for (int i = 0; i < player.getHealth() / 5; i++) {
                    g.drawImage(loadImage("images/hearts.png"), 10 + (i * 30), 30, null);
                }
            } finally {
                if (player.isAlive())
                    g.drawImage(loadImage("images/hearts.png"), 10, 30, null);
            }

            if (player.hasKeyCard()) {
                // get one frame of the keycard animation
                g.drawImage(keycard.getFrameImage(0), 10, 70, null);
            }

            // If the player is dead, display the "you died" image
            if (!player.isAlive()) {
                g.drawImage(youDiedImage, 0, tmap.getTileHeight() * 6, null);
                g.setColor(Color.blue);
                g.drawString("backspace to restart", screenWidth / 3, tmap.getTileHeight() * 2);
                g.setColor(Color.magenta);
                g.drawString("or ESC to quit", screenWidth / 3, tmap.getTileHeight() * 3);
            }

            if (player.isHasGun()) {
                g.drawImage(loadImage("images/player/gun.png"), (int) player.getX() - player.getWidth(), (int) player.getY(), null);
            }

            if (bossSpawned) {
                // draw a red rectangle over the boss to show how much health it has left
                g.setColor(Color.red);
                g.fillRect((int) boss.getX() - xo, (int) boss.getY() - yo - 10, boss.getWidth(), 5);
                g.setColor(Color.green);
                g.fillRect((int) boss.getX() - xo, (int) boss.getY() - yo - 10, boss.getHealth(), 5);

                // draw the boss's health
                g.setColor(Color.blue);
                g.setFont(customFont);
                if (boss.getHealth() < 0)
                    g.drawString("Boss Health: 0", 10, 100);
                else
                    g.drawString("Boss Health: " + boss.getHealth(), 10, 100);

                // draw the boss's text
                g.setColor(Color.magenta);
                g.drawString("Boss: Mower Guy Has Arrived", 10, 130);
            }

            if (printInstructions || messageZero) {
                g.setFont(customFont);
                g.setColor(Color.blue);
                g.drawString("Backspace : Reset", screenWidth / 100, tmap.getTileHeight() * 3);
                g.setColor(Color.magenta);
                g.drawString("Space : Attack (you need a weapon)", screenWidth / 100, tmap.getTileHeight() * 4);
                g.setColor(Color.blue);
                g.drawString("Left Mouse : Shoot (you need a gun)", screenWidth / 100, tmap.getTileHeight() * 5);
                g.setColor(Color.magenta);
                g.drawString("WAD : Move", screenWidth / 100, tmap.getTileHeight() * 6);
                g.setColor(Color.blue);
                g.drawString("M : Mute", screenWidth / 100, tmap.getTileHeight() * 7);
                g.setColor(Color.magenta);
                g.drawString("P : Pause", screenWidth / 100, tmap.getTileHeight() * 8);
                g.setColor(Color.blue);
                g.drawString("B : Debug / Controls", screenWidth / 100, tmap.getTileHeight() * 9);
                g.setColor(Color.magenta);
                g.drawString("C : Clear Collided Tiles", screenWidth / 100, tmap.getTileHeight() * 10);
                g.setColor(Color.blue);
                g.drawString("ESC : Exit", screenWidth / 100, tmap.getTileHeight() * 11);
            }

            if (messageOne) {
                g.setFont(customFont);
                g.setColor(Color.magenta);
                g.drawString("the fountain heals if you're hurt", screenWidth / 3, tmap.getTileHeight() * 2);
                g.setColor(Color.blue);
                g.drawString("get hit (not shot!) if you want to see", screenWidth / 3, tmap.getTileHeight() * 3);
            }

            if (messageTwo) {
                g.setFont(customFont);
                g.setColor(Color.magenta);
                g.drawString("you have to be close to stab", screenWidth / 3, tmap.getTileHeight() * 2);
                g.setColor(Color.blue);
                g.drawString("press space now you have the knife", screenWidth / 3, tmap.getTileHeight() * 3);
            }

            if (messageThree) {
                g.setFont(customFont);
                g.setColor(Color.magenta);
                g.drawString("you missed something", screenWidth / 3, tmap.getTileHeight() * 2);
                g.setColor(Color.blue);
                g.drawString("you need a keycard to progress", screenWidth / 3, tmap.getTileHeight() * 3);
            }

            if (messageFour) {
                g.setFont(customFont);
                g.setColor(Color.blue);
                g.drawString("you found a gun!", screenWidth / 3, tmap.getTileHeight() * 4);
                g.setColor(Color.magenta);
                g.drawString("point and click to shoot!", screenWidth / 3, tmap.getTileHeight() * 5);
            }

            if (messageFive) {
                g.setFont(customFont);
                g.setColor(Color.magenta);
                g.drawString("they can't shoot you when you're too close", screenWidth / 3, tmap.getTileHeight() * 2);
                g.setColor(Color.blue);
                g.drawString("they can't shoot you through walls", screenWidth / 3, tmap.getTileHeight() * 3);
            }

            // Debug mode drawing
            if (debug) {
                // Set color to red for bounding box drawing
                g.setColor(Color.RED);

                // Draw the player's bounding box
                player.drawBoundingBox(g);
                //player.drawBoundingCircle(g); // Draw the player's bounding circle

                // Draw the knife's bounding box
                knife.drawBoundingBox(g);
                //knife.drawBoundingCircle(g); // Draw the knife's bounding circle

                // Draw the enemies' bounding boxes
                for (Sprite s : sprites) {
                    s.drawBoundingBox(g);
                    //s.drawBoundingCircle(g); // Draw the enemy's bounding circle
                }

                // Draw the debug image and text
                drawCollidedTiles(g, tmap, xo, yo);
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

                addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        int x = e.getX();
                        int y = e.getY();

                        // Check if the mouse click is within the bounds of the start button
                        if (x >= startButtonX && x <= startButtonX + buttonWidth
                                && y >= startButtonY && y <= startButtonY + buttonHeight) {

                            // Change the game state to playing
                            mainMenu = false;

                            // Change the music
                            midiPlayer.changeMidi("sounds/02_Neon-Flavored_Starvation.mid");
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
        }
    }// End draw method

    /**
     * Update any sprites and check for collisions
     *
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */
    public void update(long elapsed) {

        if (!gameOver) {
            // Check if the game is not paused
            if (!paused) {
                if (debug) {
                    printInstructions = true;
                } else {
                    printInstructions = false;
                }

                // Make adjustments to the speed of the player sprite due to gravity
                player.setVelocityY(player.getVelocityY() + (gravity * elapsed));

                // Check for any player collisions that may have occurred with tiles
                checkTileCollision(player, tmap);

                Iterator<Sprite> shotIterator = shots.iterator();
                while (shotIterator.hasNext()) {
                    Sprite shot = shotIterator.next();
                    if (boundingBoxCollision(shot, player)) {
                        player.setHealth(player.getHealth() - player.getMAX_HEALTH());
                        // Remove the shot from the list of sprites
                        shotIterator.remove();
                        sprites.remove(shot);
                    }
                }

                // This threw a ConcurrentModificationException once when I was testing the game
                // I used this Iterator in the first place to prevent that from happening
                // I haven't been able to reproduce the error yet
                Iterator<Sprite> playerShotIterator = playerShots.iterator();
                while (playerShotIterator.hasNext()) {
                    Sprite shot = playerShotIterator.next();
                    if (boundingBoxCollision(shot, boss)) {
                        boss.setHealth(boss.getHealth() - shot.getDamage());
                        // Remove the shot from the list of sprites
                        playerShotIterator.remove();
                        sprites.remove(shot);
                        if(!mute) {
                            Sound hitSound = new Sound("sounds/punch.wav");
                            hitSound.start();
                        }
                    }
                }

                // Check if the player's bounding box has collided with the chest
                if (boundingBoxCollision(player, chest)) {
                    if (!levelTwo) {
                        messageTwo = true;
                    } else {
                        messageFour = true;
                    }
                    // Play the chest open animation
                    chestOpen.play();
                    // Give the player a weapon
                    player.setHasWeapon(true);
                    // Set the player's damage to 3
                    player.setDamage(3);
                } else {
                    messageTwo = false;
                }

                if (boundingBoxCollision(player, key)) {
                    key.hide();
                    if(!player.hasKeyCard() && !mute) {
                        Sound keySound = new Sound("sounds/effect.wav");
                        keySound.start();
                    }
                    player.setHasKeyCard(true);
                }

                // Check if the player's bounding box has collided with the fountain
                if (boundingBoxCollision(player, fountain)) {
                    messageOne = true;
                    if (player.getHealth() < player.getMAX_HEALTH()) {
                        // Restore the player's health to 4
                        player.setHealth(player.getMAX_HEALTH());
                        if(!mute) {
                            Sound waterSound = new Sound("sounds/splash.wav");
                            waterSound.start();
                        }
                    }
                } else {
                    messageOne = false;
                }


                // Check if the player's bounding box has collided with the console if the door is closed
                if (!doorOpened) {
                    if (boundingBoxCollision(player, console)) {
                        if (player.hasKeyCard()) {
                            if (!doorOpened) {
                                // The stab sound is passed through a filter that reduces the high frequencies
                                // This warps the sound and makes it sound mechanical
                                LowPassFilterStream lowPassFilter = null;
                                try {
                                    lowPassFilter = new LowPassFilterStream(new FileInputStream("sounds/Stab_sound_effect.wav"), 10);
                                } catch (FileNotFoundException ex) {
                                    throw new RuntimeException(ex);
                                }
                                if(!mute) {
                                    Sound filteredSound = new Sound("sounds/Stab_sound_effect.wav", lowPassFilter);
                                    filteredSound.start();
                                }

                                entry.pauseAt(4);
                                entry.play();
                                doorOpened = true;
                            }
                        } else {
                            if(!messageThree) {
                                if(!mute) {
                                    Sound errorSound = new Sound("sounds/effect.wav");
                                    errorSound.start();
                                }
                                messageThree = true;
                            }
                        }
                    } else {
                        messageThree = false;
                    }
                }


                // Check for any collisions that may have occurred with the player's weapon and enemies
                checkWeaponCollision(knife, enemies, elapsed);

                // Check for any collisions that may have occurred with enemies and the player
                checkEnemyCollision(player, enemies, elapsed);

                // Check for any collisions that may have occurred with projectiles and the tiles
                for (Sprite shot : shots) {
                    checkEnemyTileCollision(shot, tmap);
                }

                // Check for any collisions that may have occurred with player projectiles and the tiles
                for (Sprite shot : playerShots) {
                    checkEnemyTileCollision(shot, tmap);
                }

                // Check for any collisions that may have occurred with the player and the platforms
                for (Platform p : platforms) {
                    checkEnemyTileCollision(p, tmap);
                    while (boundingBoxCollision(player, p)) {
                        if (!player.isOnPlatform()) {
                            // this line makes the player stand slightly above the platform not on it
                            // I tried amendments to the Y location e.g. player.getHeight()*1.25 etc but it crashed
                            player.setPosition(player.getX(), p.getY() - player.getHeight());
                            player.setOnPlatform(true);
                        }
                        player.setVelocityY(p.getVelocityY());
                        player.setJump(false);
                        player.setOnGround(true);
                    }
                    if (!boundingBoxCollision(player, p)) {
                        player.setOnPlatform(false);
                    }

                }

                // Check for any collisions that may have occurred with the player and the traps
                for (Sprite s : traps) {
                    if (boundingBoxCollision(player, s)) {
                        if (player.isAlive()) {
                            EchoFilterStream echoFilter = null;
                            try {
                                // play an echoed death sound when the player dies to a trap
                                echoFilter = new EchoFilterStream(new FileInputStream("sounds/death.wav"), 44444, 0.3f);
                            } catch (FileNotFoundException ex) {
                                throw new RuntimeException(ex);
                            }
                            Sound filteredSound = new Sound("sounds/death.wav", echoFilter);
                            if (!mute)
                                filteredSound.start();
                        }
                        player.setHealth(player.getHealth() - player.getMAX_HEALTH());
                    }
                }

                // Check for any times that the player is in line of sight of an ranged enemy
                for (GunThug g : gunThugs) {
                    if (g.isAlive() && player.isAlive()) {
                        // check if enemy has line of sight to player
                        if (checkLineOfSight(player, g)) {
                            // if so, shoot at player
                            g.setAnimation(gunThugAttack);

                            // play a gunshot sound
                            Sound gunShot = new Sound("sounds/gunshot.wav");
                            if (!mute)
                                gunShot.start();

                            // create a new bullet and send it towards the player
                            // Load a single bullet animation
                            bullet = new Animation();
                            bullet.addFrame(loadImage("images/objects/bullet.png"), 1000);
                            Projectile shot = new Projectile(bullet);
                            sprites.add(shot);
                            shots.add(shot);
                            shot.setPosition(g.getX(), g.getY() + g.getHeight() / 2);
                            if (!g.isFlipped()) {
                                shot.setVelocityX(0.5f);
                            } else
                                shot.setVelocityX(-0.5f);
                        }
                    }
                }

                // Reposition the birds to the right of the screen if they have moved off the left side
                for (Sprite s : birds) {
                    if (s.getX() < player.getX() - (screenWidth)) {
                        // Set the new position of the bird
                        s.setPosition((player.getX() + (screenWidth * 2)) + (int) (Math.random() * 500), (int) (Math.random() * 250));
                    }
                }

                // Check the status of all enemies
                checkEnemyStatus();

                // Check the status of the player
                checkPlayerStatus();

                // Update the animation and position of all the sprites
                // minor conditional amendments to certain sprites to make them work better
                for (Sprite s : sprites) {
                    if (s instanceof GunThug && s.isAlive()) {
                        if (s.getVelocityX() > 0 || s.getVelocityX() < 0)
                            s.setAnimation(gunThugWalk);
                        else
                            s.setAnimation(gunThugIdle);
                    }
                    if (s instanceof Boss && s.isAlive()) {
                        if (s.getHealth() <= 0) {
                            s.setVelocity(0, 0);
                            s.setAnimation(bossDeath);

                            // Set a Timer to delay the game over screen
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    gameOver = true;
                                }
                            }, 3000);
                        }
                    }
                    s.update(elapsed);
                }

                // Handle the screen edge collision for the player sprite and tile map
                handleScreenEdge(player, tmap, elapsed);
            }
        }
    }// End update method


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
            s.setPosition(findLocationByTile(tmap, 'S').get(0), findLocationByTile(tmap, 'S').get(1) - tmap.getTileHeight());
        }
    }


    /**
     * Checks the status of the player and performs the appropriate actions
     */
    public void checkPlayerStatus() {

        if (player.getHealth() <= 0) {
            player.setAlive(false);
        }
        // Check if the player is alive. If not, set the death animation and stop its movement
        if (!player.isAlive()) {
            Animation death = new Animation();
            death = playerDeath;
            death.setLoop(false);
            player.setAnimation(death);
            player.setVelocityX(0);
            if (player.isOnGround()) {
                player.setVelocityY(0);
            }
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
        handleKnifeGun();

        // Check if the player has jumped and is on the ground. If so, perform a jump
        if (player.isJump() && player.isOnGround()) {
            jump();
        }
    }


    /**
     * Checks the status of all enemies and performs the appropriate actions
     */
    public void checkEnemyStatus() {
        // Loop through all the enemies and check their status
        for (Sprite enemy : enemies) {

            // Check if the enemy is alive
            if (enemy.isAlive()) {

                // Check if the enemy is attacking
                if (enemy.isAttack()) {
                    // if the enemy is a Thug class
                    if (enemy instanceof Thug) {
                        Thug thug = (Thug) enemy;
                        thug.setAnimation(thugAttacking);
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                thug.setAnimation(thugRunning);
                            }
                        }, 500);
                        thug.setLastAttack(System.currentTimeMillis());
                        thug.setAttack(false);
                        playerHit(thug);
                    }
                    // if the enemy is a GunThug class
                    if (enemy instanceof GunThug) {
                        GunThug gunThug = (GunThug) enemy;
                        gunThug.setAnimation(gunThugAttack);
                        gunThug.setLastAttack(System.currentTimeMillis());
                        gunThug.setAttack(false);
                        playerHit(gunThug);

                        if (System.currentTimeMillis() - gunThug.getLastAttack() >= 360)
                            gunThug.setAnimation(gunThugWalk);
                    }
                } else {
                    // if the enemy is a Thug class
                    if (enemy instanceof Thug) {
                        Thug thug = (Thug) enemy;
                        thug.setAnimation(thugRunning);
                    }
                }

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
                if (enemy instanceof Thug) {
                    Animation tDeath = new Animation();
                    tDeath = thugDeath;
                    tDeath.setLoop(false);
                    enemy.setAnimation(tDeath);
                    enemy.setVelocityX(0);
                    if (enemy.isOnGround())
                        enemy.setVelocityY(0f);
                }
                if (enemy instanceof GunThug) {
                    Animation gDeath = new Animation();
                    gDeath = gunThugDeath;
                    gDeath.setLoop(false);
                    enemy.setAnimation(gDeath);
                    enemy.setVelocityX(0);
                    if (enemy.isOnGround())
                        enemy.setVelocityY(0f);
                }
            }
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
        if (r1.intersects(r2)) {
            // If they do, then check if the individual pixels
            // also intersect
            return s1.collidesWith(s2); // check if the sprites actually collide
        }

        return false; // otherwise, they do not collide
    }


    /**
     * Checks if the player's knife is colliding with any enemies and performs the appropriate actions
     */
    public void checkWeaponCollision(Sprite knife, ArrayList<Sprite> enemies, long elapsed) {
        for (Sprite s : enemies) {
            if (boundingBoxCollision(knife, s) && player.isAttack() && s.isAlive()) { // check if the knife and the enemy collide and the player is attacking
                if (player.isFlipped()) {
                    s.setVelocityX(-0.1f); // set the enemy's x velocity if the player is facing left
                } else {
                    s.setVelocityX(0.1f); // set the enemy's x velocity if the player is facing right
                }
                s.setHealth(s.getHealth() - player.getDamage()); // decrease the enemy's health by the player's damage
                if (s.getHealth() <= 0) {
                    s.setAlive(false); // if the enemy's health is zero or less, set it as dead
                }
            }
        }
    }

    /**
     * Checks all enemies for tile collisions and performs the appropriate actions
     */
    public void checkEnemyCollision(Sprite player, ArrayList<Sprite> enemies, long elapsed) {
        for (Sprite enemy : enemies) {
            // Update the enemy's velocity due to gravity
            if (!(enemy instanceof Boss))
                enemy.setVelocityY(enemy.getVelocityY() + (gravity * elapsed));

            // Check for collisions between the enemy and the tile map
            if (!(enemy instanceof Boss))
                checkEnemyTileCollision(enemy, tmap);

            // If there is no collision between the player and the enemy, or if the enemy is already dead, continue to the next iteration of the loop
            if (!boundingBoxCollision(player, enemy) || !enemy.isAlive()) {
                enemy.setAttack(false);
                continue;
            }

            // If a collision has occurred and the enemy is alive, set the enemy's attack state
            if (enemy instanceof Thug) {
                enemy.setAttack(true);
            }

            if (enemy instanceof Boss) {
                player.setHealth(player.getHealth() - enemy.getDamage());
            }
        }
    }

    /**
     * Checks if the player is within the enemy's line of sight and returns true if so
     *
     * @param player the player sprite
     * @param enemy  the enemy sprite
     * @return true if the player is within the enemy's line of sight, false if not
     */
    public boolean checkLineOfSight(Sprite player, Sprite enemy) {
        // Safe distance where the enemy won't shoot
        int safeDistance = enemy.getWidth() * 2;

        // TODO check for tiles between player and enemy

        // Check if the player is on the same level (Y axis) as the enemy
        if (player.getY() > enemy.getY() - enemy.getHeight() && player.getY() < enemy.getY() + enemy.getHeight()) {
            // If the enemy is not flipped (facing right)
            if (enemy.isFlipped()) {
                if (player.getX() < enemy.getX()) {
                    // Calculate the distance between player and enemy on the X axis
                    int xDistance = (int) (enemy.getX() - player.getX());

                    // If the player is within the enemy's line of sight and outside the safe distance
                    if (xDistance <= enemy.getWidth() * 5 && xDistance > safeDistance) {
                        return true;
                    }
                }
            } else {
                // If the enemy is flipped (facing left)
                if (player.getX() > enemy.getX()) {
                    // Calculate the distance between player and enemy on the X axis
                    int xDistance = (int) (player.getX() - enemy.getX());

                    // If the player is within the enemy's line of sight and outside the safe distance
                    if (xDistance <= enemy.getWidth() * 5 && xDistance > safeDistance) {
                        return true;
                    }
                }
            }
        }
        // Player is either too close or not within the line of sight
        return false;
    }

    // Alternate version of checkLineOfSight I was trying to use to check if there were any obstacles between the player and the enemy
//    /**
//     * Checks if the player is within the enemy's line of sight and returns true if so
//     * @param player the player sprite
//     * @param enemy the enemy sprite
//     * @return true if the player is within the enemy's line of sight, false if not
//     */
//    public boolean checkLineOfSight(Sprite player, Sprite enemy) {
//        // Safe distance where the enemy won't shoot
//        int safeDistance = enemy.getWidth() * 2;
//
//        // Check if the player is on the same level (Y axis) as the enemy
//        if (player.getY() > enemy.getY() - enemy.getHeight() && player.getY() < enemy.getY() + enemy.getHeight()) {
//            // If the enemy is not flipped (facing right)
//            if (enemy.isFlipped()) {
//                if (player.getX() < enemy.getX()) {
//                    // Calculate the distance between player and enemy on the X axis
//                    int xDistance = (int) (enemy.getX() - player.getX());
//
//                    // If the player is within the enemy's line of sight and outside the safe distance
//                    if (xDistance <= enemy.getWidth() * 5 && xDistance > safeDistance) {
//                        // Check for tiles between player and enemy
//                        if (checkLineOfSight(player.getX(), player.getY(), enemy.getX(), enemy.getY())) {
//                            System.out.println("Player is within GunThug's line of sight");
//                            return true;
//                        }
//                    }
//                }
//            } else {
//                // If the enemy is flipped (facing left)
//                if (player.getX() > enemy.getX()) {
//                    // Calculate the distance between player and enemy on the X axis
//                    int xDistance = (int) (player.getX() - enemy.getX());
//
//                    // If the player is within the enemy's line of sight and outside the safe distance
//                    if (xDistance <= enemy.getWidth() * 5 && xDistance > safeDistance) {
//                        // Check for tiles between player and enemy
//                        if (checkLineOfSight(player.getX(), player.getY(), enemy.getX(), enemy.getY())) {
//                            System.out.println("Player is within GunThug's line of sight");
//                            return true;
//                        }
//                    }
//                }
//            }
//        }
//        // Player is either too close or not within the line of sight
//        return false;
//    }


//    /**
//     * Checks if there are any tiles between the given coordinates
//     * I don't know why it didn't work when I tried to use it in the above checkLineOfSight method
//     * @param x1 the x coordinate of the first object (player or enemy)
//     * @param y1 the y coordinate of the first object (player or enemy)
//     * @param x2 the x coordinate of the second object (player or enemy)
//     * @param y2 the y coordinate of the second object (player or enemy)
//     * @return true if there are no tiles between the given coordinates, false if there are
//     */
//    public boolean checkLineOfSight(float x1, float y1, float x2, float y2) {
//        // Determine the slope of the line formed by the two objects
//        float slope = (float) (y2 - y1) / (x2 - x1);
//
//        // Initialize the current position to the starting point
//        float x = x1;
//        float y = y1;
//
//        // Determine the step size for the x axis
//        float xStep = x2 > x1 ? 1 : -1;
//
//        // Iterate through the x axis, checking for tiles between the two objects
//        while (x != x2) {
//            x += xStep;
//            y += slope * xStep;
//
//            // Determine the tile coordinates at the current position
//            int tileX = (int) x / tmap.getTileWidth();
//            int tileY = (int) y / tmap.getTileHeight();
//
//            char tileChar = tmap.getTileChar(tileX, tileY);
//
//            // Check if there is a solid tile at the current position
//            if(tileChar != '.') {
//                System.out.println("LOS Blocked");
//                return false; // There is a tile between the objects
//            }
//        }
//        System.out.println("LOS Clear");
//        return true; // No tiles between the objects
//    }


    /**
     * Check and handles collisions with a tile map for the
     * given sprite 's'. Initial functionality is limited...
     * added functionality to control how various sprites respond to collisions
     * with different types of tiles
     * various key tile reactions are defined here and key tiles are defined in the tilemap itself
     *
     * @param s    The Sprite to check collisions for
     * @param tmap The tile map to check
     */

    public void checkTileCollision(Sprite s, TileMap tmap) {
        // Empty out our current set of collided tiles
        if (clear)
            collidedTiles.clear();

        // Get the width and height of each tile in the TileMap
        float tileWidth = tmap.getTileWidth();
        float tileHeight = tmap.getTileHeight();

        // Get the bounds of the Sprite
        Rectangle spriteBounds = new Rectangle((int) s.getX(), (int) s.getY(), (int) (s.getWidth() * 0.5f), s.getHeight());

        // Calculate the range of rows and columns to check around the player sprite
        int startRow = Math.max(0, (int) ((s.getY() - s.getHeight()) / tileHeight));
        int endRow = Math.min(tmap.getMapHeight(), (int) ((s.getY() + 2 * s.getHeight()) / tileHeight));
        int startCol = Math.max(0, (int) ((s.getX() - s.getWidth()) / tileWidth));
        int endCol = Math.min(tmap.getMapWidth(), (int) ((s.getX() + 2 * s.getWidth()) / tileWidth));

        // Iterate through the tiles around the player sprite
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                // Get the character representing the current tile
                char tileChar = tmap.getTileChar(col, row);
                Tile collidedTile = tmap.getTile(col, row);
                // If the tile is not empty / a special tile, check for collision
                if ((tileChar != '.' && tileChar != 'D' && tileChar != 'p' && tileChar != 'F' && tileChar != '!' && tileChar != ',' && tileChar != 'x' && tileChar != 'K' && tileChar != 'P') || (tileChar == 'P' && !doorOpened)) {
                    // Get the bounds of the current tile
                    Rectangle tileBounds = new Rectangle((int) (col * tileWidth), (int) (row * tileHeight), (int) tileWidth, (int) tileHeight);
                    // If the Sprite's bounds intersect with the current tile's bounds, handle the collision
                    if (spriteBounds.intersects(tileBounds)) {
                        if (collidedTile != null && collidedTile.getCharacter() != '.') // If it's not a dot (empty space), handle it
                        {
                            collidedTiles.add(collidedTile);
                        }
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
                            }
                        }
                    }
                }

                // Check for tile collision with 'Q' and set endLevel to true
                if (tileChar == 'Q') {
                    Rectangle tileBounds = new Rectangle((int) (col * tileWidth), (int) (row * tileHeight), (int) tileWidth, (int) tileHeight);
                    if (spriteBounds.intersects(tileBounds)) {
                        startLevelTwo();
                    }
                }

                if (tileChar == '£') {
                    Rectangle tileBounds = new Rectangle((int) (col * tileWidth), (int) (row * tileHeight), (int) tileWidth, (int) tileHeight);
                    if (spriteBounds.intersects(tileBounds)) {
                        //gameOver = true;
                        if (!bossSpawned) {
                            boss = new Boss(bossWalk);
                            sprites.add(boss);
                            enemies.add(boss);
                            boss.setVelocity(0.05f, 0);
                            boss.setPosition(findLocationByTile(tmap, 'R').get(0), findLocationByTile(tmap, 'R').get(1) - (tmap.getTileHeight() * 2));
                            bossSpawned = true;
                        }
                        player.setHasGun(true);
                    }
                }

                if (tileChar == '!') {
                    player.setPosition(findLocationByTile(tmap, '?').get(0), findLocationByTile(tmap, '?').get(1) - tmap.getTileHeight());
                    EchoFilterStream echoFilter = null;
                    try {
                        echoFilter = new EchoFilterStream(new FileInputStream("sounds/metal.wav"), 33000, 0.1f);
                    } catch (FileNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                    if(!mute) {
                        Sound filteredSound = new Sound("sounds/metal.wav", echoFilter);
                        filteredSound.start();
                    }
                }

                if (tileChar == 'x') {
                    messageZero = true;
                    collidedTiles.add(collidedTile);
                }
                if (tileChar == 'X') {
                    messageFive = true;
                    collidedTiles.add(collidedTile);
                }
                if (tileChar == 'M' || tileChar == 'N') {
                    messageFive = false;
                    collidedTiles.add(collidedTile);
                }
                if (tileChar == ')') {
                    messageZero = false;
                    collidedTiles.add(collidedTile);
                }

            }
        }
    } // end checkTileCollision method

    /**
     * This method is called when the player reaches the end of level one
     */
    private void startLevelTwo() {
        levelTwo = true;
        clearAll();
        midiPlayer.stopMidi();
        init();
    }

    /**
     * This method is similar to checkTileCollision, but it checks for collisions with the enemy sprites
     * In hindsight, I should have made a single method that checks for collisions with all sprites
     * however, I ran into some issues doing it that way and got around it by making two separate methods
     *
     * @param s    The Sprite to check for collisions with
     * @param tmap The TileMap to check for collisions with
     */
    public void checkEnemyTileCollision(Sprite s, TileMap tmap) {
        float tileWidth = tmap.getTileWidth();
        float tileHeight = tmap.getTileHeight();
        Rectangle spriteBounds = new Rectangle((int) s.getX(), (int) s.getY(), (int) (s.getWidth() * 0.5f), s.getHeight());
        for (int row = 0; row < tmap.getMapHeight(); row++) {
            for (int col = 0; col < tmap.getMapWidth(); col++) {
                char tileChar = tmap.getTileChar(col, row);
                if ((tileChar != '.' && tileChar != ',' && tileChar != 'p' && tileChar != '!' && tileChar != 'D' && tileChar != 'F' && tileChar != 'x' && tileChar != 'K' && tileChar != 'P') || (tileChar == 'P' && !doorOpened)) {
                    Rectangle tileBounds = new Rectangle((int) (col * tileWidth), (int) (row * tileHeight), (int) tileWidth, (int) tileHeight);
                    if (spriteBounds.intersects(tileBounds)) {
                        // Determine which side of the sprite collided with the tile
                        float xDiff = Math.abs((s.getX() + s.getWidth() / 2) - (col * tileWidth + tileWidth / 2));
                        float yDiff = Math.abs((s.getY() + s.getHeight() / 2) - (row * tileHeight + tileHeight / 2));
                        float w = s.getWidth() / 2 + tileWidth / 2;
                        float h = s.getHeight() / 2 + tileHeight / 2;
                        float dx = w - xDiff;
                        float dy = h - yDiff;

                        if (s instanceof Projectile) {
                            s.stop();
                            s.hide();
                        } else if (s instanceof Platform) {
                            // If collision is from the top or bottom, reverse the Y velocity
                            if (dy < dx) {
                                s.setVelocity(0, -s.getVelocityY());
                            }
                        } else {
                            // Only move the sprite back in the direction of the collision
                            if (dx < dy) {
                                if (s.getX() < col * tileWidth) {
                                    s.setX(s.getX() - dx + 26.5f);
                                } else {
                                    s.setX(s.getX() + dx);
                                }
                                if (!s.isStationary()) {
                                    s.setVelocity(-s.getVelocityX(), s.getVelocityY());
                                } else {
                                    s.setVelocity(0, s.getVelocityY());
                                }
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

    /**
     * This method handles the players left movement
     */
    private void moveLeft() {
        player.setFlipped(true);
        player.setVelocityX(-moveSpeed);
        if (player.isOnGround()) {
            player.setAnimation(playerRunning);
        } else {
            player.setAnimation(playerJumping);
        }
    }

    /**
     * This method handles the players right movement
     */
    private void moveRight() {
        player.setFlipped(false);
        player.setVelocityX(moveSpeed);
        if (player.isOnGround()) {
            player.setAnimation(playerRunning);
        } else {
            player.setAnimation(playerJumping);
        }
    }

    /**
     * This method handles the player idling
     */
    private void idle() {
        player.setVelocityX(0);
        player.setAnimation(playerIdle);
    }

    /**
     * This method handles the player being hit by an enemy
     */
    private void playerHit(Sprite enemy) {
        // If the enemy is moving left, set the player's velocity to move left and up
        if (enemy.isMoveLeft()) {
            player.setVelocity(-.1f, -0.06f);
        }
        // If the enemy is moving right, set the player's velocity to move right and up
        else if (enemy.isMoveRight()) {
            player.setVelocity(.1f, -0.06f);
        }

        // Set the player's 'onGround' flag to false to prevent the player from jumping while being attacked
        player.setOnGround(false);

        // Decrease the player's health by the enemy's damage value
        player.setHealth(player.getHealth() - enemy.getDamage());
        // if the player's health is zero or less
        if (player.getHealth() <= 0) {
            // play the death sound once and set the player as dead
            if (player.isAlive()) {
                EchoFilterStream echoFilter = null;
                try {
                    echoFilter = new EchoFilterStream(new FileInputStream("sounds/death.wav"), 33000, 0.1f);
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
                Sound filteredSound = new Sound("sounds/death.wav", echoFilter);
                if (!mute)
                    filteredSound.start();
            }
            player.setAlive(false);
        } else {
            // play a punch sound
            Sound punch = new Sound("sounds/punch.wav");
            if (!mute)
                punch.start();
            // play a random pain sound from pain1 to pain6
            int random = (int) (Math.random() * 6) + 1;
            Sound painSound = new Sound("sounds/pain" + random + ".wav");
            if (!mute)
                painSound.start();
        }
    }// end method playerHit

    /**
     * This method handles the knife object, as it is a separate animation sheet / sprite
     * from the player sprite so i need to update it separately based on the player's state
     */
    private void handleKnifeGun() {
        if (!player.isHasWeapon()) {
            knife.hide();
            return;
        }

        if (!player.isHasGun()) {
            gun.hide();
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
                if (boundingBoxCollision(knife, enemy)) {
                    if ((System.currentTimeMillis() - enemy.getLastAttack()) >= 360) {
                        // Full attack animation completed
                        enemy.setHealth(enemy.getHealth() - player.getDamage());
                        player.setLastAttack(System.currentTimeMillis());
                        // Handle knockback for enemy
                        if (player.isFlipped()) {
                            enemy.setVelocity(-0.1f, -0.1f);
                        } else {
                            enemy.setVelocity(0.2f, -0.1f);
                        }
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

        if (player.isHasGun()) {
            knife.hide();
            gun.setFlipped(player.isFlipped());
            if (gun.isFlipped()) {
                gun.setPosition(player.getX() + player.getWidth() / 10, player.getY() + player.getHeight() / 2);
            } else {
                gun.setPosition(player.getX() + player.getWidth() / 1.5f, player.getY() + player.getHeight() / 2);
            }
            gun.show();
        }

    }// end method handleKnife

    /**
     * This method handles the player jumping
     */
    private void jump() {
        player.setVelocityY(jumpVelocity);
        player.setAnimation(playerJumping);
        player.setOnGround(false);
    }// end method jump

    /**
     * This method draws the collided tiles to the screen for debugging purposes
     */
    public void drawCollidedTiles(Graphics2D g, TileMap map, int xOffset, int yOffset) {
        if (collidedTiles.size() > 0) {
            int tileWidth = map.getTileWidth();
            int tileHeight = map.getTileHeight();

            g.setColor(Color.red);
            for (Tile t : collidedTiles) {
                g.drawRect(t.getXC() + xOffset, t.getYC() + yOffset, tileWidth, tileHeight);
            }
        }
    }// end method drawCollidedTiles

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
                    if (player.isHasWeapon() && !mute) {
                        Sound stabSound = new Sound("sounds/Stab_sound_effect.wav");
                        stabSound.start(); // Play the stab sound
                    }
                    player.setLastAttack(System.currentTimeMillis());
                }
                break;
            case KeyEvent.VK_C:
                clear = !clear;
                break;
            case KeyEvent.VK_W:
                // if player is on the ground, set jump flag to true
                if (player.isOnGround()) {
                    player.setJump(true);
                    Sound jumpSound = new Sound("sounds/jump.wav");
                    if (!mute)
                        jumpSound.start();
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
                // This shows how by smoothing out the higher frequencies, you can make a sound more muffled
                // I use the same sound file as the stab sound effect, but it sounds like a generic sound effect
                // when filtered, like door interaction or something - You can press S then F to hear the difference
                LowPassFilterStream lowPassFilter = null;
                try {
                    lowPassFilter = new LowPassFilterStream(new FileInputStream("sounds/Stab_sound_effect.wav"), 30);
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
                Sound filteredSound = new Sound("sounds/Stab_sound_effect.wav", lowPassFilter);

//                this didnt do what i wanted it to - caused a staticy noise
//                PitchShiftFilterStream pitchFilter = null;
//                try {
//                    pitchFilter = new PitchShiftFilterStream(new FileInputStream("sounds/caw.wav"),  0.5f);
//                } catch (FileNotFoundException ex) {
//                    throw new RuntimeException(ex);
//                }
//                Sound filteredSound = new Sound("sounds/caw.wav", pitchFilter);
                if(!mute)
                filteredSound.start();
                break;
            case KeyEvent.VK_F:
                Sound unfilteredSound = new Sound("sounds/Stab_sound_effect.wav");
                if(!mute)
                unfilteredSound.start();
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
            case KeyEvent.VK_W:
                player.setJump(false);
                break;
            case KeyEvent.VK_M:
                mute = !mute;
                handleMusic();
                break;
            case KeyEvent.VK_BACK_SPACE:
                clearAll();
                midiPlayer.stopMidi();
                doorOpened = false;
                bossSpawned = false;
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
     * Attempt at getting bounding circle collision detection to work
     *
     * @param s1 sprite 1
     * @param s2 sprite 2
     * @return true if the sprites collide, false otherwise
     */
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
    }// end method boundingCircleCollision

    /**
     * Method to find the location of a tile on the map by its character
     *
     * @param map the tilemap to search
     * @param t   the character of the tile to check for
     * @return an arraylist containing the x and y coordinates of the tile
     */
    private ArrayList<Integer> findLocationByTile(TileMap map, char t) {
        for (Tile[] tiles : map.getTmap()) {
            for (Tile tile : tiles) {
                if (tile.getCharacter() == t) {
                    return new ArrayList<Integer>(Arrays.asList(tile.getXC(), tile.getYC()));
                }
            }
        }
        return null;
    }

    /**
     * Method to clear all the lists of sprites and objects
     * This is called when the player dies / restarts / reaches the end of the level
     * It also resets various player attributes and states
     */
    public void clearAll() {
        birds.clear();
        shots.clear();
        thugs.clear();
        gunThugs.clear();
        enemies.clear();
        sprites.clear();
        objects.clear();
        traps.clear();
        player.stop();
        player.setHealth(player.getMAX_HEALTH());
        player.setAlive(true);
        collidedTiles.clear();
    }

    /**
     * Method to draw the credits screen
     *
     * @param g the graphics object to draw to
     */
    public void drawCreditsScreen(Graphics2D g) {
        // draw a large semi transparent black background to the centre of the screen
        // calculate new rectangle dimensions
        int rectWidth = screenWidth;
        int rectHeight = screenHeight;
        int rectX = 0;
        int rectY = 0;

        // because this will be called multiple times in the draw method, a layer of the below rectangle will be drawn
        // each time - this will make the rectangle more opaque each time, creating a fade in effect
        // draw the rectangle with rounded corners and a grey border
        g.setColor(new Color(0, 0, 0, 10)); // semi-transparent black
        g.fillRoundRect(rectX, rectY, rectWidth, rectHeight, 20, 20); // rounded corners
        g.setColor(Color.GRAY); // grey border
        g.drawRoundRect(0, 0, rectWidth, rectHeight, 20, 20);

        String[] credits = {
                "Created by Student 2925642",
                "Creative Commons Assets",
                "Thanks for playing!",
                "Esc to Quit"
        };

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));

        int lineHeight = g.getFontMetrics().getHeight();
        int startX = screenWidth / 3;
        int startY = screenHeight / 3;

        g.setFont(customFont);

        for (int i = 0; i < credits.length; i++) {
            String line = credits[i];
            int y = startY + i * lineHeight * 2;
            if(i%2 == 0){
                g.setColor(Color.blue);
            }
            else{
                g.setColor(Color.magenta);
            }
            g.drawString(line, startX, y);
        }
    }

    /**
     * Mouse listener method to handle mouse clicks
     * This is used to shoot bullets
     *
     * @param e the mouse event
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (player.isHasGun()) {
            // Calculate the x and y offsets to center the view around the player
            int offsetX = -(int) player.getX() + 450;
            int offsetY = -(int) player.getY() + 400;

            float targetX = (float) e.getX() - offsetX;
            float targetY = (float) e.getY() - offsetY;
            float playerX = player.getX();
            float playerY = player.getY();

            double angle = Math.atan2(targetY - playerY, targetX - playerX);

            // create a new bullet and send it towards the mouseclick
            // Load a single bullet animation
            bullet = new Animation();
            bullet.addFrame(loadImage("images/objects/bullet.png"), 1000);
            Projectile playerShot = new Projectile(bullet);
            playerShot.setX(playerX + tmap.getTileWidth() / 2);
            playerShot.setY(playerY + tmap.getTileHeight() / 2);
            sprites.add(playerShot);
            playerShots.add(playerShot);

            float speed = 1;

            // set the velocity of the bullet based on the angle of the mouse click relative to the player
            playerShot.setVelocityX((float) (speed * Math.cos(angle)));
            playerShot.setVelocityY((float) (speed * Math.sin(angle)));
            Sound shot = new Sound("sounds/gunshot.wav");
            if (!mute)
            shot.start();
        }
    }


    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
