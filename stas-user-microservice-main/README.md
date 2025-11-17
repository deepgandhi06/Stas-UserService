# 🧩 STAS Microservices — Core Backend

## 🚀 Overview
This is the **core backend foundation** of the STAS application, built using **Spring Boot Microservices Architecture**.  
Currently, it includes three main services:
- **Eureka Server** → Service Discovery
- **API Gateway** → Central Routing + JWT Validation
- **User Service** → Authentication, Registration & Role-Based Access

---

## 🏗️ Current Architecture

| Service | Description | Port | Eureka Registration |
|----------|--------------|------|----------------------|
| 🧠 **Eureka Server** | Service registry for microservice discovery | `8761` | ✅ |
| 🌐 **API Gateway** | Routes all requests and verifies JWT | `9000` | ✅ |
| 🔑 **User Service** | Handles signup, login, user management, and role-based authorization | `8081` | ✅ |

---


## 🧩 Current Flow

1. **Eureka Server** runs on port `8761` and acts as the central registry.  
2. **API Gateway** (port `9000`) registers with Eureka and routes incoming requests.  
3. **User Service** (port `8081`) handles:
   - User Signup & Login
   - JWT Token Generation
   - Role-Based Authorization
   - Profile Update
   - Password Change  
4. The **Gateway** validates JWTs before routing requests to services.

---

## 🔑 Authentication & Authorization

- Uses **JWT (JSON Web Token)** for authentication.
- Roles currently available:
  - `USER`
  - `ADMIN`
  - `DEVELOPER`
  - `CLIENT`
- Role-based access control is implemented inside `SecurityConfig`.

---

## 📁 Project Structure
stas-microservices/
│
├── eureka-server/
│   └── src/main/java/com/stas/eureka/
│
├── apigateway/
│   ├── src/main/java/com/stas/apigateway/
│   └── JwtAuthenticationFilter.java  # Validates JWT for all incoming requests
│
└── user-service/
├── controller/
│   ├── AuthController.java       # Handles login & signup
│   └── UserController.java       # Handles user profile & admin operations
├── service/
├── serviceImpl/
├── repository/
├── entity/
├── dto/
└── utils/
└── JwtUtils.java             # JWT generation & validation logic

---

## 🧠 API Endpoints (User Service)

| Method | Endpoint | Description | Auth Required |
|---------|-----------|-------------|----------------|
| `POST` | `/user-service/api/auth/signup` | Register a new user | ✅ |
| `POST` | `/user-service/api/auth/login` | Login and receive JWT token | ✅ |
| `GET`  | `/user-service/api/users/all` | Get all users (Admin only) | ✅ |
| `PUT`  | `/user-service/api/users/me` | Update user profile | ✅ |
| `PUT`  | `/user-service/api/users/me/change-password` | Change user password | ✅ |

> 🔐 All protected endpoints require a JWT token in the header:  
> `Authorization: Bearer <token>`

---

## 🧩 Database Details

**Database Name:** `stas_userdb`

**Tables:**
- `users`
- `roles`
- `user_roles` (join table for many-to-many relation)

---

## 🧰 Configuration Summary

| File | Description |
|------|--------------|
| `application.properties` (Eureka) | Service registry config |
| `application.yml` (Gateway) | Routing & Eureka connection |
| `application.properties` (User Service) | DB + Eureka + JWT config |
| `.gitignore` | Ignores build & sensitive files |

---

## 🧱 How to Run the Application

Make sure **MySQL** is running locally and the database `stas_userdb` exists.

### 1️⃣ Start Eureka Server
```bash
cd eureka-server
mvn spring-boot:run