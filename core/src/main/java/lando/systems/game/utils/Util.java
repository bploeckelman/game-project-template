package lando.systems.game.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import lando.systems.game.Config;
import lando.systems.game.Config.Flag;

import java.util.List;

public class Util {

    // ------------------------------------------------------------------------
    // Logging related
    // ------------------------------------------------------------------------

    public static void log(String msg) {
        if (Flag.LOG.isDisabled()) return;
        Gdx.app.log(Util.class.getSimpleName(), msg);
    }

    public static void log(String tag, String msg) {
        if (Flag.LOG.isDisabled()) return;
        Gdx.app.log(tag, msg);
    }

    // ------------------------------------------------------------------------
    // Shader related
    // ------------------------------------------------------------------------

    public static ShaderProgram loadShader(String vertSourcePath, String fragSourcePath) {
        ShaderProgram.pedantic = false;
        var shaderProgram = new ShaderProgram(
            Gdx.files.internal(vertSourcePath),
            Gdx.files.internal(fragSourcePath));
        var log = shaderProgram.getLog();

        if (!shaderProgram.isCompiled()) {
            if (Flag.LOG.isEnabled()) {
                Gdx.app.error("LoadShader", "compilation failed:\n" + log);
            }
            throw new GdxRuntimeException("LoadShader: compilation failed:\n" + log);
        } else if (Flag.LOG.isEnabled()) {
            Gdx.app.debug("LoadShader", "ShaderProgram compilation log: " + log);
        }

        return shaderProgram;
    }

    // ------------------------------------------------------------------------
    // Color related
    // ------------------------------------------------------------------------

    private static List<Color> colors = List.of(
        /* grayscale */ Color.WHITE, Color.LIGHT_GRAY, Color.GRAY, Color.DARK_GRAY, Color.BLACK,
        /* reds      */ Color.FIREBRICK, Color.RED, Color.SCARLET, Color.CORAL, Color.SALMON,
        /* greens    */ Color.GREEN, Color.CHARTREUSE, Color.LIME, Color.FOREST, Color.OLIVE,
        /* blues     */ Color.BLUE, Color.NAVY, Color.ROYAL, Color.SLATE, Color.SKY, Color.CYAN, Color.TEAL,
        /* yellows   */ Color.YELLOW, Color.GOLD, Color.GOLDENROD, Color.ORANGE, Color.BROWN, Color.TAN,
        /* purples   */ Color.PINK, Color.MAGENTA, Color.PURPLE, Color.VIOLET, Color.MAROON);

    public static Color randomColor() {
        var index = MathUtils.random(colors.size() - 1);
        return colors.get(index);
    }

    public static Color hsvToRgb(float hue, float saturation, float value, Color outColor) {
        if (outColor == null) {
            outColor = new Color();
        }

        // rotate hue into positive range
        while (hue < 0) hue += 10f;

        hue = hue % 1f;
        int h = (int) (hue * 6);
        h = h % 6;

        float f = hue * 6 - h;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);

        switch (h) {
            case 0: outColor.set(value, t, p, 1f); break;
            case 1: outColor.set(q, value, p, 1f); break;
            case 2: outColor.set(p, value, t, 1f); break;
            case 3: outColor.set(p, q, value, 1f); break;
            case 4: outColor.set(t, p, value, 1f); break;
            case 5: outColor.set(value, p, q, 1f); break;
            default: Util.log("HSV->RGB", "Failed to convert HSV->RGB(h: %f, s: %f, v: %f)".formatted(hue, saturation, value));
        }
        return outColor;
    }
}
