package lando.systems.game.scene.components;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lando.systems.game.assets.Anims;
import lando.systems.game.math.Calc;
import lando.systems.game.scene.framework.families.RenderableComponent;
import lando.systems.game.utils.Util;

public class Animator extends RenderableComponent {

    public Animation<TextureRegion> animation;
    public TextureRegion keyframe;
    public float stateTime;
    public int facing;

    @SuppressWarnings("unchecked")
    public Animator(Anims.Type type) {
        this((Animation<TextureRegion>) Anims.container.get(type));
    }

    public Animator(Animation<TextureRegion> animation) {
        this(animation.getKeyFrame(0));
        this.animation = animation;
    }

    public Animator(TextureRegion keyframe) {
        this.animation = null;
        this.keyframe = keyframe;
        this.size.set(keyframe.getRegionWidth(), keyframe.getRegionHeight());
        this.stateTime = 0;
        this.facing = 1;
    }

    @SuppressWarnings("unchecked")
    public float play(Anims.Type type) {
        var anim = (Animation<TextureRegion>) Anims.container.get(type);
        return play(anim);
    }

    public float play(Animation<TextureRegion> anim) {
        if (anim == null) return 0;
        this.animation = anim;
        return this.animation.getAnimationDuration();
    }

    @Override
    public void update(float dt) {
        if (animation == null) return;

        stateTime += dt;
        keyframe = animation.getKeyFrame(stateTime);

        float sx = Calc.approach(Calc.abs(scale.x), defaultScale.x, dt * scaleReturnSpeed);
        float sy = Calc.approach(Calc.abs(scale.y), defaultScale.y, dt * scaleReturnSpeed);
        scale.set(facing * sx, sy);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (keyframe == null) return;

        var rect = obtainPooledRectBounds();
        Util.draw(batch, keyframe, rect, tint);
        Util.free(rect);
    }
}
