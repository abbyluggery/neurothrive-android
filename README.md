# NeuroThrive Android

A neurodivergent-friendly wellness and productivity Android application designed to sync with the NeuroThrive Salesforce platform. Built with modern Android architecture (Kotlin, Jetpack Compose, Room) for offline-first functionality.

> **Status:** Architecture Documented | Implementation Planned
> **Platform:** Android 8.0+ (API 26+)
> **Backend:** Salesforce Platform via REST API

---

## Project Vision

NeuroThrive Android brings the full wellness platform to mobile, enabling:

- **Daily Routine Tracking** - Morning/evening routines with completion tracking
- **Energy-Adaptive Scheduling** - Tasks adjusted to energy levels
- **Meal Planning Sync** - Today's meals, recipes, and shopping lists
- **Job Search Companion** - Review AI-analyzed job postings on the go
- **Wellness Check-ins** - Mood, energy, and gratitude logging

The app operates **offline-first**, syncing with Salesforce when connectivity is available.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    NEUROTHRIVE ANDROID                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    PRESENTATION LAYER                    │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │   │
│  │  │   Compose   │  │  ViewModels │  │  Navigation │     │   │
│  │  │     UI      │  │   (MVVM)    │  │    Graph    │     │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘     │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                      DOMAIN LAYER                        │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │   │
│  │  │  Use Cases  │  │  Entities   │  │ Repositories│     │   │
│  │  │             │  │  (Models)   │  │ (Interfaces)│     │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘     │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                       DATA LAYER                         │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │   │
│  │  │    Room     │  │  Sync       │  │  Salesforce │     │   │
│  │  │   Database  │  │  Manager    │  │  REST API   │     │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘     │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Technology Stack

| Layer | Technology | Purpose |
|-------|------------|---------|
| **UI** | Jetpack Compose | Modern declarative UI |
| **Architecture** | MVVM + Clean Architecture | Separation of concerns |
| **DI** | Hilt | Dependency injection |
| **Database** | Room | Local SQLite with type-safe queries |
| **Network** | Retrofit + OkHttp | REST API communication |
| **Auth** | OAuth 2.0 (Implicit Grant) | Salesforce authentication |
| **Async** | Kotlin Coroutines + Flow | Reactive data streams |
| **Testing** | JUnit + Espresso | Unit and UI testing |

---

## Module Structure

```
app/
├── data/
│   ├── local/           # Room database
│   │   ├── dao/         # Data Access Objects
│   │   ├── entities/    # Room entities
│   │   └── converters/  # Type converters
│   ├── remote/          # Salesforce API
│   │   ├── api/         # Retrofit interfaces
│   │   ├── dto/         # Data Transfer Objects
│   │   └── auth/        # OAuth handling
│   └── repository/      # Repository implementations
├── domain/
│   ├── model/           # Domain entities
│   ├── repository/      # Repository interfaces
│   └── usecase/         # Business logic
├── presentation/
│   ├── screens/         # Compose screens
│   ├── components/      # Reusable UI components
│   ├── viewmodel/       # ViewModels
│   └── navigation/      # Navigation graph
└── di/                  # Hilt modules
```

---

## Core Features

### 1. Daily Routine Tracking
- Morning routine checklist with completion status
- Evening routine wind-down tracking
- Sync status with Salesforce `Daily_Routine__c`

### 2. Energy Level Monitoring
- 1-10 energy scale with visual slider
- Historical energy patterns
- Syncs with `Daily_Routine__c.Energy_Level__c`

### 3. Meal Planning
- Today's planned meals from `Planned_Meal__c`
- Recipe viewer with ingredients from `Meal__c`
- Shopping list access from `Shopping_List__c`

### 4. Job Search Companion
- Browse AI-analyzed job postings from `Job_Posting__c`
- View fit scores, red flags, and requirements
- Quick actions (save, dismiss, apply)

### 5. Wellness Check-ins
- Mood logging with `Mood_Entry__c`
- Gratitude entries to `Gratitude_Entry__c`
- Daily wins tracking to `Win_Entry__c`

---

## Salesforce Integration

### REST Endpoints Used

| Endpoint | Object | Operations |
|----------|--------|------------|
| `/services/apexrest/routine/daily` | Daily_Routine__c | GET, POST, PUT |
| `/services/apexrest/mealplan/today` | Planned_Meal__c | GET |
| `/services/apexrest/jobposting` | Job_Posting__c | GET |
| `/services/data/vXX.0/sobjects/*` | All objects | Standard CRUD |

### Sync Strategy

```kotlin
// Offline-first approach
1. Display local Room data immediately
2. Queue changes in SyncQueue table
3. Attempt sync when network available
4. Merge conflicts (server wins for job data, device wins for check-ins)
```

---

## Data Models

### Local (Room) Entities

```kotlin
@Entity(tableName = "daily_routines")
data class DailyRoutineEntity(
    @PrimaryKey val id: String,
    val date: LocalDate,
    val energyLevel: Int?,
    val mood: String?,
    val morningComplete: Boolean,
    val eveningComplete: Boolean,
    val syncStatus: SyncStatus,
    val lastModified: Instant
)

@Entity(tableName = "job_postings")
data class JobPostingEntity(
    @PrimaryKey val id: String,
    val title: String,
    val company: String,
    val fitScore: Int?,
    val redFlags: List<String>,
    val status: String,
    val cachedAt: Instant
)

@Entity(tableName = "planned_meals")
data class PlannedMealEntity(
    @PrimaryKey val id: String,
    val mealId: String,
    val mealName: String,
    val mealType: String,
    val planDate: LocalDate,
    val recipeUrl: String?
)
```

