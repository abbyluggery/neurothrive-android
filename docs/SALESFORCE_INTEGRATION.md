# NeuroThrive Android - Salesforce Integration

## Overview

NeuroThrive Android connects to a Salesforce org containing 42 custom objects, using OAuth 2.0 for authentication and REST APIs for data synchronization.

---

## Authentication

### OAuth 2.0 Implicit Grant Flow

For mobile apps without a secure backend, we use the Implicit Grant flow with PKCE enhancement for security.

```kotlin
object SalesforceAuth {
    const val AUTH_ENDPOINT = "https://login.salesforce.com/services/oauth2/authorize"
    const val TOKEN_ENDPOINT = "https://login.salesforce.com/services/oauth2/token"
    const val REVOKE_ENDPOINT = "https://login.salesforce.com/services/oauth2/revoke"

    fun buildAuthUrl(config: OAuthConfig): String {
        return Uri.parse(AUTH_ENDPOINT).buildUpon()
            .appendQueryParameter("response_type", "token")
            .appendQueryParameter("client_id", config.clientId)
            .appendQueryParameter("redirect_uri", config.callbackUrl)
            .appendQueryParameter("scope", "api refresh_token")
            .appendQueryParameter("display", "touch")
            .build()
            .toString()
    }
}
```

### Token Storage

```kotlin
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "salesforce_tokens",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var accessToken: String?
        get() = prefs.getString("access_token", null)
        set(value) = prefs.edit().putString("access_token", value).apply()

    var instanceUrl: String?
        get() = prefs.getString("instance_url", null)
        set(value) = prefs.edit().putString("instance_url", value).apply()

    var refreshToken: String?
        get() = prefs.getString("refresh_token", null)
        set(value) = prefs.edit().putString("refresh_token", value).apply()
}
```

---

## Connected App Configuration

### Salesforce Setup Requirements

1. **Connected App Name:** NeuroThrive Android
2. **Callback URL:** `neurothrive://oauth/callback`
3. **Selected OAuth Scopes:**
   - `api` - Access REST API
   - `refresh_token` - Offline access
   - `openid` - OpenID Connect
4. **CORS Origins:** Not required for native app

### Consumer Key Location
Navigate in Salesforce:
```
Setup → Apps → App Manager → NeuroThrive Android → View → Consumer Key
```

---

## REST API Endpoints

### Custom Apex REST Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/services/apexrest/routine/daily` | GET | Get today's routine |
| `/services/apexrest/routine/daily` | POST | Create/update routine |
| `/services/apexrest/mealplan/today` | GET | Get today's meals |
| `/services/apexrest/jobposting` | GET | List analyzed jobs |

### Standard Salesforce REST

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/services/data/v59.0/sobjects/{Object}` | GET/POST | Standard CRUD |
| `/services/data/v59.0/query` | GET | SOQL queries |
| `/services/data/v59.0/composite` | POST | Batch operations |

---

## Retrofit API Interface

```kotlin
interface SalesforceApi {

    // Custom Apex REST
    @GET("services/apexrest/routine/daily")
    suspend fun getTodayRoutine(): DailyRoutineDto

    @POST("services/apexrest/routine/daily")
    suspend fun saveDailyRoutine(@Body routine: DailyRoutineDto): SaveResult

    @GET("services/apexrest/mealplan/today")
    suspend fun getTodayMeals(): List<PlannedMealDto>

    @GET("services/apexrest/jobposting")
    suspend fun getJobPostings(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 50
    ): List<JobPostingDto>

    // Standard REST
    @GET("services/data/v59.0/query")
    suspend fun query(@Query("q") soql: String): QueryResult<JsonObject>

    @POST("services/data/v59.0/sobjects/{object}")
    suspend fun createRecord(
        @Path("object") objectName: String,
        @Body record: JsonObject
    ): CreateResult

    @PATCH("services/data/v59.0/sobjects/{object}/{id}")
    suspend fun updateRecord(
        @Path("object") objectName: String,
        @Path("id") recordId: String,
        @Body fields: JsonObject
    )

    @DELETE("services/data/v59.0/sobjects/{object}/{id}")
    suspend fun deleteRecord(
        @Path("object") objectName: String,
        @Path("id") recordId: String
    )

    // Composite for batch operations
    @POST("services/data/v59.0/composite")
    suspend fun composite(@Body request: CompositeRequest): CompositeResponse
}
```

---

## Data Transfer Objects

```kotlin
// Response from /services/apexrest/routine/daily
data class DailyRoutineDto(
    val Id: String?,
    val Name: String?,
    val Date__c: String,           // 2025-12-27
    val Energy_Level__c: Int?,
    val Mood__c: String?,
    val Morning_Routine_Complete__c: Boolean?,
    val Evening_Routine_Complete__c: Boolean?,
    val Notes__c: String?
)

