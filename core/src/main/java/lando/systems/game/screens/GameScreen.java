package lando.systems.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;
import lando.systems.game.Config;
import lando.systems.game.assets.Icons;
import lando.systems.game.scene.Scene;
import lando.systems.game.utils.Util;

public class GameScreen extends BaseScreen {

    private final Color backgroundColor = new Color(0x131711ff);

    private final Scene<GameScreen> scene;

    public GameScreen() {
        this.scene = new Scene<>(this);
    }

    @Override
    public void update(float dt) {
        handleExit();

        var shouldSkipFrame = handleDebugFlags();
        if (shouldSkipFrame) {
            return;
        }

        scene.update(dt);
    }

    @Override
    public void render(SpriteBatch batch) {
        ScreenUtils.clear(backgroundColor);

        var shapes = assets.shapes;
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        {
            scene.render(batch);
            scene.render(shapes);
        }
        batch.end();

        batch.setProjectionMatrix(windowCamera.combined);
        batch.begin();
        {
            if (Config.Flag.GLOBAL.isEnabled()) {
                renderConfigFlagIcons();
            }
        }
        batch.end();
    }

    private void handleExit() {
        var shouldExit = Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);
        var shouldQuit = shouldExit && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
        if (shouldQuit) {
            Gdx.app.exit();
        } else if (shouldExit && !transitioning) {
            game.setScreen(new TitleScreen());
        }
    }

    private boolean handleDebugFlags() {
        var toggleGlobal = Gdx.input.isKeyJustPressed(Input.Keys.NUM_1);
        if (toggleGlobal) {
            Config.Flag.GLOBAL.toggle();
        }

        var toggleRender = Gdx.input.isKeyJustPressed(Input.Keys.NUM_2);
        if (toggleRender) {
            Config.Flag.RENDER.toggle();
        }

        var toggleUI = Gdx.input.isKeyJustPressed(Input.Keys.NUM_3);
        if (toggleUI) {
            Config.Flag.UI.toggle();
        }

        var toggleLog = Gdx.input.isKeyJustPressed(Input.Keys.NUM_4);
        if (toggleLog) {
            Config.Flag.UI.toggle();
        }

        var toggleFrameStep = Gdx.input.isKeyJustPressed(Input.Keys.NUM_0);
        if (toggleFrameStep) {
            Config.Flag.FRAME_STEP.toggle();
        }

        if (Config.Flag.FRAME_STEP.isEnabled()) {
            Config.stepped_frame = Gdx.input.isKeyJustPressed(Input.Keys.NUM_9);
            return !Config.stepped_frame;
        }
        return false;
    }

    private void renderConfigFlagIcons() {
        float size = 32f;
        float margin = 20f;
        float x = 0;
        float y = windowCamera.viewportHeight - margin - size;

        Color iconTint;
        Icons.Type iconType;
        TextureRegion icon;

        var rect = Util.rect.obtain();
        if (Config.Flag.FRAME_STEP.isEnabled()) {
            x += margin + size;
            rect.set(x, y, size, size);

            iconTint = Config.stepped_frame ? Color.LIME : Color.ORANGE;
            iconType = Config.stepped_frame ? Icons.Type.PERSON_PLAY : Icons.Type.PERSON_X;
            icon = assets.get(Icons.class, iconType);
            Util.draw(batch, icon, rect, iconTint);
        } else {
            x += margin + size;
            rect.set(x, y, size, size);

            iconTint = Color.LIME;
            iconType = Icons.Type.PERSON_PLAY;
            icon = assets.get(Icons.class, iconType);
            Util.draw(batch, icon, rect, iconTint);
        }
        if (Config.Flag.RENDER.isEnabled()) {
            x += margin + size;
            rect.set(x, y, size, size);

            iconTint = Color.SCARLET;
            iconType = Icons.Type.CARD_STACK;
            icon = assets.get(Icons.class, iconType);
            Util.draw(batch, icon, rect, iconTint);
        }
        if (Config.Flag.UI.isEnabled()) {
            x += margin + size;
            rect.set(x, y, size, size);

            iconTint = Color.CYAN;
            iconType = Icons.Type.PUZZLE;
            icon = assets.get(Icons.class, iconType);
            Util.draw(batch, icon, rect, iconTint);
        }
        if (Config.Flag.LOG.isEnabled()) {
            x += margin + size;
            rect.set(x, y, size, size);

            iconTint = Color.GOLDENROD;
            iconType = Icons.Type.NOTEPAD;
            icon = assets.get(Icons.class, iconType);
            Util.draw(batch, icon, rect, iconTint);
        }
        Util.free(rect);
    }
}
