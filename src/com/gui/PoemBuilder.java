package com.gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PoemBuilder {

    private static final Integer FONT_SIZE_TITLE = 16;
    private static final Integer FONT_SIZE_POEM = 22;

    private static final int titleSize = 25;

    private static final int bodySize = 35;


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

    private static JComponent title(List<String> poems) {

        Box horizontalBox = Box.createHorizontalBox();

        horizontalBox.add(Box.createHorizontalGlue());

        JPanel title = new JPanel();

        JLabel comp = buildJLabel(poems.get(1), titleSize);
        comp.setText("<html><font color='blue'>" + comp.getText() + "</font></html>");

        horizontalBox.add(title);

        horizontalBox.add(Box.createHorizontalGlue());

        title.add(comp);
        return horizontalBox;
    }

    private static JLabel buildAuthor(String poem) {
        String[] split = poem.split("");
        StringBuffer sb = new StringBuffer();
        sb.append("<html>");
        sb.append("<font color='grey'>");

        for (String item : split) {
            sb.append(item);
            sb.append("<br/>");
        }

        sb.append("</font>");
        sb.append("</html>");
        JLabel label = buildJLabel(sb.toString(), 25);
        return label;
    }

    private static JLabel convertToHtml(String poem, boolean title) {
        String[] split = poem.split("");
        StringBuffer sb = new StringBuffer();
        sb.append("<html>");
        if (title) {
            sb.append("<font color='blue'>");
        }

        for (String item : split) {
            sb.append(item);
            sb.append("<br/>");
        }

        if (title) {
            sb.append("</font>");
        }
        sb.append("</html>");
        JLabel label = buildJLabel(sb.toString(), title ? 25 : 35);
        return label;
    }


    private static JComponent build2(List<String> poems) {
        Box horizontalBox = Box.createHorizontalBox();

        String title = poems.get(1);
        int index = title.indexOf("《");

        for (int i = poems.size() - 1; i > 1; i--) {
            horizontalBox.add(convertToHtml(poems.get(i), false));
        }

        Box box = Box.createVerticalBox();
        CirclePanel song = new CirclePanel(poems.get(0));
        song.setMaximumSize(new Dimension(16, 16));
        box.add(buildAuthor("   "));
        box.add(song);
        box.add(buildAuthor(" "));
        box.add(buildAuthor(title.substring(0, index)));


        horizontalBox.add(box);
        horizontalBox.add(Box.createHorizontalStrut(15));


        title = title.substring(index);
        horizontalBox.add(convertToHtml(title.replace("《", "").replace("》", ""), true));

        horizontalBox.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        return horizontalBox;
    }


    public static JComponent build(List<String> poems) {

        if (poems.size() >= ZoomDialog.SHORT_POEM) {
            return build2(poems);
        }

        JComponent poemRoot = Box.createVerticalBox();

        poemRoot.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        poemRoot.add(title(poems));

        poemRoot.add(new JSeparator());
        poemRoot.add(Box.createVerticalStrut(10));

        int index = 2;

        List<JPanel> panels = new ArrayList<>();

        while (index < poems.size()) {

            JPanel panel = new JPanel();

            String left = poems.get(index).trim();
            panel.add(buildJLabel(left, bodySize));

            if ((index + 1) < poems.size()) {

                String right = poems.get(index + 1).trim();

                if (left.length() == right.length()) {
                    panel.add(buildJLabel("，", bodySize));
                    panel.add(buildJLabel(right, bodySize));
                    index++;
                }

            }

            index++;

            panels.add(panel);

        }

        Box poemContent = Box.createVerticalBox();
        for (JPanel poemLabel : panels) {
            poemContent.add(poemLabel);
        }


        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(new CirclePanel(poems.get(0)));
        poemContent.add(bottom);


        poemRoot.add(poemContent);

        return poemRoot;
    }


    private static JLabel buildJLabel(String text, int size) {
        JLabel item = new JLabel(text);
        item.setFont(new Font(Setting.FONT, Font.BOLD, size));
        return item;
    }


}
