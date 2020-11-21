package com.gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Env {

    public static final String POEM_DATA_FILE = "/data/one.txt";

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

    public static List<String> FONTS = new ArrayList<>();

    public static void fontList() {

        String fonts[] =
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        for (int i = 0; i < fonts.length; i++) {

            if (fonts[i].matches("[\\u4E00-\\u9FA5]+")) {
                FONTS.add(fonts[i]);
            }
        }

    }


    public static int getHeight() {
        return screenSize.height;
    }

    public static int getWidth() {
        return screenSize.width;
    }

}
