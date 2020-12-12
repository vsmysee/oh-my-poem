package com.gui;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

public class ZoomDialog extends JDialog {

    public static final int SHORT_POEM = 12;

    private JComponent poem;


    public ZoomDialog(List<String> poems, ClockAndPoem clockAndPoem) {

        clockAndPoem.hidePoem();

        setIconImage(new ImageIcon(getClass().getResource("/images/book.png")).getImage());

        setDefaultCloseOperation(2);

        ActionListener closeAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ZoomDialog.this.dispose();
            }
        };
        getRootPane().registerKeyboardAction(closeAction,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        poem = PoemBuilder.build(poems);
        add(poem);
        pack();

        if (poems.size() < SHORT_POEM) {

            if (getWidth() < 600) {
                setSize(600, getHeight());
            }
        }

        setLocationRelativeTo(null);

        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                clockAndPoem.showPoem();
                clockAndPoem.clearZoom();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ZoomDialog.this.dispose();
                }
            }
        });

        getRootPane().getInputMap().put(KeyStroke.getKeyStroke("RIGHT"),
                "nextPoem");
        getRootPane().getInputMap().put(KeyStroke.getKeyStroke("DOWN"),
                "nextPoem");

        getRootPane().getInputMap().put(KeyStroke.getKeyStroke("LEFT"),
                "lastPoem");

        getRootPane().getInputMap().put(KeyStroke.getKeyStroke("UP"),
                "lastPoem");


        getRootPane().getActionMap().put("nextPoem",
                new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        Env.db.popRandom();
                        refresh(Env.db.current);
                    }
                });

        getRootPane().getActionMap().put("lastPoem",
                new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        Env.db.popHistory();
                        refresh(Env.db.current);
                    }
                });
    }


    public void refresh(List<String> poems) {

        remove(poem);

        poem = PoemBuilder.build(poems);
        add(poem);

        pack();

        if (poems.size() < SHORT_POEM) {

            if (getWidth() < 600) {
                setSize(600, getHeight());
            }
        }


        setLocationRelativeTo(null);

    }


}
