package com.example.model

enum class OrderStatus {
    PENDING, ACCEPTED, COOKING, READY, SERVED, OUT_FOR_DELIVERY, DELIVERED, COMPLETED, CANCELLED
}

enum class LogType {
    ORDER, DELIVERY, WARNING, INFO
}

data class MenuItem(
    val id: String,
    val name: String,
    val category: String, // e.g. "Chicken", "Biryani", "Fry Piece", "Curry", "Starters", "Beverages"
    val price: Int, // in Rupees
    val rating: Double,
    val reviewsCount: String, // e.g. "2.4k reviews"
    val prepTime: String, // e.g. "25-30 min"
    val imageUrl: String,
    val description: String = "",
    val originalPrice: Int? = null, // for showing discount badges!
    val bestSeller: Boolean = false
)

data class CartItem(
    val item: MenuItem,
    val quantity: Int,
    val specialNotes: List<String> = emptyList()
)

data class SplitMember(
    val id: String,
    val name: String,
    val amount: Int,
    var isPaid: Boolean = false
)

data class SplitBillInfo(
    val splitMode: String, // "Equal" or "Custom"
    val numberOfPeople: Int,
    val perPersonAmount: Int,
    val members: List<SplitMember> = emptyList()
)

data class Order(
    val id: Int,
    val tableNumber: String, // e.g., "Table 5", "Table 12", "Takeaway"
    val items: List<CartItem>,
    val status: OrderStatus,
    val elapsedSeconds: Int = 0,
    val specialNotes: List<String> = emptyList(),
    val cancellationRequested: Boolean = false,
    val cancellationReason: String? = null,
    val cancellationDeniedReason: String? = null,
    val tokensUsedCount: Int = 0,
    val tokenDiscountAmount: Int = 0,
    val splitBillInfo: SplitBillInfo? = null,
    val customerType: String = "Internal Customer (Dine-in)",
    val assignedWaiterName: String? = null
) {
    val totalValue: Int
        get() = (items.sumOf { it.item.price * it.quantity } - tokenDiscountAmount).coerceAtLeast(0)
}

data class TableState(
    val id: String, // e.g. "04", "07", "12", "01", "09", "02", "03", "05", "06", "08"
    val status: String, // "Occupied", "New Order", "Ready", "Cooking", "Empty"
    val itemsCount: Int = 0,
    val elapsedMinutes: Int = 0,
    val currentOrderId: Int? = null,
    val totalAmount: Double = 0.0
)

data class ActivityLog(
    val id: String,
    val message: String,
    val detail: String,
    val timestamp: String, // e.g. "2 mins ago"
    val type: LogType,
    val value: String = "" // e.g. "₹840" or "--"
)

// New Data Models for complete restaurant management
data class ExtraRequest(
    val id: String,
    val orderId: Int,
    val tableNumber: String,
    val description: String, // e.g., "Extra pieces", "Extra cheese topping"
    var status: String = "Pending" // "Pending", "Accepted", "Rejected"
)

enum class StaffRole {
    WAITER, KITCHEN, DELIVERY
}

enum class StaffStatus {
    ACTIVE, OFFLINE, BUSY
}

data class StaffMember(
    val id: String,
    val name: String,
    val role: StaffRole,
    val status: StaffStatus,
    val ordersHandled: Int,
    val completionRate: Double, // e.g. 98.5%
    val revenueContribution: Double // e.g. 15400.0
)

data class CustomerFeedback(
    val id: String,
    val customerName: String,
    val rating: Double,
    val review: String,
    val type: String, // "Food", "Delivery", "Overall"
    val dishName: String? = null,
    val timestamp: String
)

data class SupportMessage(
    val id: String,
    val sender: String, // "Customer", "Support", "Driver"
    val message: String,
    val timestamp: String
)

data class OfferTemplate(
    val id: String,
    val code: String,
    val title: String,
    val type: String, // "Percentage", "Fixed", "Combo", "Festival"
    val value: Double,
    val isActive: Boolean
)

