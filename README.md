# SmartStock — Smart Inventory Management & Demand Forecasting System

SmartStock is a full-stack smart inventory management system designed to help businesses monitor stock, analyze demand, identify inventory risks, manage suppliers, and make intelligent replenishment decisions.

Unlike a basic inventory CRUD application, SmartStock combines inventory management with **demand analytics, stock alerts, supplier-aware replenishment, inventory intelligence, and financial insights**.

## 🚀 Key Features

### 📦 Product Management

* Add, update, view, and delete products
* SKU-based product identification
* Track product price and available quantity
* Configure reorder levels and low-stock thresholds
* Configure safety stock, minimum order quantity, maximum stock level, and lead time
* Associate products with suppliers

### 🔄 Stock Movement Management

* Record purchases and sales
* Automatically update inventory quantities
* Track movement date and notes
* Maintain inventory transaction history
* Prevent invalid stock operations through backend validation

### ⚠️ Inventory Alerts

SmartStock continuously evaluates inventory conditions and identifies:

* Out-of-stock products
* Critical stock levels
* High-risk inventory
* Products approaching reorder levels
* Healthy stock conditions

### 📊 Demand Analytics & Forecasting

The system analyzes historical sales data to calculate:

* Average daily demand
* 7-day demand forecast
* 30-day demand forecast
* 90-day demand forecast
* Product-level demand trends

### 🤖 Smart Replenishment

SmartStock recommends how much inventory should be ordered based on:

* Historical demand
* Supplier lead time
* Lead-time demand
* Safety stock
* Reorder level
* Target stock level
* Minimum order quantity
* Maximum stock level
* Current inventory

The system also provides understandable inventory status such as:

`OUT OF STOCK` → `REORDER REQUIRED` → `MONITOR` → `STOCK HEALTHY`

### 🏭 Supplier Management

* Add and manage suppliers
* Store supplier contact information
* Track supplier lead time
* Activate/deactivate suppliers
* Link suppliers with products
* Use supplier lead time in replenishment calculations

### 💰 Inventory Financial Insights

SmartStock provides inventory value and financial information based on product price and current stock levels, helping users understand the financial value of their inventory.

### 🧠 Inventory Intelligence

The project includes an inventory intelligence layer that combines inventory, demand, and replenishment information to provide actionable insights rather than simply displaying raw database data.

---

## 🛠️ Technology Stack

### Backend

* Java
* Spring Boot
* Spring Data JPA
* Hibernate
* Maven
* MySQL

### Frontend

* React.js
* Vite
* JavaScript
* HTML
* CSS

### Development Tools

* Visual Studio Code
* Git
* GitHub
* MySQL

---

## 🏗️ System Architecture

```text
                ┌──────────────────────────┐
                │       React Frontend     │
                │                          │
                │ Dashboard                │
                │ Products                 │
                │ Stock Movements          │
                │ Alerts                   │
                │ Analytics                │
                │ Recommendations          │
                │ Suppliers                │
                └────────────┬─────────────┘
                             │
                         REST APIs
                             │
                             ▼
                ┌──────────────────────────┐
                │    Spring Boot Backend   │
                │                          │
                │ Controllers              │
                │ Services                 │
                │ Repositories             │
                │ Entities                 │
                │ Exception Handling       │
                └────────────┬─────────────┘
                             │
                          JPA / Hibernate
                             │
                             ▼
                ┌──────────────────────────┐
                │       MySQL Database     │
                │                          │
                │ Products                 │
                │ Suppliers                │
                │ Stock Movements          │
                └──────────────────────────┘
```

---

## 📁 Project Structure

```text
SmartStock/
│
├── smartstock/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/com/stocksense/smartstock/
│   │   │       ├── controller/
│   │   │       ├── entity/
│   │   │       ├── exception/
│   │   │       ├── repository/
│   │   │       └── service/
│   │   │
│   │   └── test/
│   │
│   ├── pom.xml
│   └── mvnw
│
├── smartstock-frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── vite.config.js
│
└── .gitignore
```

---

## ⚙️ Backend Configuration

The backend uses MySQL for persistent storage.

