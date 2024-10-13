package lando.systems.game;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import lando.systems.game.assets.Assets;
import lando.systems.game.assets.ScreenTransitions;
import lando.systems.game.scene.Factory;
import lando.systems.game.scene.framework.Entities;
import lando.systems.game.screens.BaseScreen;
import lando.systems.game.screens.GameScreen;
import lando.systems.game.screens.TitleScreen;
import lando.systems.game.screens.Transition;
import lando.systems.game.utils.Time;
import lando.systems.game.utils.accessors.CameraAccessor;
import lando.systems.game.utils.accessors.CircleAccessor;
import lando.systems.game.utils.accessors.ColorAccessor;
import lando.systems.game.utils.accessors.PerspectiveCameraAccessor;
import lando.systems.game.utils.accessors.RectangleAccessor;
import lando.systems.game.utils.accessors.Vector2Accessor;
import lando.systems.game.utils.accessors.Vector3Accessor;

public class Main extends ApplicationAdapter {

    public static Main game;

    public Assets assets;
    public Entities entities;
    public TweenManager tween;
    public FrameBuffer frameBuffer;
    public TextureRegion frameBufferRegion;
    public OrthographicCamera windowCamera;

    public BaseScreen currentScreen;

    public Main() {
        Main.game = this;
    }

    @Override
    public void create() {
        Time.init();

        assets = new Assets();
        Transition.init(assets);

        entities = new Entities();
        Factory.init(assets, entities);

        tween = new TweenManager();
        Tween.setWaypointsLimit(4);
        Tween.setCombinedAttributesLimit(4);
        Tween.registerAccessor(OrthographicCamera.class, new CameraAccessor());
        Tween.registerAccessor(Circle.class, new CircleAccessor());
        Tween.registerAccessor(Color.class, new ColorAccessor());
        Tween.registerAccessor(PerspectiveCamera.class, new PerspectiveCameraAccessor());
        Tween.registerAccessor(Rectangle.class, new RectangleAccessor());
        Tween.registerAccessor(Vector2.class, new Vector2Accessor());
        Tween.registerAccessor(Vector3.class, new Vector3Accessor());

        var format = Pixmap.Format.RGBA8888;
        int width = Config.framebuffer_width;
        int height = Config.framebuffer_height;
        var hasDepth = true;

        frameBuffer = new FrameBuffer(format, width, height, hasDepth);
        var frameBufferTexture = frameBuffer.getColorBufferTexture();
        frameBufferTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        frameBufferRegion = new TextureRegion(frameBufferTexture);
        frameBufferRegion.flip(false, true);

        windowCamera = new OrthographicCamera();
        windowCamera.setToOrtho(false, Config.window_width, Config.window_height);
        windowCamera.update();

        var startingScreen = Config.Flag.START_ON_GAMESCREEN.isEnabled() ? new GameScreen() : new TitleScreen();
        setScreen(startingScreen);
    }

    public void update(float delta) {
        // update things that must update every tick
        Time.update();
        tween.update(Time.delta);
        currentScreen.alwaysUpdate(Time.delta);
        Transition.update(Time.delta);

        // handle a pause
        if (Time.pause_timer > 0) {
            Time.pause_timer -= Time.delta;
            if (Time.pause_timer <= -0.0001f) {
                Time.delta = -Time.pause_timer;
            } else {
                // skip updates if we're paused
                return;
            }
        }
        Time.millis += Time.delta;
        Time.previous_elapsed = Time.elapsed_millis();

        currentScreen.update(delta);
    }

    @Override
    public void render() {
        update(Time.delta);

        ScreenUtils.clear(Color.DARK_GRAY);
        if (Transition.inProgress()) {
            Transition.render(assets.batch);
        } else {
            currentScreen.renderOffscreenBuffers(assets.batch);
            currentScreen.render(assets.batch);
        }
    }

    public void setScreen(BaseScreen newScreen) {
        setScreen(newScreen, null, false);
    }

    public void setScreen(BaseScreen newScreen, ScreenTransitions.Type transitionType) {
        setScreen(newScreen, transitionType, false);
    }

    public void setScreen(BaseScreen newScreen, ScreenTransitions.Type transitionType, boolean instant) {
        // nothing to transition from, just set the current screen
        if (currentScreen == null) {
            currentScreen = newScreen;
            return;
        }

        // only one transition allowed at a time
        if (Transition.inProgress()) {
            return;
        }

        Transition.to(newScreen, transitionType, instant);
    }
}
