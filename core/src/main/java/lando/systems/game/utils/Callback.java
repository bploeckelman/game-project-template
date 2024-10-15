package lando.systems.game.utils;

@FunctionalInterface
public interface Callback<T extends Callback.Params> {

    interface Params {}

    /**
     * Primary {@link FunctionalInterface} callback method,
     * accepting typed params for a specific usage
     * by extending {@link Callback.Params}
     */
    void run(T params);

    /**
     * Optional no-arg callback function
     */
    default void run() {}

    /**
     * Optional arbitrary-arg callback function
     */
    default void run(Object... params) {}
}
