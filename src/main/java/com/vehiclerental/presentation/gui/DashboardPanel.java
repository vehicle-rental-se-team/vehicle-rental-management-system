package com.vehiclerental.presentation.gui;

import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.exception.UnauthorizedAccessException;

import javax.swing.BorderFactory;
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
        setOpaque(true);

        add(createStatsPanel(), BorderLayout.NORTH);
        add(createSprintPanel(), BorderLayout.CENTER);
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 16, 16));
        panel.setBackground(UiTheme.BACKGROUND);
        panel.setOpaque(true);

        int availableCount = 0;

        try {
            List<Vehicle> availableVehicles = appContext.getVehicleCatalogService().getAvailableVehicles();
            availableCount = availableVehicles.size();
        } catch (UnauthorizedAccessException ignored) {
            availableCount = 0;
        }

        panel.add(createStatCard("Available Vehicles", String.valueOf(availableCount), "Ready to rent"));
        panel.add(createStatCard("Authentication", "Active", "Manager session is protected"));
        panel.add(createStatCard("Sprint", "2", "Rental GUI prepared"));

        return panel;
    }

    private JPanel createSprintPanel() {
        JPanel card = UiTheme.cardPanel();
        card.setLayout(new BorderLayout(0, 16));
        card.setBackground(UiTheme.SURFACE);
        card.setOpaque(true);

        JLabel title = UiTheme.titleLabel("Project Roadmap");
        title.setForeground(UiTheme.TEXT);

        JLabel subtitle = UiTheme.subtitleLabel("Current progress overview for the Vehicle Rental Management System.");

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setBackground(UiTheme.SURFACE);
        header.setOpaque(true);
        header.add(title);
        header.add(subtitle);

        card.add(header, BorderLayout.NORTH);

        JPanel items = new JPanel(new GridLayout(5, 1, 8, 8));
        items.setBackground(UiTheme.SURFACE);
        items.setOpaque(true);

        items.add(createRoadmapItem("Sprint 1", "Authentication and available vehicle catalog - ready."));
        items.add(createRoadmapItem("Sprint 2", "Rental operations GUI is ready under Rent Vehicle."));
        items.add(createRoadmapItem("Sprint 3", "Notification service can be added as a separate panel/service."));
        items.add(createRoadmapItem("Sprint 4", "Returns and billing can be added under Returns and Billing."));
        items.add(createRoadmapItem("Sprint 5", "Vehicle types and pricing strategies can extend the current catalog."));

        card.add(items, BorderLayout.CENTER);

        return card;
    }

    private JPanel createRoadmapItem(String sprint, String text) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(UiTheme.SURFACE_ALT);
        item.setOpaque(true);
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel sprintLabel = new JLabel(sprint);
        sprintLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        sprintLabel.setForeground(UiTheme.PRIMARY_LIGHT);
        sprintLabel.setPreferredSize(new Dimension(80, 24));

        JLabel textLabel = UiTheme.subtitleLabel(text);
        textLabel.setForeground(UiTheme.TEXT);

        item.add(sprintLabel, BorderLayout.WEST);
        item.add(textLabel, BorderLayout.CENTER);

        return item;
    }

    private JPanel createStatCard(String title, String value, String subtitle) {
        JPanel card = UiTheme.cardPanel();
        card.setLayout(new BorderLayout(0, 8));
        card.setPreferredSize(new Dimension(200, 130));
        card.setBackground(UiTheme.SURFACE);
        card.setOpaque(true);

        JLabel titleLabel = UiTheme.subtitleLabel(title);
        titleLabel.setForeground(UiTheme.MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 34));
        valueLabel.setForeground(UiTheme.PRIMARY_LIGHT);

        JLabel subtitleLabel = UiTheme.subtitleLabel(subtitle);
        subtitleLabel.setForeground(UiTheme.MUTED);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        center.setBackground(UiTheme.SURFACE);
        center.setOpaque(true);
        center.add(valueLabel);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(subtitleLabel, BorderLayout.SOUTH);

        return card;
    }
}