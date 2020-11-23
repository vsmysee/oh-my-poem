package com.gui;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClockAndPoem {

    private static final Integer FREQ = 30;

    public boolean stopAutoRefresh = false;

    private static Set<String> selectAuthor = new HashSet<>();

    private static ZoomDialog zoomDialog;

    private Date endDate;

    private JFrame frame;

    private Box colorBar;
    private JComponent poemContainer;
    private JPanel nextStatus;
    private JPanel bottomPanel;


    public void show() {

        frame = new JFrame();

        if (Env.isWindows()) {
            frame.setIconImage(new ImageIcon(getClass().getResource("/images/book.png")).getImage());
        }

        if (Env.isMacOs()) {
        }

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem select = new JMenuItem("选择");
        JMenuItem reset = new JMenuItem("重置");
        JMenuItem fonts = new JMenuItem("字体");

        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectAuthor.clear();
                Env.db.source.clear();
            }
        });

        fonts.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JDialog jDialog = new JDialog();
                JComboBox<String> comp = new JComboBox<String>(Env.FONTS.toArray(new String[]{}));
                jDialog.add(comp);
                comp.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        Object selectedItem = comp.getSelectedItem();
                        Setting.FONT = selectedItem.toString();
                        jDialog.dispose();
                    }
                });
                jDialog.pack();
                jDialog.setLocationRelativeTo(null);
                jDialog.setVisible(true);
            }
        });

        select.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JDialog jDialog = new JDialog();
                jDialog.setSize(600, 500);
                jDialog.setLayout(new GridLayout(0, 6));
                for (String item : Env.db.authors) {
                    JToggleButton selectBtn = new JToggleButton(item);
                    if (selectAuthor.contains(item)) {
                        selectBtn.setSelected(true);
                    }
                    selectBtn.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            selectAuthor.add(item);
                            Env.db.source.addAll(Env.db.authorPoems.get(item));
                        }
                    });
                    jDialog.add(selectBtn);
                }
                jDialog.setLocationRelativeTo(null);
                jDialog.setVisible(true);
            }
        });
        popupMenu.add(select);
        popupMenu.add(reset);
        popupMenu.add(fonts);


        JComponent content = Box.createVerticalBox();

        content.add(navPanel);

        //color bar
        colorBar = Box.createHorizontalBox();


        //poem
        poemContainer = Box.createVerticalBox();
        content.add(poemContainer);
        poemContainer.setComponentPopupMenu(popupMenu);

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
                    } else {
                        zoomDialog.refresh(Env.db.current);
                    }
                }
            }
        });

        SimpleDateFormat sf = new SimpleDateFormat("HH:mm:ss");
        bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createEtchedBorder());
        JLabel timeLabel = new JLabel(sf.format(new Date()));
        timeLabel.setFont(new Font(Font.SERIF, Font.BOLD, 18));
        bottomPanel.add(timeLabel);
        content.add(bottomPanel);


        JSlider timeSlider = new JSlider(0, 60, 10);
        timeSlider.setPaintTrack(true);
        timeSlider.setPaintTicks(true);
        timeSlider.setPaintLabels(true);

        timeSlider.setMajorTickSpacing(10);
        timeSlider.setMinorTickSpacing(5);
        timeSlider.setVisible(false);
        content.add(timeSlider);

        JPanel confirmPanel = new JPanel();
        JButton confirmButton = new JButton("计时");
        confirmPanel.add(confirmButton);
        JButton cancelButton = new JButton("取消");
        confirmPanel.add(cancelButton);
        confirmPanel.setVisible(false);
        content.add(confirmPanel);


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
                        new ZoomDialog(Env.db.current, ClockAndPoem.this);
                    }
                });


        // when time over

        AtomicBoolean down = new AtomicBoolean(true);

        ActionListener move = ex -> {
            Point location = frame.getLocation();
            int y = down.get() ? Double.valueOf(location.getY()).intValue() + 5 : Double.valueOf(location.getY()).intValue() - 5;
            if (y <= 0) {
                down.set(true);
            }
            if (y > Env.getHeight() - frame.getSize().getHeight()) {
                down.set(false);
            }
            frame.setLocation(Env.getWidth() - Double.valueOf(frame.getSize().getWidth()).intValue(), y);
            frame.revalidate();
        };

        Timer moveTimer = new Timer(10, move);


        // time setting

        final Integer[] selectTime = {0};
        final boolean[] timeSettingLock = {false};


        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeSlider.setVisible(false);
                confirmPanel.setVisible(false);
                timeSettingLock[0] = false;

                long end = System.currentTimeMillis() + selectTime[0] * 60 * 1000;
                endDate = new Date(end);
                timeLabel.setFont(new Font(Font.SERIF, Font.BOLD, 18));
                timeLabel.setText("<html><font color='rgb(200,200,200)'>剩余" + (end - System.currentTimeMillis()) + "毫秒</font></html>");

                frame.pack();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (timeSettingLock[0]) {
                    timeSlider.setVisible(false);
                    confirmPanel.setVisible(false);
                    timeSettingLock[0] = false;
                    timeLabel.setText(sf.format(new Date()));
                    frame.pack();

                    return;
                }
            }
        });

        bottomPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                timeSlider.setVisible(true);
                confirmPanel.setVisible(true);
                timeSettingLock[0] = true;
                timeLabel.setText("<html><font color='rgb(200,200,200)'>" + timeSlider.getValue() + "分钟后提醒</font></html>");
                selectTime[0] = timeSlider.getValue();
                endDate = null;

                timeSlider.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        endDate = null;
                        selectTime[0] = timeSlider.getValue();
                        timeLabel.setText("<html><font color='rgb(200,200,200)'>" + selectTime[0] + "分钟后提醒</font></html>");
                    }
                });

                frame.pack();

            }
        });


        Timer endTimer = new Timer(200, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (endDate != null && !timeSettingLock[0]) {
                    long cust = endDate.getTime() - System.currentTimeMillis();
                    if (cust < 0) {
                        cust = 0;
                    }
                    timeLabel.setText("<html><font color='rgb(200,200,200)'>剩余" + cust + "毫秒</font></html>");
                    timeLabel.updateUI();
                }
            }
        });

        endTimer.start();


        long timeRecorder = 0;

        while (true) {

            if (timeRecorder > FREQ && timeRecorder % FREQ == 0 && !stopAutoRefresh) {

                refreshPoem(false);

                if (zoomDialog != null) {
                    zoomDialog.refresh(Env.db.current);
                }

            }

            if (endDate == null && !timeSettingLock[0]) {
                timeLabel.setText(sf.format(new Date()));
            }


            if (endDate != null && endDate.before(new Date())) {
                bottomPanel.setBackground(Color.RED);
                timeLabel.setText(sf.format(new Date()));
                bottomPanel.updateUI();
                if (!moveTimer.isRunning()) {
                    moveTimer.start();
                }
                endDate = null;
            }


            try {
                Thread.sleep(1000);
                timeRecorder++;
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }



    private JLabel prev;
    private JLabel next;

    private JPanel navPanel = new JPanel();


    {

        prev = new JLabel(new ImageIcon(getClass().getResource("/images/prev.png")));
        prev.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                prev.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                Env.db.popHistory();
                zoomDialog.refresh(Env.db.current);
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
                zoomDialog.refresh(Env.db.current);
            }
        });
    }

    {
        navPanel.add(prev);
        navPanel.add(new JPanel());
        navPanel.add(next);
        navPanel.setVisible(false);
    }

    public void hidePoem() {
        poemContainer.setVisible(false);
        colorBar.setVisible(false);
        bottomPanel.setVisible(false);

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
        bottomPanel.setVisible(true);

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
