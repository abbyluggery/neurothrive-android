package com.neurothrive.assistant.ui.screens

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
import com.neurothrive.assistant.data.local.entities.Coupon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouponScreen(
    coupons: List<Coupon> = emptyList(),
    onNavigateBack: () -> Unit,
    onMatchCoupons: () -> Unit
) {
    val totalSavings = coupons
        .filter { it.isActive && it.expirationDate > System.currentTimeMillis() }
        .sumOf { it.discountAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Coupons") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (coupons.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalOffer,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "No Coupons Available",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Check back later for money-saving coupons!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // Total savings card
                        if (totalSavings > 0) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Total Potential Savings",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "${coupons.filter { it.isActive && it.expirationDate > System.currentTimeMillis() }.size} active coupons",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "$%.2f".format(totalSavings),
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    item {
                        // Match to Grocery List button
                        Button(
                            onClick = onMatchCoupons,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Filled.CompareArrows, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Match to Grocery List")
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Separate active and expired coupons
                    val activeCoupons = coupons.filter {
                        it.isActive && it.expirationDate > System.currentTimeMillis()
                    }
                    val expiredCoupons = coupons.filter {
                        !it.isActive || it.expirationDate <= System.currentTimeMillis()
                    }

                    if (activeCoupons.isNotEmpty()) {
                        item {
                            Text(
                                text = "Active Coupons",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(activeCoupons) { coupon ->
                            CouponCard(coupon = coupon, isExpired = false)
                        }
                    }

                    if (expiredCoupons.isNotEmpty()) {
                        item {
                            Text(
                                text = "Expired Coupons",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp, top = 16.dp)
                            )
                        }

                        items(expiredCoupons) { coupon ->
                            CouponCard(coupon = coupon, isExpired = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CouponCard(
    coupon: Coupon,
    isExpired: Boolean
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val isExpiringSoon = coupon.expirationDate - System.currentTimeMillis() < 7 * 24 * 60 * 60 * 1000L

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isExpired -> MaterialTheme.colorScheme.surfaceVariant
                isExpiringSoon -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = coupon.itemName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isExpired)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isExpiringSoon && !isExpired)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isExpired) {
                            "Expired ${dateFormat.format(Date(coupon.expirationDate))}"
                        } else {
                            "Expires ${dateFormat.format(Date(coupon.expirationDate))}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isExpiringSoon && !isExpired)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Discount badge
            Surface(
                color = when {
                    isExpired -> MaterialTheme.colorScheme.surfaceVariant
                    else -> MaterialTheme.colorScheme.primaryContainer
                },
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (coupon.discountType == "percentage") {
                            "${coupon.discountAmount.toInt()}%"
                        } else {
                            "$%.2f".format(coupon.discountAmount)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isExpired)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "OFF",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isExpired)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
