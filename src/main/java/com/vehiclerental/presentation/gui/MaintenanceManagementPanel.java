package com.vehiclerental.presentation.gui;

import com.vehiclerental.domain.MaintenanceRecord;
import com.vehiclerental.domain.Vehicle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class MaintenanceManagementPanel extends JPanel implements RefreshablePanel {

    private final AppContext appContext;
    private final JComboBox<String> vehicleComboBox;
    private final JTextField lastMaintenanceField;
    private final JTextField completionDateField;
    private final JTextField checkDateField;
    private final JLabel statusLabel;
    private final DefaultTableModel tableModel;
    private final JTable maintenanceTable;

    public MaintenanceManagementPanel(AppContext appContext) {
        this.appContext = appContext;
        this.vehicleComboBox = new JComboBox<>();
        this.lastMaintenanceField = new JTextField(LocalDate.now().toString(), 11);
        this.completionDateField = new JTextField(LocalDate.now().toString(), 11);
        this.checkDateField = new JTextField(LocalDate.now().toString(), 11);
        this.statusLabel = UiTheme.subtitleLabel("Manage six-month maintenance schedules.");
        this.tableModel = new DefaultTableModel(
                new Object[]{
                        "Record ID", "Vehicle", "Last Maintenance",
                        "Next Maintenance", "Status"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.maintenanceTable = new JTable(tableModel);

        setLayout(new BorderLayout(0, 14));
        setBackground(UiTheme.BACKGROUND);

        UiTheme.styleComboBox(vehicleComboBox);
        UiTheme.styleTextField(lastMaintenanceField);
        UiTheme.styleTextField(completionDateField);
        UiTheme.styleTextField(checkDateField);
        UiTheme.styleTable(maintenanceTable);

        add(createControlsCard(), BorderLayout.NORTH);
        add(new JScrollPane(maintenanceTable), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        maintenanceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectVehicleFromTable();
            }
        });

        refreshData();
    }

    private JPanel createControlsCard() {
        JPanel card = UiTheme.cardPanel();
        card.setLayout(new BorderLayout(10, 10));

        JPanel fields = new JPanel(new GridLayout(2, 4, 8, 8));
        fields.setBackground(UiTheme.SURFACE);
        fields.add(new JLabel("Vehicle"));
        fields.add(new JLabel("Last Maintenance Date"));
        fields.add(new JLabel("Completion Date"));
        fields.add(new JLabel("Check Date"));
        fields.add(vehicleComboBox);
        fields.add(lastMaintenanceField);
        fields.add(completionDateField);
        fields.add(checkDateField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(UiTheme.SURFACE);

        JButton refreshButton = new JButton("Refresh");
        JButton checkButton = new JButton("Run Maintenance Check");
        JButton completeButton = new JButton("Complete Maintenance");
        JButton scheduleButton = new JButton("Schedule Maintenance");

        UiTheme.styleSecondaryButton(refreshButton);
        UiTheme.styleSecondaryButton(checkButton);
        UiTheme.styleSecondaryButton(completeButton);
        UiTheme.stylePrimaryButton(scheduleButton);

        refreshButton.addActionListener(e -> refreshData());
        checkButton.addActionListener(e -> runMaintenanceCheck());
        completeButton.addActionListener(e -> completeMaintenance());
        scheduleButton.addActionListener(e -> scheduleMaintenance());

        buttons.add(refreshButton);
        buttons.add(checkButton);
        buttons.add(completeButton);
        buttons.add(scheduleButton);

        card.add(fields, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private void scheduleMaintenance() {
        String vehicleId = (String) vehicleComboBox.getSelectedItem();
        if (vehicleId == null) {
            showWarning("Please select a vehicle.");
            return;
        }

        LocalDate lastDate;
        try {
            lastDate = LocalDate.parse(lastMaintenanceField.getText().trim());
        } catch (DateTimeParseException exception) {
            showWarning("Last maintenance date must use yyyy-MM-dd.");
            return;
        }

        try {
            MaintenanceRecord record = appContext.getMaintenanceService()
                    .scheduleMaintenance(vehicleId, lastDate);
            JOptionPane.showMessageDialog(
                    this,
                    "Maintenance scheduled for "
                            + record.getNextMaintenanceDate() + ".",
                    "Maintenance Scheduled",
                    JOptionPane.INFORMATION_MESSAGE
            );
            refreshData();
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    private void completeMaintenance() {
        String vehicleId = (String) vehicleComboBox.getSelectedItem();
        if (vehicleId == null) {
            showWarning("Please select a vehicle.");
            return;
        }

        LocalDate completionDate;
        try {
            completionDate = LocalDate.parse(completionDateField.getText().trim());
        } catch (DateTimeParseException exception) {
            showWarning("Completion date must use yyyy-MM-dd.");
            return;
        }

        try {
            MaintenanceRecord nextRecord = appContext.getMaintenanceService()
                    .completeMaintenance(vehicleId, completionDate);
            JOptionPane.showMessageDialog(
                    this,
                    "Maintenance completed. Next date: "
                            + nextRecord.getNextMaintenanceDate(),
                    "Maintenance Completed",
                    JOptionPane.INFORMATION_MESSAGE
            );
            refreshData();
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    private void runMaintenanceCheck() {
        LocalDate checkDate;
        try {
            checkDate = LocalDate.parse(checkDateField.getText().trim());
        } catch (DateTimeParseException exception) {
            showWarning("Check date must use yyyy-MM-dd.");
            return;
        }

        try {
            int notifications = appContext.getMaintenanceService()
                    .checkMaintenance(checkDate);
            JOptionPane.showMessageDialog(
                    this,
                    notifications + " maintenance notification(s) generated.",
                    "Maintenance Check",
                    JOptionPane.INFORMATION_MESSAGE
            );
            refreshData();
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    @Override
    public void refreshData() {
        String selectedVehicle = (String) vehicleComboBox.getSelectedItem();
        vehicleComboBox.removeAllItems();

        List<Vehicle> vehicles = appContext.getVehicleCatalogService().getAllVehicles();
        for (Vehicle vehicle : vehicles) {
            vehicleComboBox.addItem(vehicle.getId());
        }
        if (selectedVehicle != null) {
            vehicleComboBox.setSelectedItem(selectedVehicle);
        }

        tableModel.setRowCount(0);
        List<MaintenanceRecord> records = appContext.getMaintenanceService()
                .getAllMaintenanceRecords();

        for (MaintenanceRecord record : records) {
            tableModel.addRow(new Object[]{
                    record.getId(),
                    record.getVehicleId(),
                    record.getLastMaintenanceDate(),
                    record.getNextMaintenanceDate(),
                    record.getStatus()
            });
        }

        int pending = 0;
        for (MaintenanceRecord record : records) {
            if (record.isPending()) {
                pending++;
            }
        }
        statusLabel.setText(
                records.size() + " maintenance records loaded. "
                        + pending + " pending."
        );
    }

    private void selectVehicleFromTable() {
        int selectedRow = maintenanceTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        int modelRow = maintenanceTable.convertRowIndexToModel(selectedRow);
        vehicleComboBox.setSelectedItem(
                String.valueOf(tableModel.getValueAt(modelRow, 1))
        );
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
