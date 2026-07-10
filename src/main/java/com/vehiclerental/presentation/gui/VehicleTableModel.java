package com.vehiclerental.presentation.gui;

import com.vehiclerental.domain.Vehicle;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class VehicleTableModel extends AbstractTableModel {

    private final String[] columns = {"Vehicle ID", "Brand", "Model", "Daily Rate", "Status"};
    private final List<Vehicle> vehicles;

    public VehicleTableModel() {
        this.vehicles = new ArrayList<>();
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles.clear();
        this.vehicles.addAll(vehicles);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return vehicles.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Vehicle vehicle = vehicles.get(rowIndex);

        if (columnIndex == 0) {
            return vehicle.getId();
        }

        if (columnIndex == 1) {
            return vehicle.getBrand();
        }

        if (columnIndex == 2) {
            return vehicle.getModel();
        }

        if (columnIndex == 3) {
            return vehicle.getDailyRate();
        }

        if (columnIndex == 4) {
            return vehicle.getStatus();
        }

        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    public Vehicle getVehicleAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= vehicles.size()) {
            return null;
        }

        return vehicles.get(rowIndex);
    }
}
