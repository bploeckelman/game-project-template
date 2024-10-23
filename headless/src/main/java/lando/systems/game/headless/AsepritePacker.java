package lando.systems.game.headless;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.PixmapPackerIO;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import lando.systems.game.utils.aseprite.Aseprite;

import java.nio.file.Path;

public class AsepritePacker extends ApplicationAdapter {

    private static final String TAG = AsepritePacker.class.getSimpleName();

    private static class Defaults {
        private static final String ASEPRITE_SOURCE_DIR = "../sprites/aseprite";
        private static final String SPRITE_DEST_DIR = "../assets/sprites";
        private static final String ATLAS_DEST_DIR = "../assets/sprites";
        private static final String ATLAS_FILENAME = "aseprites.atlas";
    }

    private record PathArgs(Path aseSource, Path spriteDest, Path atlasDest, String atlasName) {
        @Override
        public String toString() {
            return """
                PathArgs[
                    aseSource: %s
                    spriteDest: %s
                    atlasDest: %s
                    atlasName: %s
                ]""".formatted(aseSource, spriteDest, atlasDest, atlasName);
        }
    }

    private final String[] args;

    public AsepritePacker(String[] args) {
        this.args = args;
    }

    @Override
    public void create() {
        process();
    }

    private PathArgs parseArgs(String[] args) {
        var aseSrcDir = Defaults.ASEPRITE_SOURCE_DIR;
        var spriteOutputDir = Defaults.SPRITE_DEST_DIR;
        var atlasOutputDir = Defaults.ATLAS_DEST_DIR;
        var atlasFileName = Defaults.ATLAS_FILENAME;

        // parse out pack params from args
        //@formatter:off
        switch (args.length) {
            case 4: atlasFileName = args[3];
            case 3: atlasOutputDir = args[2];
            case 2: spriteOutputDir = args[1];
            case 1: aseSrcDir = args[0];
            case 0: break;
            default: {
                Gdx.app.log(TAG, """
                Usage: [aseprite-source-dir] [sprite-dest-dir] [atlas-dest-dir] [atlas-file-name]");
                Defaults:
                  - aseprite-source-dir: ../sprites/aseprite
                  - sprite-dest-dir: ../assets/sprites
                  - atlas-dest-dir: ../assets/sprites
                  - atlas-file-name: aseprites.atlas
                """);
                Gdx.app.exit();
            }
        }
        //@formatter:on

        var pathArgs = new PathArgs(
            Path.of(aseSrcDir).toAbsolutePath(),
            Path.of(spriteOutputDir).toAbsolutePath(),
            Path.of(atlasOutputDir).toAbsolutePath(),
            atlasFileName
        );
        Gdx.app.log(TAG, "Starting AsepritePacker");
        Gdx.app.log(TAG, pathArgs.toString());
        return pathArgs;
    }

    private void process() {
        var paths = parseArgs(args);

        var inputDir = paths.aseSource.toAbsolutePath().toString();
        var spriteOutputDir = paths.spriteDest.toAbsolutePath().toString();
        var atlasOutputDir = paths.atlasDest.toAbsolutePath().toString();
        var atlasFileName = paths.atlasName;

        // configure a pixmap packer
        // TODO: maybe optionally pass some of these as args?
        int pageWidth = 1024;
        int pageHeight = 1024;
        var pageFormat = Pixmap.Format.RGBA8888;
        int padding = 0;
        var duplicateBorder = false;
        var stripWhitespaceX = false;
        var stripWhitespaceY = false;
        var packStrategy = new PixmapPacker.GuillotineStrategy();
        var packer = new PixmapPacker(
            pageWidth, pageHeight, pageFormat, padding,
            duplicateBorder, stripWhitespaceX, stripWhitespaceY,
            packStrategy);

        // load aseprite files, pack animation frame pixmaps into atlas, write out sprite info
        var json = new Json();
        var aseFiles = Gdx.files.absolute(inputDir).list(".ase");
        for (var aseFile : aseFiles) {
            var spriteInfo = Aseprite.loadAndPack(packer, aseFile.path());
            var spriteOutFile = Gdx.files.absolute("%s/%s.json".formatted(spriteOutputDir, spriteInfo.name));
            json.toJson(spriteInfo, Aseprite.SpriteInfo.class, spriteOutFile);
        }

        // write out texture atlas files to system
        var outFileHandle = Gdx.files.absolute("%s/%s".formatted(atlasOutputDir, atlasFileName));
        var saveParams = new PixmapPackerIO.SaveParameters();
        saveParams.useIndexes = true;
        try {
            (new PixmapPackerIO()).save(outFileHandle, packer, saveParams);
        } catch(Exception e) {
            throw new GdxRuntimeException("Failed to pack atlas from aseprite files", e);
        }

        Gdx.app.log(TAG, "Processing complete");
        Gdx.app.exit();
    }
}
