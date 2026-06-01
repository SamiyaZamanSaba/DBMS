# Hospital Management System

## Overview
The Hospital Management System is a database management project designed to automate and manage hospital operations efficiently. The system helps maintain records of patients, doctors, appointments, treatments, and billing information in a centralized database.

## Objectives
- Manage patient records efficiently.
- Maintain doctor information and specialization details.
- Schedule and track appointments.
- Store medical records and treatment history.
- Generate and manage billing information.

## Features
- Patient Registration and Management
- Doctor Management
- Appointment Scheduling
- Medical Records Management
- Billing and Payment Tracking
- Database Search and Reporting

## Database Tables

### Patients
- Patient_ID (Primary Key)
- Name
- Age
- Gender
- Contact_Number
- Address

### Doctors
- Doctor_ID (Primary Key)
- Name
- Specialization
- Contact_Number

### Appointments
- Appointment_ID (Primary Key)
- Patient_ID (Foreign Key)
- Doctor_ID (Foreign Key)
- Appointment_Date
- Status

### Medical_Records
- Record_ID (Primary Key)
- Patient_ID (Foreign Key)
- Diagnosis
- Treatment
- Prescription

### Billing
- Bill_ID (Primary Key)
- Patient_ID (Foreign Key)
- Amount
- Payment_Status

## ER Diagram
Add your ER Diagram image here.

Example:

![ER Diagram](docs/ER_Diagram.png)

## Technologies Used
- MySQL
- SQL
- VS Code
- Git & GitHub

## Installation

1. Clone the repository:

```bash
git clone https://github.com/yourusername/Hospital-Management-System.git
```

2. Open MySQL and create a database.

3. Import the SQL schema:

```sql
SOURCE schema.sql;
```

4. Import sample data if available.

## Project Structure

```
Hospital-Management-System/
│
├── database/
│   ├── schema.sql
│   └── sample_data.sql
│
├── docs/
│   └── ER_Diagram.png
│
├── src/
│   └── source_code_files
│
├── README.md
└── LICENSE
```

## Future Enhancements
- Online Appointment Booking
- Email/SMS Notifications
- Role-Based Access Control
- Patient Portal
- Analytics Dashboard

## Author
Your Name

## License
This project is developed for academic and educational purposes.
