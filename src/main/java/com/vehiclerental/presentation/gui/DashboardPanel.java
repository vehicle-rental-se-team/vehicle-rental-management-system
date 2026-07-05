package com.vehiclerental.presentation.gui;

import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.exception.UnauthorizedAccessException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final AppContext appContext;

    public DashboardPanel(AppContext appContext) {
        this.appContext = appContext;
        setLayout(new BorderLayout(0, 18));
        setBackground(UiTheme.BACKGROUND);
        add(createStatsPanel(), BorderLayout.NORTH);
        add(createSprintPanel(), BorderLayout.CENTER);
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 16, 16));
        panel.setBackground(UiTheme.BACKGROUND);

        int availableCount = 0;

        try {
            List<Vehicle> availableVehicles = appContext.getVehicleCatalogService().getAvailableVehicles();
            availableCount = availableVehicles.size();
        } catch (UnauthorizedAccessException ignored) {
        }

        panel.add(createStatCard("Available Vehicles", String.valueOf(availableCount), "Ready to rent"));
        panel.add(createStatCard("Authentication", "Active", "Manager session is protected"));
        panel.add(createStatCard("Sprint", "1", "Login, logout, catalog"));

        return panel;
    }

    private JPanel createSprintPanel() {
        JPanel card = UiTheme.cardPanel();
        card.setLayout(new BorderLayout(0, 14));

        JLabel title = UiTheme.titleLabel("Project Roadmap");
        card.add(title, BorderLayout.NORTH);

        JPanel items = new JPanel(new GridLayout(5, 1, 8, 8));
        items.setBackground(UiTheme.SURFACE);
        items.add(UiTheme.subtitleLabel("Sprint 1: Authentication and available vehicle catalog - ready."));
        items.add(UiTheme.subtitleLabel("Sprint 2: Rental operations can be added under Rent Vehicle."));
        items.add(UiTheme.subtitleLabel("Sprint 3: Notification service can be added as a separate panel/service."));
        items.add(UiTheme.subtitleLabel("Sprint 4: Returns and billing can be added under Returns and Billing."));
        items.add(UiTheme.subtitleLabel("Sprint 5: Vehicle types and pricing strategies can extend the current catalog."));

        card.add(items, BorderLayout.CENTER);
        return card;
    }

    private JPanel createStatCard(String title, String value, String subtitle) {
        JPanel card = UiTheme.cardPanel();
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(200, 130));

        JLabel titleLabel = UiTheme.subtitleLabel(title);
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 34));
        valueLabel.setForeground(UiTheme.PRIMARY);
        JLabel subtitleLabel = UiTheme.subtitleLabel(subtitle);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        center.setBackground(UiTheme.SURFACE);
        center.add(valueLabel);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(subtitleLabel, BorderLayout.SOUTH);
        return card;
    }
}
