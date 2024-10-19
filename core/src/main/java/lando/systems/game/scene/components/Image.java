package lando.systems.game.scene.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import lando.systems.game.math.Calc;
import lando.systems.game.scene.framework.families.RenderableComponent;
import lando.systems.game.utils.Util;

public class Image extends RenderableComponent {

    private static final String TAG = Image.class.getSimpleName();


    public final Vector2 size = new Vector2();
    public final Vector2 origin = new Vector2();
    public final Vector2 defaultScale = new Vector2(1, 1);
    public final Vector2 scale = defaultScale.cpy();
    public final Color tint = Color.WHITE.cpy();

    public TextureRegion region;

    public Image(Texture texture) {
        this(new TextureRegion(texture));
    }

    public Image(TextureRegion region) {
        this.region = region;
        this.size.set(region.getRegionWidth(), region.getRegionHeight());
    }

    @Override
    public void update(float dt) {
        scale.x = Calc.approach(Calc.abs(scale.x), defaultScale.x, dt * 4);
        scale.y = Calc.approach(Calc.abs(scale.y), defaultScale.y, dt * 4);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (region == null) return;

        var rect = getPooledRectBounds();
        Util.draw(batch, region, rect, tint, scale.x, scale.y);
        Util.free(rect);
    }

    // NOTE: don't forget to free the returned object back to the pool!
    private Rectangle getPooledRectBounds() {
        float x = 0;
        float y = 0;
        var position = entity.getIfActive(Position.class);
        if (position != null) {
            x = position.x();
            y = position.y();
        }

        return Util.rect.obtain().set(
            x - origin.x,
            y - origin.y,
            size.x, size.y
        );
    }
}
