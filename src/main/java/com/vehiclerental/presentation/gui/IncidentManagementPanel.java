package com.vehiclerental.presentation.gui;

import com.vehiclerental.domain.IncidentType;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleIncident;

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

public class IncidentManagementPanel extends JPanel implements RefreshablePanel {

    private final AppContext appContext;
    private final JComboBox<String> vehicleComboBox;
    private final JComboBox<IncidentType> typeComboBox;
    private final JTextField dateField;
    private final JTextField descriptionField;
    private final JLabel statusLabel;
    private final DefaultTableModel tableModel;
    private final JTable incidentTable;

    public IncidentManagementPanel(AppContext appContext) {
        this.appContext = appContext;
        this.vehicleComboBox = new JComboBox<>();
        this.typeComboBox = new JComboBox<>(IncidentType.values());
        this.dateField = new JTextField(LocalDate.now().toString(), 11);
        this.descriptionField = new JTextField(24);
        this.statusLabel = UiTheme.subtitleLabel("Record an accident or violation.");
        this.tableModel = new DefaultTableModel(
                new Object[]{
                        "Incident ID", "Vehicle", "Type", "Date",
                        "Description", "Inspection"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.incidentTable = new JTable(tableModel);

        setLayout(new BorderLayout(0, 14));
        setBackground(UiTheme.BACKGROUND);

        UiTheme.styleComboBox(vehicleComboBox);
        UiTheme.styleComboBox(typeComboBox);
        UiTheme.styleTextField(dateField);
        UiTheme.styleTextField(descriptionField);
        UiTheme.styleTable(incidentTable);

        add(createFormCard(), BorderLayout.NORTH);
        add(new JScrollPane(incidentTable), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        incidentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectVehicleFromTable();
            }
        });

        refreshData();
    }

    private JPanel createFormCard() {
        JPanel card = UiTheme.cardPanel();
        card.setLayout(new BorderLayout(10, 10));

        JPanel fields = new JPanel(new GridLayout(2, 4, 8, 8));
        fields.setBackground(UiTheme.SURFACE);
        fields.add(new JLabel("Vehicle"));
        fields.add(new JLabel("Incident Type"));
        fields.add(new JLabel("Date"));
        fields.add(new JLabel("Description"));
        fields.add(vehicleComboBox);
        fields.add(typeComboBox);
        fields.add(dateField);
        fields.add(descriptionField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(UiTheme.SURFACE);

        JButton refreshButton = new JButton("Refresh");
        JButton inspectionButton = new JButton("Complete Inspection");
        JButton recordButton = new JButton("Record Incident");

        UiTheme.styleSecondaryButton(refreshButton);
        UiTheme.styleSecondaryButton(inspectionButton);
        UiTheme.stylePrimaryButton(recordButton);

        refreshButton.addActionListener(e -> refreshData());
        inspectionButton.addActionListener(e -> completeInspection());
        recordButton.addActionListener(e -> recordIncident());

        buttons.add(refreshButton);
        buttons.add(inspectionButton);
        buttons.add(recordButton);

        card.add(fields, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private void recordIncident() {
        String vehicleId = (String) vehicleComboBox.getSelectedItem();
        IncidentType type = (IncidentType) typeComboBox.getSelectedItem();
        String description = descriptionField.getText().trim();

        if (vehicleId == null || description.isEmpty()) {
            showWarning("Vehicle and description are required.");
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateField.getText().trim());
        } catch (DateTimeParseException exception) {
            showWarning("Date format must be yyyy-MM-dd.");
            return;
        }

        try {
            appContext.getVehicleIncidentService().recordIncident(
                    vehicleId, type, date, description.replace(',', ';')
            );
            JOptionPane.showMessageDialog(
                    this,
                    "Incident recorded successfully.",
                    "Incident Saved",
                    JOptionPane.INFORMATION_MESSAGE
            );
            descriptionField.setText("");
            refreshData();
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    private void completeInspection() {
        String vehicleId = (String) vehicleComboBox.getSelectedItem();
        if (vehicleId == null) {
            showWarning("Please select a vehicle.");
            return;
        }

        LocalDate inspectionDate;
        try {
            inspectionDate = LocalDate.parse(dateField.getText().trim());
        } catch (DateTimeParseException exception) {
            showWarning("Date format must be yyyy-MM-dd.");
            return;
        }

        try {
            int completed = appContext.getVehicleIncidentService()
                    .completeInspection(vehicleId, inspectionDate);
            JOptionPane.showMessageDialog(
                    this,
                    completed + " pending accident inspection(s) completed.",
                    "Inspection Completed",
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
        List<VehicleIncident> incidents = appContext.getVehicleIncidentService()
                .getAllIncidents();

        for (VehicleIncident incident : incidents) {
            tableModel.addRow(new Object[]{
                    incident.getId(),
                    incident.getVehicleId(),
                    incident.getType(),
                    incident.getDate(),
                    incident.getDescription(),
                    incident.requiresInspection() ? "PENDING" : "COMPLETED"
            });
        }

        int pending = 0;
        for (VehicleIncident incident : incidents) {
            if (incident.requiresInspection()) {
                pending++;
            }
        }
        statusLabel.setText(
                incidents.size() + " incidents loaded. "
                        + pending + " inspections pending."
        );
    }

    private void selectVehicleFromTable() {
        int selectedRow = incidentTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        int modelRow = incidentTable.convertRowIndexToModel(selectedRow);
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
