# Salesforce Connected App Setup Guide

This guide explains how to configure the Salesforce Connected App required for OAuth 2.0 authentication.

## Prerequisites
- Access to Salesforce org: abbyluggery179@agentforce.com
- System Administrator or API Enabled User permissions

## Step 1: Create Connected App

1. Log in to Salesforce: https://abbyluggery179.my.salesforce.com
2. Navigate to: **Setup → Apps → App Manager**
3. Click **New Connected App**

### Basic Information
- **Connected App Name**: `NeuroThrive Mobile App`
- **API Name**: `NeuroThrive_Mobile_App`
- **Contact Email**: your-email@example.com

### API (Enable OAuth Settings)
Check **Enable OAuth Settings**

- **Callback URL**: `neurothrive://oauth/callback`
- **Selected OAuth Scopes**:
  - `Access and manage your data (api)`
  - `Perform requests on your behalf at any time (refresh_token, offline_access)`
  - `Provide access to your data via the Web (web)`

- **Require Secret for Web Server Flow**: ✓ Checked
- **Require Secret for Refresh Token Flow**: ✓ Checked

4. Click **Save**
5. Click **Continue**

## Step 2: Get Consumer Key and Secret

1. After creating the app, go to **App Manager**
2. Find **NeuroThrive Mobile App** in the list
3. Click the dropdown arrow → **View**
4. Under **API (Enable OAuth Settings)**, you'll see:
   - **Consumer Key** (Client ID)
   - **Consumer Secret** (Click "Click to reveal" to see it)

5. **Copy these values** - you'll need them in the next step

## Step 3: Configure Android App

Update the OAuth configuration in the Android app:

**File**: `android/app/src/main/java/com/neurothrive/assistant/auth/OAuthManager.kt`

Replace the placeholder values:

```kotlin
const val CLIENT_ID = "YOUR_CONSUMER_KEY_HERE"
const val CLIENT_SECRET = "YOUR_CONSUMER_SECRET_HERE"
```

With your actual values from Step 2:

```kotlin
const val CLIENT_ID = "3MVG9..." // Your actual Consumer Key
const val CLIENT_SECRET = "A1B2..." // Your actual Consumer Secret
```

## Step 4: Create Custom Objects

The app requires custom objects in Salesforce. Run these scripts in Developer Console:

### 4.1 Mood Entry Object

Navigate to: **Setup → Object Manager → Create → Custom Object**

**Object Name**: `Mood Entry`
**API Name**: `Mood_Entry__c`

**Custom Fields**:
- `Mood_Level__c` (Number, 0 decimal places)
- `Energy_Level__c` (Number, 0 decimal places)
- `Pain_Level__c` (Number, 0 decimal places)
- `Entry_Date__c` (Date/Time)
- `Notes__c` (Long Text Area, 32,768 characters)
- `External_Id__c` (Text, 255, External ID ✓, Unique ✓)

### 4.2 Win Entry Object

**Object Name**: `Win Entry`
**API Name**: `Win_Entry__c`

**Custom Fields**:
- `Description__c` (Long Text Area, 32,768 characters)
- `Category__c` (Picklist: career, health, personal)
- `Win_Date__c` (Date/Time)
- `External_Id__c` (Text, 255, External ID ✓, Unique ✓)

### 4.3 Job Posting Object

**Object Name**: `Job Posting`
**API Name**: `Job_Posting__c`

**Custom Fields**:
- `Job_Title__c` (Text, 255)
- `Company_Name__c` (Text, 255)
- `URL__c` (URL)
- `Salary_Min__c` (Currency, 2 decimals)
- `Salary_Max__c` (Currency, 2 decimals)
- `Remote_Policy__c` (Picklist: Remote, Hybrid, On-site)
- `Description__c` (Long Text Area, 32,768)
- `Fit_Score__c` (Number, 2 decimals)
- `ND_Friendliness_Score__c` (Number, 2 decimals)
- `Green_Flags__c` (Long Text Area, 32,768)
- `Red_Flags__c` (Long Text Area, 32,768)
- `Date_Posted__c` (Date/Time)
- `External_Id__c` (Text, 255, External ID ✓, Unique ✓)

### 4.4 Daily Routine Object

**Object Name**: `Daily Routine`
**API Name**: `Daily_Routine__c`

**Custom Fields**:
- `Routine_Date__c` (Date/Time)
- `Mood_Level__c` (Number, 0 decimals)
- `Energy_Level__c` (Number, 0 decimals)
- `Pain_Level__c` (Number, 0 decimals)
- `Sleep_Quality__c` (Number, 0 decimals)
- `Exercise_Minutes__c` (Number, 0 decimals)
- `Hydration_Ounces__c` (Number, 0 decimals)
- `Meals_Eaten__c` (Number, 0 decimals)
- `Journal_Entry__c` (Long Text Area, 32,768)
- `External_Id__c` (Text, 255, External ID ✓, Unique ✓)

## Step 5: Set Permissions

1. Navigate to: **Setup → Users → Permission Sets**
2. Create new Permission Set: **NeuroThrive Mobile Access**
3. Add Object Permissions:
   - Mood Entry: Read, Create, Edit
   - Win Entry: Read, Create, Edit
   - Job Posting: Read, Create, Edit
   - Daily Routine: Read, Create, Edit
4. Assign this permission set to your user

## Step 6: Test OAuth Flow

1. Build and run the Android app
2. Tap the **Login** button in the top right
3. You'll be redirected to Salesforce login
4. Enter credentials for: abbyluggery179@agentforce.com
5. Click **Allow** to grant permissions
6. App should redirect back and show "Connected to Salesforce"

## Step 7: Test Sync

1. In the app, create a test mood entry (requires UI from Session 3/4)
2. Tap the **Sync** button (refresh icon)
3. Check Salesforce to verify the record was created

**Verify in Salesforce**:
```
Setup → Object Manager → Mood Entry → Records
```

## Troubleshooting

### Error: "invalid_client_id"
- Double-check Consumer Key is correct
- Ensure Connected App is approved
- Wait 2-10 minutes after creating Connected App

### Error: "redirect_uri_mismatch"
- Verify callback URL is exactly: `neurothrive://oauth/callback`
- Check for typos or extra spaces

### Error: "INVALID_FIELD"
- Ensure all custom fields are created with correct API names
- Field names must end with `__c`

### Error: "INSUFFICIENT_ACCESS"
- Verify Permission Set is assigned to user
- Check object-level permissions

## Security Notes

1. **Never commit** `CLIENT_SECRET` to version control
2. Store secrets in:
   - `local.properties` (gitignored)
   - Environment variables
   - Android Keystore
3. Use separate Connected Apps for dev/staging/production
4. Rotate secrets regularly

## API Version

Current API version: **v59.0**

To update: modify `SalesforceApiService.kt`:
```kotlin
@POST("/services/data/v59.0/sobjects/Mood_Entry__c")
```

## References

- [Salesforce OAuth 2.0 Documentation](https://help.salesforce.com/s/articleView?id=sf.remoteaccess_authenticate.htm)
- [Connected App Setup](https://help.salesforce.com/s/articleView?id=sf.connected_app_create.htm)
- [REST API Guide](https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/)
