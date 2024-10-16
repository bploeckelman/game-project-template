package lando.systems.game;

import lando.systems.game.utils.Util;

public class Config {

    public static final String window_title = "Game";
    public static final String preferences_name = "game-prefs";
    public static final int window_width = 1280;
    public static final int window_height = 720;
    public static final int framebuffer_width = window_width;
    public static final int framebuffer_height = window_height;

    public static boolean stepped_frame = false;

    /**
     * Flags for enabling/disabling certain features, mostly used for debugging.
     * {@link Flag#GLOBAL} can be disabled to globally ignore any debug flag,
     * intended for disabling all debug features at once in production builds
     */
    public enum Flag {
          GLOBAL(true)
        , RENDER(false)
        , UI(false)
        , LOG(true)
        , FRAME_STEP(false)
        , START_ON_GAMESCREEN(false)
        ;

        private boolean isEnabled;

        Flag(boolean isEnabled) {
            this.isEnabled = isEnabled;
        }

        public boolean isEnabled() {
            return GLOBAL.isEnabled && isEnabled;
        }

        public boolean isDisabled() {
            return !GLOBAL.isEnabled || !isEnabled;
        }

        /**
         * @return whether the specified flag is enabled or not, taking into account {@link Flag#GLOBAL} for global control
         */
        public static boolean isEnabled(Flag flag) {
            return GLOBAL.isEnabled && flag.isEnabled;
        }

        /**
         * Enables this flag, regardless of its current status
         */
        public boolean enable() {
            isEnabled = true;
            Util.log("Enabled Config.Flag.%s='%b'".formatted(name(), isEnabled));
            return isEnabled;
        }

        /**
         * Disables this flag, regardless of its current status
         */
        public boolean disable() {
            isEnabled = false;
            Util.log("Disabled Config.Flag.%s='%b'".formatted(name(), isEnabled));
            return isEnabled;
        }

        /**
         * Enables or disables this flag, based of the specified value regardless of its current status
         * @param enabled whether to enable or disable this flag
         * @return the new value of this flag, or false if a null flag type was provided
         */
        public boolean set(boolean enabled) {
            isEnabled = enabled;
            Util.log("Set Config.Flag.%s='%b'".formatted(name(), isEnabled));
            return isEnabled;
        }

        /**
         * Toggles whether this flag is enabled or disabled
         * @return the new value of this flag, after toggling
         */
        public boolean toggle() {
            isEnabled = !isEnabled;
            Util.log("Toggled Config.Flag.%s='%b'".formatted(name(), isEnabled));
            return isEnabled;
        }
    }
}
