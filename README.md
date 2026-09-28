```markdown
# CropAdvisor 🌱

## Crop Disease Query and Advisory Ticketing System

CropAdvisor is a web-based agricultural advisory ticketing system that connects farmers with agricultural officers. Farmers can raise crop disease and agricultural advisory queries, while agricultural officers can manage assigned tickets, provide recommendations, close tickets, and monitor escalated requests.

The system provides region-based officer assignment, ticket lifecycle management, ticket reopening, and automatic 48-hour escalation monitoring.

---

## 📌 Project Overview

CropAdvisor provides a structured digital workflow for farmers to submit agricultural advisory requests and for agricultural officers to manage those requests.

The system provides two separate portals:

- **Farmer Portal** – Raise advisory tickets and track submitted requests.
- **Agricultural Officer Portal** – View assigned tickets, provide recommendations, close tickets, and manage escalated tickets.

Each farmer belongs to a region. When a ticket is created, the farmer's region is automatically identified and an agricultural officer from that region is assigned to the ticket.

---

## 🎯 Objectives

- Provide a digital platform for farmers to raise agricultural advisory queries.
- Automatically associate farmers with their respective regions.
- Automatically assign tickets to agricultural officers based on region.
- Allow officers to provide recommendations for crop-related issues.
- Track the complete ticket lifecycle.
- Prevent unauthorized officers from modifying tickets.
- Allow farmers to reopen their own closed tickets.
- Automatically identify tickets exceeding the 48-hour threshold.
- Provide separate interfaces for farmers and agricultural officers.
- Store farmer, officer, region, and ticket information using MySQL.

---

## ✨ Key Features

### 👨‍🌾 Farmer Portal

- Farmer profile selection
- Raise new crop advisory tickets
- Enter crop name and symptoms
- View submitted tickets
- Track ticket status
- View assigned agricultural officer
- View officer recommendations
- Reopen eligible closed tickets

### 👨‍🔬 Agricultural Officer Portal

- Officer profile selection
- View assigned tickets
- View escalated tickets
- View farmer and crop information
- Add recommendations
- Move tickets to `IN_PROGRESS`
- Close tickets after providing recommendations
- Manage assigned tickets

### 🎫 Ticket Management

Tickets follow a controlled lifecycle:

```text
OPEN
  ↓
IN_PROGRESS
  ↓
CLOSED
```

A closed ticket can be reopened by its owning farmer:

```text
CLOSED
   ↓
REOPEN
   ↓
OPEN
```

### ⏱️ Automatic Escalation

- Normal tickets are monitored based on their creation time.
- Reopened tickets are monitored from their reopening time.
- Tickets exceeding 48 hours are marked as `ESCALATED`.
- Exactly 48 hours does not trigger escalation.
- The application periodically checks tickets using a scheduled process.

---

## 🔄 Ticket Workflow

```text
                 Farmer
                    │
                    ▼
          Raise Advisory Ticket
                    │
                    ▼
             Ticket Created
                    │
                    ▼
          Farmer Region Identified
                    │
                    ▼
       Officer Automatically Assigned
                    │
                    ▼
                  OPEN
                    │
                    ▼
        Officer Adds Recommendation
                    │
                    ▼
              IN_PROGRESS
                    │
                    ▼
           Officer Closes Ticket
                    │
                    ▼
                 CLOSED
                    │
                    ▼
          Farmer Reopens Ticket
                    │
                    ▼
                  OPEN
                    │
                    ▼
          48-Hour Escalation Check
                    │
                    ▼
               ESCALATED
```

---

## 🏗️ System Architecture

```text
┌─────────────────────────────┐
│           Farmer            │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│      CropAdvisor Web UI     │
│       HTML/CSS/JavaScript   │
└──────────────┬──────────────┘
               │
               │ REST API
               ▼
