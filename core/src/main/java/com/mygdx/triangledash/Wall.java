package com.mygdx.triangledash;

import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Wall {
    public float gapX, wallY; // The gap's X position and the wall's Y position
    private static float screenWidth; // Screen width
    private static float screenHeight; // Screen height
    public static final float GAP_SIZE = 235; // Size of the gap
    public static final float WALL_HEIGHT = 75; // Wall height
    private static Random random = new Random(); // Random generator for where gaps are

    // Constructor: Creates a wall with a random gap at a given Y position
    public Wall(float gapX, float wallY, Viewport viewport) {
        this.gapX = gapX;
        this.wallY = wallY;

        // store the viewport values
        Wall.screenWidth = viewport.getWorldWidth();
        Wall.screenHeight = viewport.getWorldHeight();
    }

    // Update the wall's position every frame
    public void update(float delta, float speed, float wallSpacing, float highestWallY) {
        wallY -= speed * delta; // Move down at delta speed

        // If the wall moves off-screen, reset it to the top with a new gap position
        if (wallY < -WALL_HEIGHT) {
            wallY = highestWallY + wallSpacing; // Move back to the top of the list of 5 walls
            gapX = random.nextFloat() * (screenWidth - GAP_SIZE); // New random gap
        }
    }
}


