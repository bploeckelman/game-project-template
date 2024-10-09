package lando.systems.game.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.I18NBundle;
import lando.systems.game.Config;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Assets implements Disposable {

    public enum Load { SYNC, ASYNC }
    public boolean loaded = false;

    public final Preferences prefs;
    public final AssetManager mgr;
    public final SpriteBatch batch;
    public final ShapeDrawer shapes;
    public final GlyphLayout layout;
    public final Array<Disposable> disposables;
    public final Array<AssetContainer> containers;

    public TextureAtlas atlas;
    public I18NBundle strings;

    public final Texture pixel;

    public TextureRegion pixelRegion;

    public Assets() {
        this(Load.SYNC);
    }

    public Assets(Load load) {
        prefs = Gdx.app.getPreferences(Config.preferences_name);

        mgr = new AssetManager();
        batch = new SpriteBatch();
        shapes = new ShapeDrawer(batch);
        layout = new GlyphLayout();
        disposables = new Array<>();
        containers = new Array<>();

        disposables.add(mgr);
        disposables.add(batch);

        // create a single pixel texture and associated region
        var pixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        {
            pixmap.setColor(Color.WHITE);
            pixmap.drawPixel(0, 0);
            pixmap.drawPixel(1, 0);
            pixmap.drawPixel(0, 1);
            pixmap.drawPixel(1, 1);
        }
        pixel = new Texture(pixmap);
        pixelRegion = new TextureRegion(pixel);

        disposables.add(pixmap);
        disposables.add(pixel);

        // populate asset manager
        {
            // one-off items
            mgr.load("sprites/sprites.atlas", TextureAtlas.class);
            mgr.load("i18n/strings", I18NBundle.class);

            // textures
            mgr.load("images/libgdx.png", Texture.class);

            // fonts
            // TODO: asset loader for FreeTypeFont that takes ttf filename and params?

            // music

            // sounds

            // shaders
            // TODO: asset loader for ShaderProgram, or just use the util method?
        }

        if (load == Load.SYNC) {
            mgr.finishLoading();
            updateLoading();
        }
    }

    public float updateLoading() {
        if (loaded) return 1;
        if (!mgr.update()) {
            return mgr.getProgress();
        }

        atlas = mgr.get("sprites/sprites.atlas");
        strings = mgr.get("i18n/strings");

        loaded = true;
        return 1;
    }


    @Override
    public void dispose() {
        disposables.forEach(Disposable::dispose);
    }
}
