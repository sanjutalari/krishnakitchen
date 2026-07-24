package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.RestaurantViewModel

@Composable
fun AdminAnalyticsScreen(
    viewModel: RestaurantViewModel,
    innerPadding: PaddingValues
) {
    val logs by viewModel.logs.collectAsState()
    val todayIncome by viewModel.todayIncome.collectAsState()
    val orders by viewModel.orders.collectAsState()

    val curryDiscount by viewModel.curryDiscountActive
    val dinnerCombo by viewModel.dinnerForTwoActive

    val customerFeedback by viewModel.customerFeedback.collectAsState()
    val offersList by viewModel.offers.collectAsState()
    val staffMembers by viewModel.staffMembers.collectAsState()

    var noteInput by remember { mutableStateOf(viewModel.quickCustomerNote.value) }
    var newOfferCode by remember { mutableStateOf("") }
    var newOfferTitle by remember { mutableStateOf("") }
    var newOfferValue by remember { mutableStateOf("") }
    var newOfferType by remember { mutableStateOf("Percentage") }
    var newStaffName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(StaffRole.WAITER) }

    val percentReached = (todayIncome / viewModel.targetIncome) * 100
    val progressFraction = (todayIncome / viewModel.targetIncome).coerceIn(0.0, 1.0).toFloat()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark),
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding() + 16.dp,
            bottom = 100.dp,
            start = 16.dp,
            end = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // App top navigation bar anchor
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = OnSurfaceLight)
                    }
                    Text(
                        "Analytics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = FlameRed
                    )
                }

                // Small outlet avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, FlameRed, CircleShape)
                ) {
                    AsyncImage(
                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBVhj7HFP0-FRlksUBzy4vsQhON6UVIhjHOV8ts8MP1rl2MVv-pOOTEWjB6XkQUgg7jd1GK-58ZrsnpzFjoM3ZdtI1VY3dynCSqb7B7cSLjPV90vHMIf24CVe30vCArlb8R-fxuXlvQH45ihyCOlMcKcLpVQ0uu4tU1X-czXiaMRUhYDHyHA-GwoMjPbgvqt-QaAuE05ZrcH6phEwlH4o_A4Riv58zgbOOzshHfMY5aET01kl5TL26HGA",
                        contentDescription = "User Headshot",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // --- PENDING CUSTOMER ORDERS APPROVAL SECTION (Internal & External) ---
        item {
            val pendingOrders = orders.filter { it.status == OrderStatus.PENDING }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (pendingOrders.isNotEmpty()) 2.dp else 1.dp,
                        color = if (pendingOrders.isNotEmpty()) FlameRed else AmberGlow.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(if (pendingOrders.isNotEmpty()) FlameRed else StatusReady.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HowToReg,
                                    contentDescription = "Pending Approval",
                                    tint = if (pendingOrders.isNotEmpty()) Color.White else StatusReady,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    "Pending Customer Orders (${pendingOrders.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceLight
                                )
                                Text(
                                    "Accept orders from Internal or External customers & auto-assign waiter",
                                    fontSize = 11.sp,
                                    color = OnSurfaceVariantLight
                                )
                            }
                        }

                        if (pendingOrders.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = FlameRed
                            ) {
                                Text(
                                    "REQUIRES ACTION",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (pendingOrders.isEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceDark, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StatusReady, modifier = Modifier.size(16.dp))
                            Text(
                                "All customer orders are accepted and sent to kitchen!",
                                fontSize = 12.sp,
                                color = OnSurfaceVariantLight
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            pendingOrders.forEach { order ->
                                val isInternal = order.customerType.contains("Internal") || order.tableNumber.startsWith("Table")
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceDark, RoundedCornerShape(12.dp))
                                        .border(1.dp, if (isInternal) AmberGlow.copy(alpha = 0.3f) else FlameRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                "Order #${order.id}",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                color = OnSurfaceLight
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isInternal) AmberGlow.copy(alpha = 0.2f) else FlameRed.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = if (isInternal) "🏢 INTERNAL (${order.tableNumber})" else "🛵 EXTERNAL (${order.tableNumber})",
                                                    color = if (isInternal) AmberGlow else FlameRed,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            "₹${order.totalValue}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = StatusReady
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Items: ${order.items.joinToString { "${it.quantity}x ${it.item.name}" }}",
                                        fontSize = 12.sp,
                                        color = OnSurfaceVariantLight
                                    )

                                    if (order.specialNotes.isNotEmpty()) {
                                        Text(
                                            "Notes: ${order.specialNotes.joinToString()}",
                                            fontSize = 11.sp,
                                            color = AmberGlow,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = { viewModel.acceptCustomerOrder(order.id) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(38.dp)
                                            .testTag("accept_order_${order.id}"),
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusReady),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Accept Order -> Send to Kitchen & Assign Waiter",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Order Cancellation Permissions Control (5-Minute Window)
        item {
            val pendingCancellations = orders.filter { it.cancellationRequested }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (pendingCancellations.isNotEmpty()) 2.dp else 1.dp,
                        color = if (pendingCancellations.isNotEmpty()) FlameRed else AmberGlow.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(if (pendingCancellations.isNotEmpty()) FlameRed else StatusReady.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = "Cancellation Permissions",
                                    tint = if (pendingCancellations.isNotEmpty()) Color.White else StatusReady,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    "Order Cancellation Permissions",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceLight
                                )
                                Text(
                                    "5-min cancellation constraint control & Admin approval requests",
                                    fontSize = 11.sp,
                                    color = OnSurfaceVariantLight
                                )
                            }
                        }

                        if (pendingCancellations.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = FlameRed
                            ) {
                                Text(
                                    "${pendingCancellations.size} PENDING",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (pendingCancellations.isEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceDark, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StatusReady, modifier = Modifier.size(16.dp))
                            Text(
                                "No pending cancellation requests. All orders are active or processed.",
                                fontSize = 12.sp,
                                color = OnSurfaceVariantLight
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            pendingCancellations.forEach { order ->
                                val elapsedSec = order.elapsedSeconds
                                val secsLeft = (300 - elapsedSec).coerceAtLeast(0)
                                val mins = secsLeft / 60
                                val secs = secsLeft % 60

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text("Order #${order.id}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = AmberGlow)
                                                Surface(shape = RoundedCornerShape(4.dp), color = FlameRed.copy(alpha = 0.2f)) {
                                                    Text(order.tableNumber, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FlameRed, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                }
                                            }
                                            Text("Time left: ${mins}m ${secs}s", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberGlow)
                                        }

                                        Text(
                                            "Requested Reason: \"${order.cancellationReason ?: "User requested cancel"}\"",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OnSurfaceLight
                                        )

                                        Text(
                                            "Items (${order.items.size}): ${order.items.joinToString { "${it.quantity}x ${it.item.name}" }} • Total: ₹${order.totalValue}",
                                            fontSize = 11.sp,
                                            color = OnSurfaceVariantLight
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { viewModel.approveOrderCancellation(order.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = StatusReady),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(36.dp)
                                                    .testTag("admin_approve_cancel_${order.id}")
                                            ) {
                                                Text("APPROVE CANCEL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = { viewModel.rejectOrderCancellation(order.id, "Kitchen preparation already started") },
                                                border = BorderStroke(1.dp, FlameRed),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(36.dp)
                                                    .testTag("admin_reject_cancel_${order.id}")
                                            ) {
                                                Text("REJECT REQUEST", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FlameRed)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live revenue tracking progress bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .appleLiquidGlass(cornerRadius = 16),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(StatusReady, CircleShape)
                            )
                            Text(
                                "LIVE REVENUE TRACKING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceVariantLight,
                                letterSpacing = 1.sp
                            )
                        }

                        // Vs last Tuesday
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(imageVector = Icons.Default.TrendingUp, contentDescription = "Up", tint = StatusReady, modifier = Modifier.size(14.dp))
                            Text("12%", color = StatusReady, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("vs last Tue", color = OnSurfaceVariantLight, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = String.format("₹%,.2f", todayIncome),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Text(
                        text = "Today's Income",
                        fontSize = 13.sp,
                        color = OnSurfaceVariantLight
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressFraction)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(FlameRed, AmberGlow)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format("Target: ₹%,.0f", viewModel.targetIncome),
                            fontSize = 11.sp,
                            color = AmberGlow,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format("%.1f%% Reached", percentReached),
                            fontSize = 11.sp,
                            color = OnSurfaceVariantLight
                        )
                    }
                }
            }
        }

        // Top Selling Products Bento Box
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .appleLiquidGlass(cornerRadius = 16),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Top Selling Products",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceLight
                        )
                        Text(
                            text = "Performance metrics across items",
                            fontSize = 11.sp,
                            color = OnSurfaceVariantLight
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val topProducts = listOf(
                        TopProduct(
                            name = "Butter Chicken Hero",
                            units = 142,
                            earnings = 42600,
                            status = "High Demand",
                            statusColor = StatusReady,
                            img = "https://lh3.googleusercontent.com/aida-public/AB6AXuC_DRB0pwI6q8Vcx1ROj3kzWIxjjo9mRNrrnFnFlKNqOJAFWjl8z37WD9A7i5USfKJaXKBASBHA8exWZ52pTRSioo-2fQhZGWG_UkJJuunE8RnN_gkvDINCU5N3jstX4rMh6ClS9m9cdfaouL8xdz2Pt5Vx0R8oyDDWXQhVNQ80Lm1lq_DVyUN79QL2FOWegwmUPncu1KPzdBHUvwihR6q2b3PRrudKG-w35oBOk6GJmVMr6J5kDWvAtA"
                        ),
                        TopProduct(
                            name = "Flame Grilled Platter",
                            units = 98,
                            earnings = 24500,
                            status = "Trending",
                            statusColor = StatusCooking,
                            img = "https://lh3.googleusercontent.com/aida-public/AB6AXuD1tR2YD5CLpPy-NspInlkEC8y-pS7Ppd6JVWEXiWdf_rocsHz5cTp8st2JrQyZ1bRAb8ynNcSymlHCM854ugAOwy3LpwDE-ZmmrN51CuPJBCRH4XozgSqOZh_oJsRgF1iObewXB8fphcm0Ntym1vBTb9fdPyyBZszaRcFABBWfgvA9yv3eBnNTnLyNHJ6gbOYMGFwoRnfdrH6wEaUSgf_wBZSPdX1sis-4oC6ZcQXJ_0LwCwoc2EB0GQ"
                        ),
                        TopProduct(
                            name = "Hyderabadi Dum Biryani",
                            units = 76,
                            earnings = 19000,
                            status = "Stable",
                            statusColor = StatusDelivered,
                            img = "https://lh3.googleusercontent.com/aida-public/AB6AXuCELrrC3E1EqNattJ4Px-gFnl5jB8lxxs6E29thm3Ek5q3hK-caaPV-eaf9dXGZ6-baYvbGeuvJp9C39eypOjxydlcN9jhe_AUxVrY2aLCtiLcnBpyEi6AclvbVJggLO4CWZp6j2l6_4YxjDiRSt-oghUiEtXIHui6EDisEeMnJ69wfxXesib-Y5safVNjQO33L4ySJs78XJTbUuW5-tT5BhACu-Vj7Z6zrARhWUPqqm88UhK6FwfuhKg"
                        )
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        topProducts.forEach { product ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
                                            model = product.img,
                                            contentDescription = product.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            product.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = OnSurfaceLight,
                                            maxLines = 1
                                        )
                                        Text(
                                            "${product.units} units sold",
                                            fontSize = 11.sp,
                                            color = OnSurfaceVariantLight
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "₹${product.earnings}",
                                        fontWeight = FontWeight.Bold,
                                        color = AmberGlow,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(product.statusColor.copy(alpha = 0.15f), CircleShape)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            product.status,
                                            color = product.statusColor,
                                            fontSize = 8.sp,
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

        // Admin Advisor Section with triggerable actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(AmberGlow.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.SmartToy, contentDescription = "Advisor", tint = AmberGlow, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        "Admin Advisor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceLight
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Action 1: Curry Discount volume boost opportunity
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (curryDiscount) FlameRed else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = CharcoalBlack.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "Volume Boost Opportunity",
                                color = FlameRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Business is 15% slower than usual for a Tuesday evening.",
                                color = OnSurfaceLight,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { viewModel.toggleCurryDiscount() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .testTag("discount_trigger_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (curryDiscount) StatusReady else FlameRed
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (curryDiscount) "Disable curry discount (Active)" else "Push 15% Curry Discount",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Estimated volume increase: +22%",
                                color = OnSurfaceVariantLight,
                                fontSize = 9.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Action 2: Dinner for two combination bundling
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (dinnerCombo) StatusReady else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = CharcoalBlack.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "Average Order Value (AOV) Optimization",
                                color = StatusDelivered,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Current AOV is ₹450. Enable a custom dinner bundle to drive check size.",
                                color = OnSurfaceLight,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { viewModel.toggleDinnerForTwo() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .testTag("combo_trigger_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (dinnerCombo) StatusReady else SurfaceContainerHighest
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (dinnerCombo) "Disable Dinner Bundle (Active)" else "Activate \"Dinner for Two\"",
                                    color = if (dinnerCombo) Color.White else OnSurfaceLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // AI Driven Insights list
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(FlameRed.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = "AI", tint = FlameRed, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        "AI Driven Insights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceLight
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Panel 1: Dish Improvements
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(180.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Dish Improvements", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OnSurfaceLight)
                                Icon(imageVector = Icons.Default.EditNote, contentDescription = "Edit", tint = FlameRed, modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(modifier = Modifier.size(5.dp).background(FlameRed, CircleShape).padding(top = 4.dp))
                                    Text(
                                        "Biryani: 78% of reviews mention \"too spicy\". Reduce pepper content by 15%.",
                                        fontSize = 10.sp,
                                        color = OnSurfaceVariantLight,
                                        lineHeight = 13.sp
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(modifier = Modifier.size(5.dp).background(AmberGlow, CircleShape).padding(top = 4.dp))
                                    Text(
                                        "Combos: Receives \"low value\" tags. Increase rice portion by 10%.",
                                        fontSize = 10.sp,
                                        color = OnSurfaceVariantLight,
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // Panel 2: Inventory Suggestions
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(180.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Predictive Stock", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OnSurfaceLight)
                                Icon(imageVector = Icons.Default.Inventory, contentDescription = "Inventory", tint = AmberGlow, modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceDark, RoundedCornerShape(6.dp))
                                        .padding(6.dp)
                                ) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Boneless Chicken", fontSize = 8.sp, color = OnSurfaceLight, fontWeight = FontWeight.Bold)
                                        Text("+45kg Recommendation", fontSize = 8.sp, color = StatusReady, fontWeight = FontWeight.ExtraBold)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceDark, RoundedCornerShape(6.dp))
                                        .padding(6.dp)
                                ) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Basmati Rice", fontSize = 8.sp, color = OnSurfaceLight, fontWeight = FontWeight.Bold)
                                        Text("+100kg Recommendation", fontSize = 8.sp, color = StatusReady, fontWeight = FontWeight.ExtraBold)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceDark, RoundedCornerShape(6.dp))
                                        .padding(6.dp)
                                ) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Dairy Cream", fontSize = 8.sp, color = OnSurfaceLight, fontWeight = FontWeight.Bold)
                                        Text("Low Stock Warning", fontSize = 8.sp, color = StatusCooking, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Customer Feedback & Reviews
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FlameRed.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Live Customer Feedbacks",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceLight
                            )
                            val averageRating = if (customerFeedback.isNotEmpty()) customerFeedback.map { it.rating }.average() else 4.8
                            Text(
                                String.format("Average Dish Quality: %.1f ★ (%d reviews)", averageRating, customerFeedback.size),
                                fontSize = 11.sp,
                                color = AmberGlow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(imageVector = Icons.Default.RateReview, contentDescription = "Reviews", tint = FlameRed)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (customerFeedback.isEmpty()) {
                        Text("No reviews submitted today.", fontSize = 12.sp, color = OnSurfaceVariantLight)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            customerFeedback.take(3).forEach { fb ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceDark, RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(fb.customerName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OnSurfaceLight)
                                            Box(
                                                modifier = Modifier
                                                    .background(AmberGlow.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("${fb.rating} ★", color = AmberGlow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Text(
                                            "Dish: ${fb.dishName}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FlameRed,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                        Text(fb.review, fontSize = 11.sp, color = OnSurfaceLight)
                                    }
                                    Text(fb.timestamp, fontSize = 8.sp, color = OnSurfaceVariantLight)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Feedback recommendations box based on real ratings
                    val lowRatings = customerFeedback.filter { it.rating < 4.0 }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CharcoalBlack.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Culinary Quality Recommendations:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = OnSurfaceLight)
                            Spacer(modifier = Modifier.height(6.dp))
                            if (lowRatings.isEmpty()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(modifier = Modifier.size(5.dp).background(StatusReady, CircleShape).padding(top = 4.dp))
                                    Text("Portion sizes & spice metrics are fully optimal today. No negative reviews reported.", fontSize = 10.sp, color = OnSurfaceVariantLight)
                                }
                            } else {
                                lowRatings.take(2).forEach { r ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 2.dp)) {
                                        Box(modifier = Modifier.size(5.dp).background(FlameRed, CircleShape).padding(top = 4.dp))
                                        Text("Fix ${r.dishName}: User '${r.customerName}' rated ${r.rating}★ stating: \"${r.review}\"", fontSize = 10.sp, color = OnSurfaceVariantLight)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Offer Coupon & Bundle Management
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FlameRed.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Offers & Coupons Control",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceLight
                            )
                            Text(
                                "Push active discounts and combos to customer checkout screens",
                                fontSize = 11.sp,
                                color = OnSurfaceVariantLight
                            )
                        }
                        Icon(imageVector = Icons.Default.LocalActivity, contentDescription = "Coupons", tint = FlameRed)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Active offers list
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        offersList.forEach { offer ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceDark, RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(offer.code, fontWeight = FontWeight.ExtraBold, color = FlameRed, fontSize = 13.sp)
                                        Box(
                                            modifier = Modifier
                                                .background(if (offer.isActive) StatusReady.copy(alpha = 0.15f) else OnSurfaceVariantLight.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                if (offer.isActive) "ACTIVE" else "DISABLED",
                                                color = if (offer.isActive) StatusReady else OnSurfaceVariantLight,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 8.sp
                                            )
                                        }
                                    }
                                    Text(offer.title, fontSize = 12.sp, color = OnSurfaceLight)
                                    Text("Value: ${offer.value} • Type: ${offer.type}", fontSize = 10.sp, color = OnSurfaceVariantLight)
                                }
                                Switch(
                                    checked = offer.isActive,
                                    onCheckedChange = { viewModel.toggleOfferActive(offer.id) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = StatusReady,
                                        uncheckedThumbColor = OnSurfaceVariantLight,
                                        uncheckedTrackColor = SurfaceElevated
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Form to create custom coupon code
                    Text("Add Custom Coupon Offer:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OnSurfaceLight)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newOfferCode,
                        onValueChange = { newOfferCode = it },
                        placeholder = { Text("Coupon Code (e.g. KRISHNA10)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurfaceLight,
                            unfocusedTextColor = OnSurfaceLight,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark,
                            focusedBorderColor = FlameRed,
                            unfocusedBorderColor = SurfaceContainerHighest
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = newOfferTitle,
                        onValueChange = { newOfferTitle = it },
                        placeholder = { Text("Title Description (e.g. 10% discount on curries)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurfaceLight,
                            unfocusedTextColor = OnSurfaceLight,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark,
                            focusedBorderColor = FlameRed,
                            unfocusedBorderColor = SurfaceContainerHighest
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newOfferValue,
                            onValueChange = { newOfferValue = it },
                            placeholder = { Text("Value (e.g. 10)", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = OnSurfaceLight,
                                unfocusedTextColor = OnSurfaceLight,
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark,
                                focusedBorderColor = FlameRed,
                                unfocusedBorderColor = SurfaceContainerHighest
                            )
                        )

                        Button(
                            onClick = {
                                val dVal = newOfferValue.toDoubleOrNull() ?: 0.0
                                if (newOfferCode.isNotBlank() && newOfferTitle.isNotBlank()) {
                                    viewModel.addOffer(newOfferCode, newOfferTitle, newOfferType, dVal)
                                    newOfferCode = ""
                                    newOfferTitle = ""
                                    newOfferValue = ""
                                } else {
                                    viewModel.showToastMessage("Please fill in code and title.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FlameRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Text("Create", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Live Broadcast Announcement to Customer Menu
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FlameRed.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Live Broadcast Notes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceLight
                            )
                            Text(
                                "Broadcast real-time notes and updates to all customer devices",
                                fontSize = 11.sp,
                                color = OnSurfaceVariantLight
                            )
                        }
                        Icon(imageVector = Icons.Default.Campaign, contentDescription = "Broadcast", tint = FlameRed)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        placeholder = { Text("Write announcement here...", fontSize = 12.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurfaceLight,
                            unfocusedTextColor = OnSurfaceLight,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark,
                            focusedBorderColor = FlameRed,
                            unfocusedBorderColor = SurfaceContainerHighest
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.quickCustomerNote.value = noteInput
                                viewModel.showToastMessage("Announcement broadcasted successfully!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FlameRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Broadcast Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                noteInput = ""
                                viewModel.quickCustomerNote.value = ""
                                viewModel.showToastMessage("Broadcast cleared!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHighest),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear Broadcast", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OnSurfaceLight)
                        }
                    }
                }
            }
        }

        // Staff Member Management Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FlameRed.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Staff Management",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceLight
                            )
                            Text(
                                "Add new staff, monitor performance, and manage roles",
                                fontSize = 11.sp,
                                color = OnSurfaceVariantLight
                            )
                        }
                        Icon(imageVector = Icons.Default.People, contentDescription = "Staff", tint = FlameRed)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Active staff members list
                    if (staffMembers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceDark, RoundedCornerShape(10.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No staff members added yet.", fontSize = 12.sp, color = OnSurfaceVariantLight)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            staffMembers.forEach { staff ->
                                val statusColor = when (staff.status) {
                                    StaffStatus.ACTIVE -> StatusReady
                                    StaffStatus.BUSY -> StatusCooking
                                    StaffStatus.OFFLINE -> OnSurfaceVariantLight
                                }
                                val roleIcon = when (staff.role) {
                                    StaffRole.WAITER -> Icons.Default.Person
                                    StaffRole.KITCHEN -> Icons.Default.Restaurant
                                    StaffRole.DELIVERY -> Icons.Default.DeliveryDining
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceDark, RoundedCornerShape(10.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(FlameRed.copy(alpha = 0.1f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = roleIcon,
                                                contentDescription = staff.role.name,
                                                tint = FlameRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    staff.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = OnSurfaceLight
                                                )
                                                // Role Chip
                                                Box(
                                                    modifier = Modifier
                                                        .background(FlameRed.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        staff.role.name,
                                                        color = FlameRed,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 8.sp
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))

                                            // Staff Stats Row
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "Orders: ${staff.ordersHandled}",
                                                    fontSize = 10.sp,
                                                    color = OnSurfaceVariantLight
                                                )
                                                Text(
                                                    "Rate: ${staff.completionRate}%",
                                                    fontSize = 10.sp,
                                                    color = OnSurfaceVariantLight
                                                )
                                                Text(
                                                    "Contrib: ₹${staff.revenueContribution.toInt()}",
                                                    fontSize = 10.sp,
                                                    color = OnSurfaceVariantLight
                                                )
                                            }
                                        }
                                    }

                                    // Action Area: Status Toggle and Delete Button
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Status Toggle Button (custom click area with minimum touch target size of 48dp)
                                        Box(
                                            modifier = Modifier
                                                .minimumInteractiveComponentSize()
                                                .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                                .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .clickable { viewModel.toggleStaffStatus(staff.id) }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                                .testTag("toggle_staff_status_button_${staff.id}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(statusColor, CircleShape)
                                                )
                                                Text(
                                                    staff.status.name,
                                                    color = statusColor,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        // Delete/Remove Staff Button (interactive component size 48dp)
                                        IconButton(
                                            onClick = { viewModel.deleteStaffMember(staff.id) },
                                            modifier = Modifier
                                                .minimumInteractiveComponentSize()
                                                .background(StatusCritical.copy(alpha = 0.12f), CircleShape)
                                                .testTag("delete_staff_button_${staff.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove Staff",
                                                tint = StatusCritical,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(SurfaceContainerHighest)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Form to add a new staff member
                    Text("Add New Staff Member:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OnSurfaceLight)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newStaffName,
                        onValueChange = { newStaffName = it },
                        placeholder = { Text("Staff Full Name (e.g. Rajesh Kumar)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_staff_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurfaceLight,
                            unfocusedTextColor = OnSurfaceLight,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark,
                            focusedBorderColor = FlameRed,
                            unfocusedBorderColor = SurfaceContainerHighest
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Role Select Title
                    Text("Select Staff Role:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariantLight)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Custom role selection row with beautiful modern design
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_staff_role_select"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StaffRole.values().forEach { role ->
                            val isSelected = selectedRole == role
                            val roleColor = if (isSelected) Color.White else OnSurfaceVariantLight
                            val roleBg = if (isSelected) FlameRed else SurfaceDark
                            val roleBorder = if (isSelected) FlameRed else SurfaceContainerHighest

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(roleBg)
                                    .border(1.dp, roleBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedRole = role }
                                    .testTag("new_staff_role_${role.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val rIcon = when (role) {
                                        StaffRole.WAITER -> Icons.Default.Person
                                        StaffRole.KITCHEN -> Icons.Default.Restaurant
                                        StaffRole.DELIVERY -> Icons.Default.DeliveryDining
                                    }
                                    Icon(
                                        imageVector = rIcon,
                                        contentDescription = role.name,
                                        tint = roleColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = role.name,
                                        color = roleColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Button to add staff member
                    Button(
                        onClick = {
                            if (newStaffName.isNotBlank()) {
                                viewModel.addStaffMember(newStaffName.trim(), selectedRole)
                                newStaffName = ""
                            } else {
                                viewModel.showToastMessage("Please enter staff member's name.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FlameRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("add_staff_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Staff Icon", modifier = Modifier.size(16.dp))
                            Text("Add Staff Member", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Recent Activity log list
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recent Live Activity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceLight
                        )
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh logs",
                            tint = OnSurfaceVariantLight,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        logs.take(4).forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceDark.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    val icon = when (log.type) {
                                        com.example.model.LogType.ORDER -> Icons.Default.TableRestaurant
                                        com.example.model.LogType.DELIVERY -> Icons.Default.DeliveryDining
                                        com.example.model.LogType.WARNING -> Icons.Default.Error
                                        com.example.model.LogType.INFO -> Icons.Default.Info
                                    }
                                    val iconColor = when (log.type) {
                                        com.example.model.LogType.ORDER -> StatusDelivered
                                        com.example.model.LogType.DELIVERY -> StatusReady
                                        com.example.model.LogType.WARNING -> StatusCritical
                                        com.example.model.LogType.INFO -> AmberGlow
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(iconColor.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = icon, contentDescription = log.message, tint = iconColor, modifier = Modifier.size(16.dp))
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            log.message,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = OnSurfaceLight,
                                            maxLines = 1
                                        )
                                        Text(
                                            log.detail,
                                            fontSize = 10.sp,
                                            color = OnSurfaceVariantLight
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        log.value,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = OnSurfaceLight
                                    )
                                    Text(
                                        log.timestamp,
                                        fontSize = 9.sp,
                                        color = OnSurfaceVariantLight
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

data class TopProduct(
    val name: String,
    val units: Int,
    val earnings: Int,
    val status: String,
    val statusColor: Color,
    val img: String
)