---

## Authentication Flow

```
┌──────────┐      ┌───────────────┐      ┌────────────────┐
│  App     │──────│  Salesforce   │──────│  NeuroThrive   │
│  Login   │      │  OAuth Login  │      │  Connected App │
└──────────┘      └───────────────┘      └────────────────┘
     │                    │                      │
     │  1. Launch OAuth   │                      │
     │ ─────────────────► │                      │
     │                    │  2. User Credentials │
     │                    │ ◄──────────────────► │
     │  3. Access Token   │                      │
     │ ◄───────────────── │                      │
     │                    │                      │
     │  4. Store Token    │                      │
     │  (EncryptedSharedPrefs)                   │
     │                    │                      │
```

### OAuth Configuration

```kotlin
object OAuthConfig {
    const val AUTH_URL = "https://login.salesforce.com/services/oauth2/authorize"
    const val TOKEN_URL = "https://login.salesforce.com/services/oauth2/token"
    const val CALLBACK_URL = "neurothrive://oauth/callback"
    const val CLIENT_ID = "[From NeuroThrive Connected App]"
    // Client secret handled via PKCE for mobile
}
```

---

## Offline-First Design

### Room Database Schema

```kotlin
@Database(
    entities = [
        DailyRoutineEntity::class,
        JobPostingEntity::class,
        PlannedMealEntity::class,
        MealEntity::class,
        SyncQueueEntity::class
    ],
    version = 1
)
abstract class NeuroThriveDatabase : RoomDatabase() {
    abstract fun dailyRoutineDao(): DailyRoutineDao
    abstract fun jobPostingDao(): JobPostingDao
    abstract fun mealDao(): MealDao
    abstract fun syncQueueDao(): SyncQueueDao
}
```

### Sync Queue

```kotlin
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val objectType: String,
    val recordId: String,
    val operation: SyncOperation,  // CREATE, UPDATE, DELETE
    val payload: String,           // JSON serialized changes
    val createdAt: Instant,
    val attempts: Int = 0,
    val lastError: String? = null
)
```

---

## UI/UX Design Principles

### Neurodivergent-Friendly Features

| Feature | Purpose |
|---------|---------|
| **Low cognitive load** | One primary action per screen |
| **Clear visual hierarchy** | Important info prominent |
| **Progress indicators** | Always show sync/loading state |
| **Undo actions** | Forgiveness for accidental taps |
| **Dark mode** | Reduce sensory strain |
| **Quiet hours** | No notifications during focus time |

### Screen Navigation

```
Home (Dashboard)
├── Daily Routine
│   ├── Morning Checklist
│   └── Evening Checklist
├── Energy & Mood
│   ├── Log Energy
│   └── Mood History
├── Meals
│   ├── Today's Meals
│   ├── Recipe Viewer
│   └── Shopping List
├── Jobs
│   ├── Job Feed
│   ├── Job Details
│   └── Saved Jobs
└── Settings
    ├── Sync Status
    ├── Account
    └── Notifications
```

---

## Development Roadmap

### Phase 1: Core Infrastructure
- [ ] Project setup with Hilt, Room, Compose
- [ ] OAuth flow implementation
- [ ] Room database schema
- [ ] Basic sync manager

### Phase 2: Daily Routine Module
- [ ] Morning routine screen
- [ ] Evening routine screen
- [ ] Energy/mood logging
- [ ] Sync with Daily_Routine__c

### Phase 3: Meal Planning Module
- [ ] Today's meals view
- [ ] Recipe detail screen
- [ ] Shopping list viewer
- [ ] Offline recipe caching

### Phase 4: Job Search Module
- [ ] Job feed list
- [ ] Job detail view (scores, red flags)
- [ ] Quick actions (save, dismiss)
- [ ] Pull-to-refresh sync

### Phase 5: Polish & Testing
- [ ] Unit tests for ViewModels
- [ ] Integration tests for sync
- [ ] UI tests with Compose Testing
- [ ] Performance optimization

---

## Related Repositories

| Repository | Description |
|------------|-------------|
| [salesforce-ai-platform](https://github.com/abbyluggery/salesforce-ai-platform) | Salesforce platform (143 Apex classes) |
| [neurothrive-pwa](https://github.com/abbyluggery/neurothrive-pwa) | Progressive Web App version |
| [safehaven-android](https://github.com/abbyluggery/safehaven-android) | Safety companion app |

---

## Getting Started (Future)

```bash
# Clone repository
git clone https://github.com/abbyluggery/neurothrive-android.git

# Open in Android Studio (Arctic Fox or later)
# Sync Gradle dependencies
# Create local.properties with SDK path

# Configure OAuth
# 1. Get Consumer Key from NeuroThrive Connected App in Salesforce
# 2. Add to app/src/main/res/values/secrets.xml (gitignored)

# Build and run
./gradlew assembleDebug
```

---

## Author

**Abby Luggery**
- GitHub: [@abbyluggery](https://github.com/abbyluggery)
- LinkedIn: [linkedin.com/in/abby-luggery-02a4b815a](https://www.linkedin.com/in/abby-luggery-02a4b815a/)

---

*Part of the NeuroThrive ecosystem - built for neurodivergent minds that need systems that actually work.*
