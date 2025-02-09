package lando.systems.game.scene.components;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import lando.systems.game.scene.framework.Entity;
import lando.systems.game.scene.framework.families.RenderableComponent;

import java.util.List;
import java.util.stream.StreamSupport;

public class Tilemap extends RenderableComponent {

    private static final float UNIT_SCALE = 1f;
    private static final TmxMapLoader.Parameters params = new TmxMapLoader.Parameters() {{
        generateMipMaps = true;
        textureMinFilter = Texture.TextureFilter.MipMapLinearLinear;
        textureMagFilter = Texture.TextureFilter.MipMapLinearLinear;
    }};

    private final List<TiledMapTileLayer> layers;
    private final List<TiledMapImageLayer> imageLayers;
    private final Rectangle bounds;

    // TODO(brian): could move to separate component to share between Tilemap components
    //  less important if there's only ever one Tilemap component in a Scene like Squatch
    public final TiledMapRenderer renderer;

    public final TiledMap map;
    public final int cols;
    public final int rows;
    public final int tileSize;

    public OrthographicCamera camera;

    public Tilemap(Entity entity, String tmxFilePath, OrthographicCamera camera, SpriteBatch batch) {
        super(entity);
        this.camera = camera;
        this.map = (new TmxMapLoader()).load(tmxFilePath, params);
        this.renderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE, batch);

        // TODO(brian): refactor, this is very squatch-map-specific currently
        this.layers = StreamSupport.stream(map.getLayers().spliterator(), false)
            .filter(layer -> !layer.getName().equals("solid"))
            .filter(layer -> layer instanceof TiledMapTileLayer)
            .map(TiledMapTileLayer.class::cast)
            .toList();

        this.imageLayers = StreamSupport.stream(map.getLayers().spliterator(), false)
            .filter(layers -> layers instanceof TiledMapImageLayer)
            .map(TiledMapImageLayer.class::cast)
            .toList();

        this.bounds = new Rectangle();

        var props = map.getProperties();
        this.cols = props.get("width", Integer.class);
        this.rows = props.get("height", Integer.class);
        this.tileSize = props.get("tilewidth", Integer.class);
    }

    public Collider makeGridCollider(String layerName) {
        var layer = map.getLayers().get(layerName);
        if (layer instanceof TiledMapTileLayer solidLayer) {
            var collider = Collider.makeGrid(entity, Collider.Mask.solid, tileSize, cols, rows);
            var grid = collider.shape(Collider.GridShape.class);
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    var isSolid = (null != solidLayer.getCell(x, y));
                    grid.set(x, y, isSolid);
                }
            }
            return collider;
        }
        throw new GdxRuntimeException("Unable to create grid collider, layer '%s' not found or not TiledMapTileLayer type".formatted(layerName));
    }

    public Boundary makeBoundary() {
        var bounds = calcBounds();
        return new Boundary(entity, bounds);
    }

    public Rectangle calcBounds() {
        var pos = entity.get(Position.class);
        var x = (pos != null) ? pos.x() : 0f;
        var y = (pos != null) ? pos.y() : 0f;
        return bounds.set(x, y, cols * tileSize, rows * tileSize);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (map == null) return;
        var pos = entity.get(Position.class);
        var x = (pos != null) ? pos.x() : 0f;
        var y = (pos != null) ? pos.y() : 0f;

        renderer.setView(camera);

        // TODO(brian): assumes all image layers are 'background'
        for (var layer : imageLayers) {
            layer.setOffsetX(x);
            layer.setOffsetY(-y);
            renderer.renderImageLayer(layer);
        }

        for (var layer : layers) {
            layer.setOffsetX(x);
            layer.setOffsetY(-y);
            renderer.renderTileLayer(layer);
        }
    }
}
