package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.RestaurantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryPartnerScreen(
    viewModel: RestaurantViewModel,
    innerPadding: PaddingValues
) {
    var selectedDeliveryId by remember { mutableStateOf<Int?>(145) } // Default active mock delivery #145
    val supportMessages by viewModel.supportMessages.collectAsState()
    var chatText by remember { mutableStateOf("") }
    val driverOtpInput by viewModel.driverOtpInput
    val deliveryOtp = viewModel.deliveryOtp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .padding(top = innerPadding.calculateTopPadding(), bottom = 72.dp)
    ) {
        // Delivery Panel Header
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
                            .background(AmberGlow.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeliveryDining,
                            contentDescription = "Delivery Partner",
                            tint = AmberGlow,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "DELIVERY PORTAL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = AmberGlow,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Driver: Amit Singh (Active)",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariantLight,
                            fontSize = 10.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(StatusReady.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ONLINE",
                        color = StatusReady,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Task List Header
            item {
                Text(
                    text = "Your Assigned Deliveries",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceLight
                )
            }

            // Mock Delivery Job Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .appleLiquidGlass(cornerRadius = 14)
                        .border(
                            1.dp,
                            if (selectedDeliveryId == 145) AmberGlow else Color.Transparent,
                            RoundedCornerShape(14.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(14.dp),
                    onClick = { selectedDeliveryId = 145 }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Job #145 • Krishna Chicken",
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceLight,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "To: Sanjeev (Flat 401, Hitech City)",
                                    fontSize = 12.sp,
                                    color = OnSurfaceVariantLight
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(FlameRed.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "READY FOR PICKUP",
                                    color = FlameRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column {
                                    Text("Distance", fontSize = 10.sp, color = OnSurfaceVariantLight)
                                    Text("2.4 KM", fontWeight = FontWeight.Bold, color = OnSurfaceLight, fontSize = 12.sp)
                                }
                                Column {
                                    Text("Est. Time", fontSize = 10.sp, color = OnSurfaceVariantLight)
                                    Text("12 Mins", fontWeight = FontWeight.Bold, color = OnSurfaceLight, fontSize = 12.sp)
                                }
                                Column {
                                    Text("Earning", fontSize = 10.sp, color = OnSurfaceVariantLight)
                                    Text("₹60.00", fontWeight = FontWeight.Bold, color = StatusReady, fontSize = 12.sp)
                                }
                            }

                            Button(
                                onClick = { viewModel.showToastMessage("Delivery Accepted! Routing map updated.") },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberGlow),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Accept Job", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (selectedDeliveryId == 145) {
                // Simulated Google Maps Integration Block
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .appleLiquidGlass(cornerRadius = 14),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Map Navigation",
                                    tint = AmberGlow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Simulated GPS Google Maps Routing",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = OnSurfaceLight
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Grid representation of maps simulation
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceDark)
                                    .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = "Navigation Arrow",
                                        tint = StatusReady,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "In transit to Hitech City Flat 401...",
                                        fontSize = 11.sp,
                                        color = OnSurfaceVariantLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Radius: Within 5.0 KM limit",
                                        fontSize = 9.sp,
                                        color = AmberGlow
                                    )
                                }
                            }
                        }
                    }
                }

                // Customer OTP Delivery Verification Process
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, StatusReady.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Secure OTP Delivery Completion",
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceLight,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Ask the customer for the 4-digit code shown in their app to mark delivered safely.",
                                fontSize = 11.sp,
                                color = OnSurfaceVariantLight,
                                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = driverOtpInput,
                                    onValueChange = { viewModel.driverOtpInput.value = it },
                                    placeholder = { Text("Enter 4-Digit OTP", fontSize = 12.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = OnSurfaceLight,
                                        unfocusedTextColor = OnSurfaceLight,
                                        focusedContainerColor = SurfaceDark,
                                        unfocusedContainerColor = SurfaceDark,
                                        focusedBorderColor = StatusReady,
                                        unfocusedBorderColor = SurfaceContainerHighest
                                    ),
                                    singleLine = true
                                )

                                Button(
                                    onClick = {
                                        if (driverOtpInput == deliveryOtp) {
                                            viewModel.showToastMessage("OTP Verified! Order marked DELIVERED successfully.")
                                            viewModel.driverOtpInput.value = ""
                                        } else {
                                            viewModel.showToastMessage("Incorrect OTP! Customer has OTP: $deliveryOtp")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusReady),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(52.dp)
                                ) {
                                    Text("Verify", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                // Chat Support Pane (Customer Privacy Protected)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = "Chat",
                                        tint = FlameRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Privacy Protected Chat Support",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = OnSurfaceLight
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(SurfaceDark, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "No Phone Sharing",
                                        color = OnSurfaceVariantLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Chat Area
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceDark)
                                    .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(supportMessages) { msg ->
                                        val isDriver = msg.sender == "Driver"
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = if (isDriver) Alignment.End else Alignment.Start
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (isDriver) FlameRed.copy(alpha = 0.2f) else SurfaceContainerHighest)
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = "${msg.sender}: ${msg.message}",
                                                    color = OnSurfaceLight,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Text(
                                                text = msg.timestamp,
                                                fontSize = 8.sp,
                                                color = OnSurfaceVariantLight,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = chatText,
                                    onValueChange = { chatText = it },
                                    placeholder = { Text("Type private message...", fontSize = 12.sp) },
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
                                        if (chatText.isNotBlank()) {
                                            viewModel.sendSupportMessage("Driver", chatText)
                                            chatText = ""
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
                }
            }
        }
    }
}
