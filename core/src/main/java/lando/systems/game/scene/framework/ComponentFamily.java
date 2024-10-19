package lando.systems.game.scene.framework;


import com.badlogic.gdx.utils.Array;

/**
 * Tag interface for interfaces and classes defining a logical 'family' of component types.
 * Tracked by the {@link World} to allow for efficient querying of all components of a given family.
 * Each family should have a unique integer type id, set from the {@link Component#NEXT_FAMILY_TYPE_ID} value.
 * <code><pre>
 * // Example family definition
 * public interface MyFamily extends ComponentFamily {
 *     int familyType = Component.NEXT_FAMILY_TYPE_ID++;
 *
 *     static int familyType() {
 *       return MyFamily.familyType;
 *     }
 *     static Class<? extends ComponentFamily> familyClass() {
 *       return MyFamily.class;
 *     }
 *
 *     // Add any additional family-specific methods here...
 * }
 * </pre></code>
 */
public interface ComponentFamily {

    /**
     * Marker for an undefined family type
     */
    int FAMILY_TYPE_NONE = 0;

    /**
     * Get the unique integer type id for this family
     *
     * @return the unique integer type id for this family
     */
    static int familyType() {
        return FAMILY_TYPE_NONE;
    }

    /**
     * Marker for an undefined family class
     */
    Class<? extends ComponentFamily> FAMILY_CLASS_NONE = ComponentFamily.class;

    /**
     * Get the {@link Class} instance for this family
     *
     * @return the {@link Class} instance for this family
     */
    static Class<? extends ComponentFamily> familyClass() {
        return FAMILY_CLASS_NONE;
    }

    /**
     * Convenience method to get all components of this family from the {@link World}
     */
    @SuppressWarnings("unchecked")
    static <F extends ComponentFamily> Array<F> getComponents() {
        return (Array<F>) World.families.getFamily(familyClass());
    }
}
