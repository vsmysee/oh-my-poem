package com.gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PoemBuilder {

    private static final Integer FONT_SIZE_TITLE = 16;
    private static final Integer FONT_SIZE_POEM = 22;


    public static java.util.List<JPanel> buildPoemItem(List<String> items) {

        java.util.List<JPanel> list = new ArrayList<>();
        list.add(new JPanel()); //blank size

        for (int i = 1; i < items.size(); i++) {

            boolean title = (i == 1);

            JPanel panel = new JPanel();
            String text = items.get(i);


            JLabel poemItem = new JLabel(text);
            poemItem.setFont(new Font(Setting.FONT, Font.BOLD, title ? FONT_SIZE_TITLE : FONT_SIZE_POEM));
            if (title) {

                //add ico
                panel.add(new CirclePanel(items.get(0)));

                poemItem.setText("<html><font color='blue'>" + poemItem.getText() + "</font></html>");
            }

            panel.add(poemItem);

            list.add(panel);
        }

        list.add(new JPanel()); //blank size

        return list;
    }

}
