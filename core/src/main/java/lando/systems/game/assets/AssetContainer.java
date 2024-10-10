package lando.systems.game.assets;

import com.badlogic.gdx.utils.ObjectMap;
import lando.systems.game.utils.Util;

public abstract class AssetContainer<T extends Enum<T> & AssetType<ResourceType>, ResourceType> {

    protected final String containerClassName;
    protected final Class<ResourceType> resourceTypeClass;
    protected final ObjectMap<T, ResourceType> resources;

    public AssetContainer(Class<? extends AssetContainer<T, ResourceType>> assetContainerClass, Class<ResourceType> resourceTypeClass) {
        this.containerClassName = assetContainerClass.getSimpleName();
        this.resourceTypeClass = resourceTypeClass;
        this.resources = new ObjectMap<>();
    }

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