┌─────────────────────────────┐
│       Spring Boot API       │
├─────────────────────────────┤
│ Controllers                 │
│ Services                    │
│ Repositories                │
│ Entities                    │
│ DTOs                        │
│ Exception Handling          │
│ Scheduled Escalation        │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│       MySQL Database        │
└─────────────────────────────┘
```

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| Frontend | HTML5, CSS3, JavaScript |
| Backend | Java |
| Framework | Spring Boot |
| Database | MySQL 8 |
| ORM | Spring Data JPA / Hibernate |
| Build Tool | Maven |
| JDK | Java 21 |
| API | REST |
| IDE | Visual Studio Code |
| Database Tool | MySQL Workbench |

---

## 🗄️ Database Design

The application uses four main entities.

### Region

Stores the geographical region associated with farmers and agricultural officers.

### Farmer

Stores farmer information and their associated region.

### Officer

Stores agricultural officer information, specialization, and associated region.

### Ticket

Stores advisory requests raised by farmers.

Ticket information includes:

- Farmer
- Officer
- Region
- Crop name
- Symptoms
- Photo URL
- Ticket status
- Recommendation
- Created timestamp
- Assigned timestamp
- Resolved timestamp
- Reopened timestamp
- Escalation status

### Entity Relationship

```text
                 ┌──────────────┐
                 │    Region    │
                 └──────┬───────┘
                        │
              ┌─────────┴─────────┐
              │                   │
              ▼                   ▼
       ┌────────────┐      ┌────────────┐
       │   Farmer   │      │   Officer  │
       └─────┬──────┘      └──────┬─────┘
             │                    │
             │                    │
             └────────┬───────────┘
                      ▼
                ┌───────────┐
                │  Ticket   │
                └───────────┘
```

---

## 📋 Business Rules

1. A ticket derives its region from the farmer who creates it.
2. An officer is automatically assigned based on the farmer's region.
3. Only the assigned officer can add a recommendation.
4. Only the assigned officer can close the ticket.
5. A ticket cannot be closed without a recommendation.
6. Only the owning farmer can reopen a closed ticket.
7. Reopening changes the ticket status back to `OPEN`.
8. Reopening clears the resolved timestamp.
9. Reopening resets the escalation status to `NORMAL`.
10. Reopened tickets use `reopenedAt` as the escalation reference time.
11. Tickets exceeding 48 hours are marked `ESCALATED`.
12. Exactly 48 hours does not trigger escalation.

---

## 🔌 REST API

### Region APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/regions` | Create region |
| GET | `/api/regions` | Get all regions |
| GET | `/api/regions/{id}` | Get region by ID |
| PUT | `/api/regions/{id}` | Update region |
| DELETE | `/api/regions/{id}` | Delete region |

### Farmer APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/farmers` | Create farmer |
| GET | `/api/farmers` | Get all farmers |
| GET | `/api/farmers/{id}` | Get farmer by ID |
| PUT | `/api/farmers/{id}` | Update farmer |
| DELETE | `/api/farmers/{id}` | Delete farmer |

### Officer APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/officers` | Create officer |
| GET | `/api/officers` | Get all officers |
| GET | `/api/officers/{id}` | Get officer by ID |
| PUT | `/api/officers/{id}` | Update officer |
| DELETE | `/api/officers/{id}` | Delete officer |
| GET | `/api/officers/region/{regionId}` | Get officers by region |

### Ticket APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/tickets` | Create ticket |
| GET | `/api/tickets` | Get all tickets |
| GET | `/api/tickets/{id}` | Get ticket by ID |
| GET | `/api/tickets/status/{status}` | Get tickets by status |
| GET | `/api/tickets/escalated` | Get escalated tickets |
| GET | `/api/tickets/region/{regionId}` | Get tickets by region |
| GET | `/api/tickets/farmer/{farmerId}` | Get farmer tickets |
| GET | `/api/tickets/officer/{officerId}` | Get officer tickets |

### Ticket Actions

| Method | Endpoint | Description |
|---|---|---|
| PUT | `/api/tickets/{ticketId}/recommendation/{officerId}` | Add recommendation |
| PUT | `/api/tickets/{ticketId}/close/{officerId}` | Close ticket |
| PUT | `/api/tickets/{ticketId}/reopen/{farmerId}` | Reopen ticket |

---

## 🖥️ Application Screenshots

### Landing Page

<img width="1917" height="970" alt="image" src="https://github.com/user-attachments/assets/ca763f4b-a277-4ebc-84df-dd7f76549f74" />


### Farmer Dashboard

<img width="1917" height="968" alt="image" src="https://github.com/user-attachments/assets/e13b3435-65c2-49a5-8c11-549fe4730676" />


### Raise Advisory

<img width="1917" height="967" alt="image" src="https://github.com/user-attachments/assets/e7dd0162-ee18-4f31-93f3-0456b1018960" />


### Officer Dashboard

<img width="1917" height="962" alt="image" src="https://github.com/user-attachments/assets/d69a8c61-9249-47e8-85e6-f9dcd16fabdf" />


### Officer Assigned Tickets

<img width="1917" height="966" alt="image" src="https://github.com/user-attachments/assets/3ded06d1-289c-4068-8b98-e9f8d6b1b571" />


### Ticket Details / Recommendation

