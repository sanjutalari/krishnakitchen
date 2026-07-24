# System Design Specification: Krishna Chicken Web & Mobile Model

This document outlines the architectural design and structural models for **Krishna Chicken** - a highly scalable, real-time hot chicken restaurant platform. It details the separation of concerns across multiple panels and how the application scales from a mobile-first app into a responsive web-app experience.

---

## 1. Architectural Overview

The system is designed with a decoupled, three-tier service layout:
1. **Frontend Presentation Layers** (Mobile App via Jetpack Compose & Web Interface Model via responsive layouts).
2. **Central State Orchestrator** (MVVM Pattern utilizing a unified, thread-safe, reactive state view model).
3. **Enterprise Business Logic** (Real-time order synchronization, physical table QR detection, GPS routing, and instant feedback processing).

```
   ┌────────────────────────────────────────────────────────┐
   │                    USER INGRESS POINT                  │
   └───────────────────────────┬────────────────────────────┘
                               │
            ┌──────────────────┼──────────────────┐
            ▼                  ▼                  ▼
     [CUSTOMER USAGE]    [STAFF USAGE]      [ADMIN CONTROL]
     • Menu & Ordering   • Kitchen Queue    • All Panel View
     • Cart & Checkout   • Waiter Table Map • Analytics Logs
     • Live Feedback     • Delivery Partner • Global Controls
            │                  │                  │
            └──────────────────┼──────────────────┘
                               │
                               ▼
   ┌────────────────────────────────────────────────────────┐
   │             CENTRAL SYSTEM STATE ENGINE                │
   │           (StateFlows & Reactive Drivers)              │
   └────────────────────────────────────────────────────────┘
```

---

## 2. Separate Panel Classification & Categories

The application strictly classifies screen panels into specific security and functional domains:

### A. Customer Panel (Customer-Only Usage)
Dedicated purely to end-customers. It is decoupled from any backend administrative panels to ensure clean UX, security, and low cognitive load.
*   **Menu Browser:** Categorized view of dishes (Starters, Curries, Combos, Bread, Desserts) with live pricing.
*   **Active Cart & Special Instructions:** In-memory cart with line-item notes (e.g. "No Onion", "Extra Spicy").
*   **Simulated Dine-In / Delivery Checkout:** Multi-modal support with instant GPS/QR table-code detection.
*   **Interactive Reviews & Feedback:** Customers can submit ratings (1-5 stars) and reviews for specific dishes.
*   **Support Desk:** Direct messaging line to staff members.

### B. Staff Panels (Isolated Operational Terminals)
Created exclusively to drive real-time operational workflows. To prevent interference, these terminals operate on separate screens and do not expose navigation pathways to other roles, while data transfers in real-time across the unified state model:
1.  **Kitchen Queue Panel:**
    *   Dedicated kitchen terminal interface locked to order prep workflows.
    *   Tracks orders progressing from *Pending* -> *Cooking* -> *Ready*.
    *   Provides high-contrast indicators and flash alerts when urgent or extra items are requested.
2.  **Waiter Table-Map Panel:**
    *   Dedicated waiter terminal locked to physical seating, occupancy, and guest requests.
    *   Visual representation of table occupancy, special requests, and active service calls.
    *   Empowers staff with instant action buttons to approve or dismiss table-side requests.
3.  **Delivery Partner Panel:**
    *   Dedicated courier terminal locked to live routes, map simulations, and customer chat.
    *   Active map routing simulation with dynamic delivery charge calculation based on custom distance filters.
    *   Maintains direct-messaging channels with customers.

### C. Admin Panel (Universal Panel Access)
The absolute highest privilege tier in the organization.
*   **Universal Visibility:** Only the **Admin** is authorized to view all customer, kitchen, waiter, and delivery sub-panels in a single session.
*   **Real-time Revenue Analytics:** Monitors progress towards the restaurant's daily financial goals.
*   **Global Overrides:** Controls broadcasted announcements, active menu catalog edits, staff list management, and dynamic coupon templates.

---

## 3. Web-App & Mobile Adaptive Data Models

### A. State Schema (Kotlin / JSON Web API Equivalence)
```kotlin
// Unified Domain Entities representing the entire digital ecosystem
data class MenuItem(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val description: String,
    val prepTime: String,
    val isVegetarian: Boolean
)

data class Order(
    val id: Int,
    val tableNumber: String,
    val items: List<CartItem>,
    val totalAmount: Double,
    val status: OrderStatus,
    val timestamp: String,
    val isDelivery: Boolean = false,
    val specialNotes: List<String> = emptyList()
)

data class CustomerFeedback(
    val id: String,
    val customerName: String,
    val rating: Double,
    val review: String,
    val dishName: String? = null,
    val timestamp: String
)
```

