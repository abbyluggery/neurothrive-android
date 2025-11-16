# NeuroThrive Android App

Native Android mobile app for neurodivergent wellness tracking.

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Database**: Room (SQLite)
- **Security**: AES-256 encryption via Android Keystore
- **Dependency Injection**: Hilt
- **API Client**: Retrofit
- **Background Tasks**: WorkManager

## Project Structure
```
android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/neurothrive/assistant/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── entities/    # Room entities (MoodEntry, WinEntry, etc.)
│   │   │   │   │   │   ├── dao/         # Data Access Objects
│   │   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   │   └── SecurityUtils.kt
│   │   │   │   │   ├── api/             # Salesforce API clients (Session 2)
│   │   │   │   │   └── voice/           # Voice features (Session 3)
│   │   │   │   ├── ui/                  # Jetpack Compose UI
│   │   │   │   └── NeuroThriveApplication.kt
│   │   │   ├── res/                     # Android resources
│   │   │   └── AndroidManifest.xml
│   │   └── test/                        # Unit tests
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## Database Schema

### Entities
1. **MoodEntry** - Daily mood/energy/pain tracking
2. **WinEntry** - Personal achievements and wins
3. **JobPosting** - Job opportunities with ND-friendliness scores
4. **DailyRoutine** - Comprehensive daily wellness tracking

### Features
- Local SQLite database with Room ORM
- AES-256 encryption for sensitive data
- Sync flags for Salesforce integration
- Flow-based reactive queries

## Session 1 Status: ✅ COMPLETE

### Completed
- [x] Android project structure
- [x] Gradle configuration
- [x] Room database setup
- [x] 4 entity classes
- [x] 4 DAO interfaces with CRUD operations
- [x] AES-256 encryption utilities
- [x] MainActivity with Jetpack Compose placeholder
- [x] Unit tests (3 test files)

### Validation
- ✅ No .cls files (this is Android, not Salesforce)
- ✅ No -meta.xml files (this is Android, not Salesforce)
- ✅ MainActivity.kt created
- ✅ Kotlin/Jetpack Compose architecture

## Next Steps (Session 2)
- Salesforce OAuth 2.0 integration
- REST API client with Retrofit
- Sync Manager with WorkManager
- Background sync every 15 minutes

## Build Instructions
```bash
cd android
./gradlew build
./gradlew test
```

## Integration Target
- Salesforce Org: abbyluggery179@agentforce.com
- API Base URL: https://abbyluggery179.my.salesforce.com
- OAuth 2.0 Connected App (to be created in Session 2)
