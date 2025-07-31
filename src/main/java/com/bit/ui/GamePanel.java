package com.bit.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

//这个类里的内容越写越多，由于理解不深所以整理困难QAQ
class GamePanel extends JPanel implements KeyListener {
    private final int WIDTH = 10;
    private final int HEIGHT = 20;
    private final int BLOCK_SIZE = 25;
    private final int BASE_SPEED = 500;
    private final int[][] board = new int[HEIGHT][WIDTH];
    private Shape currentShape;
    private Shape nextShape;
    private javax.swing.Timer gameTimer;
    private final Tetris tetris;
    private boolean isGameOver;
    private long startTime;
    private int currentSpeedLevel = 1;
    private int score = 0;
    private final int[] powerups = new int[3];
    private long pausedTime = 0; // 累计暂停时间
    private long pauseStartTime = 0; // 单次暂停开始时间

    private boolean isSelectingShape = false;
    private int selectedShapeIndex = 0;
    private final List<int[][]> shapeTemplates = new ArrayList<>();
    private boolean isSelectingArea = false;
    private int selectionX = 0, selectionY = 0;

    // 1.初始化与配置
    public GamePanel(Tetris tetris) {
        this.tetris = tetris;
        setPreferredSize(new Dimension(WIDTH * BLOCK_SIZE, HEIGHT * BLOCK_SIZE));
        setBackground(new Color(20, 20, 20));
        addKeyListener(this);
        setFocusable(true);
        initShapeTemplates();
//        //调试时初始化道具数量都为1
//        generatePowerup(0);
//        generatePowerup(1);
//        generatePowerup(2);
        initGame();
    }
    private void initGame() {
        startTime = System.currentTimeMillis();
        spawnNewShape();
        startGameTimer();
    }
    private void initShapeTemplates() {
        // 添加不同形状模板
        shapeTemplates.add(new int[][]{{1, 1}, {1, 1}}); // O形
        shapeTemplates.add(new int[][]{{0, 1, 0}, {1, 1, 1}}); // T型
        shapeTemplates.add(new int[][]{{1, 1, 1, 1}}); // I型
        shapeTemplates.add(new int[][]{{1,0},{1,0},{1,1}}); // L型
        shapeTemplates.add(new int[][]{{0,1},{0,1},{1,1}}); // J型
        shapeTemplates.add(new int[][]{{1,1,0},{0,1,1}}); // S型
        shapeTemplates.add(new int[][]{{0,1,1},{1,1,0}}); // Z型
    }
    private void startGameTimer() {
        gameTimer = new javax.swing.Timer(BASE_SPEED, e -> gameLoop());
        gameTimer.start();
    }

    // 2.游戏循环与状态管理
    private void gameLoop() {
        if (!moveDown()) {
            mergeShape();
            checkLines();
            if (shouldGameOver()) {
                gameOver();
            } else {
                spawnNewShape();
            }

        }
        updateTimeDisplay();
        updateGameSpeed();
        repaint();
    }
    private void gameOver() {
        gameTimer.stop();
        tetris.stopGameTimer();
        isGameOver = true;
        int finalScore = calculateFinalScore();
        long elapsed = (System.currentTimeMillis() - startTime - pausedTime)/1000;
        DatabaseManager.saveScore(finalScore, elapsed);
        tetris.showGameOver(finalScore);
    }
    private boolean spawnNewShape() {
        currentShape = new Shape();

        if (checkCollision(0, 0)) {
            return false;
        }
        return true;
    }

