# NeuroThrive Android - Feature Specifications

## Overview

NeuroThrive Android provides mobile access to the full NeuroThrive wellness platform, designed specifically for neurodivergent users who need low-friction, high-clarity interfaces.

---

## Feature 1: Daily Routine Tracking

### Purpose
Track morning and evening routine completion with energy-aware scheduling.

### User Stories
- As a user, I want to check off my morning routine steps so I start the day structured
- As a user, I want my evening routine to appear at wind-down time
- As a user, I want to see my routine streak to stay motivated

### Screens

#### Morning Routine Screen
```
┌─────────────────────────────────┐
│  Good Morning, Abby             │
│  Energy: [━━━━━━━━░░] 7/10      │
├─────────────────────────────────┤
│  Morning Routine                │
│  ┌───────────────────────────┐  │
│  │ ☑ Take medication         │  │
│  │ ☑ Drink water             │  │
│  │ ☐ 5-minute stretch        │  │
│  │ ☐ Review today's plan     │  │
│  └───────────────────────────┘  │
│                                 │
│  Progress: 2/4 ▓▓▓▓░░░░        │
│                                 │
│  [ Complete Morning Routine ]   │
└─────────────────────────────────┘
```

### Data Model
```kotlin
data class DailyRoutine(
    val id: String?,
    val date: LocalDate,
    val energyLevel: Int?,           // 1-10
    val mood: String?,               // Great, Good, Okay, Low, Struggling
    val morningComplete: Boolean,
    val eveningComplete: Boolean,
    val notes: String?
)
```

### Sync Behavior
- Changes sync immediately when online
- Offline changes queue for sync
- Pull-to-refresh fetches latest from Salesforce

---

## Feature 2: Energy Level Monitoring

### Purpose
Track energy patterns to enable energy-adaptive scheduling.

### User Stories
- As a user, I want to log my energy at different times of day
- As a user, I want to see my energy patterns over the week
- As a user, I want high-energy tasks suggested during my peak times

### Screens

#### Energy Logger
```
┌─────────────────────────────────┐
│  How's your energy right now?   │
│                                 │
│     1  2  3  4  5  6  7  8  9 10│
│     ●  ●  ●  ●  ●  ●  ●  ○  ○  ○│
│                    ▲             │
│                Energy: 7         │
│                                 │
│  ┌───────────────────────────┐  │
│  │ Quick Notes (optional)    │  │
│  │ _________________________ │  │
│  └───────────────────────────┘  │
│                                 │
│  [ Save Energy Level ]          │
└─────────────────────────────────┘
```

#### Weekly Pattern View
```
┌─────────────────────────────────┐
│  This Week's Energy             │
│                                 │
│  10 │                           │
│   8 │    ▄▄  ▄▄      ▄▄         │
│   6 │ ▄▄ ██  ██  ▄▄  ██  ▄▄     │
│   4 │ ██ ██  ██  ██  ██  ██     │
│   2 │ ██ ██  ██  ██  ██  ██     │
│     └─M──T──W──Th─F──Sa─Su──    │
│                                 │
│  Average: 6.2 | Peak: Tuesday   │
└─────────────────────────────────┘
```

---

## Feature 3: Meal Planning

### Purpose
View today's planned meals and access recipes with ingredients.

### User Stories
- As a user, I want to see what meals are planned for today
- As a user, I want to view the recipe and ingredients for each meal
- As a user, I want to access my shopping list when at the store

### Screens

#### Today's Meals
```
┌─────────────────────────────────┐
│  Today's Meals                  │
│  December 27, 2025              │
├─────────────────────────────────┤
│  🌅 BREAKFAST                   │
│  ┌───────────────────────────┐  │
│  │ Overnight Oats            │  │
│  │ Prep: 5 min | 320 cal     │  │
│  └───────────────────────────┘  │
│                                 │
│  ☀️ LUNCH                       │
│  ┌───────────────────────────┐  │
│  │ Chicken Caesar Wrap       │  │
│  │ Prep: 15 min | 480 cal    │  │
│  └───────────────────────────┘  │
│                                 │
│  🌙 DINNER                      │
│  ┌───────────────────────────┐  │
│  │ Sheet Pan Salmon          │  │
│  │ Prep: 25 min | 520 cal    │  │
│  └───────────────────────────┘  │
└─────────────────────────────────┘
```

#### Recipe Detail
```
┌─────────────────────────────────┐
│ ← Sheet Pan Salmon              │
├─────────────────────────────────┤
│  [        Photo         ]       │
│                                 │
│  Prep: 10 min | Cook: 15 min    │
│  Servings: 2 | Calories: 520    │
│                                 │
│  INGREDIENTS                    │
│  ☐ 2 salmon fillets (6 oz)      │
│  ☐ 2 cups broccoli florets      │
│  ☐ 1 tbsp olive oil             │
│  ☐ 1 lemon, sliced              │
│  ☐ Salt & pepper                │
│                                 │
│  INSTRUCTIONS                   │
│  1. Preheat oven to 400°F       │
│  2. Arrange salmon and broccoli │
│  3. Drizzle with olive oil      │
│  4. Bake 15-18 minutes          │
│                                 │
│  [ Add to Shopping List ]       │
└─────────────────────────────────┘
```