### B. Security, Isolation & Access Rules
*   **Access Control Rule #1:** A user with role `Customer` is strictly confined to the customer-facing ordering, review, and feedback interface. They have no access to staff panels or bottom navigation.
*   **Access Control Rule #2 (Staff Terminal Separation):** Users categorized under staff roles (`Kitchen Staff`, `Waiter Staff`, `Delivery Partner`) are entirely isolated in their respective operational views with zero navigation controls to other panels. This ensures staff members on a kitchen display cannot accidentally interfere with waiter tables or delivery routes, nor can customers interfere with internal staff status logs.
*   **Access Control Rule #3 (Unified Real-Time Data Flow):** Despite total UI and layout isolation, all terminals communicate with the same central system state. When a customer places an order, the status instantly flows to the Kitchen display, then to the Waiter terminal, and finally updates the Delivery tracker app seamlessly.
*   **Access Control Rule #4 (Universal Admin Authorization):** Only the `Admin` persona possesses universal security credentials, allowing them to view and switch between all active terminals and the financial analytics suite.

---

## 4. UI/UX Design System Guidelines

### A. Theming Philosophy: Natural Tones Design System
The visual style of **Krishna Chicken** is designed around organic, warm earth-bound color palettes that evoke high-quality culinary craftsmanship. The app supports a dynamic **Light Mode** (warm organic beige/peach) and **Dark Mode** (cozy obsidian and dark chocolate) that can be instantly toggled across all perspectives.

### B. Color Scheme Reference

#### 1. Core Brand Colors
| Brand Color | Light Mode (Hex) | Dark Mode (Hex) | Visual Identity & Purpose |
| :--- | :--- | :--- | :--- |
| **Flame Red** | `#7D5700` | `#FFFFB85C` | Deep clay/ochre vs Warm vibrant gold-orange accent for action items |
| **Amber Glow** | `#FFDDA1` | `#422F18` | Soft peach-yellow vs Deep warm container background |
| **Charcoal Black** | `#281900` | `#F2EAE0` | Rich deep brown/black vs Light creamy tan for main text & details |
| **Surface Dark** | `#FDF8F6` | `#140D06` | Very light organic beige/peach vs Cozy obsidian background |
| **Surface Elevated** | `#F7F2EB` | `#1F150B` | Warm cream/tan bottom nav & sheets vs Dark chocolate elevation panels |
| **Surface Container Low** | `#FFFFFF` | `#2A1E11` | Pure white for cards vs Warm medium dark for containers |
| **Surface Container** | `#ECE0CF` | `#352617` | Warm tan container color vs Dark warm border lines |
| **Surface Container High** | `#E9E1D1` | `#45321E` | Light tan for thin borders vs High-contrast dark separator lines |
| **Surface Container Highest** | `#D7C4A7` | `#553F27` | Darker tan for secondary items vs Prominent dark elements |

#### 2. Typography & Text Colors
| Text Element | Light Mode (Hex) | Dark Mode (Hex) | Purpose |
| :--- | :--- | :--- | :--- |
| **OnSurfaceLight** | `#1D1B16` | `#F9F3EB` | High-contrast headings and primary titles |
| **OnSurfaceVariantLight** | `#7A7568` | `#CABFA3` | Medium-contrast secondary text and status labels |

#### 3. Operational & Semantic State Colors
| Order State / Status | Light Mode (Hex) | Dark Mode (Hex) | Purpose & Stage Representation |
| :--- | :--- | :--- | :--- |
| **StatusPending** | `#D7C4A7` | `#553F27` | Warm neutral/tan for newly queued customer orders |
| **StatusCooking** | `#FFE08D` | `#F5C542` | Mustard yellow representing kitchen food preparation |
| **StatusReady** | `#4B634B` | `#81C784` | Pale sage green representing ready-to-serve orders |
| **StatusDelivered** | `#926B00` | `#FFFFCC00`| Amber/gold representing completed and dispatched shipments |
| **StatusCritical** | `#8B4513` | `#FFFF8A80`| Terracotta/reddish brown for highly urgent or cancelled items |
| **ErrorColor** | `#FDF2E9` | `#3E1C1C` | Pale warm orange/peach vs Deep crimson container for errors |
| **OnErrorColor** | `#690005` | `#FFFFB4AB`| Dark crimson vs Light pink-red text for error labels |

---

## 5. Web & Mobile Layout Adaptation & Responsiveness

*   **Responsive Width Adapters:** Fluid container layouts utilizing constraints like `widthIn(max = 600.dp)` centered horizontally to ensure readability on wide web browsers, tablets, and desktop displays while keeping compact layouts pristine on mobile devices.
*   **Accessibility Design:** Every touch target is configured with a minimum of 48dp x 48dp dimension to support physical touch interaction and compliant web/desktop pointer hover targets.
*   **Floating Persona Simulator:** A dedicated, finger-friendly floating badge (56dp circle decorated with a "SIM" tag) anchors to the screen edge. Tapping it invokes a translucent backdrop scrim with a high-contrast modal selection dialog, allowing testers to safely switch active user perspectives and test background data synchronizations without cluttering the primary app layouts.
*   **Persistent Micro-Transitions:** Transitioning between colors, light and dark themes, and navigation selections features clean Material Design 3 ripples, tactile surface indicators, and instant visual toasts for continuous operational feedback.
