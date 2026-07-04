package com.vehiclerental.presentation.gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class LoginFrame extends JFrame {

    private final AppContext appContext;
    private final VehicleRentalApplication application;
    private final JTextField usernameField;
    private final JPasswordField passwordField;

    public LoginFrame(AppContext appContext, VehicleRentalApplication application) {
        this.appContext = appContext;
        this.application = application;
        this.usernameField = new JTextField();
        this.passwordField = new JPasswordField();

        setTitle("Vehicle Rental Management System - Login");
        setSize(980, 620);
        setMinimumSize(new Dimension(860, 540));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UiTheme.BACKGROUND);

        add(createBrandPanel(), BorderLayout.WEST);
        add(createLoginPanel(), BorderLayout.CENTER);
    }

    private JPanel createBrandPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(380, 620));
        panel.setBackground(UiTheme.SIDEBAR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 38, 8, 38);

        JLabel title = new JLabel("Vehicle Rental");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        panel.add(title, gbc);

        gbc.gridy++;
        JLabel subtitle = new JLabel("Management System");
        subtitle.setForeground(new Color(195, 205, 225));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 19));
        panel.add(subtitle, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(30, 38, 8, 38);
        JLabel sprint = new JLabel("Sprint 1 Ready");
        sprint.setOpaque(true);
        sprint.setBackground(UiTheme.PRIMARY);
        sprint.setForeground(Color.WHITE);
        sprint.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        sprint.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(sprint, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(16, 38, 0, 38);
        JLabel details = new JLabel("Login, logout, and available vehicle catalog.");
        details.setForeground(new Color(195, 205, 225));
        details.setFont(new Font("SansSerif", Font.PLAIN, 13));
        panel.add(details, gbc);

        return panel;
    }

    private JPanel createLoginPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(UiTheme.BACKGROUND);

        JPanel card = UiTheme.cardPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(430, 360));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 8, 4);

        card.add(UiTheme.titleLabel("Manager Login"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 4, 26, 4);
        card.add(UiTheme.subtitleLabel("Enter your manager credentials to continue."), gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 4, 8, 4);
        card.add(new JLabel("Username"), gbc);

        gbc.gridx = 1;
        UiTheme.styleTextField(usernameField);
        card.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        card.add(new JLabel("Password"), gbc);

        gbc.gridx = 1;
        UiTheme.stylePasswordField(passwordField);
        card.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(24, 4, 8, 4);
        JButton loginButton = new JButton("Login");
        UiTheme.stylePrimaryButton(loginButton);
        loginButton.setPreferredSize(new Dimension(100, 42));
        card.add(loginButton, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(8, 4, 4, 4);
        JLabel hint = UiTheme.subtitleLabel("Demo users: admin/admin123 or manager1/pass123");
        card.add(hint, gbc);

        loginButton.addActionListener(e -> login());
        passwordField.addActionListener(e -> login());

        outer.add(card);
        return outer;
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.", "Missing Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean loginSuccess = appContext.getAuthenticationService().login(username, password);

        if (!loginSuccess) {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        application.showMainWindow();
        dispose();
    }
}
