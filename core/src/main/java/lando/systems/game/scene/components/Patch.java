package lando.systems.game.scene.components;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.game.assets.Patches;
import lando.systems.game.math.Calc;
import lando.systems.game.scene.framework.Entity;
import lando.systems.game.scene.framework.families.RenderableComponent;
import lando.systems.game.utils.Util;

public class Patch extends RenderableComponent {

    public NinePatch patch;

    public Patch(Entity entity, Patches.Type patchType) {
        this(entity, entity.scene.screen.assets.get(Patches.class, patchType));
    }

    public Patch(Entity entity, NinePatch patch) {
        super(entity);
        this.patch = patch;

        var maxSize = Calc.max(patch.getTotalWidth(), patch.getTotalHeight());
        this.size.set(maxSize, maxSize);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (patch == null) return;

        var rect = obtainPooledRectBounds();
        Util.draw(batch, patch, rect, tint);
        Util.free(rect);
    }
}
