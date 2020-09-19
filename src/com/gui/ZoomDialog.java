package com.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

public class ZoomDialog extends JDialog {

    private static final String FONT = "黑体";

    private static final int titleSize = 40;

    private static final int bodySize = 50;

    public ZoomDialog(List<String> poems) {

        setDefaultCloseOperation(2);


        getRootPane().registerKeyboardAction(new ActionListener() {
                                                 @Override
                                                 public void actionPerformed(ActionEvent e) {
                                                     ZoomDialog.this.dispose();
                                                 }
                                             },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);


        add(poem(poems));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

    }

    private JLabel buildJLabel(String text, int size) {
        JLabel item = new JLabel(text);
        item.setFont(new Font(FONT, Font.BOLD, size));
        return item;
    }

    private JPanel title(List<String> poems) {
        JPanel title = new JPanel();

        JLabel comp = buildJLabel(poems.get(1), titleSize);

        comp.setText("<html><font color='blue'>" + comp.getText() + "</font></html>");

        title.add(comp);
        return title;
    }


    private JComponent poem(List<String> poems) {
        JComponent poem = Box.createVerticalBox();

        poem.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        poem.add(title(poems));

        poem.add(new JSeparator());
        poem.add(Box.createVerticalStrut(10));

        int index = 2;

        while (index < poems.size()) {

            JPanel panel = new JPanel();

            String left = poems.get(index).trim().replace("\r", "");

            panel.add(buildJLabel(left, bodySize));

            if ((index + 1) < poems.size()) {

                String right = poems.get(index + 1).trim().replace("\r", "");

                if (left.length() == right.length()) {
                    panel.add(buildJLabel("，", bodySize));
                    panel.add(buildJLabel(right, bodySize));
                    index++;
                }

            }

            index++;

            poem.add(panel);

        }

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(new ClockAndPoem.CirclePanel(poems.get(0), 20, 25));

        poem.add(bottom);

        return poem;
    }
}