<img width="1596" height="967" alt="image" src="https://github.com/user-attachments/assets/9e296bfd-9bb9-4645-b048-de3ceb393234" />

---

## 📁 Project Structure

```text
cropadvisor/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── cropadvisor/
│   │   │           ├── controller/
│   │   │           ├── service/
│   │   │           ├── repository/
│   │   │           ├── entity/
│   │   │           ├── dto/
│   │   │           ├── exception/
│   │   │           └── CropadvisorApplication.java
│   │   │
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── index.html
│   │       │   ├── css/
│   │       │   │   └── style.css
│   │       │   └── js/
│   │       │       └── app.js
│   │       │
│   │       └── application.properties
│   │
│   └── test/
│       └── java/
│
├── screenshots/
│   ├── landing-page.png
│   ├── farmer-dashboard.png
│   ├── raise-advisory.png
│   ├── officer-dashboard.png
│   ├── ticket-details.png
│   └── escalated-ticket.png
│
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

---

## ⚙️ Prerequisites

Before running CropAdvisor, install:

- Java JDK 21
- MySQL 8
- Maven or Maven Wrapper
- Git
- Visual Studio Code
- Modern web browser

---

## 🗃️ Database Setup

Start MySQL and create the database:

```sql
CREATE DATABASE cropadvisor;
```

Configure the database connection in:

```text
src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cropadvisor
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

server.port=8080
```

Replace `YOUR_PASSWORD` with your local MySQL password.

**Do not commit real passwords or credentials to GitHub.**

---

## ▶️ Running the Application

Clone the repository:

```bash
git clone https://github.com/YOUR_USERNAME/cropadvisor.git
cd cropadvisor
```

Run the Spring Boot application on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Or:

```powershell
mvn spring-boot:run
```

Once the application starts, open:

```text
http://localhost:8080/
```

---

## 🧪 Testing

Run the test suite using:

```powershell
.\mvnw.cmd test
```

The integration tests cover important application and business rules, including:

- Application and ticket retrieval
- Escalated ticket detection
- Escalated ticket endpoint
- Unauthorized officer ticket operations
- Invalid ticket reopening
- Invalid farmer creation
- Ticket details containing farmer and officer information

---

## 🔐 Access Control

The current version uses demo farmer/officer profile selection rather than production authentication.

The project does not currently implement:

- Spring Security
- JWT authentication
- OAuth
- Password-based login

The backend applies business-level authorization rules for ticket operations. Only the assigned officer can add recommendations or close a ticket, and only the owning farmer can reopen a closed ticket.

---

## 🔁 Example Usage

### Farmer Workflow

```text
Select Farmer
      ↓
Open Farmer Portal
      ↓
Raise Advisory
      ↓
Enter Crop + Symptoms
      ↓
Submit Ticket
      ↓
View Ticket Status
```

### Agricultural Officer Workflow

```text
Select Officer
      ↓
Open Officer Portal
      ↓
View Assigned Tickets
      ↓
Open Ticket
      ↓
Add Recommendation
      ↓
Ticket → IN_PROGRESS
      ↓
Close Ticket
      ↓
Ticket → CLOSED
```

### Reopening Workflow

```text
Farmer
  ↓
View CLOSED Ticket
  ↓
Reopen Ticket
  ↓
Ticket → OPEN
  ↓
Escalation Monitoring Restarts
```

---

## 🚀 Future Enhancements

- Real farmer and officer authentication
- Spring Security and JWT
- Mobile application
- Crop disease image upload
- AI/ML-based disease detection
- Agricultural knowledge base
- SMS and email notifications
- WhatsApp notifications
- Multilingual support
- GPS/location integration
- Analytics and reporting dashboard
- Cloud deployment
- Advanced officer workload balancing

---

## 🤝 Contribution

Contributions and improvements are welcome.

```bash
git checkout -b feature/your-feature
git add .
git commit -m "Add your feature"
git push origin feature/your-feature
```

Then create a Pull Request on GitHub.

---

## 📄 License

This project currently does not specify an open-source license.

---

## 👨‍💻 Author

**Nanthana Uthayakumar**

### CropAdvisor

**Crop Disease Query and Advisory Ticketing System**
```

**For the screenshots**, create this folder in your project:

```text
cropadvisor
└── screenshots
    ├── landing-page.png
    ├── farmer-dashboard.png
    ├── raise-advisory.png
    ├── officer-dashboard.png
    ├── ticket-details.png
    └── escalated-ticket.png
```

Then push:

```powershell
git add README.md screenshots
git commit -m "Add README and application screenshots"
git push
```
