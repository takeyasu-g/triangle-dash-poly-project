package com.mygdx.triangledash;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.BitmapFont; // Add this at the top
import com.badlogic.gdx.graphics.g2d.GlyphLayout; // Add this at the top
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import static com.mygdx.triangledash.Wall.GAP_SIZE;


/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class TriangleDashGame extends ApplicationAdapter {
    // Sprites
    SpriteBatch spriteBatch;
    Texture triangleTexture;
    Texture backgroundImage;

    // screen viewport, camera, background
    FitViewport viewport;
    OrthographicCamera camera;
    private float backgroundY = 0;
    private float scrollSpeed = 100; // adjust speed

    // Player (Triangle Ship)
    private float playerX, playerY;
    private float playerSpeed = 400; // Speed of diagonal movement
    private boolean movingRight = true; // Direction control
    private float playerSize = 75; // Triangle texture size

    // Wall
    private Texture wallTexture;
    private Array<Wall> walls; // array of Wall class
    private float wallSpeed = 500; // speed walls going down
    private float wallSpacing = 600; // Spacing between walls

    // Game State
    private enum GameState {MENU, PLAYING, GAME_OVER}

    private GameState gameState = GameState.MENU; // Start in the Menu
    private BitmapFont font; // Font for displaying text
    private int score = 0; // Player's current score
    private int highScore = 0; // Store highest score
    private Preferences prefs;  // keeps saved data

    // Button variables
    private Texture playAgainTexture;
    private TextureRegion playAgainRegion;
    private Vector2 playAgainPosition;
    private float playAgainWidth = 300;
    private float playAgainHeight = 100;

    // music
    private Music menuMusic; // Background music for the main menu
    private Music gameMusic; // Background music for gameplay
    private float gameMusicVolume = 0.0f; // Start muted for fade-in effect
    private boolean fadingIn = false; // Track if fade-in is happening
    // sound effect
    private Sound deathSound; // Sound effect for player death
    private Sound pointSound; // Sound effect for passing through a gap
    private Sound buttonClickSound; // Sound effect for clicking "Play"


    @Override
    public void create() {
        // create Textures , Sprites
        spriteBatch = new SpriteBatch(); // create batch
        triangleTexture = new Texture(Gdx.files.internal("triangleplayer.png")); // store texture
        backgroundImage = new Texture(Gdx.files.internal("space_background2.png"));
        wallTexture = new Texture(Gdx.files.internal("wall_brick2.png"));
        walls = new Array<>();


        // Camera
        float virtualWidth = 720;
        float virtualHeight = 1280;
        camera = new OrthographicCamera();
        viewport = new FitViewport(virtualWidth, virtualHeight, camera);
        camera.position.set(virtualWidth / 2, virtualHeight / 2, 0); // set middle of screen

        // Initialize player position (1/3 from the bottom)
        playerX = (viewport.getWorldWidth() / 2) - (playerSize / 2);
        playerY = viewport.getWorldHeight() / 5; // Places it 1/3 up from the bottom

        // create initial 5 walls in array
        for (int i = 0; i < 5; i++) {
            float gapX = (float) Math.random() * (viewport.getWorldWidth() - GAP_SIZE);
            float startY = viewport.getWorldHeight() + (i * wallSpacing);
            walls.add(new Wall(gapX, startY, viewport));
        }

        // fonts for text
        font = new BitmapFont(); // Default LibGDX font
        font.getData().setScale(3); // Make text bigger

        // Make save for High score
        prefs = Gdx.app.getPreferences("TriangleDashPrefs"); // Create storage
        highScore = prefs.getInteger("highScore", 0); // Load saved high score

        // Play again button
        playAgainTexture = new Texture(Gdx.files.internal("new_game_btn.png"));
        playAgainRegion = new TextureRegion(playAgainTexture);
        playAgainPosition = new Vector2(
                (viewport.getWorldWidth() - playAgainWidth) / 2,
                viewport.getWorldHeight() / 2 - 260
        );

        // Music menu bgm
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("menu_bgm.mp3"));
        menuMusic.setLooping(true); // Loop the music
        menuMusic.setVolume(0.2f); // Set a lower volume
        menuMusic.play(); // Start playing the music
        // Music gameplay bgm
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("bgm1.mp3"));
        gameMusic.setLooping(true); // Loop the music
        gameMusic.setVolume(0.0f); // Start at 0 volume (fade-in will increase this)
        // sound effects
        deathSound = Gdx.audio.newSound(Gdx.files.internal("boom.wav")); // Load the sound
        pointSound = Gdx.audio.newSound(Gdx.files.internal("coin.wav"));
        buttonClickSound = Gdx.audio.newSound(Gdx.files.internal("confirm.wav")); // Load the sound


    }

    @Override
    public void render() {
        float touchX = Gdx.input.getX() * (viewport.getWorldWidth() / Gdx.graphics.getWidth());
        float touchY = (Gdx.graphics.getHeight() - Gdx.input.getY()) * (viewport.getWorldHeight() / Gdx.graphics.getHeight());

        // Handle New Game button in the Main Menu
        if (gameState == GameState.MENU && Gdx.input.justTouched()) {
            if (touchX >= playAgainPosition.x && touchX <= playAgainPosition.x + playAgainWidth &&
                    touchY >= playAgainPosition.y && touchY <= playAgainPosition.y + playAgainHeight) {

                // click sound effect
                buttonClickSound.play(0.2f);

                gameState = GameState.PLAYING; // Start the game
                score = 0; // Reset score
                menuMusic.stop(); // stop menu_bgm when gameState = PLAYING

                // start gameplay bgm
                gameMusic.play(); // Start game music
                fadingIn = true; // Enable fade-in effect
                gameMusic.setVolume(0.0f); // Ensure it starts at 0 volume
            }
        }

        // Handle New Game button in the Game Over screen
        if (gameState == GameState.GAME_OVER && Gdx.input.justTouched()) {
            if (touchX >= playAgainPosition.x && touchX <= playAgainPosition.x + playAgainWidth &&
                    touchY >= playAgainPosition.y && touchY <= playAgainPosition.y + playAgainHeight) {

                // click sound effect
                buttonClickSound.play(0.2f);

                restartGame(); // Restart the game properly
            }
        }

        if (gameState == GameState.PLAYING) {
            update(); // Only update if the game is in PLAYING mode
        }

        draw();
    }


    // Update game logic
    public void update() {
        float delta = Gdx.graphics.getDeltaTime();

        // Update background scrolling
        backgroundY -= scrollSpeed * delta;
        if (backgroundY <= -1920) {
            backgroundY = 0;
        }

        // Fade in the game music
        if (fadingIn) {
            gameMusicVolume += Gdx.graphics.getDeltaTime() * 0.2f; // Increase volume gradually
            if (gameMusicVolume >= 0.5f) { // Target volume level
                gameMusicVolume = 0.5f;
                fadingIn = false; // Stop fading in
            }
            gameMusic.setVolume(gameMusicVolume); // Apply volume change
        }

        // Handle player movement
        if (movingRight) {
            playerX += playerSpeed * delta;
        } else {
            playerX -= playerSpeed * delta;
        }

        // Keep player within screen bounds
        if (playerX <= 0) {
            playerX = 0;
            movingRight = true;
        } else if (playerX + playerSize >= viewport.getWorldWidth()) {
            playerX = viewport.getWorldWidth() - playerSize;
            movingRight = false;
        }

        // Handle input for changing direction
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
            movingRight = !movingRight;
        }

        // Find the highest wall Y position
        float highestWallY = 0;
        for (Wall wall : walls) {
            if (wall.wallY > highestWallY) {
                highestWallY = wall.wallY;
            }
        }

        // Move each wall down and reset when necessary
        for (Wall wall : walls) {
            wall.update(delta, wallSpeed, wallSpacing, highestWallY);
        }

        // Increase score when a wall is passed successfully
        for (Wall wall : walls) {
            if (wall.wallY + Wall.WALL_HEIGHT < playerY && !wall.passed) {
                wall.passed = true; // Mark this wall as passed
                score++; // Increase score

                // play pointSound
                pointSound.play(0.6f);

                System.out.println("Score: " + score); // Debug message
            }
        }

        // Check collision after updating walls
        for (Wall wall : walls) {
            if (checkCollision(wall)) {
                System.out.println("Game Over!"); // Debug message
                scrollSpeed = 0; // Stop background scrolling
                wallSpeed = 0; // Stop walls from moving
                playerSpeed = 0; // Stop player movement

                // Update high score if needed
                if (score > highScore) {
                    highScore = score;
                    prefs.putInteger("highScore", highScore); // Save new high score
                    prefs.flush(); // Write to storage
                    System.out.println("New High Score Saved: " + highScore); // Debug message
                }


                gameState = GameState.GAME_OVER; // Switch to Game Over mode
                return; // stops updates
            }
        }

    }

    // Restart method
    public void restartGame() {
        // Reset player position
        playerX = (viewport.getWorldWidth() / 2) - (playerSize / 2);
        playerY = viewport.getWorldHeight() / 5;

        // Reset movement
        movingRight = true;
        scrollSpeed = 100;
        wallSpeed = 500;
        playerSpeed = 400;

        // Reset score
        score = 0;

        // Reset walls
        walls.clear();
        for (int i = 0; i < 5; i++) {
            float gapX = (float) Math.random() * (viewport.getWorldWidth() - Wall.GAP_SIZE);
            float startY = viewport.getWorldHeight() + (i * wallSpacing);
            walls.add(new Wall(gapX, startY, viewport));
        }

        // menuMusic stop
        menuMusic.stop();

        // restart gameMusic
        gameMusic.play();
        gameMusic.setVolume(0.0f); // Start at 0 volume
        fadingIn = true; // Enable fade-in effect

        // Switch back to playing mode
        gameState = GameState.PLAYING;
    }


    public Rectangle getPlayerBounds() {
        float paddingX = playerSize * 0.2f; // Reduce width by 20%
        float paddingY = playerSize * 0.3f; // Reduce height by 30%

        return new Rectangle(
                playerX + paddingX / 2, // Shift right slightly
                playerY + paddingY / 2, // Shift up slightly
                playerSize - paddingX,  // Reduce width
                playerSize - paddingY   // Reduce height
        );
    }


    public boolean checkCollision(Wall wall) {
        Rectangle playerBounds = getPlayerBounds(); // Use rectangle collision

        // Define left wall bounding box
        Rectangle leftWall = new Rectangle(0, wall.wallY, wall.gapX, Wall.WALL_HEIGHT);

        // Define right wall bounding box
        Rectangle rightWall = new Rectangle(wall.gapX + Wall.GAP_SIZE, wall.wallY, viewport.getWorldWidth() - (wall.gapX + Wall.GAP_SIZE), Wall.WALL_HEIGHT);

        // Check if player overlaps with either wall
        if (playerBounds.overlaps(leftWall) || playerBounds.overlaps(rightWall)) {
            // play death sound effect
            deathSound.play(0.7f);

            // Stop game music
            gameMusic.stop();
            fadingIn = false; // Cancel fade-in if still running

            // Restart menu music
            if (!menuMusic.isPlaying()) {
                menuMusic.play();
            }

            return true; // Collision detected
        }

        return false; // No collision
    }


    // draw method
    public void draw() {
        ScreenUtils.clear(Color.BLACK); // Clear screen

        viewport.apply();
        camera.update();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin(); // Begin once

        // Draw background (always visible)
        float worldWidth = viewport.getWorldWidth();
        spriteBatch.draw(backgroundImage, 0, backgroundY, worldWidth, 1920);
        spriteBatch.draw(backgroundImage, 0, backgroundY + 1920, worldWidth, 1920);

        // If in MENU, only draw the menu UI and exit
        if (gameState == GameState.MENU) {
            GlyphLayout titleText = new GlyphLayout(font, "Triangle Dash");
            GlyphLayout highScoreText = new GlyphLayout(font, "Top Score: " + highScore);

            float titleX = (viewport.getWorldWidth() - titleText.width) / 2;
            float titleY = viewport.getWorldHeight() / 1.5f;

            float highScoreX = (viewport.getWorldWidth() - highScoreText.width) / 2;
            float highScoreY = titleY - 100;

            font.draw(spriteBatch, titleText, titleX, titleY);
            font.draw(spriteBatch, highScoreText, highScoreX, highScoreY);

            // Draw Play Button
            spriteBatch.draw(playAgainRegion, playAgainPosition.x, playAgainPosition.y, playAgainWidth, playAgainHeight);

            spriteBatch.end(); // End the batch early and return
            return;
        }

        // Draw walls (only if game is running)
        for (Wall wall : walls) {
            float rightWallX = wall.gapX + Wall.GAP_SIZE; // Right wall starts after gap

            // Left Wall
            if (wall.gapX > 0) {
                spriteBatch.draw(wallTexture, 0, wall.wallY, wall.gapX, Wall.WALL_HEIGHT);
            }

            // Right Wall
            if (rightWallX < viewport.getWorldWidth()) {
                spriteBatch.draw(wallTexture, rightWallX, wall.wallY, viewport.getWorldWidth() - rightWallX, Wall.WALL_HEIGHT);
            }
        }

        // Display score in the top left (only when playing)
        if (gameState == GameState.PLAYING) {
            font.setColor(Color.WHITE); // Set text color to white
            String scoreText = "Score: " + score;

            float scoreX = 20; // Left padding
            float scoreY = viewport.getWorldHeight() - 20; // Top padding

            font.draw(spriteBatch, scoreText, scoreX, scoreY);
        }

        // Draw the player (only in PLAYING or GAME_OVER)
        if (gameState == GameState.PLAYING || gameState == GameState.GAME_OVER) {
            // Determine rotation angle based on movement direction
            float rotationAngle = movingRight ? -45 : 45; // Rotate right when moving right, left when moving left

            spriteBatch.draw(triangleTexture,
                    playerX, playerY,                  // Position
                    playerSize / 2, playerSize / 2,    // Rotation origin (center of the triangle)
                    playerSize, playerSize,            // Width and height
                    1, 1,                              // Scale
                    rotationAngle,                     // Rotation angle
                    0, 0,
                    triangleTexture.getWidth(), triangleTexture.getHeight(), // Source width/height
                    false, false                        // Flip texture
            );
        }


        // If game is over, show "Game Over" screen
        if (gameState == GameState.GAME_OVER) {
            GlyphLayout gameOverText = new GlyphLayout(font, "Game Over");
            GlyphLayout scoreText = new GlyphLayout(font, "Score: " + score);
            GlyphLayout highScoreText = new GlyphLayout(font, "Top Score: " + highScore);

            float textX = (viewport.getWorldWidth() - gameOverText.width) / 2;
            float textY = viewport.getWorldHeight() / 2;

            float scoreX = (viewport.getWorldWidth() - scoreText.width) / 2;
            float scoreY = textY - 50;

            float highScoreX = (viewport.getWorldWidth() - highScoreText.width) / 2;
            float highScoreY = scoreY - 50;

            font.draw(spriteBatch, gameOverText, textX, textY);
            font.draw(spriteBatch, scoreText, scoreX, scoreY);
            font.draw(spriteBatch, highScoreText, highScoreX, highScoreY);

            spriteBatch.draw(playAgainRegion, playAgainPosition.x, playAgainPosition.y, playAgainWidth, playAgainHeight);
        }

        spriteBatch.end(); // End once at the bottom
    }


    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        backgroundImage.dispose();
        triangleTexture.dispose();
        wallTexture.dispose();
        font.dispose();

        // Dispose music and sound effects
        menuMusic.dispose();
        gameMusic.dispose();
        deathSound.dispose();
        pointSound.dispose();
    }

}