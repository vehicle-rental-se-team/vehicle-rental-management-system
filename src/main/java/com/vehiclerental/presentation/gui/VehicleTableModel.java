package com.vehiclerental.presentation.gui;

import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.Vehicle;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class VehicleTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Vehicle ID", "Type", "Brand", "Model", "Daily Rate", "Status", "Battery"
    };
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
    public int getRowCount() { return vehicles.size(); }

    @Override
    public int getColumnCount() { return columns.length; }

    @Override
    public String getColumnName(int column) { return columns[column]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Vehicle vehicle = vehicles.get(rowIndex);

        switch (columnIndex) {
            case 0: return vehicle.getId();
            case 1: return vehicle.getType();
            case 2: return vehicle.getBrand();
            case 3: return vehicle.getModel();
            case 4: return vehicle.getDailyRate();
            case 5: return vehicle.getStatus();
            case 6:
                if (vehicle instanceof ElectricVehicle) {
                    return ((ElectricVehicle) vehicle).getBatteryLevel() + "%";
                }
                return "-";
            default: return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }

    public Vehicle getVehicleAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= vehicles.size()) {
            return null;
        }
        return vehicles.get(rowIndex);
    }
}