// Response from /services/apexrest/mealplan/today
data class PlannedMealDto(
    val Id: String,
    val Meal__c: String,           // Meal__c record Id
    val Meal_Name__c: String,
    val Meal_Type__c: String,      // Breakfast, Lunch, Dinner, Snack
    val Plan_Date__c: String,
    val Recipe_URL__c: String?,
    val Prep_Time__c: Int?,
    val Calories__c: Int?
)

// Response from /services/apexrest/jobposting
data class JobPostingDto(
    val Id: String,
    val Name: String,
    val Company__c: String,
    val Title__c: String,
    val Fit_Score__c: Int?,
    val Red_Flags__c: String?,     // Comma-separated
    val Status__c: String,
    val Salary_Min__c: Double?,
    val Salary_Max__c: Double?,
    val Remote_Status__c: String?,
    val Application_URL__c: String?
)
```

---

## Object Mappings

### Daily_Routine__c

| Salesforce Field | Android Field | Type |
|-----------------|---------------|------|
| Id | id | String |
| Date__c | date | LocalDate |
| Energy_Level__c | energyLevel | Int? |
| Mood__c | mood | String? |
| Morning_Routine_Complete__c | morningComplete | Boolean |
| Evening_Routine_Complete__c | eveningComplete | Boolean |
| Notes__c | notes | String? |

### Job_Posting__c

| Salesforce Field | Android Field | Type |
|-----------------|---------------|------|
| Id | id | String |
| Title__c | title | String |
| Company__c | company | String |
| Fit_Score__c | fitScore | Int? |
| Red_Flags__c | redFlags | List<String> |
| Status__c | status | JobStatus |
| Salary_Min__c | salaryMin | Double? |
| Salary_Max__c | salaryMax | Double? |
| Remote_Status__c | remoteStatus | RemoteStatus |

### Meal__c

| Salesforce Field | Android Field | Type |
|-----------------|---------------|------|
| Id | id | String |
| Name | name | String |
| Meal_Type__c | type | MealType |
| Prep_Time_Minutes__c | prepTimeMinutes | Int |
| Total_Calories__c | calories | Int? |
| Recipe_URL__c | recipeUrl | String? |
| Instructions__c | instructions | String |

---

## Sync Conflict Resolution

### Strategy: Server Wins (with exceptions)

```kotlin
enum class ConflictStrategy {
    SERVER_WINS,      // Default for job postings (AI analysis is authoritative)
    CLIENT_WINS,      // For check-ins (local entry is user intent)
    MERGE             // For routines (combine morning/evening independently)
}

fun resolveConflict(local: Entity, remote: Entity, strategy: ConflictStrategy): Entity {
    return when (strategy) {
        ConflictStrategy.SERVER_WINS -> remote
        ConflictStrategy.CLIENT_WINS -> local
        ConflictStrategy.MERGE -> mergeFields(local, remote)
    }
}
```

---

## Error Handling

```kotlin
sealed class SalesforceError : Exception() {
    object Unauthorized : SalesforceError()           // 401 - Need re-auth
    object Forbidden : SalesforceError()              // 403 - Missing permission
    object NotFound : SalesforceError()               // 404 - Record deleted
    object Conflict : SalesforceError()               // 409 - Version mismatch
    data class ValidationError(val errors: List<String>) : SalesforceError() // 400
    data class ServerError(val code: Int) : SalesforceError() // 5xx
}

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.accessToken
            ?: throw SalesforceError.Unauthorized

        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .build()

        val response = chain.proceed(request)

        if (response.code == 401) {
            // Attempt token refresh
            if (refreshToken()) {
                // Retry with new token
                return chain.proceed(request.newBuilder()
                    .header("Authorization", "Bearer ${tokenManager.accessToken}")
                    .build())
            }
            throw SalesforceError.Unauthorized
        }

        return response
    }
}
```

---

## Batch Operations

For efficiency, use Composite API to batch multiple operations:

```kotlin
data class CompositeRequest(
    val allOrNone: Boolean = true,
    val compositeRequest: List<CompositeSubrequest>
)

data class CompositeSubrequest(
    val method: String,
    val url: String,
    val referenceId: String,
    val body: JsonObject? = null
)

// Example: Sync 5 routine updates in single call
suspend fun batchSyncRoutines(routines: List<DailyRoutine>) {
    val subrequests = routines.mapIndexed { index, routine ->
        CompositeSubrequest(
            method = if (routine.id == null) "POST" else "PATCH",
            url = if (routine.id == null)
                "/services/data/v59.0/sobjects/Daily_Routine__c"
            else
                "/services/data/v59.0/sobjects/Daily_Routine__c/${routine.id}",
            referenceId = "routine_$index",
            body = routine.toSalesforceJson()
        )
    }

    salesforceApi.composite(CompositeRequest(compositeRequest = subrequests))
}
```
