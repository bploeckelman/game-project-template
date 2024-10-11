package lando.systems.game.scene.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import lando.systems.game.assets.Assets;
import lando.systems.game.assets.Icons;
import lando.systems.game.scene.Entity;
import lando.systems.game.scene.components.Image;
import lando.systems.game.scene.components.Position;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class TestEntity extends Entity {

    private final Position position;
    private final Vector2 velocity;
    private final Image image;

    private final TextureRegion heart;
    private final TextureRegion heartBroken;

    public TestEntity(Assets assets, int x, int y) {
        this.heart = assets.get(Icons.class, Icons.Type.HEART);
        this.heartBroken = assets.get(Icons.class, Icons.Type.HEART_BROKEN);
        this.position = new Position(this, x, y);
        this.velocity = new Vector2(0, 0);
        this.image = new Image(this, heart);
        this.image.bounds.setPosition(position.value);
    }

    public void randomizeVelocity() {
        var angle = MathUtils.random(0f, 360f);
        var speed = MathUtils.random(10f, 100f);
        velocity.set(
            MathUtils.cosDeg(angle) * speed,
            MathUtils.sinDeg(angle) * speed);
    }

    @Override
    public void update(float dt) {
        position.move(
            velocity.x * dt,
            velocity.y * dt);
        image.bounds.setPosition(position.value);

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            randomizeVelocity();
            if (image.region == heart) {
                image.region = heartBroken;
            } else {
                image.region = heart;
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        image.render(batch);
    }

    @Override
    public void render(ShapeDrawer shapes) {
        var radius = 3f;
        shapes.filledCircle(position.value, radius, Color.CYAN);
        shapes.filledCircle(position.value, radius * (2 / 3f), Color.MAGENTA);
    }
}
