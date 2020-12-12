package com.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NavPanel extends JPanel {

    private JLabel prev;
    private JLabel next;

    private ZoomDialog zoomDialog;

    public NavPanel() {

        prev = new JLabel(new ImageIcon(getClass().getResource("/images/prev.png")));
        prev.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                prev.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                Env.db.popHistory();
                if (zoomDialog != null) {
                    zoomDialog.refresh(Env.db.current);
                }
            }
        });

        next = new JLabel(new ImageIcon(getClass().getResource("/images/next.png")));
        next.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                next.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                Env.db.popRandom();
                if (zoomDialog != null) {
                    zoomDialog.refresh(Env.db.current);
                }
            }
        });


        add(prev);
        add(new JPanel());
        add(next);
        setVisible(false);

    }

    public void setZoomDialog(ZoomDialog zoomDialog) {
        this.zoomDialog = zoomDialog;
    }
}
