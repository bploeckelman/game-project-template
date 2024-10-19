package lando.systems.game.scene.framework;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import space.earlygrey.shapedrawer.ShapeDrawer;

public interface FamilyRenderable extends ComponentFamily {

    int familyType = Component.NEXT_FAMILY_TYPE_ID++;

    static int familyType() {
        return FamilyRenderable.familyType;
    }

    static Class<? extends ComponentFamily> familyClass() {
        return FamilyRenderable.class;
    }

    void render(SpriteBatch batch);

    default void render(ShapeDrawer shapes) {
        // default implementation is no-op
        // since this is more rare than drawing with a sprite batch
    }
}
