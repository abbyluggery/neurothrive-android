# NeuroThrive Android - Technical Architecture

## Overview

NeuroThrive Android follows Clean Architecture with MVVM presentation pattern, designed for offline-first operation with Salesforce synchronization.

---

## Layer Responsibilities

### Presentation Layer
- **Screens:** Jetpack Compose UI components
- **ViewModels:** State management with `StateFlow`
- **Navigation:** Single Activity with Compose Navigation

### Domain Layer
- **Use Cases:** Single-responsibility business operations
- **Repository Interfaces:** Abstraction over data sources
- **Domain Models:** Pure Kotlin data classes

### Data Layer
- **Room Database:** Local persistence with DAOs
- **Retrofit Client:** Salesforce REST API calls
- **Repository Implementations:** Data source coordination

---

## Key Architectural Patterns

### 1. Offline-First with Sync Queue

```kotlin
// All writes go to local DB first, then queue for sync
class SyncAwareRepository<T>(
    private val localDataSource: LocalDataSource<T>,
    private val remoteDataSource: RemoteDataSource<T>,
    private val syncQueue: SyncQueueDao
) {
    suspend fun save(entity: T) {
        localDataSource.insert(entity)
        syncQueue.enqueue(SyncOperation.UPDATE, entity)
    }
}
```

### 2. Unidirectional Data Flow

```
User Action → ViewModel → Use Case → Repository → Data Source
                ↑                                      ↓
              UI State ←←←←←←←←←← StateFlow ←←←←←←←←←←
```

### 3. Repository Pattern with Multiple Sources

```kotlin
class DailyRoutineRepositoryImpl(
    private val localDao: DailyRoutineDao,
    private val salesforceApi: SalesforceApi,
    private val syncManager: SyncManager
) : DailyRoutineRepository {

    override fun getTodayRoutine(): Flow<DailyRoutine> {
        return localDao.getByDate(LocalDate.now())
            .map { it?.toDomain() ?: DailyRoutine.empty() }
    }

    override suspend fun updateRoutine(routine: DailyRoutine) {
        localDao.upsert(routine.toEntity())
        syncManager.queueSync(SyncItem.routine(routine))
    }
}
```

---

## Dependency Injection (Hilt)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NeuroThriveDatabase {
        return Room.databaseBuilder(context, NeuroThriveDatabase::class.java, "neurothrive.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideDailyRoutineDao(db: NeuroThriveDatabase): DailyRoutineDao = db.dailyRoutineDao()

    @Provides
    @Singleton
    fun provideSalesforceApi(
        authManager: AuthManager,
        @Named("baseUrl") baseUrl: String
    ): SalesforceApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(authManager))
                .build())
            .build()
            .create(SalesforceApi::class.java)
    }
}
```

---

## ViewModel Pattern

```kotlin
@HiltViewModel
class DailyRoutineViewModel @Inject constructor(
    private val getTodayRoutine: GetTodayRoutineUseCase,
    private val updateRoutine: UpdateRoutineUseCase,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<DailyRoutineUiState>(DailyRoutineUiState.Loading)
    val uiState: StateFlow<DailyRoutineUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getTodayRoutine()
                .catch { _uiState.value = DailyRoutineUiState.Error(it.message) }
                .collect { routine ->
                    _uiState.value = DailyRoutineUiState.Success(routine)
                }
        }
    }

    fun onEnergyLevelChanged(level: Int) {
        viewModelScope.launch {
            val current = (_uiState.value as? DailyRoutineUiState.Success)?.routine ?: return@launch
            updateRoutine(current.copy(energyLevel = level))
        }
    }
}

sealed interface DailyRoutineUiState {
    object Loading : DailyRoutineUiState
    data class Success(val routine: DailyRoutine) : DailyRoutineUiState
    data class Error(val message: String?) : DailyRoutineUiState
}
```

---

## Sync Manager

```kotlin
class SyncManager @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val salesforceApi: SalesforceApi,
    private val connectivityManager: ConnectivityManager
) {
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun queueSync(item: SyncItem) {
        syncScope.launch {
            syncQueueDao.insert(item.toEntity())
            if (connectivityManager.isNetworkAvailable()) {
                processPendingSync()
            }
        }
    }

    suspend fun processPendingSync() {
        val pending = syncQueueDao.getPending()
        pending.forEach { item ->
            try {
                when (item.operation) {
                    SyncOperation.CREATE -> salesforceApi.create(item.objectType, item.payload)
                    SyncOperation.UPDATE -> salesforceApi.update(item.objectType, item.recordId, item.payload)
                    SyncOperation.DELETE -> salesforceApi.delete(item.objectType, item.recordId)
                }
                syncQueueDao.markComplete(item.id)
            } catch (e: Exception) {
                syncQueueDao.incrementRetry(item.id, e.message)
            }
        }
    }
}
```

---

## Error Handling Strategy

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// In Repository
suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
    return try {
        Result.Success(apiCall())
    } catch (e: HttpException) {
        when (e.code()) {
            401 -> Result.Error(AuthenticationException("Session expired"))
            403 -> Result.Error(PermissionException("Access denied"))
            else -> Result.Error(e)
        }
    } catch (e: IOException) {
        Result.Error(NetworkException("No internet connection"))
    }
}
```

---

## Testing Strategy

### Unit Tests (ViewModels, Use Cases)
```kotlin
@Test
fun `when routine updated, state reflects change`() = runTest {
    val viewModel = DailyRoutineViewModel(mockGetRoutine, mockUpdateRoutine, mockSyncManager)

    viewModel.onEnergyLevelChanged(8)

    val state = viewModel.uiState.value as DailyRoutineUiState.Success
    assertEquals(8, state.routine.energyLevel)
}
```

### Integration Tests (Repository + Room)
```kotlin
@Test
fun `saved routine persists across app restart`() = runTest {
    repository.updateRoutine(testRoutine)

    val loaded = repository.getTodayRoutine().first()

    assertEquals(testRoutine.energyLevel, loaded.energyLevel)
}
```

### UI Tests (Compose)
```kotlin
@Test
fun `energy slider updates displayed value`() {
    composeTestRule.setContent {
        EnergyLevelSlider(value = 5, onValueChange = {})
    }

    composeTestRule.onNodeWithTag("energy_slider").performTouchInput {
        swipeRight()
    }

    composeTestRule.onNodeWithText("8").assertIsDisplayed()
}
```

---

## Performance Considerations

1. **Room Queries:** Use `@Query` with proper indexing on date fields
2. **Flow Collection:** Collect in `repeatOnLifecycle` to avoid leaks
3. **Image Loading:** Use Coil with disk caching for meal photos
4. **Sync Batching:** Group multiple changes into single API calls
5. **WorkManager:** Schedule background sync during charging/WiFi
