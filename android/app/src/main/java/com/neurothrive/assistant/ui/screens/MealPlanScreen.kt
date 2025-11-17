package com.neurothrive.assistant.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.neurothrive.assistant.data.local.entities.MealPlan
import com.neurothrive.assistant.data.local.entities.MealPlanItem
import com.neurothrive.assistant.data.local.entities.Recipe
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    mealPlan: MealPlan?,
    mealPlanItems: List<MealPlanItem> = emptyList(),
    recipes: List<Recipe> = emptyList(),
    onNavigateBack: () -> Unit,
    onGeneratePlan: () -> Unit,
    onRecipeClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Plan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (mealPlan == null) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "No Meal Plan Yet",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Generate a personalized 7-day meal plan based on your preferences",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onGeneratePlan,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Meal Plan")
                    }
                }
            }
        } else {
            // Show 7-day meal plan
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Header with date range
                    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    Text(
                        text = "${dateFormat.format(Date(mealPlan.startDate))} - ${dateFormat.format(Date(mealPlan.endDate))}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Generate day cards for 7 days
                items(7) { dayIndex ->
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = mealPlan.startDate
                    calendar.add(Calendar.DAY_OF_YEAR, dayIndex)

                    DayMealCard(
                        dayOfWeek = dayIndex,
                        date = calendar.time,
                        mealPlanItems = mealPlanItems.filter { it.dayOfWeek == dayIndex },
                        recipes = recipes,
                        onRecipeClick = onRecipeClick
                    )
                }
            }
        }
    }
}

@Composable
private fun DayMealCard(
    dayOfWeek: Int,
    date: Date,
    mealPlanItems: List<MealPlanItem>,
    recipes: List<Recipe>,
    onRecipeClick: (String) -> Unit
) {
    val dayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Day header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayNames.getOrElse(dayOfWeek) { "Day ${dayOfWeek + 1}" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = dateFormat.format(date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider()

            // Meal slots
            listOf("breakfast", "lunch", "dinner").forEach { mealType ->
                val mealItem = mealPlanItems.find { it.mealType.lowercase() == mealType }
                val recipe = recipes.find { it.id == mealItem?.recipeId }

                MealSlot(
                    mealType = mealType,
                    recipe = recipe,
                    onRecipeClick = onRecipeClick
                )
            }
        }
    }
}

@Composable
private fun MealSlot(
    mealType: String,
    recipe: Recipe?,
    onRecipeClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = when (mealType) {
                    "breakfast" -> Icons.Filled.WbSunny
                    "lunch" -> Icons.Filled.LunchDining
                    "dinner" -> Icons.Filled.DinnerDining
                    else -> Icons.Filled.Restaurant
                },
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.secondary
            )

            Column {
                Text(
                    text = mealType.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (recipe != null) {
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { onRecipeClick(recipe.id) }
                    )
                } else {
                    Text(
                        text = "Not planned",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (recipe != null) {
            IconButton(onClick = { onRecipeClick(recipe.id) }) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "View recipe",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
