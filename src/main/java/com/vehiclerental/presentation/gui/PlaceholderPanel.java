package com.vehiclerental.presentation.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagLayout;

public class PlaceholderPanel extends JPanel {

    public PlaceholderPanel(String title, String message) {
        setLayout(new GridBagLayout());
        setBackground(UiTheme.BACKGROUND);

        JPanel card = UiTheme.cardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JLabel titleLabel = UiTheme.titleLabel(title);
        JLabel messageLabel = UiTheme.subtitleLabel(message);
        JLabel statusLabel = new JLabel("Prepared for upcoming sprint");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        statusLabel.setForeground(UiTheme.SUCCESS);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(messageLabel, BorderLayout.CENTER);
        card.add(statusLabel, BorderLayout.SOUTH);

        add(card);
    }
}
