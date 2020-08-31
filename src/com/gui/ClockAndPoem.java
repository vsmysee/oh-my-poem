package com.gui;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ClockAndPoem {

    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();


    private static final String OS_NAME = System.getProperty("os.name");
    private static final String OS_VERSION = System.getProperty("os.version");


    //private static final String FONT = "华文隶书";
    private static final String FONT = "黑体";
    private static final Integer FONT_SIZE_TITLE = 15;
    private static final Integer FONT_SIZE_POEM = 20;
    private static Integer CHUNK_SIZE = 16;
    private static final Integer FREQ = 30;

    private static PoemStack db = new PoemStack();


    public static boolean isWindows() {
        return OS_NAME.indexOf("Windows") > -1;
    }


    public static boolean isLinux() {
        return OS_NAME.indexOf("Linux") > -1;
    }


    public static boolean isMacOs() {
        return OS_NAME.indexOf("Mac OS") > -1;
    }

    static void initDB() {

        if (screenSize.getHeight() < 1000) {
            CHUNK_SIZE = 12;
        }

        try {

            String data = "";
            try (InputStream is = ClockAndPoem.class.getResourceAsStream("/data/poem.txt")) {
                try (InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                     BufferedReader reader = new BufferedReader(isr)) {
                    data = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                }
            }

            for (String item : data.split("\n")) {
                if (!item.trim().equals("")) {
                    db.push(item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        initDB();
    }


    public static <T> List<List<T>> chunkList(List<T> list, int chunkSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Invalid chunk size: " + chunkSize);
        }

        List<List<T>> chunkList = new ArrayList<>(list.size() / chunkSize);
        for (int i = 0; i < list.size(); i += chunkSize) {

            List<T> base = new ArrayList<>();

            if (i != 0) {
                base.add(list.get(0));
                base.add(list.get(1));
            }

            List<T> poems = list.subList(i, i + chunkSize >= list.size() ? list.size() : i + chunkSize);
            if (poems.size() > 0) {
                base.addAll(poems);
                chunkList.add(base);
            }
        }
        return chunkList;
    }


    static class PoemStack {

        private List<List<String>> cache = new ArrayList<>();

        private java.util.List<String> items = new ArrayList<>();

        public void push(String item) {
            items.add(item);
        }

        public List<String> pop() {
            if (cache.size() > 0) {
                List<String> res = cache.get(0);
                cache.remove(0);
                return res;
            }

            Random rand = new Random();
            int index = 0 + rand.nextInt((items.size() - 1 - 0) + 1);
            List<String> poems = Arrays.asList(items.get(index).split(";"));

            if (poems.size() > CHUNK_SIZE) {

                cache = chunkList(poems, CHUNK_SIZE);

                return pop();
            }

            return poems;
        }
    }

    static class CirclePanel extends JPanel {

        public CirclePanel(String tag) {
            setLayout(new GridLayout(0, 1));
            this.setPreferredSize(new Dimension(14, 20));
            add(new JLabel("<html><font color='white'>" + tag + "</font></html>"));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.RED);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
        }
    }

    private Date endDate;

    private java.util.List<JPanel> buildPoemItem(List<String> items) {

        java.util.List<JPanel> list = new ArrayList<>(items.size());

        for (int i = 1; i < items.size(); i++) {

            boolean title = (i == 1);

            JPanel panel = new JPanel();
            String text = items.get(i);


            JLabel poemItem = new JLabel(text);
            poemItem.setFont(new Font(FONT, Font.BOLD, title ? FONT_SIZE_TITLE : FONT_SIZE_POEM));
            if (title) {

                //add ico
                panel.add(new CirclePanel(items.get(0)));

                poemItem.setText("<html><font color='blue'>" + poemItem.getText() + "</font></html>");
                panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            }


            panel.add(poemItem);

            list.add(panel);
        }

        return list;
    }

    public void show() {

        JFrame frame = new JFrame();
        frame.setIconImage(new ImageIcon(getClass().getResource("/images/book.png")).getImage());

        JComponent content = Box.createVerticalBox();

        MyDrawPanel drawPanel = new MyDrawPanel(Color.BLACK);
        drawPanel.setPreferredSize(new Dimension(250, 250));
        content.add(drawPanel);


        JPanel poem = new JPanel(new GridLayout(0, 1));
        content.add(poem);


        poem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = screenSize.width - Double.valueOf(frame.getSize().getWidth()).intValue();
                int y = screenSize.height - Double.valueOf(frame.getSize().getHeight()).intValue();
                frame.setLocation(x, y);
                frame.revalidate();
            }
        });

        List<String> pop = db.pop();
        java.util.List<JPanel> jPanels = buildPoemItem(pop);
        for (JPanel jPanel : jPanels) {
            poem.add(jPanel);
        }


        SimpleDateFormat sf = new SimpleDateFormat("HH:mm:ss");
        JPanel bottomPanel = new JPanel();
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

        frame.pack();
        frame.setLocation(screenSize.width - Double.valueOf(frame.getSize().getWidth()).intValue(), 0);
        frame.setAlwaysOnTop(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);


        // when time over

        AtomicBoolean down = new AtomicBoolean(true);

        ActionListener move = ex -> {
            Point location = frame.getLocation();
            int y = down.get() ? Double.valueOf(location.getY()).intValue() + 5 : Double.valueOf(location.getY()).intValue() - 5;
            if (y <= 0) {
                down.set(true);
            }
            if (y > screenSize.getHeight() - frame.getSize().getHeight()) {
                down.set(false);
            }
            frame.setLocation(screenSize.width - Double.valueOf(frame.getSize().getWidth()).intValue(), y);
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
                drawPanel.color = Color.BLACK;
                timeLabel.setFont(new Font(Font.SERIF, Font.BOLD, 18));
                timeLabel.setText("<html><font color='rgb(200,200,200)'>剩余" + (end - System.currentTimeMillis()) + "毫秒</font></html>");
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

        drawPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point location = frame.getLocation();
                if (location.x > screenSize.getWidth() / 2) {
                    frame.setLocation(0, 0);
                    frame.revalidate();
                } else {
                    frame.setLocation(screenSize.width - Double.valueOf(frame.getSize().getWidth()).intValue(), 0);
                    frame.revalidate();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                moveTimer.stop();
                frame.setLocation(screenSize.width - Double.valueOf(frame.getSize().getWidth()).intValue(), 0);
                frame.pack();
                drawPanel.color = Color.BLACK;
                bottomPanel.setBackground(null);
                frame.revalidate();
            }
        });


        long timeRecorder = 0;

        while (true) {

            drawPanel.repaint();

            if (timeRecorder > FREQ && timeRecorder % FREQ == 0) {
                poem.removeAll();

                java.util.List<JPanel> poemItems = buildPoemItem(db.pop());

                drawPanel.setVisible(true);

                for (JPanel jPanel : poemItems) {
                    poem.add(jPanel);
                }

                poem.updateUI();
                frame.pack();
                int x = Double.valueOf(frame.getSize().getWidth()).intValue();
                if (frame.getLocation().x == 0) {
                    frame.setLocation(0, 0);
                } else {
                    frame.setLocation(screenSize.width - x, 0);
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
                drawPanel.color = Color.RED;
                endDate = null;
            }

            try {
                Thread.sleep(1000);
                timeRecorder++;
            } catch (Exception ex) {
            }
        }
    }

    class MyDrawPanel extends JPanel {

        private Color color;

        public MyDrawPanel(Color color) {
            this.color = color;
        }

        public void paintComponent(Graphics g) {

            Graphics2D g2d = (Graphics2D) g;

            //去锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            //清空原来的图形
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
            g2d.setColor(color);

            //圆中心坐标
            int xCenter = this.getWidth() / 2;
            int yCenter = this.getHeight() / 2;
            // 计算半径
            int radius = (int) (this.getHeight() * 0.8 * 0.5);
            // 画圆
            g2d.drawOval(xCenter - radius, yCenter - radius, radius * 2, radius * 2);

            g2d.drawOval(xCenter - 10, yCenter - 10, 20, 20);
            g2d.fillOval(xCenter - 10, yCenter - 10, 20, 20);


            //画时钟的12个数字(如果用rotate方法则数字会倾斜倒立)
            for (int i = 0; i < 12; i++) {

                double dd = Math.PI / 180 * i * (360 / 12); //每次转360/12度
                int x = (xCenter - 4) + (int) ((radius - 12) * Math.cos(dd));
                int y = (yCenter + 4) + (int) ((radius - 12) * Math.sin(dd));

                //因为是从顺时钟3点钟开始画;所以索引i需要加上3
                int j = i + 3;
                if (j > 12)
                    j = j - 12;
                g2d.setColor(Color.BLUE);
                g2d.drawString(Integer.toString(j), x, y);
            }

            AffineTransform old = g2d.getTransform();

            g2d.setColor(Color.BLACK);

            //时钟的60个刻度
            for (int i = 0; i < 60; i++) {
                int w = i % 5 == 0 ? 5 : 3;
                g2d.fillRect(xCenter - 2, 28, w, 3);
                g2d.rotate(Math.toRadians(6), xCenter, yCenter);
            }
            //设置旋转重置
            g2d.setTransform(old);

            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);

            //画日期和星期
            DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat df2 = new SimpleDateFormat("E");
            //g2d.drawString(df1.format(calendar.getTime()), xCenter - 40, yCenter + 35);
            //g2d.drawString(df2.format(calendar.getTime()), xCenter - 20, yCenter + 50);

            // 画时钟的图形
            double hAngle = (hour - 12 + minute / 60d) * 360d / 12d;
            g2d.rotate(Math.toRadians(hAngle), xCenter, yCenter);
            int xhArr[] = {xCenter, xCenter + 9, xCenter, xCenter - 9};
            int yhArr[] = {65, yCenter, yCenter + 8, yCenter};
            g2d.drawPolygon(xhArr, yhArr, xhArr.length);
            g2d.fillPolygon(xhArr, yhArr, xhArr.length);
            g2d.setTransform(old);

            // 画分钟的图形
            double mAngle = (minute + second / 60d) * 360d / 60d;
            g2d.rotate(Math.toRadians(mAngle), xCenter, yCenter);
            int xmArr[] = {xCenter, xCenter + 6, xCenter, xCenter - 6};
            int ymArr[] = {45, yCenter, yCenter + 10, yCenter};
            g2d.drawPolygon(xmArr, ymArr, xmArr.length);
            g2d.fillPolygon(xmArr, ymArr, xmArr.length);
            g2d.setTransform(old);

            // 画秒钟的图形
            double sAngle = second * 360d / 60d;
            g2d.rotate(Math.toRadians(sAngle), xCenter, yCenter);
            g2d.setColor(Color.RED);
            g2d.drawLine(xCenter, yCenter, xCenter, 35);
        }
    }


    public static void main(String[] args) {
        ClockAndPoem gui = new ClockAndPoem();
        gui.show();
    }
}
