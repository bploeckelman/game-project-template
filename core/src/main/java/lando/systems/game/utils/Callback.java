package lando.systems.game.utils;

@FunctionalInterface
public interface Callback {
    void run(Object... params);
}
