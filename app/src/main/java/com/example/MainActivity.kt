package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AdminAnalyticsScreen
import com.example.ui.screens.CustomerMenuScreen
import com.example.ui.screens.KitchenQueueScreen
import com.example.ui.screens.WaiterPanelScreen
import com.example.ui.screens.DeliveryPartnerScreen
import com.example.ui.theme.*
import com.example.viewmodel.RestaurantViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: RestaurantViewModel = viewModel()
                MainAppContainer(viewModel)
            }
        }
    }
}

data class NavTab(
    val id: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)

@Composable
fun MainAppContainer(viewModel: RestaurantViewModel) {
    val currentPersona by viewModel.currentPersona
    val currentPerspective by viewModel.currentPerspective
    val toastMessage by viewModel.currentToast
    var isSimulatorExpanded by remember { mutableStateOf(false) }

    // Categorized panel routing based on active persona
    val navItems = when (currentPersona) {
        "Customer" -> emptyList() // Screen locked to Customer perspective
        "Kitchen Staff" -> emptyList() // Screen locked to Kitchen display perspective
        "Waiter Staff" -> emptyList() // Screen locked to Waiter service perspective
        "Delivery Partner" -> emptyList() // Screen locked to Delivery partner perspective
        "Admin" -> listOf(
            NavTab("Customer", "Menu", Icons.Default.RestaurantMenu, "nav_menu_button"),
            NavTab("Kitchen", "Kitchen", Icons.Default.SoupKitchen, "nav_kitchen_button"),
            NavTab("Waiter", "Tables", Icons.Default.TableBar, "nav_waiter_button"),
            NavTab("Delivery", "Delivery", Icons.Default.Moped, "nav_delivery_button"),
            NavTab("Analytics", "Analytics", Icons.Default.Analytics, "nav_analytics_button")
        )
        else -> emptyList()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (navItems.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .appleLiquidGlass(cornerRadius = 24)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        navItems.forEach { item ->
                            val isSelected = currentPerspective == item.id
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        viewModel.currentPerspective.value = item.id
                                        if (item.id == "Customer") {
                                            viewModel.currentCustomerTab.value = "Menu"
                                        }
                                    }
                                    .testTag(item.testTag),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val tint = if (isSelected) FlameRed else OnSurfaceVariantLight
                                val bgAlpha = if (isSelected) 0.12f else 0f
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(FlameRed.copy(alpha = bgAlpha)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = tint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = tint
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceDark)
        ) {
            // Render Selected Perspective Screen with visual state transitions
            AnimatedContent(
                targetState = currentPerspective,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "perspective_transition"
            ) { perspective ->
                when (perspective) {
                    "Customer" -> CustomerMenuScreen(viewModel, innerPadding)
                    "Kitchen" -> KitchenQueueScreen(viewModel, innerPadding)
                    "Waiter" -> WaiterPanelScreen(viewModel, innerPadding)
                    "Delivery" -> DeliveryPartnerScreen(viewModel, innerPadding)
                    "Analytics" -> AdminAnalyticsScreen(viewModel, innerPadding)
                    else -> CustomerMenuScreen(viewModel, innerPadding)
                }
            }

            // Floating Persona Simulator Ball
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 110.dp, end = 16.dp)
                    .size(56.dp)
                    .shadow(8.dp, CircleShape, ambientColor = FlameRed.copy(alpha = 0.3f), spotColor = FlameRed)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AmberGlow,
                                FlameRed,
                                FlameRed.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    .clickable { isSimulatorExpanded = true }
                    .testTag("persona_simulator_ball"),
                contentAlignment = Alignment.Center
            ) {
                val currentIcon = when (currentPersona) {
                    "Customer" -> Icons.Default.Person
                    "Kitchen Staff" -> Icons.Default.SoupKitchen
                    "Waiter Staff" -> Icons.Default.TableBar
                    "Delivery Partner" -> Icons.Default.Moped
                    else -> Icons.Default.Security
                }
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(
                        imageVector = currentIcon,
                        contentDescription = "Open Persona Simulator",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    // Small "SIM" developer badge on the floating ball
                    Box(
                        modifier = Modifier
                            .offset(x = 6.dp, y = (-6).dp)
                            .background(CharcoalBlack, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "SIM",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FlameRed
                        )
                    }
                }
            }

            // Beautiful slide-in/fade-in Simulator Selection Dialog Scrim Overlay
            AnimatedVisibility(
                visible = isSimulatorExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { isSimulatorExpanded = false },
                    contentAlignment = Alignment.Center
                ) {
                    // Central Dialog Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .appleLiquidGlass(cornerRadius = 24)
                            .clickable(enabled = false) {} // Prevent click-through closing
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = FlameRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Active Persona Simulator",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceLight
                                )
                            }

                            // Dynamic Dark/Light Mode switch inside dialog
                            IconButton(
                                onClick = { isThemeDarkGlobal = !isThemeDarkGlobal },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(FlameRed.copy(alpha = 0.1f), CircleShape)
                                    .testTag("dark_mode_toggle")
                            ) {
                                Icon(
                                    imageVector = if (isThemeDarkGlobal) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Dark Mode",
                                    tint = FlameRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Simulation Purpose Notice
                        Text(
                            text = "To prevent layout interference and simulate production workflows, staff and customer terminals run on isolated screens. All data synchronizes instantly across perspectives in real-time.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnSurfaceVariantLight,
                            lineHeight = 15.sp
                        )

                        // 5 Persona Options List
                        val personas = listOf(
                            Triple("Customer", "Customer Area", "Browse menu, submit orders, view live cooking status & give reviews."),
                            Triple("Kitchen Staff", "Kitchen Display", "Kitchen preparation dashboard. Advance order status to Cooking & Ready."),
                            Triple("Waiter Staff", "Waiter Desk", "Monitor live dine-in table states, service requests, and dispatch bills."),
                            Triple("Delivery Partner", "Delivery App", "Live couriers routing simulator, distance calculation & messaging."),
                            Triple("Admin", "Admin Suite", "All-access control panel. Real-time sales analytics and global settings.")
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            personas.forEach { (pId, pLabel, pDesc) ->
                                val isSelected = currentPersona == pId
                                val itemBgBrush = if (isSelected) {
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            FlameRed,
                                            FlameRed.copy(alpha = 0.85f)
                                        )
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            SurfaceDark.copy(alpha = 0.5f),
                                            SurfaceDark.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(itemBgBrush)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color.White.copy(alpha = 0.3f) else SurfaceContainer,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            viewModel.currentPersona.value = pId
                                            when (pId) {
                                                "Customer" -> {
                                                    viewModel.currentPerspective.value = "Customer"
                                                    viewModel.currentCustomerTab.value = "Menu"
                                                }
                                                "Kitchen Staff" -> {
                                                    viewModel.currentPerspective.value = "Kitchen"
                                                }
                                                "Waiter Staff" -> {
                                                    viewModel.currentPerspective.value = "Waiter"
                                                }
                                                "Delivery Partner" -> {
                                                    viewModel.currentPerspective.value = "Delivery"
                                                }
                                                "Admin" -> {
                                                    viewModel.currentPerspective.value = "Analytics"
                                                }
                                            }
                                            viewModel.showToastMessage("Switched to $pId Terminal")
                                            isSimulatorExpanded = false // Auto close on select
                                        }
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val icon = when (pId) {
                                            "Customer" -> Icons.Default.Person
                                            "Kitchen Staff" -> Icons.Default.SoupKitchen
                                            "Waiter Staff" -> Icons.Default.TableBar
                                            "Delivery Partner" -> Icons.Default.Moped
                                            else -> Icons.Default.Security
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    if (isSelected) Color.White.copy(alpha = 0.2f) else FlameRed.copy(alpha = 0.1f),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = pLabel,
                                                tint = if (isSelected) Color.White else FlameRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = pLabel,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else OnSurfaceLight
                                            )
                                            Text(
                                                text = pDesc,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = if (isSelected) Color.White.copy(alpha = 0.85f) else OnSurfaceVariantLight,
                                                lineHeight = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Close Dialog Button
                        Button(
                            onClick = { isSimulatorExpanded = false },
                            colors = ButtonDefaults.buttonColors(containerColor = FlameRed),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                        ) {
                            Text(
                                text = "Close Simulator Menu",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }


            // Beautiful slide-in Toast notifications panel
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp) // Anchored safely above the bottom navigation
            ) {
                toastMessage?.let { msg ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .border(1.5.dp, FlameRed, RoundedCornerShape(12.dp)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Alert Info",
                                tint = FlameRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = msg,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceLight
                            )
                        }
                    }
                }
            }
        }
    }
}
