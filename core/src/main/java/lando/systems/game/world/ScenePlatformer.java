package lando.systems.game.world;

import com.badlogic.gdx.maps.MapObject;
import lando.systems.game.scene.Scene;
import lando.systems.game.scene.components.Boundary;
import lando.systems.game.scene.components.Tilemap;
import lando.systems.game.scene.components.ViewController;
import lando.systems.game.screens.GameScreen;
import lando.systems.game.utils.Util;

public class ScenePlatformer extends Scene<GameScreen> {

    private static final String TAG = ScenePlatformer.class.getSimpleName();

    public ScenePlatformer(GameScreen screen) {
        super(screen);

        // configure the camera to emulate a low res display
        var width = 240;
        var height = 160;
        var camera = screen.worldCamera;
        camera.setToOrtho(false, width, height);
        camera.update();

        var map = EntityFactory.map(this, "maps/start.tmx", "middle");
        var boundary = map.get(Boundary.class);
        var tilemap = map.get(Tilemap.class);

        makeMapObjects(tilemap);

        var cam = EntityFactory.cam(this, boundary);
        cam.get(ViewController.class).target(boundary.center());
    }

    private void makeMapObjects(Tilemap tilemap) {
        var objectLayerName = "objects";

        var layer = tilemap.map.getLayers().get(objectLayerName);
        var objects = layer.getObjects();

        for (var object : objects) {
            logParseMapObject(object);

            var name = object.getName();
            var props = object.getProperties();
            var x = props.get("x", Float.class);
            var y = props.get("y", Float.class);

            if (name.equals("spawn")) {
                EntityFactory.hero(this, x, y, 1f);
            }
        }
    }

    private void logParseMapObject(MapObject mapObject) {
        Util.log(TAG, mapObject, obj -> "parsing map object: %s[name='%s', pos=(%.1f, %.1f)]..."
            .formatted(
                obj.getClass().getSimpleName(),
                mapObject.getName(),
                mapObject.getProperties().get("x", Float.class),
                mapObject.getProperties().get("y", Float.class)
            )
        );
    }
}
