package com.vehiclerental.presentation.gui;

import com.vehiclerental.repository.ManagerRepository;
import com.vehiclerental.repository.RentalRepository;
import com.vehiclerental.repository.VehicleRepository;
import com.vehiclerental.service.AuthenticationService;
import com.vehiclerental.service.BillingService;
import com.vehiclerental.service.RentalService;
import com.vehiclerental.service.ReturnService;
import com.vehiclerental.service.VehicleCatalogService;
import com.vehiclerental.service.notification.EmailNotificationService;
import com.vehiclerental.service.notification.NotificationService;
import com.vehiclerental.service.reminder.RentalReminderService;

public class AppContext {

    private final ManagerRepository managerRepository;
    private final VehicleRepository vehicleRepository;
    private final RentalRepository rentalRepository;
    private final AuthenticationService authenticationService;
    private final VehicleCatalogService vehicleCatalogService;
    private final RentalService rentalService;
    private final BillingService billingService;
    private final ReturnService returnService;
    private final NotificationService notificationService;
    private final RentalReminderService rentalReminderService;

    public AppContext() {
        this.managerRepository = new ManagerRepository();
        this.vehicleRepository = new VehicleRepository();
        this.rentalRepository = new RentalRepository();

        this.authenticationService = new AuthenticationService(managerRepository);
        this.vehicleCatalogService = new VehicleCatalogService(vehicleRepository, authenticationService);
        this.rentalService = new RentalService(vehicleRepository, rentalRepository, authenticationService);
        this.billingService = new BillingService(20.0);
        this.returnService = new ReturnService(rentalRepository, billingService, authenticationService);
        this.notificationService = new EmailNotificationService();
        this.rentalReminderService = new RentalReminderService(notificationService);
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public VehicleCatalogService getVehicleCatalogService() {
        return vehicleCatalogService;
    }

    public RentalService getRentalService() {
        return rentalService;
    }

    public RentalRepository getRentalRepository() {
        return rentalRepository;
    }

    public BillingService getBillingService() {
        return billingService;
    }

    public ReturnService getReturnService() {
        return returnService;
    }

    public RentalReminderService getRentalReminderService() {
        return rentalReminderService;
    }
}
