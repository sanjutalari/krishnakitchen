package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CartItem
import com.example.model.Order
import com.example.model.TableState
import com.example.ui.theme.*
import com.example.viewmodel.RestaurantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaiterPanelScreen(
    viewModel: RestaurantViewModel,
    innerPadding: PaddingValues
) {
    val tables by viewModel.tables.collectAsState()
    val orders by viewModel.orders.collectAsState()

    var activeFilter by remember { mutableStateOf("All") }
    var selectedTableForDetail by remember { mutableStateOf<TableState?>(null) }
    var showDetailSheet by remember { mutableStateOf(false) }

    // Filter tables based on choice
    val filteredTables = tables.filter { table ->
        when (activeFilter) {
            "New" -> table.status == "New Order"
            "Pending" -> table.status == "Cooking" || table.status == "Occupied"
            "Served" -> table.status == "Ready"
            else -> true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .padding(top = innerPadding.calculateTopPadding(), bottom = 72.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header panel
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
                                .size(40.dp)
                                .background(FlameRed.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Waiter",
                                tint = FlameRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Waiter Panel",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = OnSurfaceLight
                            )
                            Text(
                                text = "Krishna Chicken HQ",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariantLight,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Tables indicators
                    Row(
                        modifier = Modifier
                            .background(SurfaceContainer, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(AmberGlow, CircleShape)
                        )
                        Text(
                            "14 Active Tables",
                            color = OnSurfaceLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Quick filters
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val filters = listOf("All", "New", "Pending", "Served")
                items(filters) { filter ->
                    val isSelected = activeFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) FlameRed else SurfaceContainer)
                            .clickable { activeFilter = filter }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) Color.White else OnSurfaceVariantLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Tables Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredTables) { table ->
                    TableGridItem(
                        table = table,
                        onClick = {
                            selectedTableForDetail = table
                            showDetailSheet = true
                        }
                    )
                }
            }
        }

        // Table Order details Bottom Sheet
        if (showDetailSheet && selectedTableForDetail != null) {
            val table = selectedTableForDetail!!
            val activeOrderId = table.currentOrderId

            // Lookup active order
            val order = orders.find { it.id == activeOrderId }

            ModalBottomSheet(
                onDismissRequest = {
                    showDetailSheet = false
                    selectedTableForDetail = null
                },
                containerColor = SurfaceElevated,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .background(OnSurfaceVariantLight.copy(alpha = 0.4f), CircleShape)
                    )
                }
            ) {
                WaiterTableDetailsSheetContent(
                    table = table,
                    order = order,
                    onCallKitchen = {
                        viewModel.callKitchenForTable(table.id)
                    },
                    onMarkServed = {
                        if (order != null) {
                            viewModel.completeAndDeliverOrder(order.id)
                        } else {
                            viewModel.showToastMessage("No active order bound to Table ${table.id}!")
                        }
                        showDetailSheet = false
                        selectedTableForDetail = null
                    }
                )
            }
        }
    }
}

