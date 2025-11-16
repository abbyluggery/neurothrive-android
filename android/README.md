# NeuroThrive Android App

Native Android mobile app for neurodivergent wellness tracking with Salesforce integration.

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Database**: Room (SQLite)
- **Security**: AES-256 encryption via Android Keystore
- **Dependency Injection**: Hilt
- **API Client**: Retrofit + OkHttp
- **Background Tasks**: WorkManager
- **Authentication**: OAuth 2.0 with Salesforce

## Project Structure
```
android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/neurothrive/assistant/
│   │   │   │   ├── auth/              # OAuth Manager & Token Storage
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/         # Room database (entities, DAOs)
│   │   │   │   │   ├── remote/        # Salesforce API (models, service)
│   │   │   │   │   └── repository/    # Data sync logic
│   │   │   │   ├── di/                # Hilt dependency injection
│   │   │   │   ├── sync/              # WorkManager sync infrastructure
│   │   │   │   ├── ui/                # Jetpack Compose screens
│   │   │   │   ├── voice/             # Voice features (Session 3)
│   │   │   │   └── NeuroThriveApplication.kt
│   │   │   ├── res/                   # Android resources
│   │   │   └── AndroidManifest.xml
│   │   └── test/                      # Unit tests (4 test files)
│   └── build.gradle.kts
├── SALESFORCE_SETUP.md               # Salesforce Connected App guide
├── build.gradle.kts
└── settings.gradle.kts
```

## Features

### Database Schema
1. **MoodEntry** - Daily mood/energy/pain tracking
2. **WinEntry** - Personal achievements and wins
3. **JobPosting** - Job opportunities with ND-friendliness scores
4. **DailyRoutine** - Comprehensive daily wellness tracking

### Security
- AES-256-GCM encryption for sensitive data (Android Keystore)
- Encrypted SharedPreferences for OAuth tokens
- Automatic token refresh on expiration
- Secure storage of Salesforce credentials

### Salesforce Integration
- OAuth 2.0 authentication flow
- Bi-directional sync (local ↔ Salesforce)
- Background sync every 15 minutes (WorkManager)
- Manual sync option
- Offline-first architecture with sync queue
- External ID field for deduplication

### UI Components
- **MainActivity**: Dashboard with auth status, sync controls, database stats
- **OAuthActivity**: WebView-based Salesforce login
- Material 3 design system
- Real-time sync status indicators

## Session Status

### ✅ Session 1: Database Foundation (COMPLETE)
- [x] Android project structure
- [x] Gradle configuration
- [x] Room database with 4 entities
- [x] 4 DAO interfaces with CRUD operations
- [x] AES-256 encryption utilities
- [x] Unit tests (3 test files)

### ✅ Session 2: Salesforce OAuth Integration (COMPLETE)
- [x] OAuth 2.0 authentication flow (OAuthManager)
- [x] Secure token storage (TokenStorage + EncryptedSharedPreferences)
- [x] Retrofit API service for Salesforce REST API
- [x] SalesforceRepository for bi-directional sync
- [x] WorkManager background sync (every 15 minutes)
- [x] SyncManager for manual and scheduled sync
- [x] Hilt dependency injection modules
- [x] OAuthActivity with WebView login
- [x] Updated MainActivity with auth/sync UI
- [x] Unit tests for sync logic
- [x] Salesforce Connected App setup documentation

### 🔜 Session 3: Voice Integration (PENDING)
- [ ] Voice input for mood tracking
- [ ] Voice journaling
- [ ] Speech-to-text integration
- [ ] Voice command recognition

### 🔜 Session 4: UI Polish (PENDING)
- [ ] Full CRUD screens for all entities
- [ ] Charts and data visualization
- [ ] Settings screen
- [ ] Onboarding flow
- [ ] Dark mode support

## Setup Instructions

### 1. Build Android App
```bash
cd android
./gradlew build
./gradlew test
```

### 2. Configure Salesforce Connected App
See [SALESFORCE_SETUP.md](SALESFORCE_SETUP.md) for detailed instructions.

**Quick steps**:
1. Create Connected App in Salesforce
2. Set callback URL: `neurothrive://oauth/callback`
3. Enable OAuth scopes: `api`, `refresh_token`, `web`
4. Copy Consumer Key & Secret to `OAuthManager.kt`
5. Create custom objects in Salesforce (Mood_Entry__c, Win_Entry__c, etc.)

### 3. Update OAuth Credentials
Edit `app/src/main/java/com/neurothrive/assistant/auth/OAuthManager.kt`:

```kotlin
const val CLIENT_ID = "YOUR_SALESFORCE_CONSUMER_KEY"
const val CLIENT_SECRET = "YOUR_SALESFORCE_CONSUMER_SECRET"
```

### 4. Run the App
1. Launch app in Android Studio or via `./gradlew installDebug`
2. Tap **Login** button
3. Authenticate with Salesforce (abbyluggery179@agentforce.com)
4. Grant permissions
5. App will redirect back and schedule background sync

## API Integration

### Salesforce REST API Endpoints
- **Base URL**: `https://abbyluggery179.my.salesforce.com`
- **API Version**: v59.0
- **Auth**: OAuth 2.0 Bearer token
- **Content-Type**: application/json

### Sync Behavior
- **Frequency**: Every 15 minutes (configurable)
- **Strategy**: Offline-first with sync queue
- **Conflict Resolution**: Last-write-wins
- **Deduplication**: External ID field (`External_Id__c`)
- **Retry Logic**: Exponential backoff on failure

### Sync Flow
1. Query local database for unsynced records (`syncedToSalesforce = false`)
2. For each record:
   - If no `salesforceId`: POST to Salesforce (create)
   - If has `salesforceId`: PATCH to Salesforce (update)
3. On success: Update local record with `syncedToSalesforce = true` and Salesforce ID
4. On failure: Retry with exponential backoff

## Architecture

### Dependency Injection (Hilt)
- **AppModule**: Provides database, DAOs, Gson
- **NetworkModule**: Provides Retrofit, OkHttp, API service, auth interceptor

### Data Flow
```
UI (Compose) → ViewModel → Repository → [Local DB / Remote API]
                                          ↓              ↓
                                       Room         Retrofit
                                          ↓              ↓
                                      SQLite      Salesforce
```

### Background Sync (WorkManager)
- **SyncWorker**: Performs actual sync operation
- **SyncManager**: Schedules periodic work and handles manual sync
- **Constraints**: Requires network connectivity
- **Retry Policy**: Exponential backoff

## Testing

### Run All Tests
```bash
./gradlew test
```

### Test Files
1. `MoodEntryDaoTest.kt` - Room database CRUD tests
2. `WinEntryDaoTest.kt` - Win entry persistence tests
3. `JobPostingDaoTest.kt` - Job posting queries
4. `SalesforceRepositoryTest.kt` - Sync logic and state management

### Test Framework
- **JUnit 4**
- **Robolectric** (Android unit tests without emulator)
- **Kotlinx Coroutines Test** (Async testing)
- **Room Testing** (In-memory database)

## Security Considerations

1. **OAuth Tokens**: Stored in EncryptedSharedPreferences with AES-256
2. **Sensitive Data**: Encrypted using Android Keystore (hardware-backed)
3. **Network**: HTTPS only for API calls
4. **Secrets**: Never commit `CLIENT_SECRET` to version control
5. **Token Refresh**: Automatic refresh before expiration
6. **Logout**: Revokes tokens and clears local storage

## Troubleshooting

### OAuth Errors
See [SALESFORCE_SETUP.md#Troubleshooting](SALESFORCE_SETUP.md#troubleshooting)

### Build Errors
```bash
# Clean build
./gradlew clean build

# Sync Gradle files
./gradlew --refresh-dependencies
```

### Sync Not Working
- Check auth status in app (should show "Connected to Salesforce")
- Verify network connectivity
- Check WorkManager status: `adb shell dumpsys jobscheduler | grep neurothrive`
- Review logs: `adb logcat | grep NeuroThrive`

## Development

### Add New Salesforce Object
1. Create entity in `data/local/entities/`
2. Create DAO in `data/local/dao/`
3. Add to `AppDatabase.kt`
4. Create Salesforce model in `data/remote/models/`
5. Add API endpoints to `SalesforceApiService.kt`
6. Add sync logic to `SalesforceRepository.kt`

### Code Style
- **Kotlin**: Official style guide
- **Compose**: Material 3 guidelines
- **Architecture**: MVVM + Repository pattern

## Integration Target
- **Salesforce Org**: abbyluggery179@agentforce.com
- **API Base URL**: https://abbyluggery179.my.salesforce.com
- **OAuth Callback**: neurothrive://oauth/callback

## Next Steps (Session 3)
- Voice input integration
- Speech-to-text for journaling
- Voice commands for hands-free tracking
- Accessibility improvements

## License
MIT (placeholder - update as needed)
