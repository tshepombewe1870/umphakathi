# Umphakathi (CrisisReporting)

**Umphakathi** (meaning "Community") is a mobile-first platform designed to empower citizens and local organizations to report, track, and manage community crises and infrastructure issues. By leveraging collective reporting and real-time collaboration, Umphakathi bridges the gap between community needs and effective responses.

## 🚀 Key Features

### 📢 Incident Reporting
- **Multi-step Report Wizard**: Easily report issues like water leaks, power outages, infrastructure damage, or safety concerns.
- **Privacy Controls**: Share precise GPS locations, approximate neighborhoods, or keep locations restricted to verified responders.
- **Visual Evidence**: Attach photos to reports to provide context and urgency.

### 🤝 Community Interaction
- **Join Local Communities**: Connect with others in your geographic area or specific organizations.
- **Corroboration ("Me Too")**: Strengthen reports by adding your own experience to existing incidents, helping authorities prioritize high-impact issues.
- **Volunteer Matching**: Offer specific resources (Manpower, Transport, Supplies) directly to reports in need of help.

### 🏛️ Organizational Management
- **Official Updates**: Verified organizations can post status updates on active incidents.
- **Community Notice Board**: Post and approve announcements for planned outages, community events, or protests.
- **Moderation Workflow**: Community owners and moderators can manage notice approvals to ensure feed quality.

### 🆘 Crisis Management
- **Crisis Flagging**: Escalate individual reports into broader crises for better visibility and coordination.
- **Incident Timeline**: Track the history of an issue from submission to resolution.

## 🛠️ Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for a modern, declarative UI.
- **Navigation**: [Android Navigation 3](https://developer.android.com/guide/navigation/navigation-kotlin-dsl) (Experimental/Latest).
- **Backend**: [Firebase](https://firebase.google.com/) (Firestore, Authentication, Storage, Analytics).
- **Concurrency**: Kotlin Coroutines and Flows for reactive data streams.
- **Local Persistence**: [Room Database](https://developer.android.com/training/data-storage/room) for caching (Alpha features).
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/) for Compose.
- **Material Design**: Material 3 for the latest design standards.

## 🏗️ Architecture

The project follows Clean Architecture principles and a unidirectional data flow (UDF) pattern:

- **Domain Layer**: Contains business logic, entity models, and repository interfaces.
- **Data Layer**: Implements repository interfaces using Firestore and provides mock implementations for development and testing.
- **UI Layer**: Comprises Composable screens and ViewModels that manage state using `StateFlow`.

## 🚦 Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 17
- A Firebase Project

### Setup
1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-repo/CrisisReporting.git
   ```
2. **Add Firebase**:
   - Place your `google-services.json` in the `app/` directory.
   - Enable Email/Password authentication in the Firebase Console.
   - Enable Cloud Firestore and Cloud Storage.
3. **Build & Run**:
   - Sync the project with Gradle files.
   - Run the app on an emulator (API 26+) or a physical device.

### Development Seeding
For testing purposes, the app includes a `FirebaseSeeder` that can populate your Firestore instance with sample reports, communities, and notices on the first launch.

## 📁 Project Structure
```text
app/src/main/java/com/sumsokol/umphakathi/
├── data/
│   ├── firebase/       # Firestore implementations
│   ├── mock/           # Mock data for rapid development
│   └── model/          # Data transfer objects & mapping
├── domain/
│   ├── model/          # Core business entities
│   └── repository/     # Interface definitions
├── ui/
│   ├── auth/           # Login and Registration
│   ├── community/      # Community lists & details
│   ├── crisis/         # Crisis-specific views
│   ├── profile/        # User and Public profiles
│   ├── report/         # Reporting wizard and incident details
│   └── components/     # Reusable UI widgets
└── Navigation.kt       # Centralized routing logic
```

## 🤝 Contributing
Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.
