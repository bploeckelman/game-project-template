package lando.systems.game.scene.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lando.systems.game.scene.framework.Entity;
import lando.systems.game.scene.framework.families.RenderableComponent;
import lando.systems.game.utils.Util;

public class Image extends RenderableComponent {

    private final Color prevColor = new Color(1, 0, 1, 1);

    private ImageValue value;

    public Image(Entity entity, Texture texture) {
        super(entity);
        set(texture);
        this.size.set(value.width(), value.height());
    }

    public Image(Entity entity, TextureRegion region) {
        super(entity);
        set(region);
        this.size.set(value.width(), value.height());
    }

    public void set(Texture texture) {
        value = new TextureImage(texture);
    }

    public void set(TextureRegion region) {
        value = new RegionImage(region);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (value == null) return;

        prevColor.set(batch.getColor());
        batch.setColor(tint);

        var rect = obtainPooledRectBounds();
        if (value instanceof RegionImage(TextureRegion region)) {
            Util.draw(batch, region, rect, tint);
        } else if (value instanceof TextureImage(Texture texture)) {
            // repeat texture as much as needed to fill the draw bounds
            float u2 = rect.width / texture.getWidth();
            float v2 = rect.height / texture.getHeight();
            batch.draw(texture, rect.x, rect.y, rect.width, rect.height, 0, 0, u2, v2);
        }
        Util.free(rect);

        batch.setColor(prevColor);
    }

    // ------------------------------------------------------------------------
    // Internal structures to allow for either Texture or TextureRegion values
    // but not both in the same Image component.
    // ------------------------------------------------------------------------

    private sealed interface ImageValue permits TextureImage, RegionImage {
        int width();
        int height();
    }

    private record TextureImage(Texture texture) implements ImageValue {
        public int width() { return texture.getWidth(); }
        public int height() { return texture.getHeight(); }
    }

    private record RegionImage(TextureRegion region) implements ImageValue {
        public int width() { return region.getRegionWidth(); }
        public int height() { return region.getRegionHeight(); }
    }
}
