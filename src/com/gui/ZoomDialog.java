package com.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ZoomDialog extends JDialog {

    private JComponent poem;

    private List<Point> points = new ArrayList<>();

    private Timer timer;

    private List<JPanel> poemLabels;

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


        ActionListener restoreAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restoreScroll();
            }
        };
        getRootPane().registerKeyboardAction(restoreAction,
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);


        PoemPanel pp = PoemBuilder.build(poems);
        poem = pp.getPoem();
        add(poem);
        poemLabels = pp.getPoemLabels();
        pack();

        if (getWidth() < 600) {
            setSize(600,getHeight());
        }

        resetPosition(poems, poem);

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
                if(e.getClickCount() == 2){
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
                        clockAndPoem.refreshPoem(false, true);
                        refresh(clockAndPoem.db.current);

                    }
                });

        getRootPane().getActionMap().put("lastPoem",
                new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        clockAndPoem.refreshPoem(true, true);
                        refresh(clockAndPoem.db.current);
                    }
                });
    }

    private void restoreScroll() {
        for (int i = 0; i < poemLabels.size(); i++) {

            Component component = poemLabels.get(i);
            Point p = points.get(i);
            if (p != null) {
                p.setLocation(p);
                component.setLocation(p);
                component.repaint();
            }

        }
    }

    private void resetPosition(List<String> poems, JComponent poem) {
        if (poems.size() > 10) {
            setLocation(Env.getWidth() / 2 - (getWidth() / 2), (Env.getHeight() - getHeight()) / 2);
        } else {
            setLocationRelativeTo(null);
        }

        if (getHeight() > Env.getHeight() - 30) {

            if (timer == null) {
                timer = new Timer(500, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        for (int i = 0; i < poemLabels.size(); i++) {

                            Component component = poemLabels.get(i);

                            try {
                                points.get(i);
                            } catch (IndexOutOfBoundsException ex) {
                                points.add(component.getLocation());
                            }

                            int x = component.getLocation().x;
                            int y = component.getLocation().y;

                            component.setLocation(x, y - 5);
                            component.repaint();

                            if (i == poemLabels.size() - 1 &&  y < 25) {

                                restoreScroll();

                            }
                        }

                    }
                });
            }

            timer.start();

        }

    }


    public void refresh(List<String> poems) {

        if (timer != null) {
            timer.stop();
        }

        remove(poem);
        poemLabels.clear();

        PoemPanel build = PoemBuilder.build(poems);
        poem = build.getPoem();
        poemLabels = build.getPoemLabels();
        add(poem);

        pack();

        if (getWidth() < 600) {
            setSize(600,getHeight());
        }

        resetPosition(poems, poem);
    }

}
