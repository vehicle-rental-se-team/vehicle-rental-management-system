package com.vehiclerental.presentation.gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    private static final String DASHBOARD = "dashboard";
    private static final String VEHICLES = "vehicles";
    private static final String RENTALS = "rentals";
    private static final String RETURNS = "returns";
    private static final String BILLING = "billing";
    private static final String MANAGEMENT = "management";
    private static final String INCIDENTS = "incidents";
    private static final String MAINTENANCE = "maintenance";

    private final AppContext appContext;
    private final VehicleRentalApplication application;
    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final JLabel pageTitle;
    private final JLabel pageSubtitle;
    private final Map<String, JButton> menuButtons;
    private final Map<String, JPanel> pages;

    public MainFrame(AppContext appContext, VehicleRentalApplication application) {
        this.appContext = appContext;
        this.application = application;
        this.cardLayout = new CardLayout();
        this.contentPanel = new JPanel(cardLayout);
        this.pageTitle = new JLabel();
        this.pageSubtitle = new JLabel();
        this.menuButtons = new LinkedHashMap<>();
        this.pages = new LinkedHashMap<>();

        UiTheme.applyGlobalTheme();

        setTitle("Vehicle Rental Management System");
        setSize(1240, 760);
        setMinimumSize(new Dimension(1080, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UiTheme.BACKGROUND);

        add(createSidebar(), BorderLayout.WEST);
        add(createMainArea(), BorderLayout.CENTER);

        showPage(DASHBOARD);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(285, 760));
        sidebar.setBackground(UiTheme.SIDEBAR);
        sidebar.setOpaque(true);

        sidebar.add(createBrandPanel(), BorderLayout.NORTH);

        JPanel menu = new JPanel(new GridLayout(8, 1, 0, 6));
        menu.setBackground(UiTheme.SIDEBAR);
        menu.setOpaque(true);
        menu.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        addMenuButton(menu, DASHBOARD, "  Dashboard");
        addMenuButton(menu, VEHICLES, "  Vehicle Catalog");
        addMenuButton(menu, RENTALS, "  Rent Vehicle");
        addMenuButton(menu, RETURNS, "  Returns");
        addMenuButton(menu, BILLING, "  Billing");
        addMenuButton(menu, MANAGEMENT, "  Vehicle Management");
        addMenuButton(menu, INCIDENTS, "  Incidents");
        addMenuButton(menu, MAINTENANCE, "  Maintenance");

        sidebar.add(menu, BorderLayout.CENTER);

        JButton logoutButton = new JButton("  Logout");
        UiTheme.styleSidebarButton(logoutButton);
        logoutButton.setHorizontalAlignment(SwingConstants.LEFT);
        logoutButton.addActionListener(e -> application.logout(this));

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UiTheme.SIDEBAR);
        footer.setOpaque(true);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 14, 16, 14));
        footer.add(logoutButton, BorderLayout.CENTER);

        sidebar.add(footer, BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel createBrandPanel() {
        JPanel brandPanel = new JPanel(new BorderLayout(0, 8));
        brandPanel.setBackground(UiTheme.SIDEBAR);
        brandPanel.setOpaque(true);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(14, 14, 10, 14));

        JLabel logoLabel = UiTheme.logoImageLabel(
                "/images/vehicle-rental-logo.png", 250, 70
        );

        JLabel title = new JLabel("Vehicle Rental");
        UiTheme.styleLabelAsBrand(title);

        JLabel subtitle = new JLabel("Management System");
        UiTheme.styleMutedLabel(subtitle);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setBackground(UiTheme.SIDEBAR);
        textPanel.setOpaque(true);
        textPanel.add(title);
        textPanel.add(subtitle);

        brandPanel.add(logoLabel, BorderLayout.NORTH);
        brandPanel.add(textPanel, BorderLayout.CENTER);
        return brandPanel;
    }

    private JPanel createMainArea() {
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(UiTheme.BACKGROUND);
        mainArea.setOpaque(true);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiTheme.BACKGROUND);
        header.setOpaque(true);
        header.setBorder(BorderFactory.createEmptyBorder(20, 28, 14, 28));

        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setBackground(UiTheme.BACKGROUND);
        titles.setOpaque(true);

        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 26));
        pageTitle.setForeground(UiTheme.TEXT);
        pageSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        pageSubtitle.setForeground(UiTheme.MUTED);

        titles.add(pageTitle);
        titles.add(pageSubtitle);

        JLabel userLabel = new JLabel(
                "Logged in as: "
                        + appContext.getAuthenticationService()
                        .getLoggedInManager().getUsername()
        );
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        userLabel.setForeground(UiTheme.MUTED);

        header.add(titles, BorderLayout.WEST);
        header.add(userLabel, BorderLayout.EAST);

        contentPanel.setBackground(UiTheme.BACKGROUND);
        contentPanel.setOpaque(true);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 28, 28, 28));

        registerPage(DASHBOARD, new DashboardPanel(appContext));
        registerPage(VEHICLES, new VehicleCatalogPanel(appContext));
        registerPage(RENTALS, new RentalPanel(appContext));
        registerPage(RETURNS, new ReturnPanel(appContext));
        registerPage(BILLING, new BillingPanel(appContext));
        registerPage(MANAGEMENT, new VehicleManagementPanel(appContext));
        registerPage(INCIDENTS, new IncidentManagementPanel(appContext));
        registerPage(MAINTENANCE, new MaintenanceManagementPanel(appContext));

        mainArea.add(header, BorderLayout.NORTH);
        mainArea.add(contentPanel, BorderLayout.CENTER);
        return mainArea;
    }

    private void registerPage(String key, JPanel panel) {
        pages.put(key, panel);
        contentPanel.add(panel, key);
    }

    private void addMenuButton(JPanel menu, String pageKey, String label) {
        JButton button = new JButton(label);
        UiTheme.styleSidebarButton(button);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.addActionListener(e -> showPage(pageKey));

        menuButtons.put(pageKey, button);
        menu.add(button);
    }

    private void showPage(String pageKey) {
        JPanel page = pages.get(pageKey);
        if (page instanceof RefreshablePanel) {
            ((RefreshablePanel) page).refreshData();
        }

        cardLayout.show(contentPanel, pageKey);
        updateHeader(pageKey);
        updateActiveMenu(pageKey);
    }

    private void updateHeader(String pageKey) {
        if (DASHBOARD.equals(pageKey)) {
            pageTitle.setText("Dashboard");
            pageSubtitle.setText("System status, reminders, and recent notifications.");
        } else if (VEHICLES.equals(pageKey)) {
            pageTitle.setText("Vehicle Catalog");
            pageSubtitle.setText("View vehicles that are currently available for rental.");
        } else if (RENTALS.equals(pageKey)) {
            pageTitle.setText("Rent Vehicle");
            pageSubtitle.setText("Create rentals and apply vehicle-specific rules.");
        } else if (RETURNS.equals(pageKey)) {
            pageTitle.setText("Returns");
            pageSubtitle.setText("Return vehicles and close active rental records.");
        } else if (BILLING.equals(pageKey)) {
            pageTitle.setText("Billing");
            pageSubtitle.setText("Calculate base cost and late return penalties.");
        } else if (MANAGEMENT.equals(pageKey)) {
            pageTitle.setText("Vehicle Management");
            pageSubtitle.setText("Update energy, documents, and view vehicle history.");
        } else if (INCIDENTS.equals(pageKey)) {
            pageTitle.setText("Incidents and Violations");
            pageSubtitle.setText("Record events and complete inspections after accidents.");
        } else if (MAINTENANCE.equals(pageKey)) {
            pageTitle.setText("Maintenance");
            pageSubtitle.setText("Schedule six-month maintenance and complete service records.");
        }
    }

    private void updateActiveMenu(String activePage) {
        for (Map.Entry<String, JButton> entry : menuButtons.entrySet()) {
            JButton button = entry.getValue();

            if (entry.getKey().equals(activePage)) {
                UiTheme.styleActiveSidebarButton(button);
            } else {
                UiTheme.styleSidebarButton(button);
            }

            button.setHorizontalAlignment(SwingConstants.LEFT);
        }
    }
}