Create a database named:

```sql
CREATE DATABASE smartstock_db;
```

Configure your local database credentials in:

```text
smartstock/src/main/resources/application.properties
```

Example:

```properties
spring.application.name=smartstock

spring.datasource.url=jdbc:mysql://localhost:3306/smartstock_db
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

**Important:** The real `application.properties` file containing the local database password is intentionally excluded from this public repository through `.gitignore`.

---

## ▶️ How to Run

### 1. Start MySQL

Make sure your MySQL server is running and the `smartstock_db` database exists.

### 2. Start the Spring Boot Backend

Open a terminal in:

```text
SmartStock/smartstock
```

Run:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

### 3. Start the React Frontend

Open another terminal in:

```text
SmartStock/smartstock-frontend
```

Install dependencies if required:

```powershell
npm install
```

Start the frontend:

```powershell
npm run dev
```

The frontend is available at:

```text
http://localhost:5173
```

---

## 🔗 Main API Modules

| Module                 | Endpoint                      |
| ---------------------- | ----------------------------- |
| Products               | `/api/products`               |
| Stock Movements        | `/api/stock-movements`        |
| Suppliers              | `/api/suppliers`              |
| Analytics              | `/api/analytics`              |
| Inventory Alerts       | `/api/alerts`                 |
| Recommendations        | `/api/recommendations`        |
| Inventory Intelligence | `/api/inventory-intelligence` |

---

## 🧮 Smart Replenishment Logic

SmartStock calculates replenishment recommendations using a combination of demand and inventory parameters.

Conceptually:

```text
Average Daily Demand
        ↓
Lead-Time Demand
        ↓
+ Safety Stock
        ↓
Demand-Based Reorder Point
        ↓
Compare with Configured Reorder Level
        ↓
Effective Reorder Point
        ↓
Calculate Target Stock
        ↓
Recommended Order Quantity
```

The recommendation also respects minimum and maximum stock constraints.

This makes the system more useful than a simple:

```text
if quantity < threshold → reorder
```

approach.

---

## 🎯 Real-World Use Case

SmartStock can be used by:

* Retail stores
* Supermarkets
* Pharmacies
* Warehouses
* Small and medium-sized businesses
* E-commerce inventory teams
* Wholesale businesses

The system helps reduce:

* Stockouts
* Overstocking
* Manual inventory monitoring
* Poor replenishment decisions
* Unnecessary inventory holding

---

## 🔐 Security & Repository Practices

Sensitive local configuration is not stored in the public repository.

The project uses `.gitignore` to exclude:

```text
application.properties
.env
node_modules/
dist/
target/
```

Developers should create their own local configuration when running the project.

---

## 📌 Project Status

SmartStock currently includes:

* ✅ Product Management
* ✅ Stock Movement Management
* ✅ Automatic Inventory Updates
* ✅ Inventory Alerts
* ✅ Demand Analytics
* ✅ 7/30/90-Day Demand Forecasting
* ✅ Smart Replenishment
* ✅ Supplier Management
* ✅ Supplier-Product Linking
* ✅ Supplier-Aware Replenishment
* ✅ Inventory Financial Insights
* ✅ Inventory Intelligence
* ✅ Exception Handling
* ✅ Frontend Dashboard
* ✅ Complete Backend REST API
* ✅ System Testing
* ✅ Project Validation

---

## 🔮 Future Enhancements

Possible future improvements include:

* Machine-learning-based demand forecasting
* Authentication and role-based access control
* Email/SMS inventory alerts
* Advanced sales trend visualization
* Multi-warehouse inventory management
* Purchase order generation
* Supplier performance analytics
* Cloud deployment
* Docker-based deployment
* Advanced business intelligence dashboards

---

## 👨‍💻 Author

**Giriprasanth-7**

Information Technology Student

GitHub: **https://github.com/Giriprasanth-7**

---

## ⭐ Project Goal

SmartStock aims to transform inventory management from a **reactive stock-monitoring process** into a more **data-driven and intelligent decision-support system**.

It combines real-time inventory information, historical demand analysis, supplier information, and replenishment logic to help businesses make better inventory decisions.
