package lando.systems.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import lando.systems.game.assets.Icons;
import lando.systems.game.assets.Patches;
import lando.systems.game.scene.Scene;
import lando.systems.game.utils.Util;

public class GameScreen extends BaseScreen {

    private final Circle circle = new Circle();
    private final Vector2 vel = new Vector2();
    private final Rectangle bounds = new Rectangle();

    private final Color backgroundColor = new Color(0x131711ff);
    private final Color surfaceColor = new Color(0x282c27ff);
    private final Color ballColor = new Color(0x004000ff);
    private final TextureRegion okTexture;
    private final TextureRegion hitTexture;
    private final NinePatch ninePatch;

    private final Scene<GameScreen> scene;

    private TextureRegion texture;
    private float timer;

    public GameScreen() {
        var margin = 50f;
        var radius = 50f;
        var centerX = windowCamera.viewportWidth / 2;
        var centerY = windowCamera.viewportHeight / 2;
        var speed = MathUtils.random(300f, 500f);
        var angle = MathUtils.random(0f, 360f);
        circle.set(centerX, centerY, radius);
        vel.set(MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed);
        bounds.set(margin, margin, windowCamera.viewportWidth - 2 * margin, windowCamera.viewportHeight - 2 * margin);

        var icons = assets.get(Icons.class);
        okTexture = icons.get(Icons.Type.CIRCLE_CHECK);
        hitTexture = icons.get(Icons.Type.CIRCLE_X);
        var patches = assets.get(Patches.class);
        ninePatch = patches.get(Patches.Type.ROUNDED);

        texture = okTexture;
        timer = 0f;

        this.scene = new Scene<>(this);
    }

    @Override
    public void update(float dt) {
        var shouldExit = Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);
        var shouldQuit = shouldExit && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
        if (shouldQuit) {
            Gdx.app.exit();
        } else if (shouldExit && !exiting) {
            game.setScreen(new TitleScreen());
        }

        timer -= dt;
        if (timer <= 0f) {
            timer = 0f;
            texture = okTexture;
        }

        circle.x += vel.x * dt;
        circle.y += vel.y * dt;

        var left   = circle.x - circle.radius;
        var bottom = circle.y - circle.radius;
        var right  = circle.x + circle.radius;
        var top    = circle.y + circle.radius;

        var hit = false;
        if (left < bounds.x) {
            hit = true;
            vel.x *= -1f;
            circle.x = bounds.x + circle.radius;
        } else if (right > bounds.x + bounds.width) {
            hit = true;
            vel.x *= -1f;
            circle.x = (bounds.x + bounds.width) - circle.radius;
        }

        if (bottom < bounds.y) {
            hit = true;
            vel.y *= -1f;
            circle.y = bounds.y + circle.radius;
        } else if (top > bounds.y + bounds.height) {
            hit = true;
            vel.y *= -1f;
            circle.y = (bounds.y + bounds.height) - circle.radius;
        }

        if (hit) {
            timer = 0.25f;
            texture = hitTexture;
        }

        scene.update(dt);
    }

    @Override
    public void render(SpriteBatch batch) {
        ScreenUtils.clear(backgroundColor);

        var shapes = assets.shapes;
        batch.setProjectionMatrix(windowCamera.combined);
        batch.begin();
        {
            var wasHit = (timer > 0);

            batch.setColor(wasHit ? surfaceColor : ballColor);
            Util.draw(batch, texture, circle);
            batch.setColor(Color.WHITE);

            scene.render(batch);
            scene.render(shapes);
        }
        batch.end();
    }
}
