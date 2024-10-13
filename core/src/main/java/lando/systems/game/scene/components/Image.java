package lando.systems.game.scene.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
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

    public final Rectangle bounds;

    public Position position;
    public TextureRegion region;
    public Color tint;

    public Image(TextureRegion region) {
        super(type);
        this.position = null;
        this.region = region;
        this.tint = Color.WHITE.cpy();
        this.bounds = new Rectangle();
        this.bounds.setSize(region.getRegionWidth(), region.getRegionHeight());
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

        // TODO: troubleshooting left off here
        var rect = Util.rect.obtain().set(bounds);
        if (position != null && position.active) {
            rect.x += position.x();
            rect.y += position.y();
        }

        var original = batch.getColor();
        batch.setColor(tint);
        Util.draw(batch, region, rect);
        batch.setColor(original);

        Util.free(rect);
    }

    @Override
    public void render(ShapeDrawer shapes) {
        shapes.rectangle(bounds, tint);
    }
}
