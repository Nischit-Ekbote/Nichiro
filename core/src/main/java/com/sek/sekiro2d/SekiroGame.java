package com.sek.sekiro2d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.OrthographicCamera;
import org.w3c.dom.Text;

class Player {
    private float x, y;
    private float speed;
    private int health;
    private boolean isGrounded;
    private float verticalVelocity;
    private Rectangle bounds;
    private boolean isDead;
    private Rectangle weapon;
    private float weaponRotation;
    private float attackTimer = 0;
    private boolean isAttacking = false;

    public Player(float x, float y, float speed, int health) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.health = health;
        this.isGrounded = true;
        this.verticalVelocity = 0f;
        this.bounds = new Rectangle(x, y, 100, 100);
        this.weapon = new Rectangle(x , y + y/2, 150, 5);
        this.weaponRotation = 0;
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

    public float getSpeed() {
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
        weapon.setPosition(x + 25, y + y/1/3);
    }

    public void handleDeath() {
        isDead = true;
    }

    public boolean isDead() {
        return isDead;
    }

    public Rectangle getWeapon() {
        return weapon;
    }

    public float getWeaponRotation() {
        return weaponRotation;
    }

    // Setter for rotation (allows external changes)
    public void setWeaponRotation(float weaponRotation) {
        this.weaponRotation = weaponRotation;
    }

    // Increase rotation for testing (call this in update logic)
    public void playerAttackOne() {
        if(!isAttacking) {
            isAttacking = true;
            weaponRotation += 90; // Or whatever angle you want
        }
    }

    public void updateAttack(float delta) {
        if(isAttacking) {
            attackTimer += delta;
            if(attackTimer > 0.2f) {  // 0.2 seconds duration
                isAttacking = false;
                attackTimer = 0;
                weaponRotation -= 90; // Reset rotation
            }
        }
    }


}

class Enemy {
    private float x, y;
    private float speed;
    private Rectangle bounds;
    private Texture[] walkLeft;
    private Texture[] walkRight;
    private Texture currentTexture;
    private float animationTime = 0f;

    public Enemy(float x, float y, float speed, Texture[] walkLeft, Texture[] walkRight) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.bounds = new Rectangle(x, y, 100, 100);
        this.walkLeft = walkLeft;
        this.walkRight = walkRight;
        this.currentTexture = walkRight[0];
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

    public Texture getCurrentTexture() {
        return currentTexture;
    }

    public void moveTowards(Player player, float deltaTime) {
        float playerX = player.getX();
        float dx = playerX - x;
        float distance = Math.abs(dx);

        if (distance > 50) {
            animationTime += deltaTime;
            x += (dx / distance) * speed * deltaTime;

            if (dx < 0) {
                int frame = (int) (animationTime * 10) % walkLeft.length;
                currentTexture = walkLeft[frame];
            } else {
                int frame = (int) (animationTime * 10) % walkRight.length;
                currentTexture = walkRight[frame];
            }

            updateBounds();
        }
    }

    private void updateBounds() {
        bounds.setPosition(x, y);
    }
}