---

## Feature 4: Job Search Companion

### Purpose
Review AI-analyzed job postings with fit scores and red flags.

### User Stories
- As a user, I want to browse jobs that have been analyzed for me
- As a user, I want to see my fit score and any red flags
- As a user, I want to quickly save or dismiss jobs

### Screens

#### Job Feed
```
┌─────────────────────────────────┐
│  Job Matches                    │
│  Showing: New Jobs              │
├─────────────────────────────────┤
│  ┌───────────────────────────┐  │
│  │ Senior Developer          │  │
│  │ Acme Corp | Remote        │  │
│  │ $120k-$150k               │  │
│  │                           │  │
│  │ Fit: ████████░░ 82%       │  │
│  │ ⚠️ 1 red flag              │  │
│  │                           │  │
│  │ [ Save ]     [ Dismiss ]  │  │
│  └───────────────────────────┘  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ Full-Stack Engineer       │  │
│  │ TechStart Inc | Hybrid    │  │
│  │ $100k-$130k               │  │
│  │                           │  │
│  │ Fit: ██████░░░░ 65%       │  │
│  │ ⚠️ 2 red flags             │  │
│  └───────────────────────────┘  │
└─────────────────────────────────┘
```

#### Job Detail
```
┌─────────────────────────────────┐
│ ← Senior Developer              │
│   Acme Corp                     │
├─────────────────────────────────┤
│  FIT SCORE                      │
│  ████████░░ 82/100              │
│                                 │
│  MATCH ANALYSIS                 │
│  ✅ Skills match: 9/10          │
│  ✅ Experience: Meets           │
│  ✅ Remote: Full remote         │
│  ⚠️ "Fast-paced" mentioned      │
│                                 │
│  REQUIREMENTS                   │
│  • 5+ years experience          │
│  • React, Node.js, Python       │
│  • AWS/GCP experience           │
│                                 │
│  SALARY                         │
│  $120,000 - $150,000            │
│                                 │
│  [ Apply ] [ Save ] [ Dismiss ] │
└─────────────────────────────────┘
```

### Data Sync
- Jobs pulled from Salesforce `Job_Posting__c`
- Cached locally with 1-hour refresh interval
- Filter by status: New, Saved, Applied, Dismissed

---

## Feature 5: Wellness Check-ins

### Purpose
Log mood, gratitude, and daily wins for mental wellness tracking.

### User Stories
- As a user, I want to quickly log my mood
- As a user, I want to record things I'm grateful for
- As a user, I want to capture daily wins to fight imposter syndrome

### Screens

#### Quick Check-in
```
┌─────────────────────────────────┐
│  Evening Check-in               │
│  December 27, 2025              │
├─────────────────────────────────┤
│  How are you feeling?           │
│                                 │
│  😊  😐  😔  😤  😰             │
│  Great Okay Low Frustrated Anxious│
│                                 │
│  Today's Win                    │
│  ┌───────────────────────────┐  │
│  │ I completed the API       │  │
│  │ integration ahead of      │  │
│  │ schedule                   │  │
│  └───────────────────────────┘  │
│                                 │
│  Grateful For                   │
│  ┌───────────────────────────┐  │
│  │ Quiet afternoon to focus  │  │
│  └───────────────────────────┘  │
│                                 │
│  [ Save Check-in ]              │
└─────────────────────────────────┘
```

---

## Neurodivergent-Friendly Design Principles

### 1. Reduce Cognitive Load
- One primary action per screen
- Clear, large tap targets (48dp minimum)
- Minimal required fields
- Sensible defaults pre-filled

### 2. Visual Clarity
- High contrast text (4.5:1 minimum)
- Clear section separation
- Progress indicators always visible
- Consistent iconography

### 3. Gentle Notifications
- Quiet hours configuration
- Non-intrusive reminders
- No red notification badges (anxiety-inducing)
- Celebration of streaks without shaming breaks

### 4. Forgiveness
- Undo for all destructive actions
- Confirm before permanent deletion
- Auto-save drafts
- Easy recovery from errors

### 5. Flexibility
- Dark mode support
- Adjustable text sizes
- Optional sounds/haptics
- Customizable routine items

---

## Offline Capabilities

| Feature | Offline Read | Offline Write |
|---------|--------------|---------------|
| Daily Routines | Yes (cached) | Yes (queued) |
| Energy Logging | Yes (cached) | Yes (queued) |
| Meal Plans | Yes (cached) | View only |
| Job Postings | Yes (cached) | Save/Dismiss queued |
| Wellness Check-ins | Yes (cached) | Yes (queued) |

---

## Notifications

| Notification | Default Time | Configurable |
|--------------|--------------|--------------|
| Morning Routine | 7:00 AM | Yes |
| Energy Check-in | 2:00 PM | Yes |
| Evening Routine | 8:00 PM | Yes |
| Meal Prep Reminder | 30 min before | Yes |
| New Job Matches | Batched daily | Yes |

All notifications respect Quiet Hours (default: 10 PM - 7 AM).
