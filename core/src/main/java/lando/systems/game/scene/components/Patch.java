package lando.systems.game.scene.components;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import lando.systems.game.assets.Assets;
import lando.systems.game.assets.Patches;
import lando.systems.game.scene.Entity;
import lando.systems.game.utils.Calc;
import lando.systems.game.utils.Util;

public class Patch extends VisualComponent {

    public final NinePatch patch;
    public final Rectangle bounds;

    public Patch(Entity entity, Assets assets, Patches.Type type) {
        super(entity, Patch.class);
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
}
