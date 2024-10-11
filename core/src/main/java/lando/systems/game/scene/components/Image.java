package lando.systems.game.scene.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import lando.systems.game.scene.Entity;
import lando.systems.game.utils.Util;

public class Image extends VisualComponent {

    public final Rectangle bounds;

    public TextureRegion region;

    public Image(Entity entity, TextureRegion region) {
        super(entity, Image.class);
        this.region = region;
        this.bounds = new Rectangle(0, 0, region.getRegionWidth(), region.getRegionHeight());
    }

    public Image(Entity entity, Texture texture) {
        this(entity, new TextureRegion(texture));
    }

    @Override
    public void render(SpriteBatch batch) {
        if (region == null) return;
        var original = batch.getColor();

        batch.setColor(tint);
        Util.draw(batch, region, bounds);
        batch.setColor(original);
    }
}
