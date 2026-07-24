package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.ui.theme.*
import com.example.viewmodel.RestaurantViewModel

@Composable
fun KitchenQueueScreen(
    viewModel: RestaurantViewModel,
    innerPadding: PaddingValues
) {
    val orders by viewModel.orders.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .padding(top = innerPadding.calculateTopPadding(), bottom = 72.dp)
    ) {
        // Kitchen Header Panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceElevated)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(FlameRed.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SoupKitchen,
                            contentDescription = "Kitchen Logo",
                            tint = FlameRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "KITCHEN QUEUE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = FlameRed,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Real-time Order Management",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariantLight,
                            fontSize = 10.sp
                        )
                    }
                }

                // Statistics
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Pending: ${orders.size}",
                            color = AmberGlow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Active: 4 Stations",
                            color = StatusReady,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(
                        onClick = { viewModel.showToastMessage("Refreshing order streams...") },
                        modifier = Modifier
                            .size(36.dp)
                            .background(SurfaceContainer, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = OnSurfaceLight,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Active cooking grid
        if (orders.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AssignmentTurnedIn,
                    contentDescription = "No active orders",
                    tint = OnSurfaceVariantLight.copy(alpha = 0.3f),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No pending orders in the queue!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceLight
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Incoming orders placed by customers will appear here live.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariantLight,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 320.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(orders, key = { it.id }) { order ->
                    KitchenOrderCard(
                        order = order,
                        onAdvanceStatus = { viewModel.advanceOrderStatus(order.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun KitchenOrderCard(
    order: Order,
    onAdvanceStatus: () -> Unit
) {
    // Determine timer delay colors
    val minutes = order.elapsedSeconds / 60
    val seconds = order.elapsedSeconds % 60
    val timerText = String.format("%02d:%02d elapsed", minutes, seconds)

    val timerColor = when {
        minutes >= 10 -> StatusCritical
        minutes >= 5 -> AmberGlow
        else -> OnSurfaceVariantLight
    }

    val glowAlpha = remember { Animatable(0.4f) }
    LaunchedEffect(order.status) {
        if (order.status == OrderStatus.COOKING) {
            glowAlpha.animateTo(
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            glowAlpha.snapTo(0.4f)
        }
    }

    val cardBorderColor = when (order.status) {
        OrderStatus.ACCEPTED -> SurfaceContainerHighest
        OrderStatus.COOKING -> AmberGlow.copy(alpha = glowAlpha.value)
        OrderStatus.READY -> StatusReady
        OrderStatus.SERVED -> OnSurfaceVariantLight
        else -> SurfaceContainerHighest
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .appleLiquidGlass(cornerRadius = 14)
            .border(
                width = if (order.status == OrderStatus.COOKING) 2.dp else 1.dp,
                color = if (order.status == OrderStatus.COOKING) cardBorderColor else cardBorderColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Card Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainer)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Order #${order.id}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FlameRed
                        )
                        val isTakeaway = order.tableNumber.equals("Takeaway", ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isTakeaway) SurfaceContainerHighest else FlameRed.copy(alpha = 0.1f),
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isTakeaway) OnSurfaceVariantLight.copy(alpha = 0.3f) else FlameRed.copy(alpha = 0.5f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = order.tableNumber.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTakeaway) OnSurfaceVariantLight else FlameRed
                            )
                        }
                    }

                    // Timer Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Timer",
                            tint = timerColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = timerText,
                            fontSize = 11.sp,
                            color = timerColor,
                            fontWeight = if (minutes >= 5) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }

                // Print Receipt CTA
                IconButton(
                    onClick = { /* Simulated printing */ },
                    modifier = Modifier
                        .size(32.dp)
                        .background(SurfaceDark, RoundedCornerShape(6.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = "Print Receipt",
                        tint = OnSurfaceLight,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Items Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                order.items.forEachIndexed { index, cartItem ->
                    if (index > 0) {
                        Divider(color = SurfaceContainer.copy(alpha = 0.5f), thickness = 1.dp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "${cartItem.quantity}x",
                                style = MaterialTheme.typography.titleMedium,
                                color = AmberGlow,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Column {
                                Text(
                                    text = cartItem.item.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = OnSurfaceLight
                                )

                                // Special modifier sticker notes
                                if (cartItem.specialNotes.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    cartItem.specialNotes.forEach { note ->
                                        val isNoOnion = note.equals("No Onion", ignoreCase = true)
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isNoOnion) AmberGlow.copy(alpha = 0.1f) else StatusCritical.copy(alpha = 0.1f),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isNoOnion) AmberGlow.copy(alpha = 0.3f) else StatusCritical.copy(alpha = 0.3f),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isNoOnion) Icons.Default.Warning else Icons.Default.LocalFireDepartment,
                                                    contentDescription = note,
                                                    tint = if (isNoOnion) AmberGlow else StatusCritical,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Text(
                                                    text = note.uppercase(),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isNoOnion) AmberGlow else StatusCritical
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom perspective state switches
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark.copy(alpha = 0.5f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button 1: Accepted state
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (order.status == OrderStatus.ACCEPTED) StatusPending.copy(alpha = 0.2f) else SurfaceContainer)
                        .border(
                            1.dp,
                            if (order.status == OrderStatus.ACCEPTED) StatusPending else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onAdvanceStatus() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Accepted",
                            tint = if (order.status == OrderStatus.ACCEPTED) StatusPending else OnSurfaceVariantLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "ACCEPTED",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (order.status == OrderStatus.ACCEPTED) Color.White else OnSurfaceVariantLight
                        )
                    }
                }

                // Button 2: Cooking state
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (order.status == OrderStatus.COOKING) StatusCooking.copy(alpha = 0.2f) else SurfaceContainer)
                        .border(
                            1.dp,
                            if (order.status == OrderStatus.COOKING) StatusCooking else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onAdvanceStatus() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.OutdoorGrill,
                            contentDescription = "Cooking",
                            tint = if (order.status == OrderStatus.COOKING) StatusCooking else OnSurfaceVariantLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "COOKING",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (order.status == OrderStatus.COOKING) Color.White else OnSurfaceVariantLight
                        )
                    }
                }

                // Button 3: Ready state
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (order.status == OrderStatus.READY) StatusReady.copy(alpha = 0.2f) else SurfaceContainer)
                        .border(
                            1.dp,
                            if (order.status == OrderStatus.READY) StatusReady else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onAdvanceStatus() }
                        .padding(vertical = 10.dp)
                        .testTag("ready_order_${order.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Ready",
                            tint = if (order.status == OrderStatus.READY) StatusReady else OnSurfaceVariantLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "READY",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (order.status == OrderStatus.READY) Color.White else OnSurfaceVariantLight
                        )
                    }
                }
            }
        }
    }
}
