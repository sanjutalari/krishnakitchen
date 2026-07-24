package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CartItem
import com.example.model.MenuItem
import com.example.model.OfferTemplate
import com.example.ui.theme.*
import com.example.viewmodel.RestaurantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerMenuScreen(
    viewModel: RestaurantViewModel,
    innerPadding: PaddingValues
) {
    val items by viewModel.menuItems.collectAsState()
    val cartItems by viewModel.cart.collectAsState()
    val searchQuery by viewModel.searchQuery
    val selectedCat by viewModel.selectedCategory
    val offers by viewModel.offers.collectAsState()
    val appliedOffer by viewModel.appliedOffer.collectAsState()

    val customerTokens by viewModel.customerTokens.collectAsState()
    val customerVisits by viewModel.customerVisits.collectAsState()
    val tokensToApply by viewModel.tokensToApply

    var showCancelDialogForOrderId by remember { mutableStateOf<Int?>(null) }
    var cancelReasonInput by remember { mutableStateOf("Ordered wrong dish by mistake") }

    var showCartSheet by remember { mutableStateOf(false) }

    val currentCustomerTab by viewModel.currentCustomerTab
    val orders by viewModel.orders.collectAsState()
    val extraRequests by viewModel.extraRequests.collectAsState()
    val supportMessages by viewModel.supportMessages.collectAsState()
    var messageInput by remember { mutableStateOf("") }

    // Feedback / rating states
    var feedbackRating by remember { mutableStateOf(5.0) }
    var feedbackReview by remember { mutableStateOf("") }
    var feedbackDish by remember { mutableStateOf("Special Chicken Biryani") }

    // Filter items based on Category & Search Query
    val filteredItems = items.filter { item ->
        val matchesCategory = selectedCat == "All" || item.category == selectedCat
        val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .padding(bottom = 72.dp) // Leave room for perspective switcher
    ) {
        if (currentCustomerTab == "Menu") {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 16.dp,
                    bottom = 150.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Search Section
            item {
                val orderingMode by viewModel.orderingMode
                val qrCodeTable by viewModel.qrCodeTable
                val gpsAllowed by viewModel.gpsAllowed
                val customerAddress by viewModel.customerAddress
                val deliveryRadius by viewModel.deliveryRadius

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Krishna Chicken",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = FlameRed,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "🍗", fontSize = 20.sp)
                            }
                            Text(
                                text = if (orderingMode == "Dine-In") "Dine-In QR Detected: Table $qrCodeTable" else "Delivery Pinned: $customerAddress",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberGlow,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Floating Cart Button
                        BadgedBox(
                            badge = {
                                if (cartItems.isNotEmpty()) {
                                    Badge(
                                        containerColor = FlameRed,
                                        contentColor = Color.White
                                    ) {
                                        Text(text = cartItems.sumOf { it.quantity }.toString())
                                    }
                                }
                            },
                            modifier = Modifier
                                .clickable { showCartSheet = true }
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = OnSurfaceLight,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Admin Quick Broadcast Note
                    val adminNote by viewModel.quickCustomerNote
                    if (adminNote.isNotBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, FlameRed.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = CharcoalBlack.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(FlameRed.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = "Broadcast",
                                        tint = FlameRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = adminNote,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = OnSurfaceLight,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Simulated Controls for Dine-In / Delivery
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.toggleOrderingMode() },
                            modifier = Modifier.weight(1.3f),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (orderingMode == "Dine-In") Icons.Default.DeliveryDining else Icons.Default.QrCodeScanner,
                                contentDescription = "Switch",
                                tint = AmberGlow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (orderingMode == "Dine-In") "Go to Delivery" else "Scan Table QR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceLight
                            )
                        }

                        if (orderingMode == "Dine-In") {
                            Button(
                                onClick = { viewModel.simulateQrScan("12") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = FlameRed),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text("Scan Table 12", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.allowGpsAndDetectLocation() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = if (gpsAllowed) StatusReady else FlameRed),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GpsFixed,
                                    contentDescription = "GPS",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (gpsAllowed) "GPS Active" else "Allow GPS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (orderingMode == "Delivery") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainerHighest.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Radius Limit",
                                    tint = AmberGlow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Delivery is available only within $deliveryRadius KM radius limits. (Configured by Admin)",
                                    fontSize = 10.sp,
                                    color = OnSurfaceVariantLight,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Quick Note Recommendations Section for Customers (Best-Sellers and high-rated items)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AmberGlow.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TipsAndUpdates,
                                contentDescription = "Quick recommendation",
                                tint = AmberGlow,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "QUICK NOTES & TODAY'S TRENDING",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = AmberGlow,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "🔥 Best Seller: Special Chicken Biryani is today's highest-selling item (4.9 ★, 3.1k reviews)!",
                            fontSize = 11.sp,
                            color = OnSurfaceLight,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⭐ Top Rated: Crispy Fry Piece has exceptionally high reviews for crispiness & spice levels.",
                            fontSize = 11.sp,
                            color = OnSurfaceVariantLight
                        )
                    }
                }
            }

            // Search Bar Input
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearch(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("menu_search_input"),
                    placeholder = {
                        Text(
                            "Search for biryani, chicken fry...",
                            color = OnSurfaceVariantLight.copy(alpha = 0.5f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = OnSurfaceVariantLight
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearch("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Search",
                                    tint = OnSurfaceVariantLight
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated,
                        focusedBorderColor = FlameRed,
                        unfocusedBorderColor = SurfaceContainerHighest,
                        focusedTextColor = OnSurfaceLight,
                        unfocusedTextColor = OnSurfaceLight
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Hero Banner Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, FlameRed.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .clickable {
                            viewModel.toggleDinnerForTwo()
                        }
                ) {
                    // Image Background with Gradient Overlay
                    AsyncImage(
                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCD0Q5OsKXnyCseeH7e-wZdBz9La1e01q0Z4nas5O4lEnYYqYeDfXVLAPGsvbEkdLqsabSL-QoxGIuneojbKPxbYOA_DuqcNmzxinPb0R3N9GZnfpe4qrwO7JDOohXBmVW0aB5NCjOqagQkEn_Dd_aIvmvYyVEGSQ4oRDslcpNPk2U4W7nVDj2HCCzVENEa8zi6f0-WI7LHcmS7MBJJc0BDgDNNqq1TJ_8p4H30kCNud5z7W9-_HJx91Q",
                        contentDescription = "Flame Grilled Chicken wings Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        CharcoalBlack,
                                        CharcoalBlack.copy(alpha = 0.6f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Text Content inside Banner
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(16.dp)
                            .width(220.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .background(FlameRed, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "LIMITED OFFER",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Flat 50% Off on your first order",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Valid on orders above ₹499",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariantLight
                        )
                    }
                }
            }

            // Admin Special Offers Carousel
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = "Offers",
                                tint = FlameRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "Admin Offers & Coupons",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceLight
                            )
                        }
                        Text(
                            text = "${offers.size} Cards >",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = FlameRed,
                            modifier = Modifier.clickable {
                                viewModel.currentCustomerTab.value = "Offers"
                            }
                        )
                    }

                    if (offers.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .appleLiquidGlass(cornerRadius = 12),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Text(
                                "No active offers created by Admin yet.",
                                fontSize = 12.sp,
                                color = OnSurfaceVariantLight,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(offers) { offer ->
                                val isApplied = appliedOffer?.id == offer.id
                                AdminOfferCardItem(
                                    offer = offer,
                                    isApplied = isApplied,
                                    onApply = { viewModel.applyOffer(offer) }
                                )
                            }
                        }
                    }
                }
            }

            // Categories Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Categories",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceLight
                        )
                        Text(
                            "View All",
                            style = MaterialTheme.typography.labelSmall,
                            color = FlameRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { viewModel.setCategory("All") }
                        )
                    }

                    val categories = listOf(
                        "All" to Icons.Default.Restaurant,
                        "Chicken" to Icons.Default.DinnerDining,
                        "Biryani" to Icons.Default.SoupKitchen,
                        "Fry Piece" to Icons.Default.OutdoorGrill,
                        "Curry" to Icons.Default.SetMeal,
                        "Starters" to Icons.Default.BakeryDining,
                        "Beverages" to Icons.Default.LocalBar
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { (catName, icon) ->
                            val isSelected = selectedCat == catName
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.clickable { viewModel.setCategory(catName) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) FlameRed.copy(alpha = 0.2f) else SurfaceElevated)
                                        .border(
                                            1.dp,
                                            if (isSelected) FlameRed else SurfaceContainerHighest,
                                            RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = catName,
                                        tint = if (isSelected) FlameRed else OnSurfaceVariantLight,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Text(
                                    catName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) OnSurfaceLight else OnSurfaceVariantLight,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Menu Items Grid/List Section
            item {
                Text(
                    text = if (selectedCat == "All") "Trending Now" else "$selectedCat Items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceLight,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (filteredItems.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = "Empty menu",
                            tint = OnSurfaceVariantLight.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No food items found matching constraints.",
                            color = OnSurfaceVariantLight.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(filteredItems) { dish ->
                    FoodItemCard(
                        dish = dish,
                        quantityInCart = viewModel.getCartItemCount(dish.id),
                        onAdd = { viewModel.addToCart(dish) },
                        onRemove = { viewModel.removeFromCart(dish) }
                    )
                }
            }
        }

        // Persistent "View Cart" Floating Banner
        if (cartItems.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 76.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = FlameRed),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .clickable { showCartSheet = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .size(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cartItems.sumOf { it.quantity }.toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "View Cart",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "₹${cartItems.sumOf { it.item.price * it.quantity }} • Click to review",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Next", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

        // Cart Drawer Sheet Implementation
        if (showCartSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCartSheet = false },
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
                CartSheetContent(
                    cartItems = cartItems,
                    appliedOffer = appliedOffer,
                    viewModel = viewModel,
                    onAdd = { viewModel.addToCart(it) },
                    onRemove = { viewModel.removeFromCart(it) },
                    onRemoveOffer = { viewModel.removeAppliedOffer() },
                    onCheckout = { table ->
                        viewModel.placeCustomerOrder(table)
                        showCartSheet = false
                    },
                    onDismiss = { showCartSheet = false }
                )
            }
        }

        // TAB: ADMIN CREATED OFFERS & COUPONS
        if (currentCustomerTab == "Offers") {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 16.dp,
                    bottom = 150.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Frequent Customer Token Wallet & Anti-Scam Verification Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AmberGlow.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(AmberGlow.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ConfirmationNumber,
                                            contentDescription = "Tokens",
                                            tint = AmberGlow,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                viewModel.customerName.value,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = OnSurfaceLight
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = StatusReady.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    "VERIFIED FREQUENT VISITOR",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = StatusReady,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            "${viewModel.customerPhone.value} • $customerVisits Orders Completed",
                                            fontSize = 12.sp,
                                            color = OnSurfaceVariantLight
                                        )
                                    }
                                }
                            }

                            Divider(color = SurfaceContainerHighest.copy(alpha = 0.3f))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceDark, RoundedCornerShape(14.dp))
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "AVAILABLE TOKENS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = OnSurfaceVariantLight,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        "🎫 $customerTokens Tokens",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = AmberGlow
                                    )
                                    Text(
                                        "Each token gives ₹50 instant discount",
                                        fontSize = 11.sp,
                                        color = StatusReady,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        "Anti-Scam Protection",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FlameRed
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = FlameRed.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, FlameRed.copy(alpha = 0.3f))
                                    ) {
                                        Text(
                                            "CONSUMED ON USE ✓",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = FlameRed,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                "🛡️ Protection Rule: Tokens are bound to your verified phone number. Applying tokens to an order automatically consumes and removes them from your wallet so non-registered friends or unauthorized individuals cannot scam or misuse your coupons. Visit and order frequently to earn +1 token on every completed visit!",
                                fontSize = 11.sp,
                                color = OnSurfaceVariantLight,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .appleLiquidGlass(cornerRadius = 20),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(FlameRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = "Offers",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Admin Created Offers & Coupons",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = OnSurfaceLight
                                )
                                Text(
                                    text = "All live promo codes and discount cards created by management",
                                    fontSize = 11.sp,
                                    color = OnSurfaceVariantLight
                                )
                            }
                        }
                    }
                }

                if (offers.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .appleLiquidGlass(cornerRadius = 16),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No offers created by Admin yet.\nSwitch to Admin perspective to create new coupon cards!",
                                    color = OnSurfaceVariantLight,
                                    fontSize = 13.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(offers) { offer ->
                        val isApplied = appliedOffer?.id == offer.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .appleLiquidGlass(cornerRadius = 16),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
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
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = FlameRed.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, FlameRed.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = offer.type.uppercase(),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = FlameRed,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (offer.isActive) StatusReady.copy(alpha = 0.15f) else OnSurfaceVariantLight.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (offer.isActive) "LIVE" else "DISABLED",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (offer.isActive) StatusReady else OnSurfaceVariantLight,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = offer.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurfaceLight
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = AmberGlow.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, AmberGlow.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = "CODE: ${offer.code}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = AmberGlow,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }

                                        val valueStr = when (offer.type) {
                                            "Percentage" -> "${offer.value.toInt()}% Discount"
                                            "Fixed" -> "₹${offer.value.toInt()} Flat Off"
                                            "Combo" -> "₹${offer.value.toInt()} Combo Value"
                                            else -> "${offer.value.toInt()}% Off"
                                        }
                                        Text(
                                            text = "• $valueStr",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FlameRed
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Button(
                                    onClick = { viewModel.applyOffer(offer) },
                                    enabled = offer.isActive,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isApplied) StatusReady else FlameRed
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("apply_offer_page_${offer.code}")
                                ) {
                                    Text(if (isApplied) "APPLIED ✓" else "USE CODE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 2: MY ORDERS TRACKING
        if (currentCustomerTab == "Orders") {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 16.dp,
                    bottom = 150.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Track Active Orders",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurfaceLight
                    )
                }

                if (orders.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inbox,
                                    contentDescription = "Empty",
                                    tint = OnSurfaceVariantLight.copy(alpha = 0.3f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "No active orders. Try placing one from the Menu!",
                                    color = OnSurfaceVariantLight,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else {
                    items(orders) { order ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(16.dp)),
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
                                            text = "Order #${order.id}",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = OnSurfaceLight,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "${order.tableNumber} • ${order.items.sumOf { it.quantity }} items",
                                            fontSize = 11.sp,
                                            color = OnSurfaceVariantLight
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(FlameRed.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = order.status.name,
                                            color = FlameRed,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Status Progress Indicator
                                val statusIndex = order.status.ordinal
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val phases = listOf("Pending", "Accepted", "Cooking", "Ready", "Served/Out")
                                    phases.forEachIndexed { idx, label ->
                                        val isCompleted = statusIndex >= idx
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .background(
                                                        if (isCompleted) StatusReady else SurfaceContainerHighest,
                                                        CircleShape
                                                    )
                                            )
                                            Text(
                                                text = label,
                                                fontSize = 9.sp,
                                                color = if (isCompleted) OnSurfaceLight else OnSurfaceVariantLight,
                                                fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Delivery Secure OTP display
                                val orderingMode by viewModel.orderingMode
                                if (orderingMode == "Delivery") {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = StatusReady.copy(alpha = 0.1f)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusReady.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Secure Delivery OTP",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = StatusReady
                                                )
                                                Text(
                                                    text = "Provide this code to driver to complete delivery.",
                                                    fontSize = 10.sp,
                                                    color = OnSurfaceVariantLight
                                                )
                                            }
                                            Text(
                                                text = viewModel.deliveryOtp,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 20.sp,
                                                color = StatusReady,
                                                letterSpacing = 2.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                // Extras request module
                                Text(
                                    text = "Extras & Modifications Approval:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = OnSurfaceLight
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                // List existing requests for this order
                                val reqs = extraRequests.filter { it.orderId == order.id }
                                if (reqs.isNotEmpty()) {
                                    reqs.forEach { req ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .background(SurfaceDark, RoundedCornerShape(8.dp))
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = req.description,
                                                fontSize = 11.sp,
                                                color = OnSurfaceLight
                                            )
                                            val statusColor = when (req.status) {
                                                "Accepted" -> StatusReady
                                                "Rejected" -> FlameRed
                                                else -> AmberGlow
                                            }
                                            Text(
                                                text = req.status.uppercase(),
                                                color = statusColor,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }

                                 Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.createExtraRequest(
                                            order.id,
                                            order.tableNumber,
                                            "Request Extra topping (1x) & Piece"
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHighest),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Request Extra Pieces / Toppings (Requires Approval)", fontSize = 11.sp, color = OnSurfaceLight)
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Divider(color = SurfaceContainerHighest.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(10.dp))

                                val elapsedSec = order.elapsedSeconds
                                val secsLeft = (300 - elapsedSec).coerceAtLeast(0)
                                val mins = secsLeft / 60
                                val secs = secsLeft % 60

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "5-Min Cancellation Window:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = OnSurfaceLight
                                        )
                                        Text(
                                            text = if (secsLeft > 0) "Time left to request cancel: ${mins}m ${secs}s" else "Window closed (>5 mins limit in prep)",
                                            fontSize = 10.sp,
                                            color = if (secsLeft > 0) AmberGlow else OnSurfaceVariantLight
                                        )
                                    }

                                    if (order.status != com.example.model.OrderStatus.CANCELLED && order.status != com.example.model.OrderStatus.COMPLETED && order.status != com.example.model.OrderStatus.SERVED) {
                                        if (order.cancellationRequested) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = AmberGlow.copy(alpha = 0.15f),
                                                border = BorderStroke(1.dp, AmberGlow.copy(alpha = 0.4f))
                                            ) {
                                                Text(
                                                    text = "⏳ CANCEL PENDING ADMIN APPROVAL",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = AmberGlow,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                                )
                                            }
                                        } else if (secsLeft > 0) {
                                            Button(
                                                onClick = { showCancelDialogForOrderId = order.id },
                                                colors = ButtonDefaults.buttonColors(containerColor = FlameRed),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.testTag("request_cancel_order_${order.id}")
                                            ) {
                                                Text("Request Cancel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = OnSurfaceVariantLight.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "🔒 CANCEL CLOSED",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = OnSurfaceVariantLight,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (order.cancellationDeniedReason != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "⚠️ Admin Decision: Cancellation rejected (${order.cancellationDeniedReason}). Order is being prepared.",
                                        fontSize = 10.sp,
                                        color = FlameRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Split bill breakdown
                                if (order.splitBillInfo != null) {
                                    val split = order.splitBillInfo
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("👥 Split Bill (${split.splitMode} Split):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AmberGlow)
                                                Text("₹${split.perPersonAmount}/person (${split.numberOfPeople} members)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceLight)
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                split.members.forEach { m ->
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = if (m.isPaid) StatusReady.copy(alpha = 0.2f) else FlameRed.copy(alpha = 0.15f)
                                                    ) {
                                                        Text(
                                                            text = "${m.name}: ₹${m.amount} ${if (m.isPaid) "✓" else "⏳"}",
                                                            fontSize = 9.sp,
                                                            color = if (m.isPaid) StatusReady else FlameRed,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
                }

                // Customer Feedback Rating Sheet
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Rate Your Culinary Experience",
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceLight,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Help us analyze dish quality and delivery metrics.",
                                fontSize = 11.sp,
                                color = OnSurfaceVariantLight,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Dish Selection
                            Text("Select Dish to Rate:", fontSize = 11.sp, color = OnSurfaceVariantLight)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val dishesToRate = listOf("Special Chicken Biryani", "Crispy Fry Piece", "Butter Chicken Curry")
                                dishesToRate.forEach { dish ->
                                    val isSelected = feedbackDish == dish
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) AmberGlow.copy(alpha = 0.2f) else SurfaceDark)
                                            .border(1.dp, if (isSelected) AmberGlow else Color.Transparent, RoundedCornerShape(6.dp))
                                            .clickable { feedbackDish = dish }
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dish.split(" ").last(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) AmberGlow else OnSurfaceVariantLight
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Star Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Rating: ", fontSize = 12.sp, color = OnSurfaceLight, fontWeight = FontWeight.Bold)
                                (1..5).forEach { stars ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Star",
                                        tint = if (feedbackRating >= stars) AmberGlow else SurfaceContainerHighest,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable { feedbackRating = stars.toDouble() }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = feedbackReview,
                                onValueChange = { feedbackReview = it },
                                placeholder = { Text("Write your review (e.g. portion size, salt, spice levels)...", fontSize = 11.sp) },
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

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (feedbackReview.isNotBlank()) {
                                        viewModel.submitFeedback(
                                            customerName = "Sanjeev",
                                            rating = feedbackRating,
                                            review = feedbackReview,
                                            dishName = feedbackDish
                                        )
                                        feedbackReview = ""
                                    } else {
                                        viewModel.showToastMessage("Please write a small review before submitting.")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = FlameRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Submit Review", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // TAB 3: SUPPORT CHAT
        if (currentCustomerTab == "Chat") {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = innerPadding.calculateTopPadding() + 16.dp, bottom = 150.dp)
            ) {
                Text(
                    text = "Privacy Protected Chat Support",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurfaceLight
                )
                Text(
                    text = "Customer-Driver Privacy Mode active. No actual phone numbers shared.",
                    fontSize = 11.sp,
                    color = OnSurfaceVariantLight,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Message Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(supportMessages) { msg ->
                            val isMe = msg.sender == "Customer"
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isMe) FlameRed.copy(alpha = 0.2f) else SurfaceDark)
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "${msg.sender}: ${msg.message}",
                                        color = OnSurfaceLight,
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    text = msg.timestamp,
                                    fontSize = 8.sp,
                                    color = OnSurfaceVariantLight,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = { Text("Send private message...", fontSize = 12.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurfaceLight,
                            unfocusedTextColor = OnSurfaceLight,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark,
                            focusedBorderColor = FlameRed,
                            unfocusedBorderColor = SurfaceContainerHighest
                        ),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            if (messageInput.isNotBlank()) {
                                viewModel.sendSupportMessage("Customer", messageInput)
                                messageInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .background(FlameRed, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Sub-tab Navigation controls for reviewer testing
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .appleLiquidGlass(cornerRadius = 18)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf("Menu", "Offers", "Orders", "Chat")
            tabs.forEach { tab ->
                val isSelected = currentCustomerTab == tab
                val icon = when (tab) {
                    "Menu" -> Icons.Default.RestaurantMenu
                    "Offers" -> Icons.Default.LocalOffer
                    "Orders" -> Icons.Default.ListAlt
                    else -> Icons.Default.SupportAgent
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) FlameRed.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { viewModel.currentCustomerTab.value = tab }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab,
                            tint = if (isSelected) FlameRed else OnSurfaceVariantLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = tab,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) FlameRed else OnSurfaceVariantLight
                        )
                    }
                }
            }
        }

        // Order Cancellation Reason Dialog
        if (showCancelDialogForOrderId != null) {
            val targetId = showCancelDialogForOrderId!!
            AlertDialog(
                onDismissRequest = { showCancelDialogForOrderId = null },
                containerColor = SurfaceElevated,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Cancel, contentDescription = null, tint = FlameRed)
                        Text("Request Order Cancellation", color = OnSurfaceLight, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Order #$targetId is within the 5-minute cancellation window. Your request will be sent to Admin for permission & approval.",
                            fontSize = 12.sp,
                            color = OnSurfaceVariantLight
                        )
                        Text("Reason for Cancellation:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurfaceLight)
                        OutlinedTextField(
                            value = cancelReasonInput,
                            onValueChange = { cancelReasonInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FlameRed,
                                unfocusedBorderColor = SurfaceContainerHighest,
                                focusedTextColor = OnSurfaceLight,
                                unfocusedTextColor = OnSurfaceLight
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.requestOrderCancellation(targetId, cancelReasonInput)
                            showCancelDialogForOrderId = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FlameRed)
                    ) {
                        Text("Send to Admin", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelDialogForOrderId = null }) {
                        Text("Close", color = OnSurfaceVariantLight)
                    }
                }
            )
        }
    }
}

@Composable
fun FoodItemCard(
    dish: MenuItem,
    quantityInCart: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .appleLiquidGlass(cornerRadius = 16),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AsyncImage(
                    model = dish.imageUrl,
                    contentDescription = dish.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Best seller / Discount Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (dish.bestSeller) {
                        Box(
                            modifier = Modifier
                                .background(AmberGlow, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "BEST SELLER",
                                fontSize = 8.sp,
                                color = CharcoalBlack,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (dish.originalPrice != null) {
                        Box(
                            modifier = Modifier
                                .background(FlameRed, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "15% OFF",
                                fontSize = 8.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Dark Bottom Overlay Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, CharcoalBlack.copy(alpha = 0.8f))
                            )
                        )
                )
            }

            // Info Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = dish.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = AmberGlow,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = dish.rating.toString(),
                                fontSize = 11.sp,
                                color = AmberGlow,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "(${dish.reviewsCount})",
                                fontSize = 11.sp,
                                color = OnSurfaceVariantLight
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${dish.price}",
                            style = MaterialTheme.typography.titleMedium,
                            color = FlameRed,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (dish.originalPrice != null) {
                            Text(
                                text = "₹${dish.originalPrice}",
                                fontSize = 11.sp,
                                color = OnSurfaceVariantLight,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        }
                    }
                }

                if (dish.description.isNotEmpty()) {
                    Text(
                        text = dish.description,
                        fontSize = 12.sp,
                        color = OnSurfaceVariantLight.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Divider(color = SurfaceContainerHighest.copy(alpha = 0.3f), thickness = 1.dp)

                // Bottom timing & CTA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Prep Time",
                            tint = OnSurfaceVariantLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = dish.prepTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariantLight
                        )
                    }

                    // Dynamic ADD / Counter Button
                    if (quantityInCart == 0) {
                        Button(
                            onClick = onAdd,
                            colors = ButtonDefaults.buttonColors(containerColor = FlameRed),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("add_item_${dish.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(14.dp))
                                Text("ADD", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .height(36.dp)
                                .border(1.dp, AmberGlow, RoundedCornerShape(8.dp))
                                .background(SurfaceDark, RoundedCornerShape(8.dp))
                                .padding(horizontal = 4.dp)
                        ) {
                            IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    tint = AmberGlow,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = quantityInCart.toString(),
                                color = OnSurfaceLight,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(onClick = onAdd, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase",
                                    tint = AmberGlow,
                                    modifier = Modifier.size(14.dp)
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
fun CartSheetContent(
    cartItems: List<CartItem>,
    appliedOffer: OfferTemplate?,
    viewModel: RestaurantViewModel,
    onAdd: (MenuItem) -> Unit,
    onRemove: (MenuItem) -> Unit,
    onRemoveOffer: () -> Unit,
    onCheckout: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTable by remember { mutableStateOf("Table 4") }
    val customerTokens by viewModel.customerTokens.collectAsState()
    val tokensToApply by viewModel.tokensToApply

    val isSplitActive by viewModel.isSplitBillActive
    val splitPeople by viewModel.splitPeopleCount
    val splitMode by viewModel.splitMode

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Your Cart Summary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = OnSurfaceLight
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (cartItems.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Empty",
                    tint = OnSurfaceVariantLight,
                    modifier = Modifier.size(48.dp)
                )
                Text("Your cart is empty.", color = OnSurfaceVariantLight)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .maxHeight(200.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cartItems) { cartItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceDark, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = cartItem.item.imageUrl,
                                    contentDescription = cartItem.item.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    cartItem.item.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = OnSurfaceLight,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "₹${cartItem.item.price} each",
                                    fontSize = 10.sp,
                                    color = OnSurfaceVariantLight
                                )
                            }
                        }

                        // Quantity Control
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { onRemove(cartItem.item) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RemoveCircleOutline,
                                    contentDescription = "Less",
                                    tint = FlameRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = cartItem.quantity.toString(),
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceLight,
                                fontSize = 13.sp
                            )
                            IconButton(
                                onClick = { onAdd(cartItem.item) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = "More",
                                    tint = FlameRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = SurfaceContainerHighest)
            Spacer(modifier = Modifier.height(10.dp))

            // Select Table
            Text(
                "Select Dining Table:",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = OnSurfaceVariantLight
            )
            Spacer(modifier = Modifier.height(6.dp))

            val tables = listOf("Table 4", "Table 7", "Table 12", "Table 5", "Takeaway")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(tables) { table ->
                    val isSelected = selectedTable == table
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTable = table },
                        label = { Text(table, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FlameRed,
                            selectedLabelColor = Color.White,
                            containerColor = SurfaceDark,
                            labelColor = OnSurfaceVariantLight
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- FREQUENT CUSTOMER TOKENS DISCOUNT SECTION ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AmberGlow.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.ConfirmationNumber, contentDescription = null, tint = AmberGlow, modifier = Modifier.size(16.dp))
                            Text("Apply Frequent Customer Tokens", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceLight)
                        }
                        Text("Balance: $customerTokens 🎫", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = AmberGlow)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(0, 1, 2, 3, 5).forEach { count ->
                            val isSelected = tokensToApply == count
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.applyTokenDiscount(count) },
                                label = { Text(if (count == 0) "None" else "${count}x (-₹${count * 50})", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AmberGlow,
                                    selectedLabelColor = Color.Black,
                                    containerColor = SurfaceElevated,
                                    labelColor = OnSurfaceLight
                                ),
                                modifier = Modifier.testTag("apply_tokens_$count")
                            )
                        }
                    }
                    Text("Note: Tokens will be permanently deducted upon placing order to prevent scams.", fontSize = 9.sp, color = OnSurfaceVariantLight)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- SPLIT BILL OPTION FOR CUSTOMERS ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, StatusReady.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.CallSplit, contentDescription = null, tint = StatusReady, modifier = Modifier.size(16.dp))
                            Text("Split Bill with Group / Friends", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceLight)
                        }
                        Switch(
                            checked = isSplitActive,
                            onCheckedChange = { viewModel.isSplitBillActive.value = it },
                            modifier = Modifier.testTag("toggle_split_bill")
                        )
                    }

                    if (isSplitActive) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            FilterChip(
                                selected = splitMode == "Equal",
                                onClick = { viewModel.splitMode.value = "Equal" },
                                label = { Text("Equal Split", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StatusReady, selectedLabelColor = Color.White)
                            )
                            FilterChip(
                                selected = splitMode == "Custom",
                                onClick = { viewModel.splitMode.value = "Custom" },
                                label = { Text("Custom Split", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StatusReady, selectedLabelColor = Color.White)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Group Size:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariantLight)
                            IconButton(
                                onClick = {
                                    if (splitPeople > 2) {
                                        viewModel.splitPeopleCount.value = splitPeople - 1
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = "Dec", tint = FlameRed)
                            }
                            Text("$splitPeople people", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = OnSurfaceLight)
                            IconButton(
                                onClick = {
                                    if (splitPeople < 10) {
                                        viewModel.splitPeopleCount.value = splitPeople + 1
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = "Inc", tint = StatusReady)
                            }
                        }

                        if (splitMode == "Custom") {
                            val cartSubtotal = cartItems.sumOf { it.item.price * it.quantity }
                            val offerDisc = if (appliedOffer != null) {
                                when (appliedOffer.type) {
                                    "Percentage" -> (cartSubtotal * (appliedOffer.value / 100.0)).toInt()
                                    "Fixed" -> appliedOffer.value.toInt().coerceAtLeast(cartSubtotal)
                                    else -> (cartSubtotal * 0.15).toInt()
                                }
                            } else 0
                            val tokDisc = tokensToApply * 50
                            val totBill = (cartSubtotal - (offerDisc + tokDisc)).coerceAtLeast(0)

                            LaunchedEffect(splitPeople, totBill) {
                                if (viewModel.customMemberSplits.size != splitPeople) {
                                    viewModel.initCustomSplits(splitPeople, totBill)
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Custom Split Amount Per Person:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberGlow)
                                for (i in 0 until splitPeople) {
                                    val currentVal = if (i < viewModel.customMemberSplits.size) viewModel.customMemberSplits[i] else 0
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(SurfaceElevated, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Person ${i + 1} pays:", fontSize = 11.sp, color = OnSurfaceLight, fontWeight = FontWeight.Bold)
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = { viewModel.updateCustomSplit(i, (currentVal - 20).coerceAtLeast(0)) },
                                                modifier = Modifier.size(22.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = "Dec", tint = FlameRed, modifier = Modifier.size(16.dp))
                                            }
                                            Text("₹$currentVal", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = StatusReady)
                                            IconButton(
                                                onClick = { viewModel.updateCustomSplit(i, currentVal + 20) },
                                                modifier = Modifier.size(22.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = "Inc", tint = StatusReady, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }

                                val customSum = (0 until splitPeople).sumOf { if (it < viewModel.customMemberSplits.size) viewModel.customMemberSplits[it] else 0 }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Custom Sum: ₹$customSum / ₹$totBill", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (customSum == totBill) StatusReady else FlameRed)
                                    if (customSum == totBill) {
                                        Text("✓ Balanced", fontSize = 10.sp, color = StatusReady, fontWeight = FontWeight.ExtraBold)
                                    } else {
                                        Text("Diff: ₹${totBill - customSum}", fontSize = 10.sp, color = FlameRed, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val subtotal = cartItems.sumOf { it.item.price * it.quantity }
            val offerDiscount = if (appliedOffer != null) {
                when (appliedOffer.type) {
                    "Percentage" -> (subtotal * (appliedOffer.value / 100.0)).toInt()
                    "Fixed" -> appliedOffer.value.toInt().coerceAtMost(subtotal)
                    else -> (subtotal * 0.15).toInt()
                }
            } else 0
            val tokenDiscount = tokensToApply * 50
            val totalDiscount = offerDiscount + tokenDiscount
            val finalTotal = (subtotal - totalDiscount).coerceAtLeast(0)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal Amount:", fontSize = 12.sp, color = OnSurfaceVariantLight)
                    Text("₹$subtotal", fontSize = 12.sp, color = OnSurfaceLight, fontWeight = FontWeight.Bold)
                }

                if (appliedOffer != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Coupon (${appliedOffer.code}):", fontSize = 11.sp, color = StatusReady, fontWeight = FontWeight.Bold)
                            Text(" [Remove]", fontSize = 10.sp, color = FlameRed, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onRemoveOffer() })
                        }
                        Text("-₹$offerDiscount", fontSize = 12.sp, color = StatusReady, fontWeight = FontWeight.Bold)
                    }
                }

                if (tokenDiscount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Token Discount (${tokensToApply}x):", fontSize = 11.sp, color = AmberGlow, fontWeight = FontWeight.Bold)
                        Text("-₹$tokenDiscount", fontSize = 12.sp, color = AmberGlow, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Final Total:", fontWeight = FontWeight.Bold, color = OnSurfaceLight, fontSize = 14.sp)
                    Text(
                        "₹$finalTotal",
                        fontWeight = FontWeight.ExtraBold,
                        color = AmberGlow,
                        fontSize = 17.sp
                    )
                }

                if (isSplitActive) {
                    val perPerson = finalTotal / splitPeople.coerceAtLeast(1)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Per Person Share ($splitPeople people):", fontWeight = FontWeight.Bold, color = StatusReady, fontSize = 11.sp)
                        Text("₹$perPerson / person", fontWeight = FontWeight.ExtraBold, color = StatusReady, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { onCheckout(selectedTable) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("place_order_button"),
                colors = ButtonDefaults.buttonColors(containerColor = FlameRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Confirm & Send to Kitchen ($selectedTable)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// Extension to cap size for lazy lists inside bottom sheet
private fun Modifier.maxHeight(maxHeight: androidx.compose.ui.unit.Dp): Modifier = this.then(
    Modifier.heightIn(max = maxHeight)
)

@Composable
fun AdminOfferCardItem(
    offer: OfferTemplate,
    isApplied: Boolean,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .appleLiquidGlass(cornerRadius = 16),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Type Tag & Active Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = FlameRed.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, FlameRed.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = offer.type.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FlameRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (offer.isActive) StatusReady else OnSurfaceVariantLight)
                    )
                    Text(
                        text = if (offer.isActive) "LIVE OFFER" else "DISABLED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (offer.isActive) StatusReady else OnSurfaceVariantLight
                    )
                }
            }

            // Coupon Code Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .border(1.dp, AmberGlow.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = "Coupon",
                        tint = AmberGlow,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = offer.code,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmberGlow,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Title
            Text(
                text = offer.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceLight,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Value Callout
            val valueText = when (offer.type) {
                "Percentage" -> "${offer.value.toInt()}% DISCOUNT"
                "Fixed" -> "FLAT ₹${offer.value.toInt()} OFF"
                "Combo" -> "SPECIAL ₹${offer.value.toInt()} COMBO"
                else -> "SPECIAL ${offer.value.toInt()}% OFF"
            }
            Text(
                text = valueText,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FlameRed
            )

            // Action Button
            Button(
                onClick = onApply,
                enabled = offer.isActive,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isApplied) StatusReady else FlameRed,
                    disabledContainerColor = SurfaceContainerHighest
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .testTag("apply_offer_${offer.code}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isApplied) Icons.Default.CheckCircle else Icons.Default.LocalOffer,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isApplied) "APPLIED ✓" else "APPLY COUPON",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
