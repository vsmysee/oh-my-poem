package com.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ZoomDialog extends JDialog {

    private static final int titleSize = 25;

    private static final int bodySize = 35;


    private JComponent poem;

    private List<JPanel> poemLabels = new ArrayList<>();

    private List<Point> points = new ArrayList<>();

    private Timer timer;


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


        poem = poem(poems);
        add(poem);
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


    public void addScroll(JPanel panel) {
        poemLabels.add(panel);
    }


    private void resetPosition(List<String> poems, JComponent poem) {
        if (poems.size() > 10) {
            setLocation(ClockAndPoem.screenSize.width / 2 - (getWidth() / 2), (ClockAndPoem.screenSize.height - getHeight()) / 2);
        } else {
            setLocationRelativeTo(null);
        }

        if (getHeight() > ClockAndPoem.screenSize.height - 30) {

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

        poem = poem(poems);
        add(poem);

        pack();

        if (getWidth() < 600) {
            setSize(600,getHeight());
        }

        resetPosition(poems, poem);
    }


    private JLabel buildJLabel(String text, int size) {
        JLabel item = new JLabel(text);
        item.setFont(new Font(Setting.FONT, Font.BOLD, size));
        return item;
    }


    private JComponent title(List<String> poems) {

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


    private JComponent poem(List<String> poems) {
        JComponent poemRoot = Box.createVerticalBox();


        poemRoot.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        poemRoot.add(title(poems));

        poemRoot.add(new JSeparator());
        poemRoot.add(Box.createVerticalStrut(10));

        int index = 2;

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

            addScroll(panel);

        }

        Box poemContent = Box.createVerticalBox();
        for (JPanel poemLabel : poemLabels) {
            poemContent.add(poemLabel);
        }


        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(new CirclePanel(poems.get(0), 20, 25));
        poemContent.add(bottom);

        addScroll(bottom);

        poemRoot.add(poemContent);

        return poemRoot;
    }

}
