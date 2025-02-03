package com.sek.sekiro2d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.OrthographicCamera;

abstract class GameObject implements Disposable {
    protected float x, y;
    protected Rectangle bounds;
    protected float speed;

    public GameObject(float x, float y, float width, float height, float speed) {
        this.x = x;
        this.y = y;
        this.bounds = new Rectangle(x, y, width, height);
        this.speed = speed;
    }

    public void updateBounds() {
        bounds.setPosition(x, y);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public void setX(float x) {
        this.x = x;
        updateBounds();
    }
    public void setY(float y) {
        this.y = y;
        updateBounds();
    }

    @Override
    public void dispose() {}
}

class Player extends GameObject {
    private static final float ATTACK_DURATION = 0.4f;
    private static final float WEAPON_WIDTH = 130;
    private static final float WEAPON_HEIGHT = 5;
    private static final float MAX_ROTATION = 90f;

    private int health;
    private boolean isGrounded;
    private float verticalVelocity;
    private boolean isDead;
    private Rectangle weapon;
    private float weaponRotation;
    private float attackTimer;
    private boolean isAttacking;
    private boolean isRightFacing;
    private final Texture[] idleRight;
    private final Texture[] idleLeft;
    private final Texture[] attack;
    private final Texture[] attackLeft;
    private final Texture[] walkRight;
    private final Texture[] walkLeft;
    private final Texture[] deathRight;
    private float animationTime;
    private Texture currentTexture;

    public Player(float x, float y, float speed, int health) {
        super(x, y, 100, 100, speed);
        this.health = health;
        this.isGrounded = true;
        this.weapon = new Rectangle(x, y + y/2, WEAPON_WIDTH, WEAPON_HEIGHT);
        this.isRightFacing = true;

        idleRight = new Texture[3];
        idleLeft = new Texture[3];
        attack = new Texture[5];
        attackLeft = new Texture[5];
        walkRight = new Texture[2];
        walkLeft = new Texture[2];
        deathRight = new Texture[6];

        loadTextures();
        currentTexture = idleRight[0];
    }

