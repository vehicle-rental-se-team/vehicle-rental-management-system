package com.vehiclerental.presentation.gui;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public final class UiTheme {

    public static final Color BACKGROUND = new Color(8, 20, 40);
    public static final Color SURFACE = new Color(14, 30, 56);
    public static final Color SURFACE_ALT = new Color(20, 40, 72);

    public static final Color PRIMARY = new Color(212, 175, 55);
    public static final Color PRIMARY_DARK = new Color(160, 125, 30);
    public static final Color PRIMARY_LIGHT = new Color(245, 210, 90);

    public static final Color SIDEBAR = new Color(5, 14, 30);
    public static final Color SIDEBAR_ACTIVE = new Color(23, 45, 82);

    public static final Color TEXT = new Color(238, 247, 255);
    public static final Color MUTED = new Color(175, 198, 225);
    public static final Color BORDER = new Color(55, 85, 130);
    public static final Color SUCCESS = new Color(75, 180, 110);
    public static final Color ERROR = new Color(210, 85, 85);

    private UiTheme() {
    }

    public static void applyGlobalTheme() {
        UIManager.put("Button.select", PRIMARY_LIGHT);
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("OptionPane.border", BorderFactory.createLineBorder(BORDER));
        UIManager.put("Label.foreground", TEXT);
    }

    public static void stylePrimaryButton(JButton button) {
        resetButtonLook(button);

        button.setBackground(PRIMARY);
        button.setForeground(BACKGROUND);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_LIGHT, 1),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        addButtonAnimation(button, PRIMARY, PRIMARY_LIGHT, BACKGROUND, BACKGROUND);
    }

    public static void styleSecondaryButton(JButton button) {
        resetButtonLook(button);

        button.setBackground(SURFACE_ALT);
        button.setForeground(TEXT);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY, 1),
                BorderFactory.createEmptyBorder(9, 15, 9, 15)
        ));

        addButtonAnimation(button, SURFACE_ALT, new Color(30, 55, 95), TEXT, PRIMARY_LIGHT);
    }

    public static void styleSidebarButton(JButton button) {
        resetButtonLook(button);

        button.setBackground(SIDEBAR);
        button.setForeground(TEXT);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(12, 28, 55), 1),
                BorderFactory.createEmptyBorder(13, 17, 13, 17)
        ));

        addButtonAnimation(button, SIDEBAR, new Color(20, 38, 70), TEXT, PRIMARY_LIGHT);
    }

    public static void styleActiveSidebarButton(JButton button) {
        resetButtonLook(button);

        button.setBackground(SIDEBAR_ACTIVE);
        button.setForeground(PRIMARY_LIGHT);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, PRIMARY),
                BorderFactory.createEmptyBorder(13, 13, 13, 17)
        ));
    }

    public static void styleTextField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBackground(new Color(10, 24, 46));
        field.setForeground(TEXT);
        field.setCaretColor(PRIMARY_LIGHT);
        field.setOpaque(true);
        field.setBorder(compoundInputBorder());
    }

    public static void stylePasswordField(JPasswordField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBackground(new Color(10, 24, 46));
        field.setForeground(TEXT);
        field.setCaretColor(PRIMARY_LIGHT);
        field.setOpaque(true);
        field.setBorder(compoundInputBorder());
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(36);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setBackground(new Color(11, 24, 45));
        table.setForeground(TEXT);
        table.setSelectionBackground(new Color(35, 61, 102));
        table.setSelectionForeground(TEXT);
        table.setGridColor(new Color(40, 65, 100));
        table.setShowVerticalLines(false);
        table.setOpaque(true);

        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(23, 42, 72));
        table.getTableHeader().setForeground(PRIMARY_LIGHT);
        table.getTableHeader().setOpaque(true);
    }

    public static JLabel titleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel subtitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(MUTED);
        return label;
    }

    public static JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(SURFACE);
        panel.setOpaque(true);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        return panel;
    }

    public static JLabel logoImageLabel(String path, int width, int height) {
        URL imageUrl = UiTheme.class.getResource(path);

        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(width, height));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setOpaque(false);

        if (imageUrl == null) {
            label.setText("LOGO");
            label.setForeground(PRIMARY_LIGHT);
            label.setFont(new Font("SansSerif", Font.BOLD, 15));
            return label;
        }

        ImageIcon icon = new ImageIcon(imageUrl);
        Image scaledImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        label.setIcon(new ImageIcon(scaledImage));

        return label;
    }

    public static void styleLabelAsBrand(JLabel label) {
        label.setForeground(TEXT);
        label.setFont(new Font("SansSerif", Font.BOLD, 21));
    }

    public static void styleMutedLabel(JLabel label) {
        label.setForeground(MUTED);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
    }

    private static void resetButtonLook(JButton button) {
        button.setUI(new BasicButtonUI());
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private static Border compoundInputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        );
    }

    private static void addButtonAnimation(
            JButton button,
            Color normalBackground,
            Color hoverBackground,
            Color normalForeground,
            Color hoverForeground
    ) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                animateButton(button, hoverBackground, hoverForeground);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                animateButton(button, normalBackground, normalForeground);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setLocation(button.getX(), button.getY() + 1);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setLocation(button.getX(), button.getY() - 1);
            }
        });
    }

    private static void animateButton(JButton button, Color targetBackground, Color targetForeground) {
        Color startBackground = button.getBackground();
        Color startForeground = button.getForeground();

        final int frames = 8;
        final int delay = 16;
        final int[] frame = {0};

        Timer timer = new Timer(delay, null);
        timer.addActionListener(e -> {
            frame[0]++;
            float ratio = frame[0] / (float) frames;

            button.setBackground(blend(startBackground, targetBackground, ratio));
            button.setForeground(blend(startForeground, targetForeground, ratio));

            if (frame[0] >= frames) {
                button.setBackground(targetBackground);
                button.setForeground(targetForeground);
                timer.stop();
            }
        });

        timer.start();
    }

    private static Color blend(Color start, Color end, float ratio) {
        int red = (int) (start.getRed() + ratio * (end.getRed() - start.getRed()));
        int green = (int) (start.getGreen() + ratio * (end.getGreen() - start.getGreen()));
        int blue = (int) (start.getBlue() + ratio * (end.getBlue() - start.getBlue()));
        return new Color(red, green, blue);
    }
}