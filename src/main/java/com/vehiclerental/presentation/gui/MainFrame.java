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

        UiTheme.applyGlobalTheme();

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
        sidebar.setPreferredSize(new Dimension(285, 720));
        sidebar.setBackground(UiTheme.SIDEBAR);
        sidebar.setOpaque(true);

        sidebar.add(createBrandPanel(), BorderLayout.NORTH);

        JPanel menu = new JPanel(new GridLayout(8, 1, 0, 8));
        menu.setBackground(UiTheme.SIDEBAR);
        menu.setOpaque(true);
        menu.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        addMenuButton(menu, DASHBOARD, "  Dashboard");
        addMenuButton(menu, VEHICLES, "  Vehicle Catalog");
        addMenuButton(menu, RENTALS, "  Rent Vehicle");
        addMenuButton(menu, RETURNS, "  Returns");
        addMenuButton(menu, BILLING, "  Billing");

        sidebar.add(menu, BorderLayout.CENTER);

        JButton logoutButton = new JButton("  Logout");
        UiTheme.styleSidebarButton(logoutButton);
        logoutButton.setHorizontalAlignment(SwingConstants.LEFT);
        logoutButton.addActionListener(e -> application.logout(this));

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UiTheme.SIDEBAR);
        footer.setOpaque(true);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 14, 20, 14));
        footer.add(logoutButton, BorderLayout.CENTER);

        sidebar.add(footer, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createBrandPanel() {
        JPanel brandPanel = new JPanel(new BorderLayout(0, 10));
        brandPanel.setBackground(UiTheme.SIDEBAR);
        brandPanel.setOpaque(true);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(18, 14, 14, 14));

        JLabel logoLabel = UiTheme.logoImageLabel("/images/vehicle-rental-logo.png", 250, 84);

        JPanel logoWrapper = new JPanel(new BorderLayout());
        logoWrapper.setBackground(UiTheme.SIDEBAR);
        logoWrapper.setOpaque(true);
        logoWrapper.add(logoLabel, BorderLayout.CENTER);

        JLabel title = new JLabel("Vehicle Rental");
        UiTheme.styleLabelAsBrand(title);

        JLabel subtitle = new JLabel("Management System");
        UiTheme.styleMutedLabel(subtitle);

        JPanel textPanel = new JPanel(new BorderLayout(0, 3));
        textPanel.setBackground(UiTheme.SIDEBAR);
        textPanel.setOpaque(true);
        textPanel.add(title, BorderLayout.NORTH);
        textPanel.add(subtitle, BorderLayout.CENTER);

        brandPanel.add(logoWrapper, BorderLayout.NORTH);
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
        header.setBorder(BorderFactory.createEmptyBorder(24, 28, 16, 28));

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
                "Logged in as: " + appContext.getAuthenticationService().getLoggedInManager().getUsername()
        );
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        userLabel.setForeground(UiTheme.MUTED);

        header.add(titles, BorderLayout.WEST);
        header.add(userLabel, BorderLayout.EAST);

        contentPanel.setBackground(UiTheme.BACKGROUND);
        contentPanel.setOpaque(true);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 28, 28, 28));

        contentPanel.add(new DashboardPanel(appContext), DASHBOARD);
        contentPanel.add(new VehicleCatalogPanel(appContext), VEHICLES);
        contentPanel.add(new RentalPanel(appContext), RENTALS);
        contentPanel.add(
                new PlaceholderPanel(
                        "Returns Management",
                        "Sprint 4 will be added here: return vehicle and close rental records."
                ),
                RETURNS
        );
        contentPanel.add(
                new PlaceholderPanel(
                        "Billing",
                        "Sprint 4 and Sprint 5 billing, penalties, and type-specific pricing will be added here."
                ),
                BILLING
        );

        mainArea.add(header, BorderLayout.NORTH);
        mainArea.add(contentPanel, BorderLayout.CENTER);

        return mainArea;
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
        cardLayout.show(contentPanel, pageKey);
        updateHeader(pageKey);
        updateActiveMenu(pageKey);
    }

    private void updateHeader(String pageKey) {
        if (DASHBOARD.equals(pageKey)) {
            pageTitle.setText("Dashboard");
            pageSubtitle.setText("Sprint 2 GUI overview and system status.");
        } else if (VEHICLES.equals(pageKey)) {
            pageTitle.setText("Vehicle Catalog");
            pageSubtitle.setText("Available vehicles are displayed with the updated blue and gold interface.");
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