    private void loadTextures() {
        try {
            // Load attack animations
            for (int i = 0; i < 5; i++) {
                String path = "attack/attack" + (i+1) + ".png";
                String pathLeft = "attack/attackLeft" + (i + 1) + ".png";
                attack[i] = new Texture(path);
                attackLeft[i] = new Texture(pathLeft);
                System.out.println("Successfully loaded attack texture: " + path);
            }

            // Load idle animations
            for (int i = 0; i < 3; i++) {
                idleRight[i] = new Texture("idle/idle_" + (i+1) + "_right.png");
                idleLeft[i] = new Texture("idle/idle_" + (i+1) + "_left.png");
            }

            // Load walk animations
            walkLeft[0] = new Texture("walk/walk_left_1.png");
            walkLeft[1] = new Texture("walk/walk_left_2.png");
            walkRight[0] = new Texture("walk/walk_right_1.png");
            walkRight[1] = new Texture("walk/walk_right_2.png");

            for (int i = 0; i< 6 ;i ++ ){
                deathRight[i] = new Texture("death/death" + (i+ 1) + ".png");
            }

        } catch (Exception e) {
            System.err.println("Error loading textures: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void update(float delta) {
        updateAnimation(delta);
        updateAttack(delta);
        handlePlayerDeath(delta);
        updateBounds();
    }

    private void updateAnimation(float delta) {
        animationTime += delta;

        if (isAttacking) {
            float progress = Math.min(attackTimer / ATTACK_DURATION, 1.0f);
            int frame = (int)((progress * 5) % 5);
            if (frame < attack.length) {
                currentTexture = (getRightFacing() ? attack : attackLeft)[frame];
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            int frame = (int)(animationTime * 10) % walkLeft.length;
            currentTexture = walkLeft[frame];
            isRightFacing = false;
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            int frame = (int)(animationTime * 10) % walkRight.length;
            currentTexture = walkRight[frame];
            isRightFacing = true;
        } else {
            Texture[] currentAnim = isRightFacing ? idleRight : idleLeft;
            int frame = (int)(animationTime * 5) % currentAnim.length;
            currentTexture = currentAnim[frame];
        }
    }

    private void handlePlayerDeath(float delta){
        animationTime += delta;
        if(isDead){
            int frame = (int)(animationTime * 10) % deathRight.length;
            currentTexture = deathRight[frame];
        }
    }

    public void move(float deltaX, float deltaY) {
        x += deltaX;
        y += deltaY;
        updateBounds();
    }

    @Override
    public void updateBounds() {
        super.updateBounds();
        if (isRightFacing) {
            weapon.setPosition(x + bounds.width * 0.7f, y + bounds.height * 0.5f);
        } else {
            weapon.setPosition(x - weapon.width * 0.5f, y + bounds.height * 0.5f);
        }
    }

    public void playerAttackOne() {
        if (!isAttacking) {
            isAttacking = true;
            attackTimer = 0;
            animationTime = 0;
        }
    }

    public void updateAttack(float delta) {
        if (isAttacking) {
            attackTimer += delta;
            float progress = Math.min(attackTimer / ATTACK_DURATION, 1.0f);

            if (getRightFacing()) {
                weaponRotation = 90 - (progress * 90);
            } else {
                weaponRotation = 90 + (progress * 90);
            }

            if (attackTimer >= ATTACK_DURATION) {
                isAttacking = false;
                attackTimer = 0;
                weaponRotation = 0;
            }
        }
    }
    // Getters and setters
    public boolean isGrounded() { return isGrounded; }
    public void setGrounded(boolean grounded) { isGrounded = grounded; }
    public float getVerticalVelocity() { return verticalVelocity; }
    public void setVerticalVelocity(float velocity) { verticalVelocity = velocity; }
    public boolean isDead() { return isDead; }
    public void setDead(boolean isDead){ this.isDead = isDead; }
    public void handleDeath() { isDead = true; }
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }
    public Rectangle getWeapon() { return weapon; }
    public float getWeaponRotation() { return weaponRotation; }
    public boolean getIsAttacking() { return isAttacking; }
    public Texture getCurrentTexture() { return currentTexture; }
    public boolean getRightFacing() { return isRightFacing; }

    @Override
    public void dispose() {
        for (Texture texture : idleRight) texture.dispose();
        for (Texture texture : idleLeft) texture.dispose();
        for (Texture texture : attack) texture.dispose();
        for (Texture texture : walkRight) texture.dispose();
        for (Texture texture : walkLeft) texture.dispose();
    }
}

class Enemy extends GameObject {
    private static final float ATTACK_DURATION = 0.4f;
    private static final float ATTACK_RANGE = 120f;  // Range within which enemy can attack
    private static final float ATTACK_COOLDOWN = 1.5f;

    private final Texture[] walkLeft;
    private final Texture[] walkRight;
    private final Texture[] attack;
    private final Texture[] attackLeft;
    private Texture currentTexture;
    private float animationTime;
    private boolean isRightFacing;
    private boolean isAttacking;
    private float attackTimer;
    private float cooldownTimer;
    private Rectangle attackHitbox;
    private int health;

    public Enemy(float x, float y, float speed) {
        super(x, y, 100, 150, speed);
        walkLeft = new Texture[2];
        walkRight = new Texture[2];
        attack = new Texture[5];
        attackLeft = new Texture[5];
        attackHitbox = new Rectangle(x, y, 150, 40);
        health = 1000;
        loadTextures();
        currentTexture = walkRight[0];
        isRightFacing = true;
    }

    private void loadTextures() {
        try {
            // Load walk animations
            walkLeft[0] = new Texture("enemy/move/walk_left_1.png");
            walkLeft[1] = new Texture("enemy/move/walk_left_2.png");
            walkRight[0] = new Texture("enemy/move/walk_right_1.png");
            walkRight[1] = new Texture("enemy/move/walk_right_2.png");

            // Load melee attack animations
            for (int i = 0; i < 5; i++) {
                attack[i] = new Texture("enemy/attack/e_attack_right_" + (i+1) + ".png");
                attackLeft[i] = new Texture("enemy/attack/e_attack" + (i+1) + ".png");
            }
        } catch (Exception e) {
            System.err.println("Error loading enemy textures: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void update(float delta, Player player) {
        updateAttackCooldown(delta);

        if (!isAttacking) {
            float distance = getDistanceToPlayer(player);

            // Check if within attack range and cooldown is ready
            if (cooldownTimer <= 0 && distance <= ATTACK_RANGE) {
                if (Math.random() < 0.3) { // 30% chance to attack when in range
                    startAttack();
                }
            }

            if (!isAttacking) {
                moveTowards(player, delta);
            }
        }

        updateAnimation(delta);
        updateAttack(delta);
        updateHitbox();
    }

    private float getDistanceToPlayer(Player player) {
        float dx = player.getX() - x;
        float dy = player.getY() - y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private void moveTowards(Player player, float delta) {
        float dx = player.getX() - x;
        float distance = Math.abs(dx);

        if (distance > 50) {
            animationTime += delta;
            x += Math.signum(dx) * speed * delta;
            isRightFacing = dx > 0;

            Texture[] currentAnim = isRightFacing ? walkRight : walkLeft;
            int frame = (int)(animationTime * 10) % currentAnim.length;
            currentTexture = currentAnim[frame];

            updateBounds();
        }
    }

    private void startAttack() {
        isAttacking = true;
        attackTimer = 0;
        animationTime = 0;
    }

    private void updateAttack(float delta) {
        if (isAttacking) {
            attackTimer += delta;
            float progress = Math.min(attackTimer / ATTACK_DURATION, 1.0f);

            // Update attack animation
            Texture[] currentAnim = isRightFacing ? attack : attackLeft;
            int frame = (int)((progress * 5) % 5);
            if (frame < currentAnim.length) {
                currentTexture = currentAnim[frame];
            }

            if (attackTimer >= ATTACK_DURATION) {
                isAttacking = false;
                cooldownTimer = ATTACK_COOLDOWN;
            }
        }
    }

    private void updateAttackCooldown(float delta) {
        if (cooldownTimer > 0) {
            cooldownTimer -= delta;
        }
    }

    private void updateAnimation(float delta) {
        if (!isAttacking) {
            animationTime += delta;
            Texture[] currentAnim = isRightFacing ? walkRight : walkLeft;
            int frame = (int)(animationTime * 10) % currentAnim.length;
            currentTexture = currentAnim[frame];
        }
    }

    private void updateHitbox() {
        if (isAttacking) {
            if (isRightFacing) {
                attackHitbox.setPosition(x + bounds.width, y);
            } else {
                attackHitbox.setPosition(x - attackHitbox.width, y);
            }
        }
    }

    // Getters and setters
    public Rectangle getAttackHitbox() { return attackHitbox; }
    public boolean isAttacking() { return isAttacking; }
    public Texture getCurrentTexture() { return currentTexture; }
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }

    @Override
    public void dispose() {
        for (Texture texture : walkLeft) texture.dispose();
        for (Texture texture : walkRight) texture.dispose();
        for (Texture texture : attack) texture.dispose();
        for (Texture texture : attackLeft) texture.dispose();
    }
}

public class SekiroGame extends ApplicationAdapter {
    private static final float WORLD_WIDTH = 1000;
    private static final float WORLD_HEIGHT = 520;
    private static final float GRAVITY = -0.5f;
    private static final float FLOOR_HEIGHT = 50f;
    private static final float JUMP_VELOCITY = 17f;
    private static final float PLAYER_WIDTH = 150;
    private static final float PLAYER_HEIGHT = 150;
    private static final float ATTACK_SPRITE_SCALE = 2.5f;
    private boolean hasPlayerHitInCurrentAttack = false;
    private boolean hasEnemyHitInCurrentAttack = false;

    private SpriteBatch batch;
    private BitmapFont font;
    private Player player;
    private Enemy enemy;
    private FitViewport viewport;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private Texture background;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        player = new Player(400, FLOOR_HEIGHT, 200, 100);
        enemy = new Enemy(100, FLOOR_HEIGHT, 100);

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();

        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);

        shapeRenderer = new ShapeRenderer();
        background = new Texture("background-2.jpg");
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        update(delta);
        draw();
    }

    private void update(float delta) {
        if (player.isDead()) return;

        handleInput(delta);
        applyPhysics(delta);
        handleCollisions();

        player.update(delta);
        enemy.update(delta, player);

        updateCamera();
    }

    private void handleInput(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            player.move(-player.speed * delta, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            player.move(player.speed * delta, 0);
        }
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            player.playerAttackOne();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && player.isGrounded()) {
            player.setVerticalVelocity(JUMP_VELOCITY);
            player.setGrounded(false);
        }
    }

    private void applyPhysics(float delta) {
        if (!player.isGrounded()) {
            float velocity = player.getVerticalVelocity() + GRAVITY;
            player.setVerticalVelocity(velocity);
            player.move(0, velocity);

            if (player.getY() <= FLOOR_HEIGHT) {
                player.setY(FLOOR_HEIGHT);
                player.setGrounded(true);
                player.setVerticalVelocity(0);
            }
        }
    }

    private void handleCollisions() {
        if (player.getIsAttacking()) {
            if (!hasPlayerHitInCurrentAttack && player.getWeapon().overlaps(enemy.getBounds())) {
                System.out.println("Hit enemy!");
                if (player.getHealth() > 0) {
                    // Calculate new health ensuring it doesn't go below 0
                    int newHealth = Math.max(0, enemy.getHealth() - 30);
                    System.out.println(newHealth);
                    enemy.setHealth(newHealth);
                    hasPlayerHitInCurrentAttack = true;
                }
                if (player.getHealth() == 0) {
                    player.setDead(true);
                    System.out.println("Game over: Player wins");
                }
            }
        } else {
            hasPlayerHitInCurrentAttack = false;
        }


        if (enemy.isAttacking()) {
            if (!hasEnemyHitInCurrentAttack && enemy.getAttackHitbox().overlaps(player.getBounds())) {
//                System.out.println("Hit Player!");
                if (player.getHealth() > 0) {
                    // Calculate new health ensuring it doesn't go below 0
                    int newHealth = Math.max(0, player.getHealth() - 30);
                    player.setHealth(newHealth);
                    hasEnemyHitInCurrentAttack = true;
                }
                if (player.getHealth() == 0) {
//                    System.out.println("Game over: Enemy wins");
                }
            }
        } else {
            hasEnemyHitInCurrentAttack = false;
        }

    }

    private void updateCamera() {
        camera.position.set(player.getX() + 25, viewport.getWorldHeight() / 2, 0);
        camera.update();
    }

    private void draw() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(background, 0, 0, viewport.getWorldWidth() * 2, viewport.getWorldHeight());

        if (player.getIsAttacking()) {
            float attackWidth = 80 * ATTACK_SPRITE_SCALE;
            float attackHeight = 43 * ATTACK_SPRITE_SCALE;

            float xOffset = (player.getRightFacing() ? (PLAYER_WIDTH - attackWidth * 3 / 4) / 2 : (PLAYER_WIDTH - attackWidth ));
            float yOffset = (PLAYER_HEIGHT - attackHeight * 4 / 3  - 1) / 2;

            batch.draw(player.getCurrentTexture(),
                player.getX() + xOffset,
                player.getY() + yOffset,
                attackWidth,
                attackHeight);
        } else {
            batch.draw(player.getCurrentTexture(),
                player.getX(),
                player.getY(),
                PLAYER_WIDTH,
                PLAYER_HEIGHT);
        }

        batch.draw(enemy.getCurrentTexture(),
            enemy.getX(),
            enemy.getY(),
            200,
            200);
        batch.end();

//        drawDebugShapes();
//        drawWeapon();
//        drawEnemyWeapon();
        drawHealthBar();
    }