@Composable
fun TableGridItem(
    table: TableState,
    onClick: () -> Unit
) {
    val isEmpty = table.status == "Empty"

    // Pulsing animation for New Order alert state
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val borderModifier = when (table.status) {
        "New Order" -> Modifier.border(1.5.dp, FlameRed.copy(alpha = pulseAlpha), RoundedCornerShape(12.dp))
        "Ready" -> Modifier.border(1.dp, StatusReady, RoundedCornerShape(12.dp))
        "Cooking" -> Modifier.border(1.dp, StatusCooking.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
        else -> Modifier.border(1.dp, SurfaceContainerHighest.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .appleLiquidGlass(cornerRadius = 12)
            .then(borderModifier)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Table header (ID and label badge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = table.id,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (table.status == "New Order") FlameRed else OnSurfaceLight
                    )

                    // Status pill
                    val (pillBg, pillTextColor) = when (table.status) {
                        "New Order" -> FlameRed.copy(alpha = 0.15f) to FlameRed
                        "Occupied" -> AmberGlow.copy(alpha = 0.15f) to AmberGlow
                        "Ready" -> StatusReady.copy(alpha = 0.15f) to StatusReady
                        "Cooking" -> StatusCooking.copy(alpha = 0.15f) to StatusCooking
                        else -> SurfaceContainerHighest.copy(alpha = 0.3f) to OnSurfaceVariantLight
                    }

                    Box(
                        modifier = Modifier
                            .background(pillBg, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = table.status.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = pillTextColor
                        )
                    }
                }

                // Table details footer
                if (isEmpty) {
                    Text(
                        text = "Empty",
                        fontSize = 12.sp,
                        color = OnSurfaceVariantLight.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = "Items count",
                                tint = OnSurfaceVariantLight,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${table.itemsCount} Items",
                                fontSize = 11.sp,
                                color = OnSurfaceVariantLight,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (table.elapsedMinutes > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Duration",
                                    tint = OnSurfaceVariantLight,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${table.elapsedMinutes}m ago",
                                    fontSize = 11.sp,
                                    color = OnSurfaceVariantLight
                                )
                            }
                        }

                        if (table.status == "Ready") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Serve Alert",
                                    tint = StatusReady,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Wait to Serve",
                                    fontSize = 11.sp,
                                    color = StatusReady,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WaiterTableDetailsSheetContent(
    table: TableState,
    order: Order?,
    onCallKitchen: () -> Unit,
    onMarkServed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Sheet Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Table ${table.id}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurfaceLight
                )
                Text(
                    text = if (order != null) "Active Order • #${order.id} (${order.customerType})" else "No active cooking orders bound",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariantLight
                )
                if (order != null && order.assignedWaiterName != null) {
                    Text(
                        text = "👨‍🍳 Assigned Waiter: ${order.assignedWaiterName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberGlow
                    )
                }
            }

            Box(
                modifier = Modifier
                    .background(
                        if (table.status == "Ready") StatusReady.copy(alpha = 0.15f) else AmberGlow.copy(alpha = 0.15f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = table.status.uppercase(),
                    color = if (table.status == "Ready") StatusReady else AmberGlow,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Order Items List
        if (order == null || order.items.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Inbox,
                    contentDescription = "Empty",
                    tint = OnSurfaceVariantLight.copy(alpha = 0.3f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "No active items ordered for this table.",
                    color = OnSurfaceVariantLight,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Text(
                "Ordered Items:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariantLight
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .heightIn(max = 260.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(order.items) { cartItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceDark, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = cartItem.item.imageUrl,
                                    contentDescription = cartItem.item.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    cartItem.item.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = OnSurfaceLight,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    if (cartItem.specialNotes.isNotEmpty()) cartItem.specialNotes.joinToString(" • ") else cartItem.item.prepTime,
                                    fontSize = 11.sp,
                                    color = OnSurfaceVariantLight
                                )
                            }
                        }

                        Text(
                            text = "x${cartItem.quantity}",
                            style = MaterialTheme.typography.titleMedium,
                            color = FlameRed,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = SurfaceContainerHighest)
            Spacer(modifier = Modifier.height(16.dp))

            // Pricing totals summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Subtotal",
                    fontSize = 13.sp,
                    color = OnSurfaceVariantLight
                )
                Text(
                    text = "₹${order.totalValue}",
                    fontSize = 13.sp,
                    color = OnSurfaceLight
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Amount",
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceLight
                )
                Text(
                    text = "₹${order.totalValue}",
                    fontWeight = FontWeight.ExtraBold,
                    color = AmberGlow,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Call Kitchen
            OutlinedButton(
                onClick = onCallKitchen,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BoxBorder(1.dp, FlameRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = FlameRed)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call")
                    Text("Call Kitchen", fontWeight = FontWeight.Bold)
                }
            }

            // Mark Served
            Button(
                onClick = onMarkServed,
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp)
                    .testTag("mark_served_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FlameRed)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.DoneAll, contentDescription = "Serve")
                    Text("Mark Served", fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

// Extension custom boxborder
private fun BoxBorder(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)
