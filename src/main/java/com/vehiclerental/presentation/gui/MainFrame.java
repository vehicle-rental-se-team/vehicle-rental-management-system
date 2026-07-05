package com.vehiclerental.presentation.gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
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

    private final AppContext appContext;
    private final VehicleRentalApplication application;
    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final JLabel pageTitle;
    private final JLabel pageSubtitle;
    private final Map<String, JButton> menuButtons;

    public MainFrame(AppContext appContext, VehicleRentalApplication application) {
        this.appContext = appContext;
        this.application = application;
        this.cardLayout = new CardLayout();
        this.contentPanel = new JPanel(cardLayout);
        this.pageTitle = new JLabel();
        this.pageSubtitle = new JLabel();
        this.menuButtons = new LinkedHashMap<>();

        setTitle("Vehicle Rental Management System");
        setSize(1180, 720);
        setMinimumSize(new Dimension(1020, 620));
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
        sidebar.setPreferredSize(new Dimension(245, 720));
        sidebar.setBackground(UiTheme.SIDEBAR);

        JLabel logo = new JLabel("  Vehicle Rental");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("SansSerif", Font.BOLD, 20));
        logo.setBorder(BorderFactory.createEmptyBorder(26, 14, 26, 14));
        sidebar.add(logo, BorderLayout.NORTH);

        JPanel menu = new JPanel(new GridLayout(8, 1, 0, 2));
        menu.setBackground(UiTheme.SIDEBAR);
        menu.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        addMenuButton(menu, DASHBOARD, "Dashboard");
        addMenuButton(menu, VEHICLES, "Vehicle Catalog");
        addMenuButton(menu, RENTALS, "Rent Vehicle");
        addMenuButton(menu, RETURNS, "Returns");
        addMenuButton(menu, BILLING, "Billing");

        sidebar.add(menu, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Logout");
        UiTheme.styleSidebarButton(logoutButton);
        logoutButton.addActionListener(e -> application.logout(this));

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UiTheme.SIDEBAR);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 10, 18, 10));
        footer.add(logoutButton, BorderLayout.CENTER);
        sidebar.add(footer, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createMainArea() {
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(UiTheme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiTheme.BACKGROUND);
        header.setBorder(BorderFactory.createEmptyBorder(24, 28, 16, 28));

        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setBackground(UiTheme.BACKGROUND);

        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 26));
        pageTitle.setForeground(UiTheme.TEXT);
        pageSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        pageSubtitle.setForeground(UiTheme.MUTED);

        titles.add(pageTitle);
        titles.add(pageSubtitle);

        JLabel userLabel = new JLabel("Logged in as: " + appContext.getAuthenticationService().getLoggedInManager().getUsername());
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        userLabel.setForeground(UiTheme.PRIMARY_DARK);

        header.add(titles, BorderLayout.WEST);
        header.add(userLabel, BorderLayout.EAST);

        contentPanel.setBackground(UiTheme.BACKGROUND);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 28, 28, 28));
        contentPanel.add(new DashboardPanel(appContext), DASHBOARD);
        contentPanel.add(new VehicleCatalogPanel(appContext), VEHICLES);
        contentPanel.add(new PlaceholderPanel("Rental Operations", "Sprint 2 will be added here: rent vehicle, prevent double booking, and duration limits."), RENTALS);
        contentPanel.add(new PlaceholderPanel("Returns Management", "Sprint 4 will be added here: return vehicle and close rental records."), RETURNS);
        contentPanel.add(new PlaceholderPanel("Billing", "Sprint 4 and Sprint 5 billing, penalties, and type-specific pricing will be added here."), BILLING);

        mainArea.add(header, BorderLayout.NORTH);
        mainArea.add(contentPanel, BorderLayout.CENTER);

        return mainArea;
    }

    private void addMenuButton(JPanel menu, String pageKey, String label) {
        JButton button = new JButton(label);
        UiTheme.styleSidebarButton(button);
        button.addActionListener(e -> showPage(pageKey));
        menuButtons.put(pageKey, button);
        menu.add(button);
    }

    private void showPage(String pageKey) {
        cardLayout.show(contentPanel, pageKey);
        updateHeader(pageKey);
        updateActiveMenu(pageKey);
    }

    private void updateHeader(String pageKey) {
        if (DASHBOARD.equals(pageKey)) {
            pageTitle.setText("Dashboard");
            pageSubtitle.setText("Sprint 1 overview and system status.");
        } else if (VEHICLES.equals(pageKey)) {
            pageTitle.setText("Vehicle Catalog");
            pageSubtitle.setText("Only available vehicles are displayed for Sprint 1.");
        } else if (RENTALS.equals(pageKey)) {
            pageTitle.setText("Rent Vehicle");
            pageSubtitle.setText("Prepared for Sprint 2 rental operations.");
        } else if (RETURNS.equals(pageKey)) {
            pageTitle.setText("Returns");
            pageSubtitle.setText("Prepared for Sprint 4 return operations.");
        } else if (BILLING.equals(pageKey)) {
            pageTitle.setText("Billing");
            pageSubtitle.setText("Prepared for rental cost and late penalty screens.");
        }
    }

    private void updateActiveMenu(String activePage) {
        for (Map.Entry<String, JButton> entry : menuButtons.entrySet()) {
            if (entry.getKey().equals(activePage)) {
                UiTheme.styleActiveSidebarButton(entry.getValue());
            } else {
                UiTheme.styleSidebarButton(entry.getValue());
            }
        }
    }
}
