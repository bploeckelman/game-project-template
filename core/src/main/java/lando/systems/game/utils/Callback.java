package lando.systems.game.utils;

@FunctionalInterface
public interface Callback<T extends Callback.Params> {

    interface Params {
        void parse(Object... params);

        default boolean validate(int expectedNumParams, String implName, Object... params) {
            if (params == null || params.length == expectedNumParams) {
                int actualNumParams = 0;
                if (params != null) {
                    actualNumParams = params.length;
                }

                Util.log("%s callback params invalid, expected %d values, got %d"
                    .formatted(implName, expectedNumParams, actualNumParams));
                return false;
            }
            return true;
        }
    }

    void run(Object... params);
}
