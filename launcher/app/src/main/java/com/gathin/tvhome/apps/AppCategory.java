package com.gathin.tvhome.apps;

/**
 * Classification of installed applications based on TV remote compatibility.
 */
public enum AppCategory {
    /** Native Android TV application declaring CATEGORY_LEANBACK_LAUNCHER */
    TV_NATIVE,
    /** Phone or tablet application declaring CATEGORY_LAUNCHER that may require touch/mouse input */
    SIDELOADED_TOUCH,
    /** Built-in system application or settings shortcut */
    SYSTEM;

    public boolean isTvNative() {
        return this == TV_NATIVE;
    }

    public boolean requiresTouchWarning() {
        return this == SIDELOADED_TOUCH;
    }
}
