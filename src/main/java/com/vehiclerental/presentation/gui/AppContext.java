package com.vehiclerental.presentation.gui;

import com.vehiclerental.repository.MaintenanceRepository;
import com.vehiclerental.repository.ManagerRepository;
import com.vehiclerental.repository.RentalRepository;
import com.vehiclerental.repository.VehicleIncidentRepository;
import com.vehiclerental.repository.VehicleFuelRepository;
import com.vehiclerental.repository.VehicleDocumentsRepository;
import com.vehiclerental.repository.VehicleRepository;
import com.vehiclerental.service.AuthenticationService;
import com.vehiclerental.service.BillingService;
import com.vehiclerental.service.ElectricVehicleMonitoringService;
import com.vehiclerental.service.MaintenanceService;
import com.vehiclerental.service.RentalService;
import com.vehiclerental.service.ReturnService;
import com.vehiclerental.service.VehicleCatalogService;
import com.vehiclerental.service.VehicleIncidentService;
import com.vehiclerental.service.VehicleAvailabilityService;
import com.vehiclerental.service.FuelMonitoringService;
import com.vehiclerental.service.VehicleDocumentsService;
import com.vehiclerental.service.VehicleHistoryService;
import com.vehiclerental.service.notification.EmailNotificationObserver;
import com.vehiclerental.service.notification.EmailNotificationService;
import com.vehiclerental.service.notification.NotificationPublisher;
import com.vehiclerental.service.notification.NotificationService;
import com.vehiclerental.service.reminder.RentalReminderService;

import java.time.LocalDate;

public class AppContext {

    private static final String MANAGER_NOTIFICATION_EMAIL =
            "manager@vehiclerental.com";

    private final ManagerRepository managerRepository;
    private final VehicleRepository vehicleRepository;
    private final RentalRepository rentalRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final VehicleIncidentRepository vehicleIncidentRepository;
    private final VehicleFuelRepository vehicleFuelRepository;
    private final VehicleDocumentsRepository vehicleDocumentsRepository;
    private final AuthenticationService authenticationService;
    private final VehicleCatalogService vehicleCatalogService;
    private final RentalService rentalService;
    private final BillingService billingService;
    private final ReturnService returnService;
    private final NotificationService notificationService;
    private final NotificationPublisher notificationPublisher;
    private final RentalReminderService rentalReminderService;
    private final ElectricVehicleMonitoringService electricVehicleMonitoringService;
    private final VehicleIncidentService vehicleIncidentService;
    private final MaintenanceService maintenanceService;
    private final VehicleAvailabilityService vehicleAvailabilityService;
    private final FuelMonitoringService fuelMonitoringService;
    private final VehicleDocumentsService vehicleDocumentsService;
    private final VehicleHistoryService vehicleHistoryService;

    public AppContext() {
        this.managerRepository = new ManagerRepository();
        this.vehicleRepository = new VehicleRepository();
        this.rentalRepository = new RentalRepository(vehicleRepository);
        this.maintenanceRepository = new MaintenanceRepository();
        this.vehicleIncidentRepository = new VehicleIncidentRepository();
        this.vehicleFuelRepository = new VehicleFuelRepository();
        this.vehicleDocumentsRepository = new VehicleDocumentsRepository();

        this.authenticationService = new AuthenticationService(managerRepository);
        this.vehicleCatalogService = new VehicleCatalogService(
                vehicleRepository,
                authenticationService
        );
        this.vehicleAvailabilityService = new VehicleAvailabilityService(
                rentalRepository,
                maintenanceRepository,
                vehicleIncidentRepository,
                vehicleFuelRepository,
                vehicleDocumentsRepository
        );
        this.rentalService = new RentalService(
                vehicleRepository,
                rentalRepository,
                authenticationService,
                maintenanceRepository,
                vehicleFuelRepository,
                vehicleDocumentsRepository
        );
        this.billingService = new BillingService(20.0);
        this.returnService = new ReturnService(
                rentalRepository,
                billingService,
                authenticationService,
                maintenanceRepository,
                vehicleRepository,
                vehicleAvailabilityService
        );

        this.notificationService = new EmailNotificationService();
        this.notificationPublisher = new NotificationPublisher();
        this.notificationPublisher.addObserver(
                new EmailNotificationObserver(notificationService)
        );

        this.rentalReminderService = new RentalReminderService(
                notificationPublisher
        );
        this.electricVehicleMonitoringService =
                new ElectricVehicleMonitoringService(
                        vehicleRepository,
                        rentalRepository,
                        notificationPublisher,
                        MANAGER_NOTIFICATION_EMAIL,
                        vehicleAvailabilityService
                );
        this.vehicleIncidentService = new VehicleIncidentService(
                vehicleRepository,
                vehicleIncidentRepository,
                notificationPublisher,
                authenticationService,
                rentalRepository,
                maintenanceRepository,
                MANAGER_NOTIFICATION_EMAIL,
                vehicleAvailabilityService
        );
        this.maintenanceService = new MaintenanceService(
                vehicleRepository,
                rentalRepository,
                maintenanceRepository,
                vehicleIncidentRepository,
                notificationPublisher,
                authenticationService,
                MANAGER_NOTIFICATION_EMAIL,
                vehicleAvailabilityService
        );
        this.fuelMonitoringService = new FuelMonitoringService(
                vehicleRepository,
                vehicleFuelRepository,
                vehicleAvailabilityService,
                notificationPublisher,
                authenticationService,
                MANAGER_NOTIFICATION_EMAIL
        );
        this.vehicleDocumentsService = new VehicleDocumentsService(
                vehicleRepository,
                vehicleDocumentsRepository,
                vehicleAvailabilityService,
                notificationPublisher,
                authenticationService,
                MANAGER_NOTIFICATION_EMAIL
        );
        this.vehicleHistoryService = new VehicleHistoryService(
                vehicleRepository,
                rentalRepository,
                vehicleIncidentRepository,
                maintenanceRepository,
                vehicleFuelRepository,
                vehicleDocumentsRepository,
                authenticationService
        );
        this.maintenanceService.checkMaintenance(LocalDate.now());
        this.vehicleDocumentsService.checkDocuments(LocalDate.now());
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

    public NotificationPublisher getNotificationPublisher() {
        return notificationPublisher;
    }

    public RentalReminderService getRentalReminderService() {
        return rentalReminderService;
    }

    public ElectricVehicleMonitoringService getElectricVehicleMonitoringService() {
        return electricVehicleMonitoringService;
    }

    public VehicleIncidentRepository getVehicleIncidentRepository() {
        return vehicleIncidentRepository;
    }

    public VehicleIncidentService getVehicleIncidentService() {
        return vehicleIncidentService;
    }

    public MaintenanceRepository getMaintenanceRepository() {
        return maintenanceRepository;
    }

    public MaintenanceService getMaintenanceService() {
        return maintenanceService;
    }
    public VehicleFuelRepository getVehicleFuelRepository() {
        return vehicleFuelRepository;
    }

    public VehicleDocumentsRepository getVehicleDocumentsRepository() {
        return vehicleDocumentsRepository;
    }

    public FuelMonitoringService getFuelMonitoringService() {
        return fuelMonitoringService;
    }

    public VehicleDocumentsService getVehicleDocumentsService() {
        return vehicleDocumentsService;
    }

    public VehicleHistoryService getVehicleHistoryService() {
        return vehicleHistoryService;
    }

    public VehicleAvailabilityService getVehicleAvailabilityService() {
        return vehicleAvailabilityService;
    }

}
