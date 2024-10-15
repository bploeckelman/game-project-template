package lando.systems.game.scene.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import lando.systems.game.scene.components.interfaces.RenderableComponent;
import lando.systems.game.scene.framework.Component;
import lando.systems.game.utils.Util;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Image extends Component implements RenderableComponent {

    private static final String TAG = Image.class.getSimpleName();

    public static final Integer type = Component.NEXT_TYPE_ID++;
    public static final Class<? extends Component> clazz = Image.class;
    static {
        TYPE_IDS.add(type);
        TYPES.put(type, clazz);
    }

    public final Vector2 size = new Vector2();
    public final Vector2 origin = new Vector2();
    public final Color tint = Color.WHITE.cpy();

    public TextureRegion region;
    public Position position;

    public Image(TextureRegion region) {
        super(type);
        this.region = region;
        this.position = null;
        this.size.set(region.getRegionWidth(), region.getRegionHeight());
    }

    public Image(Texture texture) {
        this(new TextureRegion(texture));
    }

    @Override
    public void update(float dt) {
        position = entity.get(Position.type);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!active) return;
        if (region == null) return;

        var rect = getPooledRectBounds();
        Util.draw(batch, region, rect, tint);
        Util.free(rect);
    }

    @Override
    public void render(ShapeDrawer shapes) {
        var rect = getPooledRectBounds();
        shapes.rectangle(rect, tint);
        Util.free(rect);
    }

    // NOTE: don't forget to free the returned object back to the pool!
    private Rectangle getPooledRectBounds() {
        float x = 0;
        float y = 0;
        if (position != null && position.active) {
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
