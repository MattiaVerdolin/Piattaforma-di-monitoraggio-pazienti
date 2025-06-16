# 🩺 Patient Monitoring Platform

## 📝 Overview  
This project is a **cloud-based patient monitoring platform**, developed as part of the **Semester Project in Computer Engineering at SUPSI** (academic year 2024/2025).  
The platform is designed to support **remote supervision of patients with pulmonary diseases**, by collecting biometric data from **Garmin wearable devices** and providing healthcare professionals with an **intuitive web interface** for data analysis and clinical record management.

<p align="center">
  <img src="https://github.com/user-attachments/assets/29c6c6c5-ba62-4c6f-8155-3b968ab92ad9" height="400px">
</p>

The application follows a **layered architecture**, with a **Spring Boot backend** and a **Thymeleaf-based frontend**, ensuring **modularity**, **scalability**, and **maintainability**.  
The user interface includes **dynamic visualizations** and **calendar-based views** for efficient patient tracking.

---

## 🎯 Features

### 📡 **Real-Time Health Data Monitoring**
- Automatically collects data from **Garmin smartwatches** via the **Garmin Health API**.
- Supports key summaries:
  - **Daily Summary** – Steps, calories, heart rate, stress, etc.
  - **Pulse Oximetry** – SpO₂ values.
  - **Stress Levels** – With detailed time-based sampling.

### 🔐 **Secure Integration with Garmin Connect**
- Uses the **OAuth 1.0a protocol** for secure authorization.
- Seamless connection between **patient Garmin accounts** and the platform.
- **Push notifications** enable near real-time data synchronization from Garmin servers.

### 📊 **Dashboard & Visualization**
- Interactive dashboard with visualizations powered by **Chart.js**.
  <p align="center">
  <img src="https://github.com/user-attachments/assets/b9c7f40d-a0b8-492d-8f2e-78e5b61a3789" height="400px">
  </p>

- **FullCalendar** integration to display historical health data.
    <p align="center">
  <img src="https://github.com/user-attachments/assets/7bec7785-48a9-4653-aeb9-02342eaa73ec" height="400px">
    </p>
- Responsive UI for both **desktop and mobile** access.

### 🗂️ **Clinical Record Management**
- Doctors can **view and manage** each patient’s clinical records directly in the web app.
- Patients can be **registered, linked to a device**, and **monitored continuously**.
  <p align="center">
  <img src="https://github.com/user-attachments/assets/f4168b47-2950-4a53-98ad-213ef116c324" height="400px">
  </p>

### 🔄 **Modular Data Parsing**
- Highly **extensible architecture** for supporting new summary types.
- Parsing system uses **interface-based design** (`SummaryParser`) and **automatic discovery** via Spring dependency injection.

---

## 🔧 Technologies Used
- **Backend:** Java, Spring Boot, REST APIs  
- **Frontend:** Thymeleaf, Bootstrap, Chart.js, FullCalendar  
- **Database:** mySQL
- **Data Sync:** Garmin Health API (OAuth 1.0a, Push notifications)  
- **Version Control:** Git, GitHub

---

## 🛠️ How to Run the Project

1. **Clone the repository**:
```bash
git clone git@github.com:MattiaVerdolin/Piattaforma-di-monitoraggio-pazienti.git
```

2. **Navigate to the project directory**:
```bash
cd Piattaforma-di-monitoraggio-pazienti
```

3. **Configure application properties**:  
Edit the `src/main/resources/application.properties` file and insert the following required information:

```properties
# Garmin Developer API credentials
garmin.api.consumerKey=YOUR_CONSUMER_KEY
garmin.api.consumerSecret=YOUR_CONSUMER_SECRET

# Database credentials
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
```

4. **Build and run the application using Maven**:
```bash
mvn spring-boot:run
```

5. **Access the application**:  
Open your browser and go to:
```
http://localhost:8080
```

---

## 👥 Team
- **Mattia Verdolin** 📧 [mattia.verdolin@student.supsi.ch](mailto:mattia.verdolin@student.supsi.ch)  
- **Francisco Viola** 📧 [francisco.viola@student.supsi.ch](mailto:francisco.viola@student.supsi.ch)  
- **Supervisor:** Salvatore Vanini

---

## 📜 License
This software was developed for **educational purposes** as part of the **Bachelor of Science in Computer Engineering at SUPSI**.  
Usage beyond the course may require prior authorization from the authors.
