package com.sek.sekiro2d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.OrthographicCamera;

class Player {
    private float x, y;
    private float speed;
    private int health;
    private boolean isGrounded;
    private float verticalVelocity;
    private Rectangle bounds;
    private boolean isDead;

    public Player(float x, float y, float speed, int health) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.health = health;
        this.isGrounded = true;
        this.verticalVelocity = 0f;
        this.bounds = new Rectangle(x, y, 50, 50);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
        updateBounds();
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
        updateBounds();
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public int getHealth() {
        return health;
    }

    public float getSpeed(){
        return speed;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public boolean isGrounded() {
        return isGrounded;
    }

    public void setGrounded(boolean isGrounded) {
        this.isGrounded = isGrounded;
    }

    public float getVerticalVelocity() {
        return verticalVelocity;
    }

    public void setVerticalVelocity(float verticalVelocity) {
        this.verticalVelocity = verticalVelocity;
    }

    public void move(float deltaX, float deltaY) {
        this.x += deltaX;
        this.y += deltaY;
        updateBounds();
    }

    private void updateBounds() {
        bounds.setPosition(x, y);
    }

    public void handleDeath(){
        isDead = true;
    }
}

class Enemy {
    private float x, y;
    private float speed;
    private Rectangle bounds;
    private boolean movingRight;  // Track enemy's movement direction

    public Enemy(float x, float y, float speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.bounds = new Rectangle(x, y, 50, 50);
        this.movingRight = true; // Initially, moving right
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
        updateBounds();
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
        updateBounds();
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void moveTowards(Player player, float deltaTime) {
        float playerX = player.getX();
        float playerY = player.getY();

        float dx = playerX - x;
        float dy = playerY - y;

        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 50) { // Stop moving if the distance is less than 50 pixels
            if (dx > 0) {
                movingRight = true;
            } else {
                movingRight = false;
            }
            x += (dx / distance) * speed * deltaTime;

            updateBounds();
        }
    }

    private void updateBounds() {
        bounds.setPosition(x, y);
    }

    public boolean isMovingRight() {
        return movingRight;
    }
}

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class SekiroGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture idleTexture;
    private Texture walkLeftTexture;
    private Texture walkRightTexture;
    private Texture currentTexture;
    private Texture background;
    private Player player;
    private Enemy enemy;
    private FitViewport viewport;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private float floor;
    private float gravity = -0.5f;
    private float jumpVelocity = 10f;

    // Time accumulator for health reduction
    private float timeSinceLastDamage = 0f;

    @Override
    public void create() {
        batch = new SpriteBatch();

        idleTexture = new Texture("idle.png");
        walkLeftTexture = new Texture("left.png");
        walkRightTexture = new Texture("right.png");
        background = new Texture("background-2.jpg");

        currentTexture = idleTexture;

        player = new Player(400, 50, 200, 100);
        enemy = new Enemy(100, 50, 100); // Start enemy at x=100, y=50 with speed=100

        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 500, camera);
        viewport.apply();
        floor = 50;
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);

        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        applyPhysics();

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            player.move(-player.getSpeed() * Gdx.graphics.getDeltaTime(), 0);
            currentTexture = walkLeftTexture;
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            player.move(player.getSpeed() * Gdx.graphics.getDeltaTime(), 0);
            currentTexture = walkRightTexture;
        } else {
            currentTexture = idleTexture;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && player.isGrounded()) {
            player.setVerticalVelocity(jumpVelocity);
            player.setGrounded(false);
        }

        enemy.moveTowards(player, Gdx.graphics.getDeltaTime());

        handleCollisions();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(background, 0, 0, worldWidth, worldHeight);
        batch.draw(currentTexture, player.getX(), player.getY(), 50, 50);

        // Draw the enemy facing the correct direction
        if (enemy.isMovingRight()) {
            batch.draw(walkRightTexture, enemy.getX(), enemy.getY(), 50, 50); // Facing right
        } else {
            batch.draw(walkLeftTexture, enemy.getX(), enemy.getY(), 50, 50); // Facing left
        }

        batch.end();

        drawHealthBar();

        camera.update();
    }

    private void applyPhysics() {
        if (!player.isGrounded()) {
            player.setVerticalVelocity(player.getVerticalVelocity() + gravity);
            player.move(0, player.getVerticalVelocity());
            if (player.getY() <= floor) {
                player.setY(floor);
                player.setGrounded(true);
                player.setVerticalVelocity(0);
            }
        }
    }

    private void handleCollisions() {
        timeSinceLastDamage += Gdx.graphics.getDeltaTime();

        // Handle collision between player and enemy
        if (player.getBounds().overlaps(enemy.getBounds())) {
            // Reduce player's health every second
            if (timeSinceLastDamage >= 1f) {
                player.setHealth(player.getHealth() - 20);
                timeSinceLastDamage = 0f;
            }

            // Stop enemy's movement for a brief moment during collision
            enemy.setX(enemy.getX()); // Maintain enemy's position
            enemy.setY(enemy.getY()); // Halts enemy's motion

            if (player.getHealth() <= 0) {
                player.handleDeath();
            }
        } else {
            timeSinceLastDamage = 0f; // Reset damage timer if no collision
        }
    }

    private void drawHealthBar() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(10, viewport.getWorldHeight() - 20, player.getHealth() * 2, 10);
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        idleTexture.dispose();
        walkLeftTexture.dispose();
        walkRightTexture.dispose();
        shapeRenderer.dispose();
    }
}
