# MedAI Firestore Database Design & Security Architecture 🛠️

This document outlines the schema architecture and document-level security rules designed for MedAI's Firestore backend. The collections are designed to provide scalable medical configuration, dynamic application content, and secure logging telemetry.

---

## 1. Firestore Architecture Overview

The database uses four primary root collections:

1.  **`content`**: Static and dynamic visual components shown to users, such as health articles, localized advice/tips, banners, and real-time pop-up notifications.
2.  **`settings`**: Runtime app configurations including active feature flags, API endpoints, minimum versions, and emergency maintenance blocks.
3.  **`errorLogs`**: Centralized telemetry tracking client-side crash files and Kotlin Exception logs to ensure rapid debugging.
4.  **`apiLogs`**: Monitoring logs for client-to-server or client-to-Gemini API interactions to measure latency, success rates, and token allocations.

---

## 2. Collection Schemas

### 2.1. `content` Collection
- **Path**: `/content/{contentId}`
- **Purpose**: Manage dynamic user-facing information.
- **Fields**:
    - `type` (string): Type of content (`"health_tip"` | `"announcement"` | `"banner"` | `"article"`)
    - `titleUz` (string): Localized Title (Uzbek)
    - `titleRu` (string): Localized Title (Russian)
    - `titleEn` (string): Localized Title (English)
    - `bodyUz` (string): Localized Body Content (Uzbek)
    - `bodyRu` (string): Localized Body Content (Russian)
    - `bodyEn` (string): Localized Body Content (English)
    - `imageUrl` (string, optional): External banner or graphic URL
    - `publishedAt` (integer): Epoch timestamp in milliseconds
    - `isActive` (boolean): Visibility toggle

### 2.2. `settings` Collection
- **Path**: `/settings/{settingId}` (Common IDs: `"app_config"`, `"feature_flags"`, `"version_config"`)
- **Purpose**: App-wide switches and dynamic controls.
- **Sub-structures**:
    - **Document `"app_config"`**:
        - `maintenanceMode` (boolean): Blocks non-admins during maintenance.
        - `supportedLanguages` (array of strings): `["uz", "ru", "en"]`
    - **Document `"feature_flags"`**:
        - `enableGeminiSymptomCheck` (boolean)
        - `enableSosEmergency` (boolean)
        - `enableLabResultVision` (boolean)
    - **Document `"version_config"`**:
        - `minSupportedVersion` (string): E.g., `"1.0.4"`
        - `latestVersion` (string): E.g., `"1.1.0"`
        - `forceUpdate` (boolean): Forces download of latest APK

### 2.3. `errorLogs` Collection
- **Path**: `/errorLogs/{logId}` (Auto-generated UUIDs)
- **Purpose**: Client diagnostic database.
- **Fields**:
    - `userId` (string): Submitting user ID
    - `userName` (string): Display name
    - `errorMessage` (string): String of exception
    - `stackTrace` (string): Full runtime stack trace
    - `deviceModel` (string): E.g. "Galaxy S24 Ultra"
    - `osVersion` (string): E.g. "Android 14 (API 34)"
    - `appVersion` (string): E.g. "1.0.2"
    - `timestamp` (integer): Epoch timestamp in milliseconds

### 2.4. `apiLogs` Collection
- **Path**: `/apiLogs/{logId}` (Auto-generated UUIDs)
- **Purpose**: Performance and token usage metrics.
- **Fields**:
    - `userId` (string): Requesting user ID
    - `endpoint` (string): E.g. "Gemini-1.5-flash:generateContent"
    - `method` (string): `"POST"` or `"GET"`
    - `statusCode` (integer): HTTP Response status code
    - `responseTimeMs` (integer): Latency measurement in ms
    - `timestamp` (integer): Epoch timestamp in milliseconds

---

## 3. Document Security Rules (`firestore.rules`)

The security policy implements **principle of least privilege**:
-   **Content** is readable by all users, ensuring immediate loading. Writes are exclusively reserved for verified Admins.
-   **Settings** require authentication to view (preventing public crawling of configurations) and Admin level to change.
-   **Logs (`errorLogs` and `apiLogs`)** permit write operations (creates) from authenticated application users. Document updates, deletes, and reads are heavily restricted to protect telemetry integrity.

---

## 4. Query Indexes (`firestore.indexes.json`)

To enable fast searches and query filters on the dashboard, we have created composite indexes:

1.  **Error Logs Filtering**: Filters logs by `userId` and displays them by `timestamp` (descending).
2.  **API Logs Telemetry**: Filters usage statistics by `userId` and displays them by `timestamp` (descending).
3.  **Active Content Querying**: Filters promotional announcements or tips by their `type` and lists them by `publishedAt` (descending) to fetch active headers first.

---

## 5. Deployment Guide

To deploy these security rules and compound indexes to your live Firebase console, run the following commands in your CLI:

```bash
# 1. Install Firebase CLI globally if not already present
npm install -g firebase-tools

# 2. Login to your associated Google Cloud / Firebase Account
firebase login

# 3. Associate project configuration (Select your active Project ID)
firebase use --add

# 4. Deploy all Firestore configuration files simultaneously
firebase deploy --only firestore
```
