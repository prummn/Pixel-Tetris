package com.bit.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class Tetris extends JFrame {
    private final GamePanel game;
    private Font pixelFont;
    private Timer gameTimer;
    private long startTime;
    private InfoPanel scorePanel;
    private PowerupPanel powerupPanel;
    private InfoPanel timePanel;
    private InfoPanel speedPanel;
    public enum PowerupType { BOMB, LINE_CLEAR, SHAPE_CHANGE }
    private final StartMenu menu;

    // 构造函数，接收 StartMenu 实例
    public Tetris(StartMenu menu) {
        setTitle("Pixel Tetris");
        setSize(450, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0,0));

        this.menu = menu;

        // 初始化像素字体
        initPixelFont();
        // 侧边栏布局
        JPanel sidePanel = createSidePanel();
        add(sidePanel, BorderLayout.EAST);

        game = new GamePanel(this);
        add(game, BorderLayout.CENTER);
        setLocationRelativeTo(null);
        startGameTimer();
        MusicManager.playGameMusic1(); // 开始游戏音乐

        setVisible(true);
    }

    // 初始化字体
    private void initPixelFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/PressStart2P.ttf");
            if (is == null) {
                System.err.println("错误：字体文件未找到！");
            } else {
                System.out.println("字体文件加载成功！");
            }
            pixelFont = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(pixelFont);
        } catch (Exception e) {
            pixelFont = new Font("Courier New", Font.BOLD, 14);
        }
    }

    // 创建右侧面板
    private JPanel createSidePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(180, 0));

        // 创建信息面板
        scorePanel = new InfoPanel("SCORE", "0", pixelFont, Color.GREEN);
        powerupPanel = new PowerupPanel(pixelFont);
        timePanel = new InfoPanel("TIME", "00:00", pixelFont, Color.CYAN);
        speedPanel = new InfoPanel("SPEED", "LV.1", pixelFont, Color.ORANGE);

        panel.add(Box.createVerticalStrut(20));
        panel.add(scorePanel);
        panel.add(Box.createVerticalStrut(30));
        panel.add(powerupPanel);
        panel.add(Box.createVerticalStrut(30));
        panel.add(timePanel);
        panel.add(Box.createVerticalStrut(30));
        panel.add(speedPanel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    // 启动游戏计时器
    private void startGameTimer() {
        startTime = System.currentTimeMillis();
        gameTimer = new Timer(1000, e -> {
            long elapsed = (System.currentTimeMillis() - startTime-game.getpausedTime()) / 1000;
            String time = String.format("%02d:%02d", elapsed/60, elapsed%60);
            timePanel.updateValue(time);
        });
        gameTimer.start();
    }
    // 暂停游戏计时器
    public void stopGameTimer() {
        if(gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
    }
    // 继续游戏计时器
    public void continueGameTimer() {
        if(gameTimer!= null && !gameTimer.isRunning()) {
            gameTimer.start();
        }
    }

    // 更新分数显示
    public void updateScore(int score) {
        scorePanel.updateValue(String.valueOf(score));
    }
    // 更新道具数量
    public void updatePowerups(PowerupType type, int count) {
        powerupPanel.updateCount(type, count);
    }
    // 更新时间显示
    public void updateTime(long seconds) {
        String time = String.format("%02d:%02d", seconds/60, seconds%60);
        timePanel.updateValue(time);
    }
    // 更新速度等级
    public void updateSpeed(int level) {
        speedPanel.updateValue("LV." + level);
    }

    // 显示游戏结束界面
    //此处用AI生成
    public void showGameOver(int finalScore) {
        MusicManager.playGameOverMusic();
        Object[] options = {"Restart", "Main Menu"};
        int choice = JOptionPane.showOptionDialog(
                this, // 父组件
                "Final Score: " + finalScore, // 显示内容
                "Game Over", // 对话框标题
                JOptionPane.DEFAULT_OPTION, // 选项类型
                JOptionPane.PLAIN_MESSAGE, // 消息类型（无图标）
                null, // 图标（无）
                options, // 按钮选项数组
                options[0] // 默认选中项
        );

        if(choice == 0) {
            MusicManager.stopAll();
            new Tetris(this.menu);
            dispose();
        } else {
            MusicManager.stopAll();
            menu.setVisible(true);  // 恢复菜单
            dispose();
        }
    }

    // 信息面板组件
    class InfoPanel extends JPanel {
        private JLabel valueLabel;

        InfoPanel(String title, String initValue, Font font, Color color) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(new Color(45, 45, 45));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color.darker(), 2),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(pixelFont.deriveFont(10f));
            titleLabel.setForeground(color);
            titleLabel.setAlignmentX(0.5f);

            valueLabel = new JLabel(initValue);
            valueLabel.setFont(pixelFont.deriveFont(16f));
            valueLabel.setForeground(Color.WHITE);

            add(titleLabel);
            add(Box.createVerticalStrut(5));
            add(valueLabel);
        }

        void updateValue(String value) {
            valueLabel.setText(value);
        }
        public String getValue() {
            return valueLabel.getText();
        }


    }
    // 道具面板组件
    static class PowerupPanel extends JPanel {
        private final JLabel[] countLabels;
        private final ImageIcon[] icons;

        PowerupPanel(Font font) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(new Color(45, 45, 45));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.MAGENTA.darker(), 2),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));

            // 加载图标资源
            icons = new ImageIcon[]{
                    createScaledIcon("/images/bomb.jpeg"),
                    createScaledIcon("/images/light.jpeg"),
                    createScaledIcon("/images/circle.jpeg")
            };

            JLabel titleLabel = new JLabel("POWERUPS");
            titleLabel.setFont(font.deriveFont(10f));
            titleLabel.setForeground(Color.MAGENTA);
            titleLabel.setAlignmentX(0.5f);

            add(titleLabel);
            add(Box.createVerticalStrut(15));

            // 初始化道具项
            countLabels = new JLabel[3];
            add(createPowerupItem(PowerupType.BOMB, font));
            add(Box.createVerticalStrut(10));
            add(createPowerupItem(PowerupType.LINE_CLEAR, font));
            add(Box.createVerticalStrut(10));
            add(createPowerupItem(PowerupType.SHAPE_CHANGE, font));
        }


        private ImageIcon createScaledIcon(String path) {
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is == null) {
                    System.err.println("无法加载资源: " + path);
                    return new ImageIcon();
                }
                Image image = ImageIO.read(is); // 使用 ImageIO 读取
                return new ImageIcon(image.getScaledInstance(42, 42, Image.SCALE_SMOOTH));
            } catch (IOException e) {
                e.printStackTrace();
                return new ImageIcon();
            }
        }

        private JPanel createPowerupItem(PowerupType type, Font font) {
            JPanel panel = new JPanel(new BorderLayout(10, 0));
            panel.setBackground(new Color(60, 60, 60));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            // 图标标签
            JLabel iconLabel = new JLabel(icons[type.ordinal()]);
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
            iconLabel.setToolTipText(getPowerupDescription(type));

            // 数量标签
            JLabel countLabel = new JLabel("0");
            countLabel.setFont(font.deriveFont(14f));
            countLabel.setForeground(Color.YELLOW);
            countLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            countLabels[type.ordinal()] = countLabel;

            // 布局组件
            panel.add(iconLabel, BorderLayout.WEST);
            panel.add(countLabel, BorderLayout.CENTER);

            return panel;
        }

        private String getPowerupDescription(PowerupType type) {
            switch (type) {
                case BOMB: return "Clear 3x3 Area (Press 1)";
                case LINE_CLEAR: return "Clear Bottom Line (Press 2)";
                case SHAPE_CHANGE: return "Change Shape (Press 3)";
                default: return "";
            }
        }

        void updateCount(PowerupType type, int count) {
            countLabels[type.ordinal()].setText("×" + count);
        }
    }

}