    private void drawDebugShapes() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(Color.WHITE);
        Rectangle playerBounds = player.getBounds();
        shapeRenderer.rect(
            playerBounds.x + playerBounds.width / 2 - 5,
            playerBounds.y,
            playerBounds.width / 2 + 2,
            playerBounds.height - 18
        );

        shapeRenderer.setColor(Color.RED);
        Rectangle enemyBounds = enemy.getBounds();
        shapeRenderer.rect(enemyBounds.x + 50, enemyBounds.y, enemyBounds.width, enemyBounds.height);

        shapeRenderer.end();
    }

    private void drawHealthBar() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Player health bar (green at top)
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(10, viewport.getWorldHeight() - 20, player.getHealth() * 2, 10);

        // Enemy health bar (red at bottom)
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(10, 10, enemy.getHealth() / 2, 10);

        shapeRenderer.end();

        // Draw health text
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.draw(batch, "Player Health: " + player.getHealth(),
            15, viewport.getWorldHeight() - 5);

        font.draw(batch, "Enemy Health: " + enemy.getHealth() ,
            15, 25);

        batch.end();
    }
    private void drawWeapon() {
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        Rectangle weapon = player.getWeapon();
        float rotation = player.getWeaponRotation();
        float originX = player.getRightFacing() ? weapon.x : weapon.x + weapon.width ;
        float originY = player.getRightFacing() ? weapon.y + weapon.height / 3 : weapon.y - 30;

        shapeRenderer.identity();
        shapeRenderer.translate(originX, originY, 0);
        shapeRenderer.rotate(0, 0, 1, rotation);
        shapeRenderer.translate(-originX, -originY, 0);
            shapeRenderer.rect(
                player.getRightFacing() ? player.x + weapon.width / 4 : player.x,
                weapon.y -35,
                weapon.width,
            weapon.height
        );
        shapeRenderer.identity();

        shapeRenderer.end();
    }

    private void drawEnemyWeapon() {
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        Rectangle weapon = enemy.getAttackHitbox();
//        float rotation = enemy.getWeaponRotation();
//        float originX = enemy.getRightFacing() ? weapon.x : weapon.x + weapon.width ;
//        float originY = enemy.getRightFacing() ? weapon.y + weapon.height / 3 : weapon.y - 30;
//
//        shapeRenderer.identity();
//        shapeRenderer.translate(originX, originY, 0);
//        shapeRenderer.rotate(0, 0, 1, rotation);
//        shapeRenderer.translate(-originX, -originY, 0);
        shapeRenderer.rect(
            enemy.x + 10,
            weapon.y + 20 ,
            weapon.width,
            weapon.height
        );
        shapeRenderer.identity();

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        player.dispose();
        enemy.dispose();
        background.dispose();
        shapeRenderer.dispose();
    }
}
