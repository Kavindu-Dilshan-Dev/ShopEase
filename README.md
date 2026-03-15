# 🛍️ ShopEase — Android Mobile Commerce App

![Platform](https://img.shields.io/badge/Platform-Android-green?logo=android)
![Language](https://img.shields.io/badge/Language-Java%2017-orange?logo=java)
![Firebase](https://img.shields.io/badge/Backend-Firebase-yellow?logo=firebase)
![PayHere](https://img.shields.io/badge/Payment-PayHere%20Sandbox-blue)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24-lightgrey)
![Target SDK](https://img.shields.io/badge/Target%20SDK-36-lightgrey)

A full-featured Android mobile commerce application built with **Java 17** and **Firebase**, covering all core Android development concepts including UI design, sensors, multimedia, maps, push notifications, and PayHere sandbox payment gateway.

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🔐 Authentication | Email/Password login & registration via Firebase Auth |
| 🛒 Product Browsing | Browse, search and filter products by category |
| 🛍️ Shopping Cart | Add, remove and update cart items (Room SQLite) |
| 💳 PayHere Payment | Sandbox payment gateway integration |
| 📦 Order Management | Place orders and track order status |
| 🗺️ Google Maps | Store locator with Directions API |
| 📳 Push Notifications | FCM push notifications with 3 channels |
| 📷 Camera | Capture photos and upload to Firebase Storage |
| 🎵 Multimedia | Audio recording/playback and video player |
| 📡 Sensors | Accelerometer, step counter, light sensor, gyroscope |
| 📞 Telephony | Call support, SMS, network operator detection |
| ⚙️ Settings | Dark mode, sort preferences, internal storage notes |
| 🔄 Background Sync | WorkManager periodic sync and order status polling |
| 📶 Network Detection | Broadcast receiver for connectivity changes |

---

## 🏗️ Project Structure

```
ShopEaseApp/
├── app/src/main/
│   ├── java/com/kavindu/shopeaseapp/
│   │   ├── activities/          # All Activity classes
│   │   │   ├── SplashActivity
│   │   │   ├── LoginActivity
│   │   │   ├── RegisterActivity
│   │   │   ├── MainActivity
│   │   │   ├── ProductDetailActivity
│   │   │   ├── CartActivity
│   │   │   ├── CheckoutActivity
│   │   │   ├── OrdersActivity
│   │   │   ├── ProfileActivity
│   │   │   ├── MapActivity
│   │   │   ├── CameraActivity
│   │   │   ├── MultimediaActivity
│   │   │   ├── SensorsActivity
│   │   │   ├── SettingsActivity
│   │   │   └── NotificationsActivity
│   │   ├── adapters/            # RecyclerView Adapters
│   │   │   ├── ProductAdapter
│   │   │   ├── CartAdapter
│   │   │   ├── OrderAdapter
│   │   │   └── NotificationAdapter
│   │   ├── fragments/           # Navigation Fragments
│   │   │   ├── HomeFragment
│   │   │   ├── SearchFragment
│   │   │   ├── OrdersFragment
│   │   │   └── ProfileFragment
│   │   ├── models/              # Data Models
│   │   │   ├── User
│   │   │   ├── Product
│   │   │   ├── CartItem
│   │   │   ├── Order
│   │   │   └── NotificationModel
│   │   ├── receivers/           # Broadcast Receivers
│   │   │   ├── BootReceiver
│   │   │   └── NetworkReceiver
│   │   ├── services/            # Background Services
│   │   │   └── FCMService
│   │   ├── utils/               # Utility Classes
│   │   │   ├── CartDatabase
│   │   │   ├── CartDao
│   │   │   ├── PrefsManager
│   │   │   ├── FileStorageHelper
│   │   │   ├── NetworkHelper
│   │   │   ├── NotificationHelper
│   │   │   ├── SensorHelper
│   │   │   ├── TelephonyHelper
│   │   │   └── FirestoreSeeder
│   │   ├── workers/             # WorkManager Workers
│   │   │   ├── SyncWorker
│   │   │   └── OrderStatusWorker
│   │   └── ShopEaseApp.java     # Application class
│   └── res/
│       ├── layout/              # XML Layouts
│       ├── drawable/            # Drawables & Icons
│       ├── mipmap-*/            # Launcher Icons
│       ├── values/              # Colors, Strings, Themes
│       ├── menu/                # Navigation Menus
│       ├── color/               # Color Selectors
│       └── xml/                 # File Provider Paths
└── google-services.json         # Firebase config (not in repo)
```

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|-----------|
| Language | Java 17 |
| Min SDK | API 24 (Android 7.0) |
| Target SDK | API 36 |
| Authentication | Firebase Auth |
| Database | Firebase Firestore |
| Local DB | Room (SQLite) |
| File Storage | Firebase Storage |
| Push Notifications | Firebase Cloud Messaging (FCM) |
| Maps | Google Maps SDK + Directions API |
| Payment | PayHere Android SDK v3.0.18 |
| Image Loading | Glide 4.16.0 |
| Networking | OkHttp3 4.12.0 |
| Background Tasks | WorkManager 2.9.0 |
| UI Components | Material Components 1.11.0 |
| View Binding | Enabled |

---

## 📋 Requirements

- Android Studio Hedgehog or later
- Java 17
- Android device or emulator with API 24+
- Firebase project with enabled services
- Google Maps API key
- PayHere sandbox merchant account

---

## 🚀 Getting Started

### Step 1 — Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/ShopEase.git
cd ShopEase
```

### Step 2 — Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a new project named **ShopEaseApp**
3. Add an Android app with package name `com.kavindu.shopeaseapp`
4. Download `google-services.json`
5. Place it in the `app/` folder
6. Enable the following services:
   - ✅ Authentication → Email/Password
   - ✅ Firestore Database → Test mode
   - ✅ Storage → Test mode
   - ✅ Cloud Messaging

### Step 3 — Add SHA-1 Fingerprint

In Android Studio Terminal:
```bash
gradlew signingReport
```
Copy the SHA-1 value and add it to:
```
Firebase Console → Project Settings → Your apps → Add fingerprint
```

Re-download `google-services.json` after adding.

### Step 4 — Google Maps Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Enable **Maps SDK for Android** and **Directions API**
3. Create an API key
4. Add it to `res/values/strings.xml`:
```xml
<string name="google_maps_key">YOUR_API_KEY_HERE</string>
```

### Step 5 — PayHere Setup

1. Register at [PayHere Sandbox](https://sandbox.payhere.lk)
2. Get your Merchant ID
3. Update in `CheckoutActivity.java`:
```java
req.setMerchantId("YOUR_MERCHANT_ID");
```

### Step 6 — Seed Sample Data

In `ShopEaseApp.java` `onCreate()`, uncomment this line once:
```java
FirestoreSeeder.seedProducts();
```
Run the app once, then comment it out again.

### Step 7 — Build and Run

```
Build → Clean Project
Build → Rebuild Project
Run → Select device
```

---

## 🔥 Firebase Firestore Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    match /users/{userId} {
      allow read, write: if request.auth != null
        && request.auth.uid == userId;
    }

    match /products/{productId} {
      allow read: if true;
      allow write: if false;
    }

    match /orders/{orderId} {
      allow read, write: if request.auth != null
        && request.resource.data.userId == request.auth.uid;
    }

    match /promotions/{promoId} {
      allow read: if request.auth != null;
      allow write: if false;
    }

    match /notifications/{notifId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

---

## 💳 PayHere Sandbox Test Cards

| Card Type | Number | CVV | Expiry |
|-----------|--------|-----|--------|
| Visa | 4916217501611292 | 123 | 12/25 |
| Master | 5307732125531191 | 123 | 12/25 |

---

## 📦 Dependencies

```groovy
// Firebase
implementation platform('com.google.firebase:firebase-bom:34.0.0')
implementation 'com.google.firebase:firebase-auth'
implementation 'com.google.firebase:firebase-firestore'
implementation 'com.google.firebase:firebase-storage'
implementation 'com.google.firebase:firebase-messaging'

// Google Maps
implementation 'com.google.android.gms:play-services-maps:18.2.0'
implementation 'com.google.android.gms:play-services-location:21.1.0'

// PayHere
implementation 'com.github.PayHereDevs:payhere-android-sdk:v3.0.18'

// Room
implementation 'androidx.room:room-runtime:2.6.1'
annotationProcessor 'androidx.room:room-compiler:2.6.1'

// Glide
implementation 'com.github.bumptech.glide:glide:4.16.0'

// OkHttp
implementation 'com.squareup.okhttp3:okhttp:4.12.0'

// WorkManager
implementation 'androidx.work:work-runtime:2.9.0'
```

---

## 📱 App Permissions

| Permission | Purpose |
|-----------|---------|
| INTERNET | Network requests & Firebase |
| ACCESS_FINE_LOCATION | Google Maps & store locator |
| CAMERA | Product photo capture |
| RECORD_AUDIO | Audio recording feature |
| CALL_PHONE | Support call feature |
| POST_NOTIFICATIONS | Push notifications (Android 13+) |
| RECEIVE_BOOT_COMPLETED | Start WorkManager on boot |
| FOREGROUND_SERVICE | Background sync service |

---

## 🗺️ App Navigation Flow

```
SplashActivity
    ↓
LoginActivity ←→ RegisterActivity
    ↓
MainActivity (BottomNav)
    ├── HomeFragment      → ProductDetailActivity → CheckoutActivity
    ├── SearchFragment    → ProductDetailActivity
    ├── CartActivity      → CheckoutActivity
    ├── OrdersFragment
    └── ProfileFragment
            ├── ProfileActivity
            ├── MapActivity
            ├── CameraActivity
            ├── MultimediaActivity
            ├── SensorsActivity
            ├── SettingsActivity
            └── NotificationsActivity
```

---

## 🔔 Notification Channels

| Channel ID | Name | Importance |
|-----------|------|-----------|
| `orders_channel` | Order Updates | HIGH |
| `promo_channel` | Promotions | DEFAULT |
| `general_channel` | General | LOW |

---

## 📂 Data Storage Used

| Storage Type | Used For |
|-------------|---------|
| Firebase Firestore | Users, Products, Orders, Notifications |
| Firebase Storage | Profile images |
| Room SQLite | Cart items (local) |
| SharedPreferences | User session, settings, sort preference |
| Internal Storage | User notes in Settings |

---

## 👨‍💻 Author

**Kavindu**
- GitHub: https://github.com/Kavindu-Dilshan-Dev
- Email: kavindu4543@gmail.com

---

## 📄 License

```
Copyright 2025 Kavindu

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

## 🙏 Acknowledgements

- [Firebase](https://firebase.google.com) — Backend & Auth
- [PayHere](https://www.payhere.lk) — Payment Gateway
- [Google Maps Platform](https://developers.google.com/maps) — Maps & Directions
- [Glide](https://github.com/bumptech/glide) — Image Loading
- [Material Components](https://material.io/components) — UI Components
