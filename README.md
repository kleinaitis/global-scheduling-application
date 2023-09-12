
<a name="readme-top"></a>
<br />
<div align="center">
  <a href="https://github.com/kleinaitis/global-scheduling-application">
    <img width=50% src="logo-no-background.png" alt="Logo">
  </a>

  <p align="center">
The "Global Scheduling Application" is a sophisticated, Java-based desktop application designed with a user-friendly graphical interface for efficient appointment scheduling. Tailored for a mock software company operating internationally, this powerful tool seamlessly manages scheduling complexities for customers across multiple languages and diverse global time zones. With a robust backend powered by a MySQL server integration, it ensures secure and reliable data storage, making it ideal for any company operating internationally.    <br />
    <a href="https://github.com/kleinaitis/global-scheduling-application/issues">Report a Bug</a>
    ·
    <a href="https://github.com/kleinaitis/global-scheduling-application/issues">Request a Feature</a>
  </p>
</div>

## Key Features

- **Customer and Appointment Management:** Effortlessly create, update, and delete customer accounts, providing a comprehensive view of your clientele. Seamlessly manage appointments, enabling efficient scheduling and rescheduling of important events.


- **Intuitive User Interface:** Experience a sleek and visually appealing user interface designed with JavaFX, making scheduling and management tasks a breeze.


- **Secure Data Storage:** Utilize the power of MySQL server integration to securely and reliably store all scheduling data, ensuring data integrity and accessibility.


- **Advanced Reporting:** Generate various reports to gain insights into your scheduling data. Get appointments categorized by type, filter by country, review monthly schedules, or obtain a detailed schedule of appointments with specific contacts.


## Getting Started

To quickly set up and run the Global Scheduling Application using command-line instructions:

### Prerequisites

Ensure you have the following tools installed:

- [Java SDK (Latest Version)](https://www.oracle.com/java/technologies/downloads/)
- [JavaFX SDK (Compatible with Java SDK)](https://openjfx.io/)
- [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/)
- [MySQL Database (Latest Version)](https://www.mysql.com/downloads/)

### Quick Setup

1. Clone the repository:

   ```bash
   git clone https://github.com/kleinaitis/global-scheduling-application.git
   cd global-scheduling-application
   ```
2. Set up JavaFX:

   Windows:
   
   ```bash
   set PATH_TO_JAVAFX="C:\path\to\javafx-sdk-19\lib"
   java --module-path %PATH_TO_JAVAFX% --add-modules javafx.controls,javafx.fxml -cp . main.Main
   ```
   macOS/Linux:
 
   ```bash
    export PATH_TO_JAVAFX="/path/to/javafx-sdk-19/lib"
    java --module-path $PATH_TO_JAVAFX --add-modules javafx.controls,javafx.fxml -cp . main.Main
   ```

3. Download MySQL Connector Java 8.0.33 and place the JAR file in a directory called mysql-connector within your project's root directory.
4. Open a MySQL client or command-line tool and log in as a user with appropriate privileges.

5. Run the SQL script to create the necessary tables and database schema. Execute the script in the MySQL client using the following command:
   ```bash
   mysql -u your_username -p < global-scheduling-application/scheduling-database.sql
6. Run the application:
    ```bash
    java -cp . main.Main
    ```
7. Use the following test credentials to log in:

    - Username: admin, Password: admin
    - Username: test, Password: test


<p align="right">(<a href="#readme-top">back to top</a>)</p>
