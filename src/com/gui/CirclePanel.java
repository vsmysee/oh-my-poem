package com.gui;

import javax.swing.*;
import java.awt.*;


public class CirclePanel extends JPanel {

    public CirclePanel(String tag) {
        setLayout(new GridLayout(0, 1));
        add(new JLabel("<html><font color='white'>" + tag + "</font></html>"));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(Color.RED);

        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
    }
}
