package lando.systems.game.scene.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
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

    public final Color tint;
    public final NinePatch patch;
    public final Rectangle bounds;

    public Patch(Assets assets, Patches.Type type) {
        super(Patch.type);

        tint = Color.WHITE.cpy();
        patch = assets.get(Patches.class, type);
        bounds = new Rectangle();

        var size = Calc.max(patch.getTotalWidth(), patch.getTotalHeight());
        bounds.setSize(size);
    }

    @Override
    public void render(SpriteBatch batch) {
        var original = batch.getColor();

        batch.setColor(tint);
        Util.draw(batch, patch, bounds);
        batch.setColor(original);
    }

    @Override
    public void render(ShapeDrawer shapes) {
        shapes.rectangle(bounds, tint);
    }
}
