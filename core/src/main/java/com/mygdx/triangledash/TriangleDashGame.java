package com.mygdx.triangledash;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.Array;

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


        spriteBatch.end();
    }

    public float getScrollSpeed() {
        return scrollSpeed;
    }

    public void setScrollSpeed(float scrollSpeed) {
        this.scrollSpeed = scrollSpeed;
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