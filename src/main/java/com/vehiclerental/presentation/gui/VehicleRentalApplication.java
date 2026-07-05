package com.vehiclerental.presentation.gui;

import javax.swing.UIManager;

public class VehicleRentalApplication {

    private final AppContext appContext;

    public VehicleRentalApplication() {
        this.appContext = new AppContext();
    }

    public void start() {
        configureLookAndFeel();
        showLoginWindow();
    }

    public void showLoginWindow() {
        LoginFrame loginFrame = new LoginFrame(appContext, this);
        loginFrame.setVisible(true);
    }

    public void showMainWindow() {
        MainFrame mainFrame = new MainFrame(appContext, this);
        mainFrame.setVisible(true);
    }

    public void logout(MainFrame mainFrame) {
        appContext.getAuthenticationService().logout();
        mainFrame.dispose();
        showLoginWindow();
    }

    private void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }
}
