package com.gui;

import java.awt.*;

public class Env {


    public static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    private static final String OS_NAME = System.getProperty("os.name");
    private static final String OS_VERSION = System.getProperty("os.version");


    public static boolean isWindows() {
        return OS_NAME.indexOf("Windows") > -1;
    }

    public static boolean isLinux() {
        return OS_NAME.indexOf("Linux") > -1;
    }

    public static boolean isMacOs() {
        return OS_NAME.indexOf("Mac OS") > -1;
    }


    public static int getHeight() {
        return screenSize.height;
    }

    public static int getWidth() {
        return screenSize.width;
    }

}
