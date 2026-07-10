package com.vehiclerental.presentation.gui;

import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.exception.UnauthorizedAccessException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class RentalPanel extends JPanel {

    private static final int MAX_RENTAL_DAYS = 30;

    private final AppContext appContext;
    private final VehicleTableModel vehicleTableModel;
    private final JTable vehicleTable;
    private final JTextField customerNameField;
    private final JTextField startDateField;
    private final JTextField endDateField;
    private final JLabel statusLabel;

    public RentalPanel(AppContext appContext) {
        this.appContext = appContext;
        this.vehicleTableModel = new VehicleTableModel();
        this.vehicleTable = new JTable(vehicleTableModel);
        this.customerNameField = new JTextField(18);
        this.startDateField = new JTextField(12);
        this.endDateField = new JTextField(12);
        this.statusLabel = UiTheme.subtitleLabel("Select an available vehicle and enter rental details.");

        setLayout(new BorderLayout(0, 16));
        setBackground(UiTheme.BACKGROUND);

        UiTheme.styleTable(vehicleTable);
        UiTheme.styleTextField(customerNameField);
        UiTheme.styleTextField(startDateField);
        UiTheme.styleTextField(endDateField);

        startDateField.setText(LocalDate.now().toString());
        endDateField.setText(LocalDate.now().plusDays(1).toString());

        add(createFormCard(), BorderLayout.NORTH);
        add(createTableCard(), BorderLayout.CENTER);

        loadAvailableVehicles();
    }

    private JPanel createFormCard() {
        JPanel card = UiTheme.cardPanel();
        card.setLayout(new BorderLayout(12, 12));

        JPanel fieldsPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        fieldsPanel.setBackground(UiTheme.SURFACE);

        fieldsPanel.add(new JLabel("Customer Name"));
        fieldsPanel.add(new JLabel("Start Date"));
        fieldsPanel.add(new JLabel("End Date"));
        fieldsPanel.add(new JLabel(""));

        fieldsPanel.add(customerNameField);
        fieldsPanel.add(startDateField);
        fieldsPanel.add(endDateField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setBackground(UiTheme.SURFACE);

        JButton rentButton = new JButton("Rent Vehicle");
        JButton refreshButton = new JButton("Refresh");

        UiTheme.stylePrimaryButton(rentButton);
        UiTheme.styleSecondaryButton(refreshButton);

        buttonPanel.add(refreshButton);
        buttonPanel.add(rentButton);

        fieldsPanel.add(buttonPanel);

        card.add(fieldsPanel, BorderLayout.CENTER);
        card.add(statusLabel, BorderLayout.SOUTH);

        rentButton.addActionListener(e -> prepareRental());
        refreshButton.addActionListener(e -> loadAvailableVehicles());

        return card;
    }

    private JPanel createTableCard() {
        JPanel card = UiTheme.cardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        card.add(new JScrollPane(vehicleTable), BorderLayout.CENTER);
        return card;
    }

    private void loadAvailableVehicles() {
        try {
            List<Vehicle> vehicles = appContext.getVehicleCatalogService().getAvailableVehicles();
            vehicleTableModel.setVehicles(vehicles);
            statusLabel.setText(vehicles.size() + " available vehicles loaded.");
        } catch (UnauthorizedAccessException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Unauthorized", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void prepareRental() {
        Vehicle selectedVehicle = getSelectedVehicle();

        if (selectedVehicle == null) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle first.", "Missing Vehicle", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String customerName = customerNameField.getText().trim();

        if (customerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the customer name.", "Missing Customer", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate startDate;
        LocalDate endDate;

        try {
            startDate = LocalDate.parse(startDateField.getText().trim());
            endDate = LocalDate.parse(endDateField.getText().trim());
        } catch (DateTimeParseException exception) {
            JOptionPane.showMessageDialog(this, "Date format must be yyyy-MM-dd.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!endDate.isAfter(startDate)) {
            JOptionPane.showMessageDialog(this, "End date must be after start date.", "Invalid Rental Period", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long rentalDays = ChronoUnit.DAYS.between(startDate, endDate);

        if (rentalDays > MAX_RENTAL_DAYS) {
            JOptionPane.showMessageDialog(this, "Rental duration cannot exceed " + MAX_RENTAL_DAYS + " days.", "Invalid Rental Period", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double estimatedCost = rentalDays * selectedVehicle.getDailyRate();

        JOptionPane.showMessageDialog(
                this,
                "GUI is ready for Sprint 2 service.\n\n" +
                        "Vehicle: " + selectedVehicle.getId() + " - " + selectedVehicle.getBrand() + " " + selectedVehicle.getModel() + "\n" +
                        "Customer: " + customerName + "\n" +
                        "Duration: " + rentalDays + " day(s)\n" +
                        "Estimated Cost: " + estimatedCost + "\n\n" +
                        "When RentalService is ready, connect it inside prepareRental().",
                "Rental Prepared",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private Vehicle getSelectedVehicle() {
        int selectedRow = vehicleTable.getSelectedRow();

        if (selectedRow < 0) {
            return null;
        }

        int modelRow = vehicleTable.convertRowIndexToModel(selectedRow);
        return vehicleTableModel.getVehicleAt(modelRow);
    }
}