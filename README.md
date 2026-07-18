# Vehicle Rental Management System

## Project Overview

The Vehicle Rental Management System is a Java desktop application that helps a rental company manage its vehicles and daily rental operations. The system allows the manager to add and view vehicles, create rentals, return vehicles, calculate bills, manage maintenance, record incidents, follow vehicle documents, and check fuel or battery levels.

The project was developed as a team project using Java, Swing, Maven, JUnit 5, Mockito, and JaCoCo. The code is divided into clear layers so that each part of the system has a specific responsibility.

## Main Features

The system provides the following features:

- Manager login and logout.
- Add and manage vehicles.
- Display available and rented vehicles.
- Create new rental records.
- Prevent renting an unavailable vehicle.
- Return rented vehicles.
- Calculate the basic rental cost and late-return penalty.
- Schedule and complete vehicle maintenance.
- Record accidents and violations.
- Store and check vehicle registration and insurance dates.
- Monitor fuel levels for normal vehicles.
- Monitor battery levels for electric vehicles.
- Send reminder messages through the Observer pattern.
- Display vehicle history.
- Save data in text files so that it remains available after restarting the program.

## Technologies Used

- Java 8 or later
- Java Swing for the graphical user interface
- Maven for project management
- JUnit 5 for unit testing
- Mockito for mocking dependencies
- JaCoCo for code coverage
- PlantUML for UML class diagrams
- Git and GitHub for version control

## Project Architecture

The project follows a layered architecture:

### Domain Layer

Contains the main objects used by the system, such as:

- `Vehicle`
- `Rental`
- `Customer`
- `MaintenanceRecord`
- `VehicleIncident`
- `VehicleDocuments`
- `FuelRecord`

### Repository Layer

Responsible for reading and writing data from text files. Examples include:

- `VehicleRepository`
- `RentalRepository`
- `MaintenanceRepository`
- `VehicleIncidentRepository`
- `VehicleDocumentsRepository`

### Service Layer

Contains the main business rules of the system, such as rental validation, billing, returns, maintenance, documents, incidents, and monitoring.

### Presentation Layer

Contains the Swing graphical user interface, including the login screen, dashboard, vehicle catalog, rental screen, return screen, billing screen, maintenance screen, and other management panels.

## Design Patterns

### Strategy Pattern

The Strategy pattern is used to apply different rental validation rules depending on the vehicle type. For example, electric vehicles, motorcycles, trucks, and standard vehicles can each use their own validation strategy.

Main classes:

- `RentalValidationStrategy`
- `StandardValidationStrategy`
- `ElectricVehicleValidationStrategy`
- `MotorcycleValidationStrategy`
- `TruckValidationStrategy`

### Observer Pattern

The Observer pattern is used for notifications and reminders. When a notification is published, the registered observers receive the message.

Main classes:

- `NotificationPublisher`
- `NotificationObserver`
- `EmailNotificationObserver`
- `NotificationLogObserver`

The current email notification service simulates sending an email by printing the message and saving it in the notification log.

## How to Run the Project

### Using IntelliJ IDEA

1. Open IntelliJ IDEA.
2. Select **Open** and choose the project folder.
3. Wait until Maven finishes loading the dependencies.
4. Open:

```text
src/main/java/com/vehiclerental/presentation/Main.java
```

5. Run the `main` method.

### Using Maven

Open a terminal inside the project folder and run:

```bash
mvn clean compile
```

Then run the `Main` class from IntelliJ IDEA.

## Login Accounts

The project includes sample manager accounts stored in:

```text
data/managers.txt
```

Example account:

```text
Username: admin
Password: admin123
```

These accounts are included for project demonstration only.

## Running the Tests

To run all unit tests, use:

```bash
mvn clean test
```

To run the tests and generate the JaCoCo coverage report, use:

```bash
mvn clean verify
```

The coverage report can be opened from:

```text
target/site/jacoco/index.html
```

The presentation package is excluded from JaCoCo because Swing GUI classes are tested manually, while the main business logic is covered by unit tests.

## Javadoc Documentation

Generated Javadoc documentation is available at:

```text
docs/javadoc/index.html
```

Open this file in a browser to view the documented classes, fields, constructors, and methods.

## UML Diagrams

The UML source files and exported diagrams are stored in:

```text
docs/uml/
```

The diagrams are divided by package to keep them readable. They include the domain, repositories, services, strategies, notifications, reminders, exceptions, and GUI classes.

## Data Files

The system stores its information inside the `data` folder:

```text
data/vehicles.txt
data/rentals.txt
data/maintenance.txt
data/incidents.txt
data/fuel.txt
data/vehicle_documents.txt
data/managers.txt
```

These files should remain inside the project folder because the application uses them during execution.

## Project Structure

```text
src/
├── main/
│   └── java/com/vehiclerental/
│       ├── domain/
│       ├── exception/
│       ├── presentation/
│       ├── repository/
│       ├── service/
│       └── strategy/
└── test/
    └── java/com/vehiclerental/

data/
docs/
pom.xml
README.md
```

## Notes

- The project uses text files instead of a database to keep the implementation simple and suitable for the course requirements.
- The GUI was created using Java Swing.
- Unit tests focus mainly on domain, repository, service, strategy, notification, and reminder logic.
- UML and Javadoc files are included in the `docs` folder.

## Team Members

Add the names and university IDs of the team members here before the final submission.
