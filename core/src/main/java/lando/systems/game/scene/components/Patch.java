package lando.systems.game.scene.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import lando.systems.game.assets.Assets;
import lando.systems.game.assets.Patches;
import lando.systems.game.math.Calc;
import lando.systems.game.scene.components.interfaces.RenderableComponent;
import lando.systems.game.scene.framework.Component;
import lando.systems.game.utils.Util;
import space.earlygrey.shapedrawer.ShapeDrawer;

// TODO(brian): should the render methods check for a Position component on the attached entity?
public class Patch extends Component implements RenderableComponent {

    private static final String TAG = Patch.class.getSimpleName();

    public static final Integer type = Component.NEXT_TYPE_ID++;
    public static final Class<? extends Component> clazz = Patch.class;
    static {
        TYPE_IDS.add(type);
        TYPES.put(type, clazz);
    }

    public final Vector2 size = new Vector2();
    public final Vector2 origin = new Vector2();
    public final Color tint = Color.WHITE.cpy();

    public NinePatch patch;
    public Position position;

    public Patch(Assets assets, Patches.Type patchType) {
        super(type);
        this.patch = assets.get(Patches.class, patchType);
        this.position = null;

        var maxSize = Calc.max(patch.getTotalWidth(), patch.getTotalHeight());
        this.size.set(maxSize, maxSize);
    }

    @Override
    public void update(float dt) {
        position = entity.get(Position.type);
    }

    @Override
    public void render(SpriteBatch batch) {
        var rect = getPooledRectBounds();
        Util.draw(batch, patch, rect, tint);
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
