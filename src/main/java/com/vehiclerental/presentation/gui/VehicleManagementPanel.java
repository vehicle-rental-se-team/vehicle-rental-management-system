package com.vehiclerental.presentation.gui;

import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleDocuments;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class VehicleManagementPanel extends JPanel implements RefreshablePanel {

    private final AppContext appContext;
    private final JComboBox<String> vehicleComboBox;
    private final JLabel vehicleInfoLabel;
    private final JLabel energyLabel;
    private final JTextField energyField;
    private final JTextField registrationField;
    private final JTextField insuranceField;
    private final JTextArea historyArea;

    public VehicleManagementPanel(AppContext appContext) {
        this.appContext = appContext;
        this.vehicleComboBox = new JComboBox<>();
        this.vehicleInfoLabel = UiTheme.subtitleLabel("Select a vehicle.");
        this.energyLabel = new JLabel("Energy Level (%)");
        this.energyField = new JTextField(10);
        this.registrationField = new JTextField(12);
        this.insuranceField = new JTextField(12);
        this.historyArea = new JTextArea();

        setLayout(new BorderLayout(0, 14));
        setBackground(UiTheme.BACKGROUND);

        UiTheme.styleComboBox(vehicleComboBox);
        UiTheme.styleTextField(energyField);
        UiTheme.styleTextField(registrationField);
        UiTheme.styleTextField(insuranceField);
        styleHistoryArea();

        add(createVehicleSelectionCard(), BorderLayout.NORTH);
        add(createManagementCards(), BorderLayout.CENTER);
        add(createHistoryCard(), BorderLayout.SOUTH);

        vehicleComboBox.addActionListener(e -> refreshSelectedVehicle());
        refreshData();
    }

    private JPanel createVehicleSelectionCard() {
        JPanel card = UiTheme.cardPanel();
        card.setLayout(new BorderLayout(12, 8));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setBackground(UiTheme.SURFACE);
        controls.add(new JLabel("Vehicle"));
        controls.add(vehicleComboBox);

        JButton refreshButton = new JButton("Refresh");
        UiTheme.styleSecondaryButton(refreshButton);
        refreshButton.addActionListener(e -> refreshData());
        controls.add(refreshButton);

        JButton addVehicleButton = new JButton("Add Vehicle");
        UiTheme.stylePrimaryButton(addVehicleButton);
        addVehicleButton.addActionListener(e -> showAddVehicleDialog());
        controls.add(addVehicleButton);

        card.add(controls, BorderLayout.WEST);
        card.add(vehicleInfoLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createManagementCards() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 14, 14));
        panel.setBackground(UiTheme.BACKGROUND);
        panel.add(createEnergyCard());
        panel.add(createDocumentsCard());
        return panel;
    }

    private JPanel createEnergyCard() {
        JPanel card = UiTheme.cardPanel();
        card.setLayout(new BorderLayout(0, 12));
        card.add(UiTheme.titleLabel("Battery or Fuel"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
        form.setBackground(UiTheme.SURFACE);
        form.add(energyLabel);
        form.add(energyField);

        JButton updateButton = new JButton("Update Level");
        UiTheme.stylePrimaryButton(updateButton);
        updateButton.addActionListener(e -> updateEnergyLevel());
        form.add(new JLabel("Allowed range: 0-100"));
        form.add(updateButton);

        card.add(form, BorderLayout.CENTER);
        card.add(UiTheme.subtitleLabel(
                "Electric vehicles use battery; other vehicles use fuel."
        ), BorderLayout.SOUTH);
        return card;
    }

    private JPanel createDocumentsCard() {
        JPanel card = UiTheme.cardPanel();
        card.setLayout(new BorderLayout(0, 12));
        card.add(UiTheme.titleLabel("Vehicle Documents"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setBackground(UiTheme.SURFACE);
        form.add(new JLabel("Registration Expiry"));
        form.add(registrationField);
        form.add(new JLabel("Insurance Expiry"));
        form.add(insuranceField);

        JButton updateButton = new JButton("Save Documents");
        UiTheme.stylePrimaryButton(updateButton);
        updateButton.addActionListener(e -> updateDocuments());
        form.add(new JLabel("Format: yyyy-MM-dd"));
        form.add(updateButton);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel createHistoryCard() {
        JPanel card = UiTheme.cardPanel();
        card.setLayout(new BorderLayout(0, 8));
        card.setPreferredSize(new java.awt.Dimension(100, 230));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiTheme.SURFACE);
        header.add(UiTheme.titleLabel("Vehicle History"), BorderLayout.WEST);

        JButton historyButton = new JButton("Load History");
        UiTheme.styleSecondaryButton(historyButton);
        historyButton.addActionListener(e -> loadHistory());
        header.add(historyButton, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);
        card.add(new JScrollPane(historyArea), BorderLayout.CENTER);
        return card;
    }

    private void showAddVehicleDialog() {
        JTextField idField = new JTextField(12);
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{
                "CAR", "MOTORCYCLE", "VAN", "TRUCK", "ELECTRIC"
        });
        JTextField brandField = new JTextField(12);
        JTextField modelField = new JTextField(12);
        JTextField rateField = new JTextField(12);
        JTextField energyLevelField = new JTextField("100", 12);
        JLabel energyLevelLabel = new JLabel("Initial Fuel Level (%)");

        UiTheme.styleTextField(idField);
        UiTheme.styleComboBox(typeComboBox);
        UiTheme.styleTextField(brandField);
        UiTheme.styleTextField(modelField);
        UiTheme.styleTextField(rateField);
        UiTheme.styleTextField(energyLevelField);

        typeComboBox.addActionListener(e -> {
            if ("ELECTRIC".equals(typeComboBox.getSelectedItem())) {
                energyLevelLabel.setText("Initial Battery Level (%)");
            } else {
                energyLevelLabel.setText("Initial Fuel Level (%)");
            }
        });

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setBackground(UiTheme.SURFACE);
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        form.add(new JLabel("Vehicle ID"));
        form.add(idField);
        form.add(new JLabel("Vehicle Type"));
        form.add(typeComboBox);
        form.add(new JLabel("Brand"));
        form.add(brandField);
        form.add(new JLabel("Model"));
        form.add(modelField);
        form.add(new JLabel("Daily Rate"));
        form.add(rateField);
        form.add(energyLevelLabel);
        form.add(energyLevelField);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Add New Vehicle",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        double dailyRate;
        int energyLevel;
        try {
            dailyRate = Double.parseDouble(rateField.getText().trim());
            energyLevel = Integer.parseInt(
                    energyLevelField.getText().trim()
            );
        } catch (NumberFormatException exception) {
            showWarning(
                    "Daily rate and battery/fuel level must be valid numbers."
            );
            return;
        }

        try {
            Vehicle vehicle = appContext.getVehicleManagementService()
                    .addVehicle(
                            idField.getText(),
                            String.valueOf(typeComboBox.getSelectedItem()),
                            brandField.getText(),
                            modelField.getText(),
                            dailyRate,
                            energyLevel
                    );

            refreshData();
            vehicleComboBox.setSelectedItem(vehicle.getId());
            refreshSelectedVehicle();
            loadHistory();

            JOptionPane.showMessageDialog(
                    this,
                    "Vehicle " + vehicle.getId()
                            + " added successfully with status "
                            + vehicle.getStatus() + ".",
                    "Vehicle Added",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    private void updateEnergyLevel() {
        Vehicle vehicle = getSelectedVehicle();
        if (vehicle == null) {
            showWarning("Please select a vehicle.");
            return;
        }

        int level;
        try {
            level = Integer.parseInt(energyField.getText().trim());
        } catch (NumberFormatException exception) {
            showWarning("Energy level must be a number from 0 to 100.");
            return;
        }

        try {
            if (vehicle instanceof ElectricVehicle) {
                appContext.getElectricVehicleMonitoringService()
                        .updateBatteryLevel(vehicle.getId(), level);
            } else {
                appContext.getFuelMonitoringService()
                        .updateFuelLevel(vehicle.getId(), level);
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Energy level updated successfully.",
                    "Vehicle Updated",
                    JOptionPane.INFORMATION_MESSAGE
            );
            refreshSelectedVehicle();
            loadHistory();
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    private void updateDocuments() {
        Vehicle vehicle = getSelectedVehicle();
        if (vehicle == null) {
            showWarning("Please select a vehicle.");
            return;
        }

        LocalDate registrationDate;
        LocalDate insuranceDate;
        try {
            registrationDate = LocalDate.parse(registrationField.getText().trim());
            insuranceDate = LocalDate.parse(insuranceField.getText().trim());
        } catch (DateTimeParseException exception) {
            showWarning("Document dates must use yyyy-MM-dd.");
            return;
        }

        try {
            appContext.getVehicleDocumentsService().updateDocuments(
                    vehicle.getId(), registrationDate, insuranceDate
            );
            JOptionPane.showMessageDialog(
                    this,
                    "Vehicle documents saved successfully.",
                    "Documents Updated",
                    JOptionPane.INFORMATION_MESSAGE
            );
            refreshSelectedVehicle();
            loadHistory();
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    private void refreshSelectedVehicle() {
        Vehicle vehicle = getSelectedVehicle();
        if (vehicle == null) {
            vehicleInfoLabel.setText("No vehicle selected.");
            return;
        }

        vehicleInfoLabel.setText(
                vehicle.getId() + " | " + vehicle.getType()
                        + " | " + vehicle.getBrand() + " " + vehicle.getModel()
                        + " | Status: " + vehicle.getStatus()
        );

        if (vehicle instanceof ElectricVehicle) {
            energyLabel.setText("Battery Level (%)");
            energyField.setText(String.valueOf(
                    ((ElectricVehicle) vehicle).getBatteryLevel()
            ));
        } else {
            energyLabel.setText("Fuel Level (%)");
            try {
                energyField.setText(String.valueOf(
                        appContext.getFuelMonitoringService()
                                .getFuelLevel(vehicle.getId())
                ));
            } catch (RuntimeException exception) {
                energyField.setText("100");
            }
        }

        try {
            VehicleDocuments documents = appContext.getVehicleDocumentsService()
                    .getDocuments(vehicle.getId());
            registrationField.setText(
                    documents.getRegistrationExpiryDate().toString()
            );
            insuranceField.setText(
                    documents.getInsuranceExpiryDate().toString()
            );
        } catch (RuntimeException exception) {
            registrationField.setText("");
            insuranceField.setText("");
        }
    }

    private void loadHistory() {
        Vehicle vehicle = getSelectedVehicle();
        if (vehicle == null) {
            historyArea.setText("Select a vehicle to view its history.");
            return;
        }

        try {
            List<String> history = appContext.getVehicleHistoryService()
                    .getVehicleHistory(vehicle.getId());
            StringBuilder builder = new StringBuilder();
            for (String line : history) {
                builder.append(line).append('\n');
            }
            historyArea.setText(builder.toString());
            historyArea.setCaretPosition(0);
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    @Override
    public void refreshData() {
        String selectedId = (String) vehicleComboBox.getSelectedItem();
        List<Vehicle> vehicles = appContext.getVehicleCatalogService().getAllVehicles();

        vehicleComboBox.removeAllItems();
        for (Vehicle vehicle : vehicles) {
            vehicleComboBox.addItem(vehicle.getId());
        }

        if (selectedId != null) {
            vehicleComboBox.setSelectedItem(selectedId);
        }
        if (vehicleComboBox.getSelectedItem() == null
                && vehicleComboBox.getItemCount() > 0) {
            vehicleComboBox.setSelectedIndex(0);
        }

        refreshSelectedVehicle();
        loadHistory();
    }

    private Vehicle getSelectedVehicle() {
        String vehicleId = (String) vehicleComboBox.getSelectedItem();
        if (vehicleId == null) {
            return null;
        }

        for (Vehicle vehicle : appContext.getVehicleCatalogService().getAllVehicles()) {
            if (vehicle.getId().equals(vehicleId)) {
                return vehicle;
            }
        }
        return null;
    }

    private void styleHistoryArea() {
        historyArea.setEditable(false);
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);
        historyArea.setBackground(new Color(10, 24, 46));
        historyArea.setForeground(UiTheme.TEXT);
        historyArea.setCaretColor(UiTheme.PRIMARY_LIGHT);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        historyArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Invalid Data", JOptionPane.WARNING_MESSAGE
        );
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Operation Error", JOptionPane.ERROR_MESSAGE
        );
    }
}
