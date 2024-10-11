package lando.systems.game.scene;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import lando.systems.game.Main;
import space.earlygrey.shapedrawer.ShapeDrawer;

public abstract class Entity {
    public static int NEXT_ID = 1;

    public final int id;
    public final String name;

    public boolean active;

    // TODO(brian): invert the control here, no direct construction, acquire through Entities.create();
    public Entity() {
        this.id = NEXT_ID++;
        this.name = ClassReflection.getSimpleName(this.getClass());
        this.active = true;

        Main.game.entities.add(this);
    }

    // default no-op implementations of common methods
    public abstract void update(float dt);
    public abstract void render(SpriteBatch batch);
    public abstract void render(ShapeDrawer shapes);
}
