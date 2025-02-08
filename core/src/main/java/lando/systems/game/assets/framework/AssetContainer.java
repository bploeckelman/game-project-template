package lando.systems.game.assets.framework;

import com.badlogic.gdx.utils.ObjectMap;
import lando.systems.game.assets.Assets;
import lando.systems.game.utils.Util;

public abstract class AssetContainer<T extends Enum<T> & AssetEnum<ResourceType>, ResourceType> {

    /**
     * Static reference to this asset container instance. This needs to be included
     * in each concrete {@link AssetContainer} implementation as well as setting
     * it in the constructor to allow for global access to each container instance:
     * <code><pre>
     * public class Things extends AssetContainer&lt;Things.Type, TextureRegion&gt; {
     *     public static AssetContainer<Things.Type, TextureRegion> container;
     *     public enum Type implements AssetEnum<TextureRegion> { ... }
     *     public Things() {
     *         super(Things.class, TextureRegion.class);
     *         Things.container = this;
     *     }
     *     &commat;Override
     *     public void init(Assets assets) {
     *         var atlas = assets.atlas;
     *         for (var type : Type.values()) {
     *             var region = atlas.findRegion(type.textureRegionName());
     *             // error checking...
     *             resources.put(type, region);
     *         }
     *     }
     * }</pre></code>
     */
    public static AssetContainer<?, ?> container;

    protected final String containerClassName;
    protected final Class<ResourceType> resourceTypeClass;
    protected final ObjectMap<T, ResourceType> resources;

    public AssetContainer(Class<? extends AssetContainer<T, ResourceType>> assetContainerClass, Class<ResourceType> resourceTypeClass) {
        this.containerClassName = assetContainerClass.getSimpleName();
        this.resourceTypeClass = resourceTypeClass;
        this.resources = new ObjectMap<>();
    }

    /**
     * Optional method to be overridden when needed to perform custom loading in
     * {@link com.badlogic.gdx.assets.AssetManager}, intended to be called in
     * {@link Assets} constructor alongside other {@link com.badlogic.gdx.assets.AssetManager#load}
     * calls
     */
    public void load(Assets assets) { /* no-op by default */ }

    public abstract void init(Assets assets);

    public ResourceType get(T assetType) {
        if (assetType == null) {
            Util.log(containerClassName, "Unable to get %s, null asset type provided".formatted(resourceTypeClass.getSimpleName()));
            return null;
        }

        var resource = resources.get(assetType);
        if (resource == null) {
            Util.log(containerClassName, "%s resource not found for asset type '%s'".formatted(resourceTypeClass.getSimpleName(), assetType.name()));
        }
        return resource;
    }
}
