package com.vehiclerental.presentation.gui;

import com.vehiclerental.domain.MaintenanceRecord;
import com.vehiclerental.domain.Rental;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleIncident;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;

public class DashboardPanel extends JPanel implements RefreshablePanel {

    private final AppContext appContext;
    private final JLabel totalVehiclesValue;
    private final JLabel availableVehiclesValue;
    private final JLabel activeRentalsValue;
    private final JLabel pendingActionsValue;
    private final JTextArea notificationArea;

    public DashboardPanel(AppContext appContext) {
        this.appContext = appContext;
        this.totalVehiclesValue = createValueLabel();
        this.availableVehiclesValue = createValueLabel();
        this.activeRentalsValue = createValueLabel();
        this.pendingActionsValue = createValueLabel();
        this.notificationArea = new JTextArea();

        setLayout(new BorderLayout(0, 16));
        setBackground(UiTheme.BACKGROUND);
        setOpaque(true);

        add(createStatsPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createActionsPanel(), BorderLayout.SOUTH);

        refreshData();
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 14, 14));
        panel.setBackground(UiTheme.BACKGROUND);
        panel.setOpaque(true);

        panel.add(createStatCard("Total Vehicles", totalVehiclesValue, "All vehicle types"));
        panel.add(createStatCard("Available", availableVehiclesValue, "Ready for rental"));
        panel.add(createStatCard("Active Rentals", activeRentalsValue, "Currently rented"));
        panel.add(createStatCard("Pending Actions", pendingActionsValue, "Maintenance or inspection"));
        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 16, 16));
        panel.setBackground(UiTheme.BACKGROUND);
        panel.setOpaque(true);
        panel.add(createProjectStatusCard());
        panel.add(createNotificationCard());
        return panel;
    }

    private JPanel createProjectStatusCard() {
        JPanel card = UiTheme.cardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setBackground(UiTheme.SURFACE);
        header.add(UiTheme.titleLabel("Project Status"));
        header.add(UiTheme.subtitleLabel(
                "Sprint 1-5 and the additional management features are connected."
        ));
        card.add(header, BorderLayout.NORTH);

        JPanel items = new JPanel(new GridLayout(6, 1, 7, 7));
        items.setBackground(UiTheme.SURFACE);
        items.add(createStatusItem("Rentals", "Persistent rental records and returns"));
        items.add(createStatusItem("Notifications", "Expiry, overdue, battery, fuel, and documents"));
        items.add(createStatusItem("Vehicle Rules", "Strategy validation for special vehicle types"));
        items.add(createStatusItem("Incidents", "Accidents, violations, and inspections"));
        items.add(createStatusItem("Maintenance", "Six-month schedules and reminders"));
        items.add(createStatusItem("History", "Rental, incident, maintenance, and document records"));
        card.add(items, BorderLayout.CENTER);
        return card;
    }

    private JPanel createNotificationCard() {
        JPanel card = UiTheme.cardPanel();
        card.setLayout(new BorderLayout(0, 10));
        card.add(UiTheme.titleLabel("Notification Log"), BorderLayout.NORTH);

        notificationArea.setEditable(false);
        notificationArea.setLineWrap(true);
        notificationArea.setWrapStyleWord(true);
        notificationArea.setBackground(new java.awt.Color(10, 24, 46));
        notificationArea.setForeground(UiTheme.TEXT);
        notificationArea.setCaretColor(UiTheme.PRIMARY_LIGHT);
        notificationArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        notificationArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        card.add(new JScrollPane(notificationArea), BorderLayout.CENTER);
        return card;
    }

    private JPanel createActionsPanel() {
        JPanel card = UiTheme.cardPanel();
        card.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton refreshButton = new JButton("Refresh Dashboard");
        JButton clearButton = new JButton("Clear Notifications");
        JButton checksButton = new JButton("Run System Checks");

        UiTheme.styleSecondaryButton(refreshButton);
        UiTheme.styleSecondaryButton(clearButton);
        UiTheme.stylePrimaryButton(checksButton);

        refreshButton.addActionListener(e -> refreshData());
        clearButton.addActionListener(e -> {
            appContext.getNotificationLogObserver().clear();
            refreshNotifications();
        });
        checksButton.addActionListener(e -> runSystemChecks());

        card.add(refreshButton);
        card.add(clearButton);
        card.add(checksButton);
        return card;
    }

    private JPanel createStatusItem(String title, String text) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(UiTheme.SURFACE_ALT);
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(9, 11, 9, 11)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(UiTheme.PRIMARY_LIGHT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        titleLabel.setPreferredSize(new Dimension(100, 22));

        JLabel textLabel = UiTheme.subtitleLabel(text);
        textLabel.setForeground(UiTheme.TEXT);
        item.add(titleLabel, BorderLayout.WEST);
        item.add(textLabel, BorderLayout.CENTER);
        return item;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, String subtitle) {
        JPanel card = UiTheme.cardPanel();
        card.setLayout(new BorderLayout(0, 6));
        card.setPreferredSize(new Dimension(160, 110));

        card.add(UiTheme.subtitleLabel(title), BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(UiTheme.subtitleLabel(subtitle), BorderLayout.SOUTH);
        return card;
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel("0");
        label.setFont(new Font("SansSerif", Font.BOLD, 30));
        label.setForeground(UiTheme.PRIMARY_LIGHT);
        return label;
    }

    private void runSystemChecks() {
        LocalDate today = LocalDate.now();
        int rentalNotifications = appContext.getRentalReminderService()
                .sendRentalNotifications(
                        appContext.getRentalRepository().findAll(), today
                ).getRemindersSent();
        int maintenanceNotifications = appContext.getMaintenanceService()
                .checkMaintenance(today);
        int documentNotifications = appContext.getVehicleDocumentsService()
                .checkDocuments(today);

        int total = rentalNotifications
                + maintenanceNotifications
                + documentNotifications;

        refreshData();
        JOptionPane.showMessageDialog(
                this,
                total + " notifications generated for " + today + ".",
                "System Checks",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    @Override
    public void refreshData() {
        List<Vehicle> allVehicles = appContext.getVehicleCatalogService().getAllVehicles();
        totalVehiclesValue.setText(String.valueOf(allVehicles.size()));
        availableVehiclesValue.setText(String.valueOf(
                appContext.getVehicleCatalogService().getAvailableVehicles().size()
        ));

        int activeRentals = 0;
        for (Rental rental : appContext.getRentalRepository().findAll()) {
            if (rental.isActive()) {
                activeRentals++;
            }
        }
        activeRentalsValue.setText(String.valueOf(activeRentals));

        int pendingMaintenance = 0;
        for (MaintenanceRecord record
                : appContext.getMaintenanceRepository().findAll()) {
            if (record.isPending()) {
                pendingMaintenance++;
            }
        }

        int pendingInspections = 0;
        for (VehicleIncident incident
                : appContext.getVehicleIncidentRepository().findAll()) {
            if (incident.requiresInspection()) {
                pendingInspections++;
            }
        }
        pendingActionsValue.setText(String.valueOf(
                pendingMaintenance + pendingInspections
        ));
        refreshNotifications();
    }

    private void refreshNotifications() {
        List<String> messages = appContext.getNotificationLogObserver().getMessages();

        if (messages.isEmpty()) {
            notificationArea.setText("No notifications in this session.");
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = messages.size() - 1; i >= 0; i--) {
            builder.append(messages.get(i));
            if (i > 0) {
                builder.append("\n\n--------------------\n\n");
            }
        }
        notificationArea.setText(builder.toString());
        notificationArea.setCaretPosition(0);
    }
}
