package com.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClockAndPoem {

    private static final Integer FREQ = 30;

    public boolean stopAutoRefresh = false;

    private static Set<String> selectAuthor = new HashSet<>();

    private static ZoomDialog zoomDialog;

    private JFrame frame;

    private Box colorBar;
    private JComponent poemContainer;
    private JPanel nextStatus;

    private NavPanel navPanel = new NavPanel();


    public void show() {

        frame = new JFrame();

        if (Env.isWindows()) {
            frame.setIconImage(new ImageIcon(getClass().getResource("/images/book.png")).getImage());
        }

        if (Env.isMacOs()) {
        }

        JComponent content = Box.createVerticalBox();

        content.add(navPanel);

        //color bar
        colorBar = Box.createHorizontalBox();


        //poem
        poemContainer = Box.createVerticalBox();
        content.add(poemContainer);

        content.add(colorBar);

        List<String> pop = Env.db.pop();

        nextStatus = new JPanel();
        nextStatus.setBackground(Color.BLUE);
        nextStatus.setPreferredSize(new Dimension(-1, 1));
        colorBar.add(nextStatus);

        if (Env.db.cacheSize() > 0) {

            for (int i = 0; i < Env.db.cacheSize(); i++) {
                JPanel rest = new JPanel();
                rest.setBackground(Color.RED);
                rest.setPreferredSize(new Dimension(-1, 1));
                colorBar.add(rest);
            }
        }

        java.util.List<JPanel> jPanels = PoemBuilder.buildPoemItem(pop);
        for (JPanel jPanel : jPanels) {
            poemContainer.add(jPanel);
        }


        poemContainer.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                stopAutoRefresh = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                stopAutoRefresh = false;
                if (!poemContainer.isVisible()) {
                    stopAutoRefresh = true;
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {

                if (e.getClickCount() == 2) {
                    if (zoomDialog == null) {
                        zoomDialog = new ZoomDialog(Env.db.current, ClockAndPoem.this);
                        navPanel.setZoomDialog(zoomDialog);
                    } else {
                        zoomDialog.refresh(Env.db.current);
                    }
                }
            }
        });


        frame.add(content);

        ActionListener anAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };
        frame.getRootPane().registerKeyboardAction(anAction,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        frame.pack();
        frame.setLocation(Env.getWidth() - Double.valueOf(frame.getSize().getWidth()).intValue(), 0);
        frame.setAlwaysOnTop(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        content.getInputMap().put(KeyStroke.getKeyStroke("RIGHT"),
                "refreshPoem");

        content.getInputMap().put(KeyStroke.getKeyStroke("DOWN"),
                "refreshPoem");

        content.getInputMap().put(KeyStroke.getKeyStroke("LEFT"),
                "lastPoem");

        content.getInputMap().put(KeyStroke.getKeyStroke("UP"),
                "lastPoem");

        content.getInputMap().put(KeyStroke.getKeyStroke("ENTER"),
                "openPoem");


        content.getActionMap().put("refreshPoem",
                new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        refreshPoem(false);
                    }
                });

        content.getActionMap().put("lastPoem",
                new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        refreshPoem(true);
                    }
                });

        content.getActionMap().put("openPoem",
                new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        ZoomDialog zoom = new ZoomDialog(Env.db.current, ClockAndPoem.this);
                        navPanel.setZoomDialog(zoom);
                    }
                });


        long timeRecorder = 0;

        while (true) {

            if (timeRecorder > FREQ && timeRecorder % FREQ == 0 && !stopAutoRefresh) {
                refreshPoem(false);
            }

            try {
                Thread.sleep(1000);
                timeRecorder++;
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }


    public void hidePoem() {
        poemContainer.setVisible(false);
        colorBar.setVisible(false);

        navPanel.setVisible(true);

        frame.pack();

        int x = Double.valueOf(frame.getSize().getWidth()).intValue();
        frame.setLocation(Env.getWidth() - x, Env.getHeight() / 2);

        stopAutoRefresh = true;
    }

    public void showPoem() {

        navPanel.setVisible(false);

        poemContainer.setVisible(true);
        colorBar.setVisible(true);

        frame.pack();

        int x = Double.valueOf(frame.getSize().getWidth()).intValue();
        frame.setLocation(Env.getWidth() - x, 0);

        stopAutoRefresh = false;
    }

    public void clearZoom() {
        zoomDialog = null;
    }


    public void refreshPoem(boolean history) {

        List<String> items = history ? Env.db.popHistory() : Env.db.pop();

        poemContainer.removeAll();

        List<JPanel> poemItems = PoemBuilder.buildPoemItem(items);

        colorBar.removeAll();
        colorBar.add(nextStatus);

        if (Env.db.cacheSize() > 0) {

            for (int i = 0; i < Env.db.cacheSize(); i++) {
                JPanel rest = new JPanel();
                rest.setBackground(Color.BLACK);
                rest.setPreferredSize(new Dimension(-1, 1));
                colorBar.add(rest);
            }
        }


        for (JPanel jPanel : poemItems) {
            poemContainer.add(jPanel);
        }

        poemContainer.updateUI();
        frame.pack();

        int x = Double.valueOf(frame.getSize().getWidth()).intValue();
        if (frame.getLocation().x == 0) {
            frame.setLocation(0, 0);
        } else {
            frame.setLocation(Env.getWidth() - x, 0);
        }
    }


    public static void main(String[] args) {
        ClockAndPoem gui = new ClockAndPoem();
        gui.show();
    }

}
