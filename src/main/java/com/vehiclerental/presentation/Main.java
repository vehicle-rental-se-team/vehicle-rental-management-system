package com.vehiclerental.presentation;

import com.vehiclerental.presentation.gui.VehicleRentalApplication;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                VehicleRentalApplication application = new VehicleRentalApplication();
                application.start();
            }
        });
    }
}