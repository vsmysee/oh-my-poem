package com.gui;

import javax.swing.*;
import java.awt.*;


public class CirclePanel extends JPanel {

    public CirclePanel(String tag) {
        setLayout(new GridLayout(0, 1));
        this.setPreferredSize(new Dimension(14, 20));
        add(new JLabel("<html><font color='white'>" + tag + "</font></html>"));
    }

    public CirclePanel(String tag, int width, int height) {
        setLayout(new GridLayout(0, 1));
        this.setPreferredSize(new Dimension(width, height));
        add(new JLabel("<html><font color='white' size=5>" + tag + "</font></html>"));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.RED);
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
    }
}