    private boolean shouldGameOver() {
        // 检查当前形状是否有一部分无法进入游戏区域
        int[][] shape = currentShape.getShape();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[0].length; j++) {
                if (currentShape.getBlock(i, j) != 0) {
                    int yPos = currentShape.y + i;
                    // 如果形状的任何部分在游戏区域外
                    if (yPos < 0) return true;
                }
            }
        }
        return false;
    }
    // 游戏速度调整
    private void updateGameSpeed() {
        int finalSpeedLevel = 6;
        if (currentSpeedLevel < finalSpeedLevel) {
            currentSpeedLevel = 1 + score / 1000;
        }
        gameTimer.setDelay(BASE_SPEED / (int)(0.2 * currentSpeedLevel + 0.8));
        tetris.updateSpeed(currentSpeedLevel);
    }
    private void updateScore(int lines) {
        int last_score = score;
        score += lines * 100 * currentSpeedLevel;

        // 添加音乐切换逻辑
        if(last_score < 5000 && score >= 5000) {
            MusicManager.playGameMusic2();
        }

        // 每得1000分随机获得一个道具
        tetris.updateScore(score);
        int last_up = last_score / 1000;
        int up = score / 1000;
        for (int i = last_up; i < up; i++) {
            generatePowerup();
        }
    }
    private void updateTimeDisplay() {
        long elapsed = (System.currentTimeMillis() - startTime - pausedTime) / 1000;
        tetris.updateTime(elapsed);
    }
    // 计算最终得分
    private int calculateFinalScore() {
        // 依据游戏时间有一定的得分加成
        long timeBonus = (System.currentTimeMillis() - startTime - pausedTime) / 1000 ;
        return score + (int) timeBonus;
    }

    // 3.移动与碰撞检测
    private boolean moveDown() {
        if (!checkCollision(0, 1)) {
            currentShape.y++;
            return true;
        }
        return false;
    }
    // 增强的碰撞检测，此处由AI辅助完成
    private boolean checkCollision(int dx, int dy) {
        int[][] shape = currentShape.getShape();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[0].length; j++) {
                if (currentShape.getBlock(i, j) != 0) {
                    int x = currentShape.x + j + dx;
                    int y = currentShape.y + i + dy;

                    if (x < 0 || x >= WIDTH) return true;
                    if (y >= HEIGHT) return true;
                    if (y >= 0 && board[y][x] != 0) return true;
                }
            }
        }
        return false;
    }
    private void rotateShape() {
        currentShape.rotate();
        if (checkCollision(0, 0)) {
            currentShape.rotateBack();
        }
    }
    private void hardDrop() {
        while(moveDown()); // 循环直到无法移动
        mergeShape();
        checkLines();
        if (shouldGameOver()) {
            gameOver();
        } else {
            spawnNewShape();
        }
        repaint();
    }

    // 4.消除行逻辑
    private void checkLines() {
        int linesCleared = 0;
        for (int i = HEIGHT - 1; i >= 0; i--) {
            while (isLineFull(i)) {
                removeLine(i);
                linesCleared++;
            }
        }

        if (linesCleared > 0) {
            updateScore(linesCleared);
            updateGameSpeed();
        }
    }
    // 检查是否整行已满
    private boolean isLineFull(int line) {
        for (int j = 0; j < WIDTH; j++) {
            if (board[line][j] == 0) return false;
        }
        return true;
    }
    // 移除整行并下移
    private void removeLine(int line) {
        for (int i = line; i > 0; i--) {
            System.arraycopy(board[i - 1], 0, board[i], 0, WIDTH);
        }
        Arrays.fill(board[0], 0);
    }

    // 5.技能系统
    //产生技能
    private void generatePowerup() {
        int typeIndex = (int) (Math.random() * 3);
        powerups[typeIndex]++;
        tetris.updatePowerups(Tetris.PowerupType.values()[typeIndex], powerups[typeIndex]);
    }
    //调试时用到的添加指定技能
    private void generatePowerup(int typeIndex){
        powerups[typeIndex]++;
        tetris.updatePowerups(Tetris.PowerupType.values()[typeIndex], powerups[typeIndex]);
    }
    private void usePowerup(Tetris.PowerupType type) {
        if (powerups[type.ordinal()] > 0) {
            powerups[type.ordinal()]--;
            tetris.updatePowerups(type, powerups[type.ordinal()]);

            switch (type) {
                case BOMB:
                    clearSelectedArea();
                    break;
                case LINE_CLEAR:
                    clearBottomLine();
                    break;
                case SHAPE_CHANGE:
                    changeCurrentShape();
                    break;
            }
        }
    }
    // 技能1：手动选择3x3区域
    private void clearSelectedArea() {
        isSelectingArea = true;
        gameTimer.stop();
        tetris.stopGameTimer();
        pauseStartTime = System.currentTimeMillis();
        selectionX = WIDTH / 2 - 1;
        selectionY = HEIGHT / 2 - 1;
        repaint();
    }
    // 技能2：消除底行并整体下移
    private void clearBottomLine() {
        // 清除最底行后，所有方块下移
        for (int i = HEIGHT - 1; i > 0; i--) {
            System.arraycopy(board[i - 1], 0, board[i], 0, WIDTH);
        }
        Arrays.fill(board[0], 0);
    }
    // 技能3：形状选择界面
    private void changeCurrentShape() {
        isSelectingShape = true;
        gameTimer.stop();
        tetris.stopGameTimer();
        pauseStartTime = System.currentTimeMillis();
        repaint();
    }

    // 6.绘制逻辑
    // 将落到底部的形状固定
    private void mergeShape() {
        //此方法由AI辅助完成
        int[][] shape = currentShape.getShape();
        Color color = currentShape.color;

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[0].length; j++) {
                if (currentShape.getBlock(i, j) != 0) {
                    int x = currentShape.x + j;
                    int y = currentShape.y + i;
                    if (y >= 0) board[y][x] = color.getRGB();
                }
            }
        }
    }
    @Override // 此方法由AI辅助完成
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGameBoard(g);
        drawCurrentShape(g);

        if (isSelectingShape) {
            drawShapeSelection(g);
        } else if (isSelectingArea) {
            drawAreaSelection(g);
        }

        if (isGameOver) drawGameOver(g);
    }
    private void drawGameBoard(Graphics g) {
        // 绘制堆积方块
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                if (board[i][j] != 0) {
                    drawBlock(g, j * BLOCK_SIZE, i * BLOCK_SIZE, new Color(board[i][j]));
                }
            }
        }

        // 绘制网格线
        g.setColor(new Color(60, 60, 60));
        for (int i = 0; i <= HEIGHT; i++) {
            g.drawLine(0, i * BLOCK_SIZE, WIDTH * BLOCK_SIZE, i * BLOCK_SIZE);
        }
        for (int j = 0; j <= WIDTH; j++) {
            g.drawLine(j * BLOCK_SIZE, 0, j * BLOCK_SIZE, HEIGHT * BLOCK_SIZE);
        }
    }
    private void drawCurrentShape(Graphics g) {
        int[][] shape = currentShape.getShape();
        Color color = currentShape.color;

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[0].length; j++) {
                if (shape[i][j] != 0) {
                    int x = (currentShape.x + j) * BLOCK_SIZE;
                    int y = (currentShape.y + i) * BLOCK_SIZE;
                    drawBlock(g, x, y, color);
                }
            }
        }
    }
    private void drawBlock(Graphics g, int x, int y, Color color) {
        // 3D风格的方块绘制
        g.setColor(color);
        g.fillRect(x + 1, y + 1, BLOCK_SIZE - 2, BLOCK_SIZE - 2);

        // 高光效果
        g.setColor(color.brighter());
        g.drawLine(x + 1, y + 1, x + BLOCK_SIZE - 2, y + 1);
        g.drawLine(x + 1, y + 1, x + 1, y + BLOCK_SIZE - 2);

        // 阴影效果
        g.setColor(color.darker());
        g.drawLine(x + BLOCK_SIZE - 2, y + 1, x + BLOCK_SIZE - 2, y + BLOCK_SIZE - 2);
        g.drawLine(x + 1, y + BLOCK_SIZE - 2, x + BLOCK_SIZE - 2, y + BLOCK_SIZE - 2);
    }
    private void drawGameOver(Graphics g) {
        g.setColor(new Color(255, 50, 50, 150));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.WHITE);
        g.setFont(new Font("Press Start 2P", Font.BOLD, 24));
        String text = "GAME OVER";
        int textWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, (getWidth() - textWidth) / 2, getHeight() / 2);
    }
    // 技能3中选择目标图形的方法
    private void drawShapeSelection(Graphics g) {
        // 绘制半透明背景
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, getWidth(), getHeight());

        // 绘制可选形状
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("← → + ENTER", 50, 50);

        int[][] selectedShape = shapeTemplates.get(selectedShapeIndex);
        drawPreviewShape(g, selectedShape, 100, 100);
    }
    // 技能3中绘制预览形状
    private void drawPreviewShape(Graphics g, int[][] shapeTemplate, int posX, int posY) {
        // 计算预览形状的尺寸（缩小显示）
        int previewBlockSize = BLOCK_SIZE / 2;
        Color previewColor = Color.CYAN; // 使用固定颜色便于识别

        // 绘制形状模板
        for (int i = 0; i < shapeTemplate.length; i++) {
            for (int j = 0; j < shapeTemplate[0].length; j++) {
                if (shapeTemplate[i][j] != 0) {
                    int x = posX + j * previewBlockSize;
                    int y = posY + i * previewBlockSize;
                    // 绘制简化版方块
                    g.setColor(previewColor);
                    g.fillRect(x, y, previewBlockSize - 1, previewBlockSize - 1);
                    // 绘制边框
                    g.setColor(previewColor.darker());
                    g.drawRect(x, y, previewBlockSize - 1, previewBlockSize - 1);
                }
            }
        }

        // 绘制选择指示器
        g.setColor(Color.YELLOW);
        g.drawRect(posX - 5, posY - 5,
                shapeTemplate[0].length * previewBlockSize + 10,
                shapeTemplate.length * previewBlockSize + 10);
    }
    // 技能1中选择目标区域的方法
    private void drawAreaSelection(Graphics g) {
        // 绘制选择框
        g.setColor(Color.RED);
        int x = selectionX * BLOCK_SIZE;
        int y = selectionY * BLOCK_SIZE;
        g.drawRect(x, y, 3 * BLOCK_SIZE, 3 * BLOCK_SIZE);
    }


    // 7.键位映射
    @Override
    public void keyPressed(KeyEvent e) {
        if (isGameOver) return;

        if (isSelectingShape) {// 是否在选择形状
            handleShapeSelection(e);
        } else if (isSelectingArea) {// 是否在选择区域
            handleAreaSelection(e);
        } else {// 游戏正常进行
            handleGameControls(e);
        }
        repaint();
    }
    private void handleShapeSelection(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                selectedShapeIndex = (selectedShapeIndex - 1 + shapeTemplates.size()) % shapeTemplates.size();
                break;
            case KeyEvent.VK_RIGHT:
                selectedShapeIndex = (selectedShapeIndex + 1) % shapeTemplates.size();
                break;
            case KeyEvent.VK_ENTER:
                currentShape = new Shape(shapeTemplates.get(selectedShapeIndex));
                isSelectingShape = false;
                gameTimer.start();
                tetris.continueGameTimer();
                pausedTime += System.currentTimeMillis() - pauseStartTime;
                // 调整位置防止碰撞
                while (checkCollision(0, 0)) currentShape.x++;
                break;
        }
    }
    private void handleAreaSelection(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                if (selectionX > 0) selectionX--;
                break;
            case KeyEvent.VK_RIGHT:
                if (selectionX < WIDTH - 3) selectionX++;
                break;
            case KeyEvent.VK_UP:
                if (selectionY > 0) selectionY--;
                break;
            case KeyEvent.VK_DOWN:
                if (selectionY < HEIGHT - 3) selectionY++;
                break;
            case KeyEvent.VK_ENTER:
                // 清除选定区域
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        int x = selectionX + j;
                        int y = selectionY + i;
                        if (x < WIDTH && y < HEIGHT) {
                            board[y][x] = 0;
                        }
                    }
                }
                isSelectingArea = false;
                gameTimer.start();
                tetris.continueGameTimer();
                pausedTime += System.currentTimeMillis() - pauseStartTime;
                break;
        }
    }
    private void handleGameControls(KeyEvent e) {
        if (isGameOver) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                if (!checkCollision(-1, 0)) currentShape.x--;
                break;
            case KeyEvent.VK_RIGHT:
                if (!checkCollision(1, 0)) currentShape.x++;
                break;
            case KeyEvent.VK_DOWN:
                moveDown();
                break;
            case KeyEvent.VK_UP:
                rotateShape();
                break;
            case KeyEvent.VK_ENTER:
                if (!isSelectingShape && !isSelectingArea) {
                    hardDrop();
                }
                break;
            case KeyEvent.VK_1:
                usePowerup(Tetris.PowerupType.BOMB);
                break;
            case KeyEvent.VK_2:
                usePowerup(Tetris.PowerupType.LINE_CLEAR);
                break;
            case KeyEvent.VK_3:
                usePowerup(Tetris.PowerupType.SHAPE_CHANGE);
                break;
        }
        repaint();
    }

    // 8.其他
    public long getpausedTime() {
        return pausedTime;
    }
    @Override
    public void keyTyped(KeyEvent e) {
    }
    @Override
    public void keyReleased(KeyEvent e) {
    }

}