public class SekiroGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture idleTexture;
    private Texture[] idle;
    private Texture[] walkLeft;
    private Texture[] walkRight;
    private Texture currentTexture;
    private Texture background;
    private Player player;
    private Enemy enemy;
    private FitViewport viewport;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private float floor;
    private float gravity = -0.5f;
    private float jumpVelocity = 17f;
    private float animationTime = 0f;

    @Override
    public void create() {
        batch = new SpriteBatch();

        idleTexture = new Texture("idle.png");
        idle = new Texture[1];
        idle[0] = new Texture("idle_1.png");

        walkLeft = new Texture[2];
        walkLeft[0] = new Texture("left1.png");
        walkLeft[1] = new Texture("left2.png");

        walkRight = new Texture[2];
        walkRight[0] = new Texture("right1.png");
        walkRight[1] = new Texture("right2.png");

        background = new Texture("background-2.jpg");

        currentTexture = idleTexture;

        player = new Player(400, 50, 200, 100);
        enemy = new Enemy(100, 50, 100, walkLeft, walkRight);

        camera = new OrthographicCamera();
        viewport = new FitViewport(1000, 520, camera);
        viewport.apply();
        floor = 50;
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);

        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        applyPhysics();

        if (player.isDead()) {
            displayGameOverScreen();
            return;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            animationTime += Gdx.graphics.getDeltaTime();
            int frame = (int) (animationTime * 10) % walkLeft.length;
            currentTexture = walkLeft[frame];
            player.move(-player.getSpeed() * Gdx.graphics.getDeltaTime(), 0);
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            animationTime += Gdx.graphics.getDeltaTime();
            int frame = (int) (animationTime * 10) % walkRight.length;
            currentTexture = walkRight[frame];
            player.move(player.getSpeed() * Gdx.graphics.getDeltaTime(), 0);
        } if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            player.playerAttackOne();
        }

        else {
            animationTime += Gdx.graphics.getDeltaTime();
            int frame = (int) (animationTime * 5) % idle.length;
            currentTexture = idle[frame];
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && player.isGrounded()) {
            player.setVerticalVelocity(jumpVelocity);
            player.setGrounded(false);
        }

        enemy.moveTowards(player, Gdx.graphics.getDeltaTime());

        handleCollisions();

        camera.position.set(player.getX() + 25, viewport.getWorldHeight() / 2, 0);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(background, 0, 0, viewport.getWorldWidth()*2, viewport.getWorldHeight());
        batch.draw(currentTexture, player.getX(), player.getY(), 150, 150);
        batch.draw(enemy.getCurrentTexture(), enemy.getX(), enemy.getY(), 100, 100);
        batch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(player.getBounds().x + player.getBounds().width / 2 - 5, player.getBounds().y,
        player.getBounds().width / 2 + 2, player.getBounds().height - 18);

        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(enemy.getBounds().x, enemy.getBounds().y,
            enemy.getBounds().width, enemy.getBounds().height);

        shapeRenderer.end();

        player.updateAttack(Gdx.graphics.getDeltaTime());

        drawHealthBar();
        drawWeapon();
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
        if (player.getBounds().overlaps(enemy.getBounds())) {
            Rectangle intersection = new Rectangle();
            Intersector.intersectRectangles(player.getBounds(), enemy.getBounds(), intersection);

            float playerCenterX = player.getX() + player.getBounds().width / 2;
            float playerCenterY = player.getY() + player.getBounds().height / 2;
            float enemyCenterX = enemy.getX() + enemy.getBounds().width / 2;
            float enemyCenterY = enemy.getY() + enemy.getBounds().height / 2;

            float directionX = playerCenterX - enemyCenterX;

            float minDistanceX = (player.getBounds().width + enemy.getBounds().width) / 2;
            float separationX = 0;

            if (Math.abs(directionX) < minDistanceX) {
                separationX = (minDistanceX - Math.abs(directionX)) * Math.signum(directionX);
            }

            float separationSpeed = 10f;
            float deltaTime = Gdx.graphics.getDeltaTime();

            player.move(
                separationX * separationSpeed * deltaTime,
                0
            );
        }
    }

    private void drawHealthBar() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(10, viewport.getWorldHeight() - 20, player.getHealth() * 2, 10);
        shapeRenderer.end();
    }

    public void drawWeapon() {
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Get weapon rectangle
        Rectangle weapon = player.getWeapon();

        // Rotation setup
        float rotation = player.getWeaponRotation();
        float originX = weapon.x + weapon.width / 3;
        float originY = weapon.y + weapon.height / 3;

        // Apply rotation
        shapeRenderer.identity();
        shapeRenderer.translate(originX, originY, 0);
        shapeRenderer.rotate(0, 0, 1, rotation);
        shapeRenderer.translate(-originX, -originY, 0);

        // Draw rectangle
        shapeRenderer.rect(weapon.x, weapon.y, weapon.width, weapon.height);

        // Reset transformation
        shapeRenderer.identity();

        shapeRenderer.end();
    }



    private void displayGameOverScreen() {
        batch.begin();
        batch.draw(new Texture("game_over.png"), 200, 200, 400, 100);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        idleTexture.dispose();
        walkLeft[0].dispose();
        walkLeft[1].dispose();
        walkRight[0].dispose();
        walkRight[1].dispose();
        background.dispose();
        shapeRenderer.dispose();
    }
}
