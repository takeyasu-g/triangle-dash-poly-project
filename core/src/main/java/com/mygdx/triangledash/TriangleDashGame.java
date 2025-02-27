package com.mygdx.triangledash;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
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
    private float wallSpeed = 300; // speed walls going down
    private float wallSpacing = 600; // Spacing between walls

    // Game State
    private enum GameState {PLAYING, GAME_OVER}

    private GameState gameState = GameState.PLAYING;
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


    }

    @Override
    public void render() {
        update();
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
        wallSpeed = 300;
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

        // Switch back to playing mode
        gameState = GameState.PLAYING;
    }


    // Inside TriangleDashGame class:
    public Rectangle getPlayerBounds() {
        return new Rectangle(playerX, playerY, playerSize, playerSize);
    }

    public boolean checkCollision(Wall wall) {
        Rectangle playerBounds = getPlayerBounds();

        // Define left wall bounding box
        Rectangle leftWall = new Rectangle(0, wall.wallY, wall.gapX, Wall.WALL_HEIGHT);

        // Define right wall bounding box
        Rectangle rightWall = new Rectangle(wall.gapX + Wall.GAP_SIZE, wall.wallY, viewport.getWorldWidth() - (wall.gapX + Wall.GAP_SIZE), Wall.WALL_HEIGHT);

        // Check if player intersects with either left or right wall
        return playerBounds.overlaps(leftWall) || playerBounds.overlaps(rightWall);
    }


    // draw method
    public void draw() {
        ScreenUtils.clear(Color.BLACK); // Clear screen

        viewport.apply(); // Adapt to screen changes
        camera.update(); // Update camera
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin();

        float worldWidth = viewport.getWorldWidth();

        // Draw background
        spriteBatch.draw(backgroundImage, 0, backgroundY, worldWidth, 1920);
        spriteBatch.draw(backgroundImage, 0, backgroundY + 1920, worldWidth, 1920);

        // Draw walls
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

        // Determine rotation angle based on movement direction
        float rotationAngle = movingRight ? -45 : 45; // Rotate right when moving right, left when moving left

        // Draw the rotated triangle
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

        if (gameState == GameState.GAME_OVER) {
            GlyphLayout gameOverText = new GlyphLayout(font, "Game Over");
            GlyphLayout scoreText = new GlyphLayout(font, "Score: " + score);
            GlyphLayout highScoreText = new GlyphLayout(font, "Top Score: " + highScore);
            spriteBatch.draw(playAgainRegion, playAgainPosition.x, playAgainPosition.y, playAgainWidth, playAgainHeight);


            float textX = (viewport.getWorldWidth() - gameOverText.width) / 2;
            float textY = viewport.getWorldHeight() / 2;

            float scoreX = (viewport.getWorldWidth() - scoreText.width) / 2;
            float scoreY = textY - 50;

            float highScoreX = (viewport.getWorldWidth() - highScoreText.width) / 2;
            float highScoreY = scoreY - 50;

            font.draw(spriteBatch, gameOverText, textX, textY);
            font.draw(spriteBatch, scoreText, scoreX, scoreY);
            font.draw(spriteBatch, highScoreText, highScoreX, highScoreY);
        }

        // to restart Game on button click
        if (gameState == GameState.GAME_OVER && Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX() * (viewport.getWorldWidth() / Gdx.graphics.getWidth());
            float touchY = (Gdx.graphics.getHeight() - Gdx.input.getY()) * (viewport.getWorldHeight() / Gdx.graphics.getHeight());

            if (touchX >= playAgainPosition.x && touchX <= playAgainPosition.x + playAgainWidth &&
                    touchY >= playAgainPosition.y && touchY <= playAgainPosition.y + playAgainHeight) {

                restartGame(); // Call restart function
            }
        }


        spriteBatch.end();
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
    }
}