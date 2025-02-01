package lando.systems.game.scene.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.game.Config;
import lando.systems.game.scene.framework.Entity;
import lando.systems.game.scene.framework.families.RenderableComponent;
import lando.systems.game.utils.Callbacks;
import lando.systems.game.utils.Util;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class DebugRender extends RenderableComponent {

    // ------------------------------------------------------------------------
    // Inner types and constants
    // ------------------------------------------------------------------------

    /**
     * Base class for optional render callback parameters. Extend this class
     * and include fields for data needed for an onRender callback for your use case.
     * The base class maintains references to {@link SpriteBatch} and {@link ShapeDrawer}
     * as well as this {@link DebugRender} component for use in callbacks if needed.
     */
    public abstract static class Params implements Callbacks.TypedArg.Params {
        public DebugRender self;
        public SpriteBatch batch;
        public ShapeDrawer shapes;
    }

    /**
     * Default render callback that draws a filled circle at the entity's position.
     * TODO(brian): could be convenient to have a way to compose multiple callbacks,
     *   define a standard set for typical things; position, collider, renderable bounds, etc...
     *   then mix and match for a given entity without needing to reimplement them
     */
    public static final Callbacks.TypedArg<Params> DRAW_POSITION = (params) -> {
        var shapes = params.shapes;
        var entity = params.self.entity;
        if (entity == Entity.NONE) return;
        var position = entity.get(Position.class);
        if (position == null) return;

        // draw position
        var outer = 4f;
        var inner = outer * (3f / 4f);
        shapes.filledCircle(position.value, outer, Color.CYAN);
        shapes.filledCircle(position.value, inner, Color.YELLOW);
    };

    /**
     * Default render callback that draws a filled circle at the entity's position
     * and a rectangle for the entity's collider if it has one.
     */
    public static final Callbacks.TypedArg<Params> DRAW_POSITION_AND_COLLIDER = (params) -> {
        var shapes = params.shapes;
        var entity = params.self.entity;
        if (entity == Entity.NONE) return;
        var position = entity.get(Position.class);
        if (position == null) return;

        // draw collider
        var color = Color.MAGENTA;
        var lineWidth = 1f;
        var collider = entity.get(Collider.class);
        if (collider != null) {
            switch (collider.shape) {
                case rect -> {
                    var rect = Util.rect.obtain().set(
                        collider.rect.x + position.x(),
                        collider.rect.y + position.y(),
                        collider.rect.width, collider.rect.height);
                    shapes.rectangle(rect, color, lineWidth);
                    Util.free(rect);
                }
                case circ -> {
                    var circ = Util.circ.obtain();
                    circ.set(
                        collider.circ.x + position.x(),
                        collider.circ.y + position.y(),
                        collider.circ.radius);
                    shapes.setColor(color);
                    shapes.circle(circ.x, circ.y, circ.radius, lineWidth);
                    shapes.setColor(Color.WHITE);
                    Util.free(circ);
                }
                case grid -> Util.log("DebugRender", "collider type 'grid' not supported yet");
            }
        }

        // draw position
        var outer = 4f;
        var inner = outer * (3f / 4f);
        shapes.filledCircle(position.value, outer, Color.CYAN);
        shapes.filledCircle(position.value, inner, Color.YELLOW);
    };

    /**
     * Default {@link Params} instance so {@link DebugRender} callbacks
     * can be used without requiring custom params to be created.
     */
    private final Params DEFAULT_PARAMS = new Params() {
    };

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    public Callbacks.TypedArg<Params> onBatchRender;
    public Callbacks.TypedArg<Params> onShapeRender;
    public Params onBatchRenderParams = DEFAULT_PARAMS;
    public Params onShapeRenderParams = DEFAULT_PARAMS;

    // ------------------------------------------------------------------------
    // Factory methods
    // ------------------------------------------------------------------------

    public static DebugRender makeForBatch(Callbacks.TypedArg<Params> onRender) {
        return new DebugRender(onRender, null, null, null);
    }

    public static DebugRender makeForBatch(Callbacks.TypedArg<Params> onRender, Params params) {
        return new DebugRender(onRender, params, null, null);
    }

    public static DebugRender makeForShapes(Callbacks.TypedArg<Params> onRender) {
        return new DebugRender(null, null, onRender, null);
    }

    public static DebugRender makeForShapes(Callbacks.TypedArg<Params> onRender, Params params) {
        return new DebugRender(null, null, onRender, params);
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public DebugRender() {
    }

    private DebugRender(Callbacks.TypedArg<Params> onBatchRender, Params batchParams,
                        Callbacks.TypedArg<Params> onShapeRender, Params shapeParams) {
        this.onBatchRender = onBatchRender;
        this.onShapeRender = onShapeRender;
        this.onBatchRenderParams = (batchParams != null) ? batchParams : DEFAULT_PARAMS;
        this.onShapeRenderParams = (shapeParams != null) ? shapeParams : DEFAULT_PARAMS;
    }

    // ------------------------------------------------------------------------
    // Component implementation
    // ------------------------------------------------------------------------

    @Override
    public void update(float dt) {
        // TODO(brian): don't really like this, but callbacks defined outside a scope
        //  where the relevant entity is available still may need to access it through
        //  a component reference, and this is a simple way to ensure the self ref is set.
        //  See the DebugRender.DRAW_POSITION for an example where this wouldn't be needed
        //  if the callback was defined in the Factory method that creates the entity
        //  and its position component.
        if (onBatchRenderParams != null) {
            onBatchRenderParams.self = this;
        }
        if (onShapeRenderParams != null) {
            onShapeRenderParams.self = this;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (Config.Flag.RENDER.isDisabled()) return;

        if (onBatchRenderParams != null) {
            onBatchRenderParams.batch = batch;
        }

        if (onBatchRender != null) {
            onBatchRender.run(onBatchRenderParams);
        }
    }

    @Override
    public void render(ShapeDrawer shapes) {
        if (Config.Flag.RENDER.isDisabled()) return;

        if (onShapeRenderParams != null) {
            onShapeRenderParams.shapes = shapes;
        }

        if (onShapeRender != null) {
            onShapeRender.run(onShapeRenderParams);
        }
    }
}
