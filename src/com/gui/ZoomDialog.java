package com.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ZoomDialog extends JDialog {

    private static final String FONT = "黑体";

    private static final int titleSize = 25;

    private static final int bodySize = 35;


    private ClockAndPoem clockAndPoem;

    private JComponent poem;

    private List<JPanel> poemLabels = new ArrayList<>();


    public ZoomDialog(List<String> poems, ClockAndPoem clockAndPoem) {

        this.clockAndPoem = clockAndPoem;
        clockAndPoem.hidePoem();

        setIconImage(new ImageIcon(getClass().getResource("/images/book.png")).getImage());

        setDefaultCloseOperation(2);

        ActionListener anAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ZoomDialog.this.dispose();
            }
        };
        getRootPane().registerKeyboardAction(anAction,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        poem = poem(poems);
        add(poem);
        pack();

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

        getRootPane().getInputMap().put(KeyStroke.getKeyStroke("LEFT"),
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

    private void resetPosition(List<String> poems, JComponent poem) {
        if (poems.size() > 10) {
            setLocation(ClockAndPoem.screenSize.width / 2 - (getWidth() / 2), 20);
        } else {
            setLocationRelativeTo(null);
        }

        if (getHeight() > ClockAndPoem.screenSize.height - 20) {

            Timer timer = new Timer(500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    for (Component component : poemLabels) {

                        int x = component.getLocation().x;
                        int y = component.getLocation().y;

                        component.setLocation(x, y - 5);
                        component.repaint();
                    }

                }
            });

            timer.start();

        }

    }


    public void refresh(List<String> poems) {

        remove(poem);

        poemLabels.clear();

        poem = poem(poems);
        add(poem);

        pack();

        resetPosition(poems, poem);


    }


    private JLabel buildJLabel(String text, int size) {
        JLabel item = new JLabel(text);
        item.setFont(new Font(FONT, Font.BOLD, size));
        return item;
    }

    private JComponent title(List<String> poems) {

        Box horizontalBox = Box.createHorizontalBox();
        JLabel prev = new JLabel(new ImageIcon(getClass().getResource("/images/prev.png")));
        prev.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                prev.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                clockAndPoem.refreshPoem(true, true);
                refresh(clockAndPoem.db.current);
            }
        });
        horizontalBox.add(prev);

        horizontalBox.add(Box.createHorizontalGlue());

        JPanel title = new JPanel();

        JLabel comp = buildJLabel(poems.get(1), titleSize);
        comp.setText("<html><font color='blue'>" + comp.getText() + "</font></html>");

        horizontalBox.add(title);

        horizontalBox.add(Box.createHorizontalGlue());
        JLabel next = new JLabel(new ImageIcon(getClass().getResource("/images/next.png")));
        next.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                next.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                clockAndPoem.refreshPoem(false, true);
                refresh(clockAndPoem.db.current);
            }
        });
        horizontalBox.add(next);

        title.add(comp);
        return horizontalBox;
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

            poem.add(panel);

            poemLabels.add(panel);

        }

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(new ClockAndPoem.CirclePanel(poems.get(0), 20, 25));

        poem.add(bottom);
        return poem;
    }

}
