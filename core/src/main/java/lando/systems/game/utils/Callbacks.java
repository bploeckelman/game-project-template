package lando.systems.game.utils;

/**
 * Functional interfaces that work with lambdas for callback methods that accept various parameters.
 */
public class Callbacks {

    @FunctionalInterface
    public interface NoArg {
        void run();
    }

    @FunctionalInterface
    public interface VarArg {
        void run(Object... args);
    }

    @FunctionalInterface
    public interface TypedArg<T extends TypedArg.Params> {
        interface Params {}
        void run(T params);
    }
}
