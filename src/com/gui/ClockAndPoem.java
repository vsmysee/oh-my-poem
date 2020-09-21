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

    public static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    private static final String OS_NAME = System.getProperty("os.name");
    private static final String OS_VERSION = System.getProperty("os.version");

    private static final String FONT = "黑体";
    private static final Integer FONT_SIZE_TITLE = 16;
    private static final Integer FONT_SIZE_POEM = 22;
    private static Integer CHUNK_SIZE = 16;
    private static final Integer FREQ = 30;

    private static boolean mouseHover = false;

    public static PoemStack db = new PoemStack();

    private static Set<String> author = new HashSet<>();
    private static Set<String> selectAuthor = new HashSet<>();

    private static ZoomDialog zoomDialog;

    private static Map<String, List<String>> authorPoems = new HashMap<>();

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

                try {
                    if (!item.trim().equals("")) {
                        db.push(item);
                    }
                } catch (Exception e) {

                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static {
        initDB();
        authorPoems.put("短诗", db.shortPoem);
        author.add("短诗");
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

        private List<String> history = new ArrayList<>();

        public List<String> shortPoem = new ArrayList<>();

        public List<String> source = new ArrayList<>();

        public List<String> current;

        public void push(String item) {
            items.add(item);
            authorData(item);
        }

        public void clearCache() {
            cache.clear();
        }

        public int cacheSize() {
            return cache.size();
        }

        public void addHistory(String poem) {
            if (history.size() > 10) {
                history.clear();
            }
            history.add(poem);
        }

        public String getHistory() {
            if (history.size() > 1) {
                String cacheItem = history.get(history.size() - 2);
                history.remove(history.size() - 2);
                return cacheItem;
            }
            return null;
        }

        public String random() {

            List<String> from = items;

            if (source.size() > 0) {
                from = source;
            }

            Random rand = new Random();
            int index = 0 + rand.nextInt((from.size() - 1 - 0) + 1);
            String poem = from.get(index);
            addHistory(poem);
            return poem;
        }

        public List<String> popHistory() {

            clearCache();

            String poem = getHistory();
            if (poem == null) {
                return pop();
            }

            List<String> poems = current = Arrays.asList(poem.split(";"));


            if (poems.size() > CHUNK_SIZE) {
                cache = chunkList(poems, CHUNK_SIZE);
                return pop();
            }

            return poems;
        }

        private String getAuthor(List<String> poems, String item) {
            String author = poems.get(1);
            if (author.indexOf("《") == -1) {
                return author;
            }

            int start = item.indexOf("》;");
            if (item.substring(start).length() < 40) {
                shortPoem.add(item);
            }

            return author.substring(0, author.indexOf("《"));
        }


        public List<String> pop() {

            if (cache.size() > 0) {
                List<String> res = cache.get(0);
                cache.remove(0);
                return res;
            }

            String poem = random();

            List<String> poems = current = Arrays.asList(poem.split(";"));

            String author = getAuthor(poems, poem);
            ClockAndPoem.author.add(author);

            if (poems.size() > CHUNK_SIZE) {

                cache = chunkList(poems, CHUNK_SIZE);

                return pop();
            }

            return poems;
        }


        private void authorData(String poem) {
            List<String> poems = Arrays.asList(poem.split(";"));

            String author = getAuthor(poems, poem);

            List<String> list = authorPoems.get(author);
            if (list == null) {
                list = new ArrayList<>();
                authorPoems.put(author, list);
            }
            list.add(poem);
        }


    }

    static class CirclePanel extends JPanel {

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
            }


            panel.add(poemItem);

            list.add(panel);
        }

        return list;
    }

    private JFrame frame;
    private MyDrawPanel drawPanel;

    private Box colorBar;
    private JComponent poem;
    private JPanel nextStatus;


    public void show() {

        frame = new JFrame();
        if (isWindows()) {
            frame.setIconImage(new ImageIcon(getClass().getResource("/images/book.png")).getImage());
        }

        if (isMacOs()) {
        }

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem select = new JMenuItem("选择");
        JMenuItem reset = new JMenuItem("重置");

        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectAuthor.clear();
                db.source.clear();
            }
        });

        select.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JDialog jDialog = new JDialog();
                jDialog.setSize(600, 500);
                jDialog.setLayout(new GridLayout(0, 6));
                for (String item : author) {
                    JToggleButton selectBtn = new JToggleButton(item);
                    if (selectAuthor.contains(item)) {
                        selectBtn.setSelected(true);
                    }
                    selectBtn.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            selectAuthor.add(item);
                            db.source.addAll(authorPoems.get(item));
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


        JComponent content = Box.createVerticalBox();


        //clock
        drawPanel = new MyDrawPanel(Color.BLACK);
        drawPanel.setPreferredSize(new Dimension(200, 200));
        content.add(drawPanel);


        //color bar
        colorBar = Box.createHorizontalBox();
        content.add(colorBar);


        //poem
        poem = Box.createVerticalBox();
        content.add(poem);
        poem.setComponentPopupMenu(popupMenu);


        List<String> pop = db.pop();

        nextStatus = new JPanel();
        nextStatus.setBackground(Color.RED);
        nextStatus.setPreferredSize(new Dimension(-1, 1));
        colorBar.add(nextStatus);

        if (db.cacheSize() > 0) {

            for (int i = 0; i < db.cacheSize(); i++) {
                JPanel rest = new JPanel();
                rest.setBackground(Color.BLACK);
                rest.setPreferredSize(new Dimension(-1, 1));
                colorBar.add(rest);
            }
        }
        java.util.List<JPanel> jPanels = buildPoemItem(pop);
        for (JPanel jPanel : jPanels) {
            poem.add(jPanel);
        }


        poem.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                mouseHover = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseHover = false;
            }

            @Override
            public void mouseClicked(MouseEvent e) {

                if (e.getClickCount() == 2) {
                    if (zoomDialog == null) {
                        zoomDialog = new ZoomDialog(db.current, ClockAndPoem.this);
                    } else {
                        zoomDialog.refresh(db.current);
                    }
                }
            }
        });

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

        content.getInputMap().put(KeyStroke.getKeyStroke("RIGHT"),
                "refreshPoem");

        content.getInputMap().put(KeyStroke.getKeyStroke("LEFT"),
                "lastPoem");

        content.getInputMap().put(KeyStroke.getKeyStroke("ENTER"),
                "openPoem");


        content.getActionMap().put("refreshPoem",
                new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        refreshPoem(false, false);
                    }
                });

        content.getActionMap().put("lastPoem",
                new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        refreshPoem(true, false);
                    }
                });

        content.getActionMap().put("openPoem",
                new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        new ZoomDialog(db.current, ClockAndPoem.this);
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

                if (e.getClickCount() == 2) {
                    Point location = frame.getLocation();
                    if (location.x > screenSize.getWidth() / 2) {
                        frame.setLocation(0, 0);
                        frame.revalidate();
                    } else {
                        frame.setLocation(screenSize.width - Double.valueOf(frame.getSize().getWidth()).intValue(), 0);
                        frame.revalidate();
                    }
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

            if (timeRecorder > FREQ && timeRecorder % FREQ == 0 && !mouseHover) {

                refreshPoem(false, false);

                if (zoomDialog != null) {
                    zoomDialog.refresh(db.current);
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


    public void hidePoem() {
        poem.setVisible(false);
        colorBar.setVisible(false);
        frame.pack();
    }

    public void showPoem() {
        poem.setVisible(true);
        colorBar.setVisible(true);
        frame.pack();
    }

    public void clearZoom() {
        zoomDialog = null;
    }


    public void refreshPoem(boolean history, boolean onlyPop) {

        List<String> items = history ? db.popHistory() : db.pop();

        if (onlyPop) {
            return;
        }

        poem.removeAll();

        List<JPanel> poemItems = buildPoemItem(items);

        colorBar.removeAll();
        colorBar.add(nextStatus);

        if (db.cacheSize() > 0) {

            for (int i = 0; i < db.cacheSize(); i++) {
                JPanel rest = new JPanel();
                rest.setBackground(Color.BLACK);
                rest.setPreferredSize(new Dimension(-1, 1));
                colorBar.add(rest);
            }
        }

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
                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
                g2d.drawString(Integer.toString(j), x, y);
            }

            AffineTransform old = g2d.getTransform();

            g2d.setColor(Color.BLACK);


            //时钟的60个刻度
            for (int i = 0; i < 60; i++) {
                int w = i % 5 == 0 ? 5 : 3;
                //g2d.fillRect(xCenter - 2, 28, w, 3);
                //g2d.rotate(Math.toRadians(6), xCenter, yCenter);
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
            g2d.drawString(df2.format(calendar.getTime()), xCenter - 20, yCenter + 50);


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
