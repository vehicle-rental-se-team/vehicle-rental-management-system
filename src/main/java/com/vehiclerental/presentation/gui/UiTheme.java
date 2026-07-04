package com.vehiclerental.presentation.gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

public final class UiTheme {

    public static final Color BACKGROUND = new Color(245, 247, 251);
    public static final Color SURFACE = Color.WHITE;
    public static final Color PRIMARY = new Color(35, 86, 190);
    public static final Color PRIMARY_DARK = new Color(25, 62, 145);
    public static final Color SIDEBAR = new Color(21, 32, 54);
    public static final Color SIDEBAR_ACTIVE = new Color(35, 86, 190);
    public static final Color TEXT = new Color(35, 39, 47);
    public static final Color MUTED = new Color(110, 118, 130);
    public static final Color BORDER = new Color(220, 225, 235);
    public static final Color SUCCESS = new Color(32, 137, 82);

    private UiTheme() {
    }

    public static void stylePrimaryButton(JButton button) {
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void styleSecondaryButton(JButton button) {
        button.setBackground(SURFACE);
        button.setForeground(PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(PRIMARY));
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void styleSidebarButton(JButton button) {
        button.setBackground(SIDEBAR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        button.setHorizontalAlignment(JButton.LEFT);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void styleActiveSidebarButton(JButton button) {
        styleSidebarButton(button);
        button.setBackground(SIDEBAR_ACTIVE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
    }

    public static void styleTextField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(compoundInputBorder());
    }

    public static void stylePasswordField(JPasswordField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(compoundInputBorder());
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(34);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(235, 239, 247));
        table.getTableHeader().setForeground(TEXT);
        table.setSelectionBackground(new Color(220, 232, 255));
        table.setSelectionForeground(TEXT);
        table.setGridColor(new Color(232, 236, 244));
        table.setShowVerticalLines(false);
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
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        return panel;
    }

    private static Border compoundInputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        );
    }
}
