# 💊 MediTrack — Pharmacy Inventory & Sales Management System

A full-stack **Spring Boot 3.3** web application for small pharmacy businesses to manage inventory, process sales, and enforce security best practices.

[![Deploy to Render](https://render.com/images/deploy-to-render.svg)](https://render.com/deploy?repo=https://github.com/Kanvaa/Medi-Track)

## 🔑 Key Features

### 🔒 Security
- **Exponential Backoff Authentication** — Prevents brute-force attacks without enabling denial-of-service (unlike traditional account lockouts)
- **AES-256 Encryption at Rest** — Customer PII (phone, prescription numbers) encrypted using AES/CBC with random IV per record via JPA `AttributeConverter`
- **Role-Based Access Control (RBAC)** — Three roles: `OWNER`, `PHARMACIST`, `STAFF` with URL-level and UI-level enforcement
- **BCrypt Password Hashing** — Secure one-way password storage
- **CSRF Protection** — Enabled by default via Spring Security

### 💊 Inventory Management
- Full CRUD for medicines with batch tracking
- Expiry date monitoring with visual alerts (expired / near-expiry)
- Stock level tracking with automatic deduction on sale
- Prescription requirement enforcement

### 🧾 Sales Processing
- **8-Step Transactional Sale Pipeline**:
  1. Medicine lookup & validation
  2. Expiry date check (blocks expired stock)
  3. Prescription validation
  4. Atomic stock deduction
  5. Price snapshot (preserves price at time of sale)
  6. Total calculation
  7. PII encryption & save
  8. Audit log entry
- All-or-nothing via `@Transactional` — partial failures trigger complete rollback

### 📋 Audit Trail
- Logs all security events (login success/failure, backoff triggers)
- Tracks inventory changes and sale transactions
- Records actor, IP, timestamp for every event

### 👥 User Management (Owner only)
- Create, edit, deactivate user accounts
- Assign roles with granular permissions

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.3 |
| **Security** | Spring Security 6 (Form Login + RBAC) |
| **ORM** | Spring Data JPA + Hibernate |
| **Database** | MySQL (production) / H2 (demo) |
| **View** | Thymeleaf (server-rendered) |
| **Validation** | Jakarta Bean Validation |
| **Build** | Maven |

## 🚀 Quick Start

### Prerequisites
- Java 17+ installed
- Maven 3.9+ (or use the included Maven Wrapper)

### Run with H2 (No Database Setup Required)
```bash
# Clone the repo
git clone https://github.com/YOUR_USERNAME/meditrack.git
cd meditrack

# Run the app
./mvnw spring-boot:run
```

Open **http://localhost:8080** in your browser.

### Default Login Credentials

| Username | Password | Role |
|----------|----------|------|
| `owner` | `ChangeMe123!` | OWNER (full access) |
| `pharmacist` | `ChangeMe123!` | PHARMACIST (inventory + sales) |
| `staff` | `ChangeMe123!` | STAFF (sales only) |

### Run with MySQL (Production)
1. Install MySQL and create the database:
   ```sql
   CREATE DATABASE meditrack_db;
   ```
2. Edit `src/main/resources/application.properties` — comment out H2 block, uncomment MySQL block
3. Set your MySQL password in the properties file
4. Run: `./mvnw spring-boot:run`

## 📁 Project Structure

```
com.meditrack.pharmacy
├── config/          SecurityConfig, DataSeeder
├── model/           JPA entities (Medicine, Sale, SaleItem, User, AuditLog) + enums
├── repository/      Spring Data JPA repositories
├── service/         Business logic (sale pipeline, stock management, encryption)
├── controller/      MVC controllers (Dashboard, Medicine, Sale, Admin)
└── security/        BackoffAwareAuthenticationProvider, CustomUserDetailsService
```

## 🏗️ Architecture

```
Browser → Spring Security (auth + RBAC + CSRF)
            → Controller (routes request)
              → Service (business rules, @Transactional)
                → Repository (JPA queries)
                  → Database (MySQL / H2)
```

## 🔐 Security Design Decisions

### Why Exponential Backoff Instead of Account Lockout?
Hard lockouts have a DoS vulnerability: an attacker can intentionally lock out any known username by entering wrong passwords repeatedly. Exponential backoff (`min(2^(n-1), 300)` seconds) slows brute-force attacks while ensuring the legitimate owner can always log in after a short wait.

### Why Random IV for AES Encryption?
A fixed IV causes identical plaintexts to produce identical ciphertexts, enabling pattern analysis. Random IV per encryption ensures the same phone number produces different ciphertext every time, preventing correlation attacks.

### Why `open-in-view=false`?
Prevents lazy-loading from masking N+1 query problems. Forces explicit data fetching via `JOIN FETCH`, resulting in predictable performance and no surprise LazyInitializationExceptions.

## 👤 Author

Kanvadithya Ganapathi Tigulla — [@Kanvaa](https://github.com/Kanvaa)
