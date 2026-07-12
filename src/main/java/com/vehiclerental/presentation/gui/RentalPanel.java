package com.vehiclerental.presentation.gui;

import com.vehiclerental.domain.Rental;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.exception.UnauthorizedAccessException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
    private final JTextField customerEmailField;
    private final JTextField customerAgeField;
    private final JCheckBox specialLicenseCheckBox;
    private final JTextField startDateField;
    private final JTextField endDateField;
    private final JLabel statusLabel;

    public RentalPanel(AppContext appContext) {
        this.appContext = appContext;
        this.vehicleTableModel = new VehicleTableModel();
        this.vehicleTable = new JTable(vehicleTableModel);
        this.customerNameField = new JTextField(14);
        this.customerEmailField = new JTextField(16);
        this.customerAgeField = new JTextField(6);
        this.specialLicenseCheckBox = new JCheckBox("Has special license");
        this.startDateField = new JTextField(10);
        this.endDateField = new JTextField(10);
        this.statusLabel = UiTheme.subtitleLabel("Select an available vehicle and enter rental details.");

        setLayout(new BorderLayout(0, 16));
        setBackground(UiTheme.BACKGROUND);

        UiTheme.styleTable(vehicleTable);
        UiTheme.styleTextField(customerNameField);
        UiTheme.styleTextField(customerEmailField);
        UiTheme.styleTextField(customerAgeField);
        UiTheme.styleTextField(startDateField);
        UiTheme.styleTextField(endDateField);

        specialLicenseCheckBox.setBackground(UiTheme.SURFACE);
        startDateField.setText(LocalDate.now().toString());
        endDateField.setText(LocalDate.now().plusDays(1).toString());

        add(createFormCard(), BorderLayout.NORTH);
        add(createTableCard(), BorderLayout.CENTER);

        loadAvailableVehicles();
    }

    private JPanel createFormCard() {
        JPanel card = UiTheme.cardPanel();
        card.setLayout(new BorderLayout(12, 12));

        JPanel fieldsPanel = new JPanel(new GridLayout(2, 7, 8, 8));
        fieldsPanel.setBackground(UiTheme.SURFACE);

        fieldsPanel.add(new JLabel("Customer Name"));
        fieldsPanel.add(new JLabel("Customer Email"));
        fieldsPanel.add(new JLabel("Customer Age"));
        fieldsPanel.add(new JLabel("Special License"));
        fieldsPanel.add(new JLabel("Start Date"));
        fieldsPanel.add(new JLabel("End Date"));
        fieldsPanel.add(new JLabel(""));

        fieldsPanel.add(customerNameField);
        fieldsPanel.add(customerEmailField);
        fieldsPanel.add(customerAgeField);
        fieldsPanel.add(specialLicenseCheckBox);
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
            showWarning("Please select a vehicle first.", "Missing Vehicle");
            return;
        }

        String customerName = customerNameField.getText().trim();
        String customerEmail = customerEmailField.getText().trim();

        if (customerName.isEmpty()) {
            showWarning("Please enter the customer name.", "Missing Customer");
            return;
        }
        if (customerEmail.isEmpty()) {
            showWarning("Please enter the customer email.", "Missing Email");
            return;
        }

        int customerAge;
        try {
            customerAge = Integer.parseInt(customerAgeField.getText().trim());
        } catch (NumberFormatException exception) {
            showWarning("Customer age must be a valid number.", "Invalid Age");
            return;
        }

        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(startDateField.getText().trim());
            endDate = LocalDate.parse(endDateField.getText().trim());
        } catch (DateTimeParseException exception) {
            showWarning("Date format must be yyyy-MM-dd.", "Invalid Date");
            return;
        }

        if (!endDate.isAfter(startDate)) {
            showWarning("End date must be after start date.", "Invalid Rental Period");
            return;
        }

        long rentalDays = ChronoUnit.DAYS.between(startDate, endDate);
        if (rentalDays > MAX_RENTAL_DAYS) {
            showWarning("Rental duration cannot exceed " + MAX_RENTAL_DAYS + " days.", "Invalid Rental Period");
            return;
        }

        try {
            Rental rental = appContext.getRentalService().rentVehicle(
                    selectedVehicle.getId(),
                    customerName,
                    customerEmail,
                    customerAge,
                    specialLicenseCheckBox.isSelected(),
                    startDate,
                    endDate
            );

            double estimatedCost = appContext.getBillingService().calculateBaseCost(rental);

            JOptionPane.showMessageDialog(
                    this,
                    "Rental created successfully.\n\n"
                            + "Rental ID: " + rental.getId() + "\n"
                            + "Vehicle: " + rental.getVehicle().getId() + "\n"
                            + "Type: " + rental.getVehicle().getType() + "\n"
                            + "Customer: " + rental.getCustomerName() + "\n"
                            + "Email: " + rental.getCustomerEmail() + "\n"
                            + "Start Date: " + rental.getStartDate() + "\n"
                            + "End Date: " + rental.getEndDate() + "\n"
                            + "Estimated Cost: " + estimatedCost,
                    "Rental Created",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clearForm();
            loadAvailableVehicles();
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Rental Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        customerNameField.setText("");
        customerEmailField.setText("");
        customerAgeField.setText("");
        specialLicenseCheckBox.setSelected(false);
    }

    private void showWarning(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
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
