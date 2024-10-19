package lando.systems.game.scene.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lando.systems.game.scene.framework.families.RenderableComponent;
import lando.systems.game.utils.Util;

public class Image extends RenderableComponent {

    public TextureRegion region;

    public Image(Texture texture) {
        this(new TextureRegion(texture));
    }

    public Image(TextureRegion region) {
        this.region = region;
        this.size.set(region.getRegionWidth(), region.getRegionHeight());
    }

    @Override
    public void render(SpriteBatch batch) {
        if (region == null) return;

        var rect = obtainPooledRectBounds();
        Util.draw(batch, region, rect, tint);
        Util.free(rect);
    }
}
