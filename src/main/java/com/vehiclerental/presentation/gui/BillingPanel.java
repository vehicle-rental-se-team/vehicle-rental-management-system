package com.vehiclerental.presentation.gui;

import com.vehiclerental.domain.Rental;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class BillingPanel extends JPanel {

    private final AppContext appContext;
    private final DefaultTableModel tableModel;
    private final JTable rentalTable;
    private final JTextField returnDateField;
    private final JLabel statusLabel;

    public BillingPanel(AppContext appContext) {
        this.appContext = appContext;
        this.tableModel = new DefaultTableModel(
                new Object[]{"Rental ID", "Vehicle", "Customer", "Daily Rate", "End Date", "Status"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.rentalTable = new JTable(tableModel);
        this.returnDateField = new JTextField(LocalDate.now().toString(), 12);
        this.statusLabel = UiTheme.subtitleLabel("Select a rental to calculate its bill.");

        setLayout(new BorderLayout(0, 16));
        setBackground(UiTheme.BACKGROUND);

        UiTheme.styleTable(rentalTable);
        UiTheme.styleTextField(returnDateField);

        add(createTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(rentalTable), BorderLayout.CENTER);

        loadRentals();
    }

    private JPanel createTopPanel() {
        JPanel card = UiTheme.cardPanel();
        card.setLayout(new BorderLayout(10, 10));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setBackground(UiTheme.SURFACE);

        JButton calculateButton = new JButton("Calculate Bill");
        JButton refreshButton = new JButton("Refresh");

        UiTheme.stylePrimaryButton(calculateButton);
        UiTheme.styleSecondaryButton(refreshButton);

        controls.add(new JLabel("Actual Return Date"));
        controls.add(returnDateField);
        controls.add(refreshButton);
        controls.add(calculateButton);

        calculateButton.addActionListener(e -> calculateSelectedBill());
        refreshButton.addActionListener(e -> loadRentals());

        card.add(controls, BorderLayout.CENTER);
        card.add(statusLabel, BorderLayout.SOUTH);
        return card;
    }

    private void loadRentals() {
        tableModel.setRowCount(0);

        List<Rental> rentals = appContext.getRentalRepository().findAll();

        for (Rental rental : rentals) {
            tableModel.addRow(new Object[]{
                    rental.getId(),
                    rental.getVehicle().getId(),
                    rental.getCustomerName(),
                    rental.getVehicle().getDailyRate(),
                    rental.getEndDate(),
                    rental.isActive() ? "ACTIVE" : "CLOSED"
            });
        }

        statusLabel.setText(rentals.size() + " rentals loaded.");
    }

    private void calculateSelectedBill() {
        int selectedRow = rentalTable.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a rental.",
                    "Missing Rental",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        LocalDate actualReturnDate;

        try {
            actualReturnDate = LocalDate.parse(returnDateField.getText().trim());
        } catch (DateTimeParseException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Date format must be yyyy-MM-dd.",
                    "Invalid Date",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int modelRow = rentalTable.convertRowIndexToModel(selectedRow);
        String rentalId = String.valueOf(tableModel.getValueAt(modelRow, 0));

        Rental rental = appContext.getRentalRepository()
                .findById(rentalId)
                .orElse(null);

        if (rental == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Rental not found.",
                    "Billing Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        double baseCost = appContext.getBillingService().calculateBaseCost(rental);
        long lateDays = appContext.getBillingService().calculateLateDays(rental, actualReturnDate);
        double latePenalty = appContext.getBillingService().calculateLatePenalty(rental, actualReturnDate);
        double totalCost = appContext.getBillingService().calculateTotalCost(rental, actualReturnDate);

        JOptionPane.showMessageDialog(
                this,
                "Base cost: " + baseCost + "\n"
                        + "Late days: " + lateDays + "\n"
                        + "Late penalty: " + latePenalty + "\n"
                        + "Total cost: " + totalCost,
                "Rental Bill",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
