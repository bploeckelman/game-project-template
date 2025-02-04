package lando.systems.game.scene.framework.families;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import lando.systems.game.math.Calc;
import lando.systems.game.scene.Scene;
import lando.systems.game.scene.components.Position;
import lando.systems.game.scene.framework.ComponentFamily;
import lando.systems.game.scene.framework.Entity;
import lando.systems.game.utils.Util;
import space.earlygrey.shapedrawer.ShapeDrawer;

public abstract class RenderableComponent extends ComponentFamily {

    public final Color tint = Color.WHITE.cpy();
    public final Vector2 size = new Vector2();
    public final Vector2 origin = new Vector2();
    public final Vector2 defaultScale = new Vector2(1, 1);
    public final Vector2 scale = defaultScale.cpy();
    public final float scaleReturnSpeed = 4f;

    public RenderableComponent(Entity entity) {
        super(entity);
    }

    public RenderableComponent(Scene<?> scene) {
        super(scene);
    }

    @Override
    public void update(float dt) {
        scale.x = Calc.approach(Calc.abs(scale.x), defaultScale.x, dt * scaleReturnSpeed);
        scale.y = Calc.approach(Calc.abs(scale.y), defaultScale.y, dt * scaleReturnSpeed);
    }

    public abstract void render(SpriteBatch batch);

    public void render(ShapeDrawer shapes) {
        // default no-op implementation since this is less likely to be used than the SpriteBatch version
    }

    /**
     * Obtain a {@link Rectangle} from a {@link com.badlogic.gdx.utils.Pool}
     * set to the bounds of this {@link RenderableComponent}.
     * <strong>Don't forget to call {@link Util#free} on the returned rect!</strong>
     *
     * @return the bounds of this renderable
     */
    protected Rectangle obtainPooledRectBounds() {
        var position = entity.getIfActive(Position.class);
        var x = (position != null) ? position.x() : 0f;
        var y = (position != null) ? position.y() : 0f;
        return Util.rect.obtain().set(
            x - origin.x * scale.x,
            y - origin.y * scale.y,
            size.x * scale.x,
            size.y * scale.y
        );
    }
}
