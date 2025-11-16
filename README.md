# NeuroThrive Android Assistant

A comprehensive Android mobile application for mental health tracking, job search management, and daily routine organization with Salesforce integration and voice commands.

## Overview

NeuroThrive is a native Android app built with Kotlin and Jetpack Compose that helps users track their mental health, celebrate wins, manage job searches, and maintain daily routines. All data is securely stored locally with AES-256 encryption and synced to Salesforce.

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Database**: Room (SQLite) with AES-256 encryption
- **Architecture**: MVVM + Repository pattern
- **Dependency Injection**: Hilt
- **API Client**: Retrofit + OkHttp
- **Background Tasks**: WorkManager (15-minute periodic sync)
- **Authentication**: OAuth 2.0 (Salesforce Connected App)
- **Voice**: Android SpeechRecognizer, TextToSpeech
- **Navigation**: Navigation Compose with bottom navigation
- **Theme**: Material 3 with dark/light mode support
- **Preferences**: DataStore

## Features

### Session 1: Database Foundation ✅ COMPLETE
- **Room Database** with 4 encrypted entities:
  - `MoodEntry`: Track mood, energy, and pain levels (1-10 scale)
  - `WinEntry`: Record daily achievements and wins
  - `JobPosting`: Save job opportunities with descriptions and links
  - `DailyRoutine`: Log daily activities and routines
- **AES-256-GCM Encryption** via Android Keystore
- **DAO Interfaces** with full CRUD operations
- **Unit Tests** for all database operations

### Session 2: Salesforce OAuth Integration ✅ COMPLETE
- **OAuth 2.0 Authentication** with Salesforce Connected App
- **Token Management** with automatic refresh using EncryptedSharedPreferences
- **Salesforce API Client** with Retrofit
- **Bi-directional Sync**:
  - Push local changes to Salesforce custom objects
  - Pull Salesforce updates to local database
- **Background Sync** with WorkManager (every 15 minutes)
- **Offline-First Architecture** with sync queue
- **Complete Documentation** in `SALESFORCE_SETUP.md`

### Session 3: Voice Integration ✅ COMPLETE
- **Voice Recognition** using Android SpeechRecognizer
- **Natural Language Processing** for voice commands:
  - "My mood is 7 with energy 6 and pain 3"
  - "I won today by completing the project"
  - "Add job posting: Software Engineer at Google"
- **Text-to-Speech Feedback** for command confirmation
- **Voice Input Screen** with animated microphone UI
- **Permissions Handling** for RECORD_AUDIO
- **24 Unit Tests** for voice command parsing

### Session 4: UI Polish ✅ COMPLETE
- **Full CRUD Screens**:
  - `MoodListScreen`: View, add, edit, delete mood entries with sliders
  - `WinListScreen`: Manage wins with category filtering
  - `SettingsScreen`: Configure app preferences and theme
  - `HomeScreen`: Dashboard with sync status and data statistics
- **Bottom Navigation** with 4 tabs (Home, Mood, Wins, Settings)
- **Dark Mode Support** with Material 3 themes
- **Settings Persistence** using DataStore
- **Floating Action Button** for quick voice access
- **Permission Handling** for microphone access

## Project Structure

```
android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/neurothrive/assistant/
│   │   │   │   ├── auth/                    # OAuth managers
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/               # Room database, DAOs, entities
│   │   │   │   │   ├── models/              # Salesforce API models
│   │   │   │   │   └── repository/          # Data repositories
│   │   │   │   ├── di/                      # Hilt dependency injection
│   │   │   │   ├── network/                 # Retrofit API services
│   │   │   │   ├── sync/                    # WorkManager sync workers
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/             # Composable screens
│   │   │   │   │   └── theme/               # Material 3 themes
│   │   │   │   └── voice/                   # Voice recognition & TTS
│   │   │   └── AndroidManifest.xml
│   │   └── test/                            # Unit tests
│   └── build.gradle.kts
└── build.gradle.kts
```

## Setup Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK with API 34
- Salesforce Developer Account

### 1. Clone the Repository
```bash
git clone https://github.com/abbyluggery/neurothrive-android.git
cd neurothrive-android
```

### 2. Configure Salesforce Connected App
Follow the detailed instructions in `SALESFORCE_SETUP.md` to:
1. Create a Salesforce Connected App
2. Configure OAuth settings
3. Create custom objects for each entity
4. Update `OAuthManager.kt` with your Client ID

### 3. Build and Run
```bash
cd android
./gradlew build
./gradlew installDebug
```

Or open the `android/` directory in Android Studio and click Run.

## Usage

### First Launch
1. **Grant Permissions**: Allow microphone access for voice features
2. **Login to Salesforce**: Tap the login button to authenticate
3. **Start Tracking**: Add mood entries, wins, and routines

### Voice Commands Examples
- **Mood**: "My mood is 8 with energy 7 and pain 2"
- **Win**: "I achieved launching the new feature today"
- **Job**: "Add job posting: Senior Android Developer at Meta, building mobile apps"

### Manual Entry
- Navigate to **Mood** or **Wins** tab
- Tap the "+" button to add entries
- Use sliders to set levels (mood/energy/pain)
- Add optional notes

### Sync Status
- View sync status on the **Home** screen
- Manual sync: Tap the refresh button
- Automatic sync: Every 15 minutes in background

### Settings
- Toggle **Dark Mode** for preferred theme
- View app information and preferences

## Security

- **AES-256-GCM Encryption**: All local database entries encrypted
- **Android Keystore**: Secure key storage
- **EncryptedSharedPreferences**: OAuth tokens encrypted at rest
- **HTTPS Only**: All Salesforce API calls over TLS 1.2+

## Testing

### Run Unit Tests
```bash
./gradlew test
```

### Test Coverage
- Database DAOs: Full CRUD operations
- Voice command parsing: 24 test cases
- Data models: Serialization/deserialization

## Dependencies

Key libraries:
- Jetpack Compose BOM 2023.10.01
- Room 2.6.0
- Retrofit 2.9.0
- Hilt 2.48
- WorkManager 2.9.0
- Navigation Compose 2.7.5
- DataStore 1.0.0
- Timber 5.0.1

See `android/app/build.gradle.kts` for complete dependency list.

## Development Sessions

- ✅ **Session 1**: Database foundation with Room, encryption, entities, DAOs, tests
- ✅ **Session 2**: Salesforce OAuth 2.0 integration, API client, background sync
- ✅ **Session 3**: Voice integration with speech-to-text and TTS
- ✅ **Session 4**: UI polish with CRUD screens, dark mode, settings, navigation

## Platform Validation

This is a **native Android application** built with Kotlin:
- ✅ 43 Kotlin (.kt) files
- ✅ 0 Salesforce Apex (.cls) files
- ✅ 0 Salesforce metadata files

## License

MIT License - See LICENSE file for details

## Support

For issues or questions, please open an issue on GitHub.

---

**Built with ❤️ using Jetpack Compose and Material 3**
