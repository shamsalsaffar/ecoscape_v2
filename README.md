# EcoScape Backend -V2

EcoScape is a full-stack eco-tourism platform for booking eco-friendly listings.

This repository contains the backend built with Spring Boot.

---

## Tech Stack

- Java 17
- Spring Boot
- Spring Security + JWT
- PostgreSQL
- JPA / Hibernate
- Maven
- Stripe API
- Java Mail (SMTP)
- WebSocket (STOMP, SockJS)
- MapStruct

---

## Authentication

- Stateless JWT authentication
- Role-based access:
  - USER
  - HOST
  - ADMIN
- Secured endpoints via Spring Security

---

## Core Features

### Listings
- Create, update, delete listings
- Filtering and search
- Support for eco-friendly features

### Booking System
- Create, update, cancel bookings
- User & host cancellation
- Booking history per user

### Availability
- Prevents overlapping bookings
- Automatically updates dates
- Handles rescheduling and release

### Payments
- Stripe PaymentIntent integration
- Secure backend validation
- Linked to bookings

---

## New in This Version

### Booking System Refactoring
- Cleaner and modular `BookingService`
- Separation of concerns
- Easier to maintain and extend

### Price Service
- Centralized price calculation
- Includes:
  - nights
  - service fee
  - cleaning fee
  - total price

### Validation System
- Pipeline-based validation
- Easy to add new rules
- Includes:
  - date validation
  - guest limits
  - contact validation

### Mapping (MapStruct)
- DTO ↔ Entity mapping
- Removes manual mapping logic
- Cleaner service layer

###  Email System
- Dedicated email package
- Handles:
  - booking confirmation
  - cancellation
  - updates

### Notification System (NEW)
- Real-time notifications using WebSocket
- STOMP + SockJS
- Supports:
  - booking created
  - booking cancelled
  - booking updated
- Template-based notification structure

---

## Architecture

- Controller Layer → API endpoints
- Service Layer → business logic
- Validation Layer → booking rules
- Mapper Layer → DTO conversion
- Repository Layer → database access

---

##  Run Project

```bash
mvn clean install
mvn spring-boot:run

```
---

## API Documentation: 
👉 https://documenter.getpostman.com/view/41126830/2sBXqCR4pu

---

## My Contribution
- Refactored booking system into modular architecture (validation, mapper, pricing)
- Designed validation pipeline for flexible business rules
- Implemented centralized pricing logic (PriceService)
- Introduced MapStruct for clean DTO mapping
- Built email system for booking lifecycle
- Improved update and cancellation logic

---

ℹ️ Notes
This repository contains only the backend
Frontend is built separately using React
