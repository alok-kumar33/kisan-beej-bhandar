# Kisan Beej Bhandar — Shop Management System

A full-stack Point-of-Sale and inventory management system built for a local wholesale/retail agricultural shop, replacing manual ledger-and-notebook record-keeping with a digital workflow covering inventory, billing, sales, returns, and customer credit tracking.

Live demo: [https://kisan-beej-bhandar.onrender.com/login](https://kisan-beej-bhandar.onrender.com/login) (note: free-tier instance may take ~110s to wake up on first load)


## Features

- Dynamic checkout - live cart calculations with dual retail/wholesale pricing toggle
- Discount module - automatically logs discounts as a business expense, keeping the cash drawer balanced without manual reconciliation
- Udhaar (Credit) Ledger - tracks customer debts, partial payments, and unified transaction history
- WhatsApp reminders - generates a URL-encoded mini-statement (last 5 transactions + total due) and routes it directly to the customer's WhatsApp
- Thermal Receipt printing- - custom HTML/CSS receipt layout built specifically for printing the receipts
- Inventory, sales, and staff management - full CRUD across products, stock levels, and staff records

## Screenshots

| Checkout | Udhaar Ledger | WhatsApp Reminder | Receipt |
|---|---|---|---|
| <img width="1917" height="927" alt="checkout" src="https://github.com/user-attachments/assets/6f95443e-c2c9-4f41-b502-adcd23095306" /> | <img width="1917" height="927" alt="Screenshot 2026-09-09 192131" src="https://github.com/user-attachments/assets/ecc57ead-2607-4576-a60b-ab25bdef0bbe" /> | <img width="1913" height="927" alt="ledger" src="https://github.com/user-attachments/assets/262a03dc-8087-43b4-86da-abf555de5ce4" /> | <img width="1915" height="926" alt="receipt" src="https://github.com/user-attachments/assets/cc1ca304-e2b0-480f-b929-af4ac8dbc5e7" /> |

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java, Spring Boot |
| Frontend | Thymeleaf, JavaScript, HTML/CSS |
| Database | PostgreSQL (hosted on Neon) |
| Deployment | Render |
| Version Control | Git / GitHub |

## Architecture

This application follows a classic monolithic MVC (Model-View-Controller) architecture using Spring Boot, enhanced with asynchronous JavaScript for a fluid Point of Sale experience.

```mermaid
graph TD
    %% Client Tier
    subgraph Frontend [Client Tier - Browser]
        UI[Thymeleaf HTML Pages]
        JS[Vanilla JavaScript]
        Print[Thermal Print CSS]
    end

    %% Web Tier
    subgraph Web [Web Tier - Controllers]
        SalesCtrl[SaleController]
        LedgerCtrl[LedgerController]
        WACtrl[WhatsApp Integration]
    end

    %% Service Tier
    subgraph Service [Business Logic - Service Layer]
        SalesSvc[SaleService]
        LedgerSvc[LedgerService]
        StockSvc[Inventory Management]
    end

    %% Data Tier
    subgraph Data [Data Tier - Spring Data JPA]
        SaleRepo[(Sale / Items)]
        LedgerRepo[(Payments)]
        CustRepo[(Customers)]
        ExpRepo[(Expenses)]
    end

    %% External
    DB[(Neon PostgreSQL\nCloud Database)]
    WA(WhatsApp Web/App)

    %% Relationships
    UI -->|HTTP GET/POST| Web
    JS -->|AJAX JSON| SalesCtrl
    
    SalesCtrl --> SalesSvc
    LedgerCtrl --> LedgerSvc
    WACtrl -->|URL Encodes Data| WA
    
    SalesSvc -->|Auto-logs Discounts| ExpRepo
    SalesSvc -->|Deducts| StockSvc
    LedgerSvc -->|Tracks Udhaar| CustRepo
    
    SalesSvc --> SaleRepo
    LedgerSvc --> LedgerRepo
    
    SaleRepo & LedgerRepo & CustRepo & ExpRepo -->|Hibernate / SQL| DB
