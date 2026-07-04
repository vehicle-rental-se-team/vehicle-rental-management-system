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
import javax.swing.RowFilter;
import javax.swing.JTextField;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

public class VehicleCatalogPanel extends JPanel {

    private final AppContext appContext;
    private final VehicleTableModel tableModel;
    private final JTable table;
    private final JTextField searchField;
    private final JLabel countLabel;
    private final TableRowSorter<VehicleTableModel> sorter;

    public VehicleCatalogPanel(AppContext appContext) {
        this.appContext = appContext;
        this.tableModel = new VehicleTableModel();
        this.table = new JTable(tableModel);
        this.searchField = new JTextField(24);
        this.countLabel = UiTheme.subtitleLabel("0 available vehicles");
        this.sorter = new TableRowSorter<>(tableModel);

        setLayout(new BorderLayout(0, 16));
        setBackground(UiTheme.BACKGROUND);

        table.setRowSorter(sorter);
        UiTheme.styleTable(table);
        UiTheme.styleTextField(searchField);

        add(createToolbar(), BorderLayout.NORTH);
        add(createTableCard(), BorderLayout.CENTER);

        loadAvailableVehicles();
    }

    private JPanel createToolbar() {
        JPanel toolbar = UiTheme.cardPanel();
        toolbar.setLayout(new BorderLayout(12, 12));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setBackground(UiTheme.SURFACE);
        left.add(new JLabel("Search:"));
        left.add(searchField);
        left.add(countLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(UiTheme.SURFACE);

        JButton searchButton = new JButton("Apply Search");
        JButton clearButton = new JButton("Clear");
        JButton refreshButton = new JButton("Refresh");

        UiTheme.stylePrimaryButton(searchButton);
        UiTheme.styleSecondaryButton(clearButton);
        UiTheme.styleSecondaryButton(refreshButton);

        right.add(searchButton);
        right.add(clearButton);
        right.add(refreshButton);

        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);

        searchButton.addActionListener(e -> applySearch());
        clearButton.addActionListener(e -> clearSearch());
        refreshButton.addActionListener(e -> loadAvailableVehicles());
        searchField.addActionListener(e -> applySearch());

        return toolbar;
    }

    private JPanel createTableCard() {
        JPanel card = UiTheme.cardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private void loadAvailableVehicles() {
        try {
            List<Vehicle> vehicles = appContext.getVehicleCatalogService().getAvailableVehicles();
            tableModel.setVehicles(vehicles);
            countLabel.setText(vehicles.size() + " available vehicles");
        } catch (UnauthorizedAccessException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Unauthorized", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void applySearch() {
        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
        }
    }

    private void clearSearch() {
        searchField.setText("");
        sorter.setRowFilter(null);
    }
}
