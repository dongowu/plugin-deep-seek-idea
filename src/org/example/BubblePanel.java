package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class BubblePanel extends JPanel {
    private final Color backgroundColor;
    private final boolean isUser;

    public BubblePanel(String text, boolean isUser) {
        this.backgroundColor = isUser ? new Color(0, 120, 215) : new Color(229, 229, 229);
        this.isUser = isUser;

        setLayout(new BorderLayout());
        setOpaque(false); // 设置透明背景

        // 使用 JTextArea 支持多行文本
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Arial", Font.PLAIN, 12));
        textArea.setForeground(isUser ? Color.WHITE : Color.BLACK);
        textArea.setBackground(backgroundColor);
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        add(textArea, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制阴影
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillRoundRect(2, 2, getWidth(), getHeight(), 20, 20);

        // 绘制圆角矩形
        int arc = 20; // 圆角半径
        int width = getWidth();
        int height = getHeight();

        RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, 0, width, height, arc, arc);
        g2d.setColor(backgroundColor);
        g2d.fill(roundedRectangle);

        // 绘制三角形箭头
        if (isUser) {
            int[] xPoints = {width, width - 10, width}; // 右箭头
            int[] yPoints = {height / 2 - 5, height / 2, height / 2 + 5};
            g2d.fillPolygon(xPoints, yPoints, 3);
        } else {
            int[] xPoints = {0, 10, 0}; // 左箭头
            int[] yPoints = {height / 2 - 5, height / 2, height / 2 + 5};
            g2d.fillPolygon(xPoints, yPoints, 3);
        }

        g2d.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.width = Math.min(size.width, 300); // 限制气泡最大宽度
        size.height = Math.max(size.height, 50); // 设置最小高度
        return size;
    }
}