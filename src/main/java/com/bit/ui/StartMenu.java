package com.bit.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

public class StartMenu extends JFrame {
    private Font pixelFont;

    // 初始化菜单界面
    public StartMenu() {
        setTitle("Pixel Tetris");
        setSize(450, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        initPixelFont();
        setupUI();
        setLocationRelativeTo(null);
        MusicManager.playStartMenuMusic();
        setVisible(true);
    }

    // 初始化像素字体
    private void initPixelFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/PressStart2P.ttf");
            if (is == null) {
                System.err.println("错误：字体文件未找到！请确认文件位于 resources 根目录。");
            } else {
                System.out.println("字体文件加载成功！");
                pixelFont = Font.createFont(Font.TRUETYPE_FONT, is);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(pixelFont);
                System.out.println("字体名称：" + pixelFont.getFontName());
            }
        } catch (FontFormatException e) {
            System.err.println("字体格式错误详情：" + e.getMessage());
            e.printStackTrace();
            pixelFont = new Font("Courier New", Font.BOLD, 14);
        } catch (IOException e) {
            System.err.println("IO 错误：无法读取字体文件");
            e.printStackTrace();
            pixelFont = new Font("Courier New", Font.BOLD, 14);
        } catch (Exception e) {
            e.printStackTrace();
            pixelFont = new Font("Courier New", Font.BOLD, 14);
        }
    }
    // 设置界面布局
    private void setupUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(30, 30, 30));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(80, 50, 80, 50));

        //添加标题
        JLabel title = new JLabel("PIXEL TETRIS");
        title.setFont(pixelFont.deriveFont(24f));
        title.setForeground(Color.CYAN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 添加按钮
        JButton startBtn = createButton("START GAME", Color.GREEN);
        JButton historyBtn = createButton("HISTORY", Color.ORANGE);
        JButton aboutBtn = createButton("ABOUT", Color.MAGENTA);

        startBtn.addActionListener(e -> {
            MusicManager.stopAll(); // 停止菜单音乐
            new Tetris(this);  // 传递当前菜单实例
            dispose();  // 关闭菜单窗口
        });

        historyBtn.addActionListener(e -> showHistoryDialog());
        aboutBtn.addActionListener(e -> showAboutDialog());

        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(80));
        mainPanel.add(startBtn);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(historyBtn);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(aboutBtn);

        add(mainPanel, BorderLayout.CENTER);
    }
    // 创建按钮
    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(pixelFont.deriveFont(14f));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color.darker());
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(8, 25, 8, 25)
        ));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT); // 确保按钮居中
        btn.setFocusPainted(false);

        return btn;
    }

    // 历史记录对话框
    // 由于技术原因此方法主要由AI完成
    private void showHistoryDialog() {
        JDialog dialog = new JDialog(this, "History", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 450);
        dialog.setLocationRelativeTo(this);

        List<String[]> historyData = DatabaseManager.getTopScores(5);
        if (historyData.isEmpty()) {
            historyData.add(new String[]{"0", "0"});
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 表头面板
        JLabel title = new JLabel("HISTORY RECORDS");
        title.setFont(pixelFont.deriveFont(16f));
        title.setForeground(Color.CYAN);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(title, BorderLayout.NORTH);

        // 表格内容容器
        JPanel contentContainer = new JPanel(new BorderLayout());

        // 列标题
        JPanel headerPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        headerPanel.setBackground(new Color(45, 45, 45));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
        Stream.of("RANK", "SCORE", "TIME").forEach(text -> {
            JLabel label = new JLabel(text);
            label.setFont(pixelFont.deriveFont(12f));
            label.setForeground(Color.CYAN);
            headerPanel.add(label);
        });

        // 数据行面板（使用BoxLayout并添加顶部弹性空间）
        // 以下显示历史记录高分部分使用AI辅助完成
        JPanel dataPanel = new JPanel();
        dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
        dataPanel.setBackground(new Color(30, 30, 30));

        int rank = 1;
        for (String[] record : historyData) {
            JPanel row = new JPanel(new GridLayout(1, 3, 20, 0));
            row.setBackground(new Color(45, 45, 45));
            row.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); // 固定行高

            Color rankColor = getRankColor(rank);

            // 时间格式化
            long totalSeconds = Long.parseLong(record[1]);
            String formattedTime = String.format("%02d:%02d",
                    (totalSeconds % 3600) / 60, totalSeconds % 60);

            // 三列数据
            addTableCell(row, "#" + rank, rankColor);
            addTableCell(row, record[0], rankColor);
            addTableCell(row, formattedTime, rankColor);

            dataPanel.add(row);

            // 添加分割线
            if (rank < historyData.size()) {
                dataPanel.add(new JSeparator() {{
                    setForeground(new Color(80, 80, 80));
                    setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                }});
            }
            rank++;
        }

        // 滚动面板
        JScrollPane scrollPane = new JScrollPane(dataPanel);
        scrollPane.setColumnHeaderView(headerPanel);
        scrollPane.getViewport().setBackground(new Color(30, 30, 30));
        scrollPane.setBorder(null);

        // 强制内容置顶（关键代码）
        SwingUtilities.invokeLater(() -> {
            JViewport viewport = scrollPane.getViewport();
            viewport.setViewPosition(new Point(0, 0));
            viewport.revalidate(); // 确保布局更新
        });

        contentContainer.add(scrollPane, BorderLayout.CENTER);
        panel.add(contentContainer, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 50, 0, 50));
        buttonPanel.setBackground(new Color(30, 30, 30));

        // Clear按钮
        JButton clearBtn = createButton("CLR", new Color(255, 165, 0));
        clearBtn.setMinimumSize(new Dimension(120, 40));
        clearBtn.addActionListener(e -> {
            DatabaseManager.clearAllScores();
            dialog.dispose();
            showHistoryDialog();
        });

        // Back按钮
        JButton backBtn = createButton("BACK", Color.RED);
        backBtn.setMinimumSize(new Dimension(120, 40));
        backBtn.addActionListener(e -> dialog.dispose());

        // 对称布局
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(clearBtn);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(backBtn);
        buttonPanel.add(Box.createHorizontalGlue());

        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    // 添加表格单元格
    private void addTableCell(JPanel panel, String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(pixelFont.deriveFont(12f));
        label.setForeground(color);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label);
    }

    // 辅助方法获取排名颜色
    private Color getRankColor(int rank) {
        switch (rank) {
            case 1: return new Color(255, 215, 0);
            case 2: return Color.LIGHT_GRAY;
            case 3: return new Color(205, 127, 50);
            case 4: return Color.GREEN;
            case 5: return Color.CYAN;
            default: return Color.WHITE;
        }
    }

    // 关于作者
    private void showAboutDialog() {
        JDialog dialog = new JDialog(this, "About", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 450);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // 标题面板
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(30, 30, 30));
        ImageIcon logoIcon = loadImageResource("/images/logo.png", 80, 80);
        // 打印图标信息
        System.out.println("图标宽度: " + logoIcon.getIconWidth());
        System.out.println("图标高度: " + logoIcon.getIconHeight());

        JLabel title = new JLabel(" PIXEL TETRIS  ", logoIcon, SwingConstants.CENTER);
        title.setFont(pixelFont.deriveFont(18f));
        title.setForeground(Color.MAGENTA);
        titlePanel.add(title);

        // 内容面板
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(30, 30, 30));

        addInfoRow(contentPanel, "Lead Developer", "Twit", Color.CYAN);
        addInfoRow(contentPanel, "QQ", "1503970774", Color.GREEN);
        addInfoRow(contentPanel, "Version", "1.0.2", Color.ORANGE);


        // 返回按钮
        JButton backBtn = createButton("BACK", Color.RED);
        backBtn.addActionListener(e -> dialog.dispose());

        panel.add(titlePanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(contentPanel);
        panel.add(Box.createVerticalStrut(30));
        panel.add(Box.createVerticalStrut(30));
        panel.add(backBtn);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private ImageIcon loadImageResource(String path, int width, int height) {
        try {
            // 使用 ImageIO 读取资源
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.err.println("[错误] 图片未找到: " + path);
                return new ImageIcon();
            }
            Image image = ImageIO.read(is);
            return new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            System.err.println("[错误] 加载图片失败: " + path);
            e.printStackTrace();
            return new ImageIcon();
        }
    }

    private void addInfoRow(JPanel parent, String label, String value, Color color) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setBackground(new Color(45, 45, 45));
        row.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel labelComp = new JLabel(label + ":");
        labelComp.setFont(pixelFont.deriveFont(10f));
        labelComp.setForeground(color);

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(pixelFont.deriveFont(10f));
        valueComp.setForeground(Color.WHITE);

        row.add(labelComp);
        row.add(valueComp);
        parent.add(row);
        parent.add(Box.createVerticalStrut(8));
    }
}
