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

public class ReturnPanel extends JPanel implements RefreshablePanel {

    private final AppContext appContext;
    private final DefaultTableModel tableModel;
    private final JTable rentalTable;
    private final JTextField returnDateField;
    private final JLabel statusLabel;

    public ReturnPanel(AppContext appContext) {
        this.appContext = appContext;
        this.tableModel = new DefaultTableModel(
                new Object[]{"Rental ID", "Vehicle", "Customer", "Start", "End"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.rentalTable = new JTable(tableModel);
        this.returnDateField = new JTextField(LocalDate.now().toString(), 12);
        this.statusLabel = UiTheme.subtitleLabel("Select an active rental to return.");

        setLayout(new BorderLayout(0, 16));
        setBackground(UiTheme.BACKGROUND);

        UiTheme.styleTable(rentalTable);
        UiTheme.styleTextField(returnDateField);

        add(createTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(rentalTable), BorderLayout.CENTER);

        loadActiveRentals();
    }

    private JPanel createTopPanel() {
        JPanel card = UiTheme.cardPanel();
        card.setLayout(new BorderLayout(10, 10));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setBackground(UiTheme.SURFACE);

        JButton returnButton = new JButton("Return Vehicle");
        JButton refreshButton = new JButton("Refresh");

        UiTheme.stylePrimaryButton(returnButton);
        UiTheme.styleSecondaryButton(refreshButton);

        controls.add(new JLabel("Actual Return Date"));
        controls.add(returnDateField);
        controls.add(refreshButton);
        controls.add(returnButton);

        returnButton.addActionListener(e -> returnSelectedRental());
        refreshButton.addActionListener(e -> loadActiveRentals());

        card.add(controls, BorderLayout.CENTER);
        card.add(statusLabel, BorderLayout.SOUTH);
        return card;
    }

    private void loadActiveRentals() {
        tableModel.setRowCount(0);

        List<Rental> rentals = appContext.getRentalRepository().findAll();
        int count = 0;

        for (Rental rental : rentals) {
            if (rental.isActive()) {
                tableModel.addRow(new Object[]{
                        rental.getId(),
                        rental.getVehicle().getId(),
                        rental.getCustomerName(),
                        rental.getStartDate(),
                        rental.getEndDate()
                });
                count++;
            }
        }

        statusLabel.setText(count + " active rentals loaded.");
    }

    private void returnSelectedRental() {
        int selectedRow = rentalTable.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an active rental.",
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

        try {
            double totalCost = appContext.getReturnService()
                    .returnVehicle(rentalId, actualReturnDate);

            JOptionPane.showMessageDialog(
                    this,
                    "Vehicle returned successfully.\nTotal cost: " + totalCost,
                    "Return Completed",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadActiveRentals();
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Return Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    @Override
    public void refreshData() {
        loadActiveRentals();
    }
}
