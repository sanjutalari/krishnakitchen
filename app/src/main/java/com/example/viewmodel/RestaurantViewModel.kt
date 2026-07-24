package com.example.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class RestaurantViewModel : ViewModel() {

    // Central System States
    var currentPersona = mutableStateOf("Admin") // "Customer", "Kitchen Staff", "Waiter Staff", "Delivery Partner", "Admin"
    var currentPerspective = mutableStateOf("Customer") // "Customer", "Kitchen", "Waiter", "Analytics"
    var currentCustomerTab = mutableStateOf("Menu") // "Menu", "Cart", "Orders", "Profile"

    // Search & Category Filters
    var searchQuery = mutableStateOf("")
    var selectedCategory = mutableStateOf("All")

    // Dynamic Live Income State
    private val _todayIncome = MutableStateFlow(84250.0)
    val todayIncome: StateFlow<Double> = _todayIncome.asStateFlow()
    val targetIncome = 100000.0

    // Admin advisor actions state
    var curryDiscountActive = mutableStateOf(false)
    var dinnerForTwoActive = mutableStateOf(false)

    // Complete ordering modes & QR code simulations
    var orderingMode = mutableStateOf("Dine-In") // "Dine-In", "Delivery"
    var qrCodeTable = mutableStateOf("12") // Auto-detected Table 12 when Dine-In
    var customerLoggedIn = mutableStateOf(true) // Simulating logged-in customer
    var customerAddress = mutableStateOf("Flat 302, Green Meadows, Hitech City, Hyderabad")
    var gpsAllowed = mutableStateOf(true)
    var deliveryRadius = mutableStateOf(5.0) // Configurable: 5.0, 10.0, 15.0 KM
    var deliveryCharge = mutableStateOf(40.0)
    var freeDeliveryThreshold = mutableStateOf(500.0)
    var driverOtpInput = mutableStateOf("")
    var deliveryOtp = "4821" // The simulated OTP the driver needs to enter to complete delivery

    // Extra pieces and toppings requests with separate approval flow
    private val _extraRequests = MutableStateFlow<List<ExtraRequest>>(emptyList())
    val extraRequests: StateFlow<List<ExtraRequest>> = _extraRequests.asStateFlow()

    // Staff Management
    private val _staffMembers = MutableStateFlow<List<StaffMember>>(emptyList())
    val staffMembers: StateFlow<List<StaffMember>> = _staffMembers.asStateFlow()

    // Customer Feedback & Reviews for analytics
    private val _customerFeedback = MutableStateFlow<List<CustomerFeedback>>(emptyList())
    val customerFeedback: StateFlow<List<CustomerFeedback>> = _customerFeedback.asStateFlow()

    // Support Chat
    private val _supportMessages = MutableStateFlow<List<SupportMessage>>(emptyList())
    val supportMessages: StateFlow<List<SupportMessage>> = _supportMessages.asStateFlow()

    // Offer Templates
    private val _offers = MutableStateFlow<List<OfferTemplate>>(emptyList())
    val offers: StateFlow<List<OfferTemplate>> = _offers.asStateFlow()

    private val _appliedOffer = MutableStateFlow<OfferTemplate?>(null)
    val appliedOffer: StateFlow<OfferTemplate?> = _appliedOffer.asStateFlow()

    // Frequent Customer Tokens & Verification (Anti-Scam disposable token wallet)
    var customerName = mutableStateOf("Sanjeev Verma")
    var customerPhone = mutableStateOf("+91 98765 43210")
    var isRegisteredCustomer = mutableStateOf(true)
    private val _customerTokens = MutableStateFlow(20) // Frequent customer starts with 20 tokens
    val customerTokens: StateFlow<Int> = _customerTokens.asStateFlow()
    private val _customerVisits = MutableStateFlow(12)
    val customerVisits: StateFlow<Int> = _customerVisits.asStateFlow()

    var tokensToApply = mutableStateOf(0) // Tokens selected for current order discount

    // Split Bill State
    var isSplitBillActive = mutableStateOf(false)
    var splitPeopleCount = mutableStateOf(2)
    var splitMode = mutableStateOf("Equal") // "Equal" or "Custom"
    val customMemberSplits = mutableStateListOf<Int>()

    // Billing preview slips
    var selectedBillingOrder = mutableStateOf<Order?>(null)
    var printerPaperSize = mutableStateOf(80) // 58 or 80 mm

    // Interactive Notification Banner
    var currentToast = mutableStateOf<String?>(null)

    // Admin Quick Broadcast Note to Customer
    var quickCustomerNote = mutableStateOf("Fresh hand-slaughtered premium chicken delivered directly from farm to table. Try our signature crispy fried pieces today!")

    // Shared list of Menu Items
    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

    // Shared list of Cart Items
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    // Shared list of Active Kitchen Orders
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    // Shared list of Physical Tables
    private val _tables = MutableStateFlow<List<TableState>>(emptyList())
    val tables: StateFlow<List<TableState>> = _tables.asStateFlow()

    // Shared list of Activity Logs
    private val _logs = MutableStateFlow<List<ActivityLog>>(emptyList())
    val logs: StateFlow<List<ActivityLog>> = _logs.asStateFlow()

    // Background timer job
    private var timerJob: Job? = null

    init {
        initializeData()
        startTimerLoop()
    }

    private fun initializeData() {
        // Build mock menu items
        val items = listOf(
            MenuItem(
                id = "1",
                name = "Special Chicken Biryani",
                category = "Biryani",
                price = 299,
                rating = 4.5,
                reviewsCount = "2.4k reviews",
                prepTime = "25-30 min",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD0QjS8egxIBvW2UUlIx5pUw5psOEuBvm5fC36XB3hVk0aE1qOC_X0kHxy1dekoDCAcmU3kFtYjAW0nNU-BDX_aohLYZDYAUw9U9IqI4TIzJiE_QW74omVWce9GjySN13UZdgu3GJ4yE_wzdRySjJbWvYpPvDkR8EnFvDeuOj5jXPFJD4B9GspZwBinFrOc43PlH4nf6CvESALq82TNoJVO9awdDtaP7pfe0is5VjsUF2UPMNSIVNeIrg",
                description = "Authentic rich basmati rice layered with juicy cooked spices, hard-boiled egg and saffron.",
                bestSeller = true
            ),
            MenuItem(
                id = "2",
                name = "Crispy Fry Piece",
                category = "Fry Piece",
                price = 249,
                rating = 4.8,
                reviewsCount = "1.2k reviews",
                prepTime = "20-25 min",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA7nAqdLnECzRRAR9BqoTRIkSz43PLoKtj-k_4rsEhqYN8wfFWETqfhjegpNO3r-A6wHoZ3W2CJN8oP0wAOWVT33x2CsADCyT4ab8Eov-LbEzZ_UKhu9PRfO0OHiVBQmrO8__v9-OS-6BSpay7Xt-XjuFrzSsMMTrcMP2HIeCF4WLOYWRznqGh5qkPkfmyB3fEuQGcyk0jsKqEePa68SfQfmm84RGqeRwWb9xQIXLTEDRAqyzuxCt20WA",
                description = "Crispy Andhra-style dry chicken fry marinated in traditional fiery southern spices."
            ),
            MenuItem(
                id = "3",
                name = "Butter Chicken Curry",
                category = "Curry",
                price = 349,
                rating = 4.7,
                reviewsCount = "980 reviews",
                prepTime = "30-35 min",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCKX_O9qTebdzpoHXcmk1Sr4PD1VAooKXa43QXiqmbzR27CvFo8M-5-PfZ9Ffg5aZBlX5HS6ICXepMChh1knuUP7Vl28nXI9Qu9ucJ-rYvo4BtgzKYCkaOh4WtufwkRB1BKR8TAlRp6x8eaDGGhUf6OA2d2NPHkVdsUDRMfbsmQS-2TS2NoPJ6GKAwstHF1Ve3GUXTQrvwAeR5xiAFXhsAWM2z82bOrsdcKvCMLKXttpUmQw0wT_ctiVQ",
                description = "Gourmet succulent boneless pieces cooked in premium silky tomato cream butter gravy."
            ),
            MenuItem(
                id = "4",
                name = "Chicken Lollipops",
                category = "Chicken",
                price = 279,
                rating = 4.6,
                reviewsCount = "1.5k reviews",
                prepTime = "15-20 min",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDGtrZA0ISAXezOGTkUafO2BuhOHLYZqyd-9AwlnTpWub8c0_M6TD7tzNQ573h43MJe1YATrzEmPeO9p9Ctk6QzdliHbCwIk7Z2ycQoLjhRONxQACajhXYIno_xAvrZSwTqBE-y7bS0K0Z6WUNnEcP5-bNShKeQnELyhEoqUKd1ifJTGTmxIe2Yg5PjuuUMhMkC9baxLkf6tTUh-_-seZtacbZOu5LNQaqkQKmPOBnl4DSseQNQ3V21Bw",
                description = "Crispy red drumsticks tossed with soy-garlic seasoning, served with extra mint sauce."
            ),
            MenuItem(
                id = "5",
                name = "Full Tandoori Chicken",
                category = "Chicken",
                price = 499,
                rating = 4.9,
                reviewsCount = "3.1k reviews",
                prepTime = "35-40 min",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD1tR2YD5CLpPy-NspInlkEC8y-pS7Ppd6JVWEXiWdf_rocsHz5cTp8st2JrQyZ1bRAb8ynNcSymlHCM854ugAOwy3LpwDE-ZmmrN51CuPJBCRH4XozgSqOZh_oJsRgF1iObewXB8fphcm0Ntym1vBTb9fdPyyBZszaRcFABBWfgvA9yv3eBnNTnLyNHJ6gbOYMGFwoRnfdrH6wEaUSgf_wBZSPdX1sis-4oC6ZcQXJ_0LwCwoc2EB0GQ",
                description = "Whole skinless chicken tandoori charcoal skewered in clay oven with robust yoghurt spices."
            ),
            MenuItem(
                id = "6",
                name = "Butter Naan",
                category = "Starters",
                price = 45,
                rating = 4.4,
                reviewsCount = "4.2k reviews",
                prepTime = "5-8 min",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAdZoM_mQ5MvlPa-4Uv9gZ973uXPlp7lgMls2W6-GVlL32JJUfKOduPtcdMb-bINof1xpJ-0JdW3PxPUEjl83b00pudfChCAIOM5DyLzXQXf4NiQBKi2Eet7KBRLacP1ORLH0DtdcRthB0GDsVQArhtta_nQqNEdYLkN2ln2_tS8AUgaeMgW_qxfaI-oUsC8oKswxc5BbvBTioFVpEFUfO01I9FRtomF5SCmy2x1UpGNnaY8BVapP96WA",
                description = "Leavened flatbread freshly slapped in traditional tandoor, loaded with butter."
            ),
            MenuItem(
                id = "7",
                name = "Lemon Mint Mojito",
                category = "Beverages",
                price = 129,
                rating = 4.3,
                reviewsCount = "720 reviews",
                prepTime = "3-5 min",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDUwyPoVd4NYsMGgT9i7MSIi6lwLVEWfOnzfEnYmAe8JqFtfN3G4wZPl3ojf-PE1bmQFholUKX2k9HdgQ1KcDVJNP9uUVmZ3POJ-xo0I7_FmtNheFhaQnJP-qNua_Z_pB6XxSW2iFu5l58FGnzLkOQW3brm-K_g6nSlMTlmhASxmd-nBXpu412P2jl3J_o5p4lVpR1ga5mbUTx2l4x2aHi4L2h3YHt0vDFaa59NQvT_RIIYAZrZzcGNrw",
                description = "Refreshing combination of ice cubes, crushed fresh mint leaves, lime juice, sparkling soda."
            ),
            MenuItem(
                id = "8",
                name = "Thums Up",
                category = "Beverages",
                price = 40,
                rating = 4.5,
                reviewsCount = "8.1k reviews",
                prepTime = "1-2 min",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD0QjS8egxIBvW2UUlIx5pUw5psOEuBvm5fC36XB3hVk0aE1qOC_X0kHxy1dekoDCAcmU3kFtYjAW0nNU-BDX_aohLYZDYAUw9U9IqI4TIzJiE_QW74omVWce9GjySN13UZdgu3GJ4yE_wzdRySjJbWvYpPvDkR8EnFvDeuOj5jXPFJD4B9GspZwBinFrOc43PlH4nf6CvESALq82TNoJVO9awdDtaP7pfe0is5VjsUF2UPMNSIVNeIrg",
                description = "Chilled spicy cola from India, carrying high carbonation and refreshment."
            ),
            MenuItem(
                id = "9",
                name = "Fry Piece + Coke Combo",
                category = "Starters",
                price = 299,
                rating = 4.8,
                reviewsCount = "5.4k reviews",
                prepTime = "15-20 min",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuC1PDMft4SRMJtlFK6UpYg6YCvkKddnmwT7ce2WWRZ5yNqUTdOAi1_JDQbvGpvNM3xUCRdpAb2nBf9sEdhc06VnJ1grnz7AXrhxAW9-CD5bPpKUICojvUHCEPn51hibkU8gO-jH1gxZw9Go8qXdZYTZW9EH98oA7sukpM3tziWyMnveGVjeBNX-_42H5fWjGzbn6lZuHMR-y788ROvLkU53JpdxQfnSooU12M3StWxZuDuw2MVwIUyXFA",
                description = "Southern spicy crispy fry piece drumsticks paired with cold carbonated Thums Up."
            ),
            MenuItem(
                id = "10",
                name = "Roti & Curry Meal",
                category = "Curry",
                price = 249,
                rating = 4.7,
                reviewsCount = "2.8k reviews",
                prepTime = "20-25 min",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAIwY77RVENYC3-lo6DY-YJw9j33qxcfWm3rd6fKjfVW1S2ZBsrpdX9JdCxI6xA8B6raJVpt5jg_bxVxm1TBVTc_Cidw7GQjZDixSlVHOAWPBfbordlOA9URFiXXQBkU2Ai186lgU53qlN1n1mN0XIg3zcGHeHQnKaFEDPOWf1heOOWTD18zKeoFiBe_vA5vfSOCWV0_1cfSWitRtiu5japs9E4rAD3AhsHnFnA0gFmdl_KJlAxkNO3sg",
                description = "Traditional wholesome platter loaded with soft home-style rotis and spicy curried chicken gravy."
            )
        )
        _menuItems.value = items

        // Prepopulate Kitchen queue orders
        _orders.value = listOf(
            Order(
                id = 122,
                tableNumber = "Table 4",
                status = OrderStatus.PENDING,
                elapsedSeconds = 45,
                specialNotes = listOf("Less Oil", "Crispy Piece"),
                customerType = "Internal Customer (Table 4)",
                assignedWaiterName = null,
                items = listOf(
                    CartItem(items[0], 1, listOf("Crispy Piece")),
                    CartItem(items[1], 1)
                )
            ),
            Order(
                id = 123,
                tableNumber = "Home Delivery",
                status = OrderStatus.PENDING,
                elapsedSeconds = 20,
                specialNotes = listOf("Pack extra salad"),
                customerType = "External Customer (Home Delivery)",
                assignedWaiterName = null,
                items = listOf(
                    CartItem(items[2], 2, listOf("Pack extra salad")),
                    CartItem(items[7], 2)
                )
            ),
            Order(
                id = 124,
                tableNumber = "Table 5",
                status = OrderStatus.ACCEPTED,
                elapsedSeconds = 165,
                specialNotes = listOf("No Onion", "Extra Spicy"),
                customerType = "Internal Customer (Table 5)",
                assignedWaiterName = "Rahul Kumar",
                items = listOf(
                    CartItem(items[0], 2, listOf("No Onion", "Extra Spicy")),
                    CartItem(items[7], 1)
                )
            ),
            Order(
                id = 125,
                tableNumber = "Takeaway",
                status = OrderStatus.ACCEPTED,
                elapsedSeconds = 30,
                specialNotes = listOf("Extra Chutney"),
                customerType = "External Customer (Takeaway)",
                assignedWaiterName = "Amit Singh",
                items = listOf(
                    CartItem(items[3], 4, listOf("Extra Chutney"))
                )
            ),
            Order(
                id = 126,
                tableNumber = "Table 12",
                status = OrderStatus.ACCEPTED,
                elapsedSeconds = 312,
                customerType = "Internal Customer (Table 12)",
                assignedWaiterName = "Anjali Roy",
                items = listOf(
                    CartItem(items[4], 1),
                    CartItem(items[5], 2)
                )
            ),
            Order(
                id = 127,
                tableNumber = "Table 2",
                status = OrderStatus.COOKING,
                elapsedSeconds = 525,
                specialNotes = listOf("Mild Spice"),
                customerType = "Internal Customer (Table 2)",
                assignedWaiterName = "Rahul Kumar",
                items = listOf(
                    CartItem(items[2], 1, listOf("Mild Spice"))
                )
            )
        )

        // Prepopulate Waiter dashboard tables
        _tables.value = listOf(
            TableState("04", "Occupied", 3, 12, 128, 45.80),
            TableState("07", "New Order", 0, 0, null, 0.0),
            TableState("12", "Cooking", 3, 5, 126, 589.0),
            TableState("01", "Occupied", 5, 18, 129, 940.0),
            TableState("09", "Cooking", 1, 8, 130, 249.0),
            TableState("02", "Empty"),
            TableState("03", "Empty"),
            TableState("05", "Occupied", 3, 2, 124, 638.0),
            TableState("06", "Empty"),
            TableState("08", "Empty")
        )

        // Prepopulate Admin log items
        _logs.value = listOf(
            ActivityLog(
                id = UUID.randomUUID().toString(),
                message = "New order placed by Table 12",
                detail = "2x Butter Naan, 1x Tandoori Chicken",
                timestamp = "2 mins ago",
                type = LogType.ORDER,
                value = "₹589"
            ),
            ActivityLog(
                id = UUID.randomUUID().toString(),
                message = "Delivery #45 completed",
                detail = "To: 4th Block, Koramangala",
                timestamp = "15 mins ago",
                type = LogType.DELIVERY,
                value = "₹1,250"
            ),
            ActivityLog(
                id = UUID.randomUUID().toString(),
                message = "Stock Warning: Leg Pieces",
                detail = "Inventory level below 15%",
                timestamp = "45 mins ago",
                type = LogType.WARNING,
                value = "--"
            )
        )

        // Prepopulate extra requests
        _extraRequests.value = listOf(
            ExtraRequest("req1", 124, "Table 5", "Extra pieces of Special Chicken (2x)", "Pending"),
            ExtraRequest("req2", 126, "Table 12", "Extra cheese toppings (1x)", "Pending")
        )

        // Prepopulate staff members
        _staffMembers.value = listOf(
            StaffMember("staff1", "Rahul Kumar", StaffRole.WAITER, StaffStatus.ACTIVE, 34, 98.2, 14250.0),
            StaffMember("staff2", "Anjali Roy", StaffRole.WAITER, StaffStatus.BUSY, 21, 95.0, 9120.0),
            StaffMember("staff3", "Chef Rajesh", StaffRole.KITCHEN, StaffStatus.ACTIVE, 128, 99.1, 48250.0),
            StaffMember("staff4", "Chef Vikram", StaffRole.KITCHEN, StaffStatus.ACTIVE, 95, 96.5, 34500.0),
            StaffMember("staff5", "Amit Singh", StaffRole.DELIVERY, StaffStatus.ACTIVE, 45, 97.8, 18450.0),
            StaffMember("staff6", "Vikas Yadav", StaffRole.DELIVERY, StaffStatus.BUSY, 32, 94.2, 12800.0),
            StaffMember("staff7", "Suresh Rao", StaffRole.DELIVERY, StaffStatus.OFFLINE, 18, 90.5, 6800.0)
        )

        // Prepopulate customer feedback with text supporting quantitative and qualitative analysis
        _customerFeedback.value = listOf(
            CustomerFeedback("fb1", "Sanjeev", 5.0, "The Biryani was outstanding! Perfect amount of spice and delicious flavor. Chicken was super tender.", "Food", "Special Chicken Biryani", "10 mins ago"),
            CustomerFeedback("fb2", "Kiran", 4.0, "Great Crispy Fry Piece, but the portion size was slightly small. Suggestions for increasing dish quantity.", "Food", "Crispy Fry Piece", "1 hr ago"),
            CustomerFeedback("fb3", "Madhuri", 3.0, "The curry had too much salt and was a bit too oily today. Suggestion for improvements: reduce oil.", "Food", "Butter Chicken Curry", "3 hrs ago"),
            CustomerFeedback("fb4", "Anoop", 5.0, "Delivery was super fast, and the food was piping hot! 10/10 service.", "Delivery", null, "1 day ago"),
            CustomerFeedback("fb5", "Deepak", 4.0, "The tandoori was cooked well, but maybe add some more garnishing and green chutney.", "Food", "Full Tandoori Chicken", "2 days ago")
        )

        // Prepopulate support messages
        _supportMessages.value = listOf(
            SupportMessage("m1", "Support", "Hello! How can we assist you with your Krishna Chicken order today?", "10:30 AM"),
            SupportMessage("m2", "Customer", "Is the delivery driver already on his way? The app says cooked.", "10:31 AM"),
            SupportMessage("m3", "Support", "Yes! Driver Amit Singh has picked up your order and is heading towards your pinned location. ETA is 12 mins.", "10:32 AM")
        )

        // Prepopulate offers
        _offers.value = listOf(
            OfferTemplate("off1", "KRISHNA50", "50% Off First Dine-in/Delivery", "Percentage", 50.0, true),
            OfferTemplate("off2", "WEEKEND100", "Flat ₹100 Off on orders above ₹600", "Fixed", 100.0, false),
            OfferTemplate("off3", "DINNER2", "Dinner for Two Combo Offer", "Combo", 449.0, true),
            OfferTemplate("off4", "MONSOON15", "15% Curry Discount Offer", "Festival", 15.0, false)
        )
    }

    private fun startTimerLoop() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                // Increment timers in orders
                _orders.update { currentOrders ->
                    currentOrders.map { order ->
                        order.copy(elapsedSeconds = order.elapsedSeconds + 1)
                    }
                }
                // Increment timers in tables
                _tables.update { currentTables ->
                    currentTables.map { table ->
                        if (table.status == "Occupied" || table.status == "Cooking" || table.status == "Ready") {
                            // Update minutes every 60s
                            val currentOrderId = table.currentOrderId
                            val order = _orders.value.find { it.id == currentOrderId }
                            if (order != null) {
                                table.copy(elapsedMinutes = order.elapsedSeconds / 60)
                            } else {
                                table
                            }
                        } else {
                            table
                        }
                    }
                }
            }
        }
    }

    fun showToastMessage(msg: String) {
        viewModelScope.launch {
            currentToast.value = msg
            delay(2500)
            if (currentToast.value == msg) {
                currentToast.value = null
            }
        }
    }

    // --- CUSTOMER PERSPECTIVE OPERATIONS ---
    fun setCategory(category: String) {
        selectedCategory.value = category
    }

    fun setSearch(query: String) {
        searchQuery.value = query
    }

    fun addToCart(item: MenuItem, specNote: String? = null) {
        _cart.update { currentCart ->
            val existingIndex = currentCart.indexOfFirst { it.item.id == item.id }
            if (existingIndex >= 0) {
                val currentItem = currentCart[existingIndex]
                val updatedNotes = if (specNote != null && !currentItem.specialNotes.contains(specNote)) {
                    currentItem.specialNotes + specNote
                } else currentItem.specialNotes

                val list = currentCart.toMutableList()
                list[existingIndex] = currentItem.copy(
                    quantity = currentItem.quantity + 1,
                    specialNotes = updatedNotes
                )
                list
            } else {
                val notesList = if (specNote != null) listOf(specNote) else emptyList()
                currentCart + CartItem(item, 1, notesList)
            }
        }
        showToastMessage("Added ${item.name} to Cart")
    }

    fun removeFromCart(item: MenuItem) {
        _cart.update { currentCart ->
            val existingIndex = currentCart.indexOfFirst { it.item.id == item.id }
            if (existingIndex >= 0) {
                val currentItem = currentCart[existingIndex]
                val list = currentCart.toMutableList()
                if (currentItem.quantity > 1) {
                    list[existingIndex] = currentItem.copy(quantity = currentItem.quantity - 1)
                } else {
                    list.removeAt(existingIndex)
                }
                list
            } else {
                currentCart
            }
        }
    }

    fun getCartItemCount(itemId: String): Int {
        return _cart.value.find { it.item.id == itemId }?.quantity ?: 0
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun applyTokenDiscount(count: Int) {
        if (!customerLoggedIn.value || !isRegisteredCustomer.value) {
            showToastMessage("Must be a registered customer to use tokens!")
            return
        }
        val available = _customerTokens.value
        if (count > available) {
            showToastMessage("Insufficient tokens! You currently have $available tokens.")
            return
        }
        tokensToApply.value = count
        if (count > 0) {
            showToastMessage("Applied $count token(s) for ₹${count * 50} discount!")
        } else {
            showToastMessage("Tokens removed from order.")
        }
    }

    fun requestOrderCancellation(orderId: Int, reason: String) {
        val order = _orders.value.find { it.id == orderId }
        if (order == null) {
            showToastMessage("Order #$orderId not found.")
            return
        }
        if (order.elapsedSeconds > 300) { // 5-minute constraint
            showToastMessage("Cancellation window expired (>5 mins limit). Order is already in kitchen prep!")
            return
        }
        if (order.cancellationRequested) {
            showToastMessage("Cancellation request is already pending Admin approval.")
            return
        }

        _orders.update { current ->
            current.map {
                if (it.id == orderId) {
                    it.copy(
                        cancellationRequested = true,
                        cancellationReason = reason,
                        cancellationDeniedReason = null
                    )
                } else it
            }
        }

        _logs.update { curLogs ->
            val log = ActivityLog(
                id = UUID.randomUUID().toString(),
                message = "Cancellation Requested for Order #$orderId",
                detail = "Reason: $reason (Sent to Admin for permission)",
                timestamp = "Just now",
                type = LogType.WARNING,
                value = "Pending"
            )
            listOf(log) + curLogs
        }

        showToastMessage("Cancellation request for Order #$orderId sent to Admin for approval!")
    }

    fun approveOrderCancellation(orderId: Int) {
        _orders.update { current ->
            current.map {
                if (it.id == orderId) {
                    it.copy(status = OrderStatus.CANCELLED, cancellationRequested = false)
                } else it
            }
        }

        val targetOrder = _orders.value.find { it.id == orderId }
        if (targetOrder != null) {
            val cleanTableId = targetOrder.tableNumber.replace("Table ", "").trim().padStart(2, '0')
            _tables.update { cur ->
                cur.map { table ->
                    if (table.currentOrderId == orderId || table.id == cleanTableId) {
                        table.copy(status = "Empty", currentOrderId = null, totalAmount = 0.0, itemsCount = 0)
                    } else table
                }
            }
        }

        _logs.update { curLogs ->
            val log = ActivityLog(
                id = UUID.randomUUID().toString(),
                message = "Admin APPROVED Cancellation for Order #$orderId",
                detail = "Order successfully cancelled and removed from kitchen queue.",
                timestamp = "Just now",
                type = LogType.INFO,
                value = "Cancelled"
            )
            listOf(log) + curLogs
        }

        showToastMessage("Order #$orderId cancellation APPROVED by Admin.")
    }

    fun rejectOrderCancellation(orderId: Int, reason: String = "Kitchen prep underway") {
        _orders.update { current ->
            current.map {
                if (it.id == orderId) {
                    it.copy(cancellationRequested = false, cancellationDeniedReason = reason)
                } else it
            }
        }

        _logs.update { curLogs ->
            val log = ActivityLog(
                id = UUID.randomUUID().toString(),
                message = "Admin REJECTED Cancellation for Order #$orderId",
                detail = "Reason: $reason",
                timestamp = "Just now",
                type = LogType.WARNING,
                value = "Denied"
            )
            listOf(log) + curLogs
        }

        showToastMessage("Order #$orderId cancellation request REJECTED by Admin.")
    }

    fun updateCustomSplit(index: Int, amount: Int) {
        while (customMemberSplits.size <= index) {
            customMemberSplits.add(0)
        }
        customMemberSplits[index] = amount.coerceAtLeast(0)
    }

    fun initCustomSplits(count: Int, totalAmount: Int) {
        val numPeople = count.coerceIn(2, 10)
        splitPeopleCount.value = numPeople
        val equalAmount = if (numPeople > 0) totalAmount / numPeople else 0
        customMemberSplits.clear()
        for (i in 0 until numPeople) {
            customMemberSplits.add(equalAmount)
        }
    }

    fun placeCustomerOrder(tableNum: String = "Table 4") {
        if (_cart.value.isEmpty()) {
            showToastMessage("Your cart is empty!")
            return
        }

        val nextOrderId = (128..999).random()
        val cartItemsCopy = _cart.value.toList()
        val specNotesList = cartItemsCopy.flatMap { it.specialNotes }

        val usedTokens = tokensToApply.value
        val tokenDiscount = usedTokens * 50
        val cartSubtotal = cartItemsCopy.sumOf { it.item.price * it.quantity }

        var splitInfo: SplitBillInfo? = null
        if (isSplitBillActive.value) {
            val finalTotal = (cartSubtotal - tokenDiscount).coerceAtLeast(0)
            val numPeople = splitPeopleCount.value.coerceAtLeast(1)
            val perPerson = finalTotal / numPeople
            val membersList = mutableListOf<SplitMember>()
            if (splitMode.value == "Equal") {
                for (i in 1..numPeople) {
                    membersList.add(SplitMember("p$i", "Member $i", perPerson))
                }
            } else {
                for (i in 0 until numPeople) {
                    val amount = if (i < customMemberSplits.size && customMemberSplits[i] > 0) customMemberSplits[i] else perPerson
                    membersList.add(SplitMember("p${i + 1}", "Member ${i + 1}", amount))
                }
            }
            splitInfo = SplitBillInfo(
                splitMode = splitMode.value,
                numberOfPeople = numPeople,
                perPersonAmount = perPerson,
                members = membersList
            )
        }

        val custType = if (tableNum.startsWith("Table")) "Internal Customer ($tableNum)" else "External Customer ($tableNum)"

        val newOrder = Order(
            id = nextOrderId,
            tableNumber = tableNum,
            status = OrderStatus.PENDING,
            elapsedSeconds = 0,
            specialNotes = specNotesList,
            items = cartItemsCopy,
            tokensUsedCount = usedTokens,
            tokenDiscountAmount = tokenDiscount,
            splitBillInfo = splitInfo,
            customerType = custType,
            assignedWaiterName = null
        )

        // Consume tokens from registered user's wallet (anti-scam: tokens are consumed upon order placement)
        if (usedTokens > 0) {
            _customerTokens.update { (it - usedTokens).coerceAtLeast(0) }
        }

        // Reward frequent visitor with visit count and +1 new token
        _customerVisits.update { it + 1 }
        _customerTokens.update { it + 1 }

        tokensToApply.value = 0
        isSplitBillActive.value = false

        // Add to global orders list
        _orders.update { it + newOrder }

        // Update corresponding physical Table state
        val cleanTableId = tableNum.replace("Table ", "").trim().padStart(2, '0')
        _tables.update { curTables ->
            curTables.map { table ->
                if (table.id == cleanTableId || (cleanTableId == "04" && table.id == "04")) {
                    table.copy(
                        status = "New Order",
                        itemsCount = cartItemsCopy.sumOf { it.quantity },
                        elapsedMinutes = 0,
                        currentOrderId = nextOrderId,
                        totalAmount = newOrder.totalValue.toDouble()
                    )
                } else {
                    table
                }
            }
        }

        // Add to central logs
        _logs.update { curLogs ->
            val log = ActivityLog(
                id = UUID.randomUUID().toString(),
                message = "New order #$nextOrderId placed by $custType",
                detail = "$tableNum ordered ${cartItemsCopy.size} items (Pending Admin Acceptance)" + if (usedTokens > 0) " (Used $usedTokens Tokens)" else "",
                timestamp = "Just now",
                type = LogType.ORDER,
                value = "₹${newOrder.totalValue}"
            )
            listOf(log) + curLogs
        }

        clearCart()
        val tokenMsg = if (usedTokens > 0) " Used $usedTokens Tokens (1 token added for frequent visit!)." else " Earned +1 Token for visiting!"
        showToastMessage("Order #$nextOrderId booked! Waiting for Admin acceptance.$tokenMsg")
        currentCustomerTab.value = "Orders"
    }

    fun acceptCustomerOrder(orderId: Int) {
        val activeWaiters = _staffMembers.value.filter { it.role == StaffRole.WAITER && it.status != StaffStatus.OFFLINE }
        val assignedWaiter = if (activeWaiters.isNotEmpty()) {
            activeWaiters[(0 until activeWaiters.size).random()].name
        } else {
            "Rahul Kumar"
        }

        var custType = ""
        _orders.update { currentOrders ->
            currentOrders.map { order ->
                if (order.id == orderId) {
                    custType = order.customerType
                    order.copy(
                        status = OrderStatus.ACCEPTED,
                        assignedWaiterName = assignedWaiter
                    )
                } else {
                    order
                }
            }
        }

        val order = _orders.value.find { it.id == orderId }
        if (order != null) {
            val cleanTableId = order.tableNumber.replace("Table ", "").trim().padStart(2, '0')
            _tables.update { curTables ->
                curTables.map { table ->
                    if (table.currentOrderId == orderId || table.id == cleanTableId) {
                        table.copy(status = "New Order")
                    } else {
                        table
                    }
                }
            }

            _logs.update { curLogs ->
                val log = ActivityLog(
                    id = UUID.randomUUID().toString(),
                    message = "Admin ACCEPTED Order #$orderId",
                    detail = "$custType -> Sent to Kitchen & Auto-assigned to Waiter $assignedWaiter",
                    timestamp = "Just now",
                    type = LogType.ORDER,
                    value = "₹${order.totalValue}"
                )
                listOf(log) + curLogs
            }

            showToastMessage("Order #$orderId ACCEPTED! Auto-assigned to Waiter $assignedWaiter & sent to Kitchen.")
        }
    }

    fun markOrderServedByWaiter(orderId: Int) {
        val order = _orders.value.find { it.id == orderId }
        val waiterName = order?.assignedWaiterName ?: "Staff"

        _orders.update { currentOrders ->
            currentOrders.map { o ->
                if (o.id == orderId) o.copy(status = OrderStatus.SERVED) else o
            }
        }

        completeAndDeliverOrder(orderId)
        showToastMessage("Order #$orderId marked as SERVED by Waiter $waiterName!")
    }


    // --- KITCHEN PERSPECTIVE OPERATIONS ---
    fun advanceOrderStatus(orderId: Int) {
        var updatedStatus: OrderStatus? = null

        _orders.update { currentOrders ->
            currentOrders.map { order ->
                if (order.id == orderId) {
                    val next = when (order.status) {
                        OrderStatus.ACCEPTED -> OrderStatus.COOKING
                        OrderStatus.COOKING -> OrderStatus.READY
                        OrderStatus.READY -> OrderStatus.SERVED
                        OrderStatus.SERVED -> OrderStatus.SERVED
                        else -> order.status
                    }
                    updatedStatus = next
                    order.copy(status = next)
                } else {
                    order
                }
            }
        }

        // Synchronize state with physical tables
        val order = _orders.value.find { it.id == orderId }
        if (order != null && updatedStatus != null) {
            val cleanTableId = order.tableNumber.replace("Table ", "").trim().padStart(2, '0')
            _tables.update { curTables ->
                curTables.map { table ->
                    if (table.currentOrderId == orderId || table.id == cleanTableId) {
                        val newTableStatus = when (updatedStatus) {
                            OrderStatus.ACCEPTED -> "New Order"
                            OrderStatus.COOKING -> "Cooking"
                            OrderStatus.READY -> "Ready"
                            OrderStatus.SERVED -> "Empty"
                            else -> "Occupied"
                        }
                        if (newTableStatus == "Empty") {
                            table.copy(status = "Empty", itemsCount = 0, elapsedMinutes = 0, currentOrderId = null, totalAmount = 0.0)
                        } else {
                            table.copy(status = newTableStatus)
                        }
                    } else {
                        table
                    }
                }
            }

            // Record completed transition logs
            if (updatedStatus == OrderStatus.SERVED) {
                completeAndDeliverOrder(orderId)
            } else {
                showToastMessage("Order #$orderId advanced to ${updatedStatus!!.name}")
            }
        }
    }


    // --- WAITER PERSPECTIVE OPERATIONS ---
    fun completeAndDeliverOrder(orderId: Int) {
        val order = _orders.value.find { it.id == orderId } ?: return

        // Update central income
        _todayIncome.update { it + order.totalValue }

        // Remove from active kitchen orders
        _orders.update { it.filter { o -> o.id != orderId } }

        // Free corresponding table
        val cleanTableId = order.tableNumber.replace("Table ", "").trim().padStart(2, '0')
        _tables.update { curTables ->
            curTables.map { table ->
                if (table.currentOrderId == orderId || table.id == cleanTableId) {
                    table.copy(status = "Empty", itemsCount = 0, elapsedMinutes = 0, currentOrderId = null, totalAmount = 0.0)
                } else {
                    table
                }
            }
        }

        // Add to delivery log
        _logs.update { curLogs ->
            val log = ActivityLog(
                id = UUID.randomUUID().toString(),
                message = "${order.tableNumber} Served Complete",
                detail = "Order #$orderId cleared and settled.",
                timestamp = "Just now",
                type = LogType.DELIVERY,
                value = "₹${order.totalValue}"
            )
            listOf(log) + curLogs
        }

        showToastMessage("Order #$orderId has been successfully marked as SERVED!")
    }

    fun callKitchenForTable(tableId: String) {
        showToastMessage("Calling kitchen support for Table $tableId...")
        _logs.update { curLogs ->
            val log = ActivityLog(
                id = UUID.randomUUID().toString(),
                message = "Table $tableId requested assistance",
                detail = "Waiter alerted kitchen service.",
                timestamp = "Just now",
                type = LogType.INFO,
                value = "--"
            )
            listOf(log) + curLogs
        }
    }


    // --- ADMIN / ANALYTICS OPERATIONS ---
    fun toggleCurryDiscount() {
        curryDiscountActive.value = !curryDiscountActive.value
        val discount = curryDiscountActive.value

        _menuItems.update { currentItems ->
            currentItems.map { item ->
                if (item.category == "Curry") {
                    if (discount) {
                        item.copy(
                            price = (item.price * 0.85).toInt(),
                            originalPrice = item.price
                        )
                    } else {
                        item.copy(
                            price = item.originalPrice ?: item.price,
                            originalPrice = null
                        )
                    }
                } else {
                    item.copy()
                }
            }
        }

        if (discount) {
            showToastMessage("Curry 15% Discount Pushed Successfully!")
            _logs.update { curLogs ->
                val log = ActivityLog(
                    id = UUID.randomUUID().toString(),
                    message = "Pushed 15% Curry Discount",
                    detail = "Adjusted live customer prices for Tuesday boost.",
                    timestamp = "Just now",
                    type = LogType.INFO,
                    value = "-15%"
                )
                listOf(log) + curLogs
            }
        } else {
            showToastMessage("15% Curry Discount Cancelled")
        }
    }

    fun toggleDinnerForTwo() {
        dinnerForTwoActive.value = !dinnerForTwoActive.value
        val active = dinnerForTwoActive.value

        _menuItems.update { currentItems ->
            val exists = currentItems.any { it.id == "combo_dinner_two" }
            if (active && !exists) {
                val dinnerCombo = MenuItem(
                    id = "combo_dinner_two",
                    name = "Dinner for Two Combo",
                    category = "Starters",
                    price = 449,
                    rating = 4.9,
                    reviewsCount = "New Choice",
                    prepTime = "20-25 min",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuC1PDMft4SRMJtlFK6UpYg6YCvkKddnmwT7ce2WWRZ5yNqUTdOAi1_JDQbvGpvNM3xUCRdpAb2nBf9sEdhc06VnJ1grnz7AXrhxAW9-CD5bPpKUICojvUHCEPn51hibkU8gO-jH1gxZw9Go8qXdZYTZW9EH98oA7sukpM3tziWyMnveGVjeBNX-_42H5fWjGzbn6lZuHMR-y788ROvLkU53JpdxQfnSooU12M3StWxZuDuw2MVwIUyXFA",
                    description = "Special combo pack containing 1x Butter Chicken, 2x Butter Naan, and 2x Coke. Value offer!",
                    originalPrice = 549,
                    bestSeller = true
                )
                currentItems + dinnerCombo
            } else if (!active && exists) {
                currentItems.filter { it.id != "combo_dinner_two" }
            } else {
                currentItems
            }
        }

        if (active) {
            showToastMessage("\"Dinner for Two\" Combo Offer Activated!")
        } else {
            showToastMessage("\"Dinner for Two\" Combo Offer Disabled")
        }
    }

    // --- QR CODE & ORDERING MODES ---
    fun simulateQrScan(tableNum: String) {
        orderingMode.value = "Dine-In"
        qrCodeTable.value = tableNum
        showToastMessage("QR Scanned successfully: Detected Table $tableNum")
        _logs.update { curLogs ->
            val log = ActivityLog(
                id = UUID.randomUUID().toString(),
                message = "Table QR Scanned",
                detail = "Customer scanned Table $tableNum QR",
                timestamp = "Just now",
                type = LogType.INFO
            )
            listOf(log) + curLogs
        }
    }

    fun toggleOrderingMode() {
        if (orderingMode.value == "Dine-In") {
            orderingMode.value = "Delivery"
            showToastMessage("Switched to Delivery Mode (GPS active)")
        } else {
            orderingMode.value = "Dine-In"
            showToastMessage("Switched to Dine-In QR Mode (Table ${qrCodeTable.value})")
        }
    }

    fun allowGpsAndDetectLocation() {
        gpsAllowed.value = true
        customerAddress.value = "Hitech City, Hyderabad (GPS Pinned)"
        showToastMessage("GPS location detected within 5 KM radius limit!")
    }

    // --- EXTRAs APPROVAL FLOW ---
    fun createExtraRequest(orderId: Int, tableNum: String, desc: String) {
        val newReq = ExtraRequest(
            id = UUID.randomUUID().toString(),
            orderId = orderId,
            tableNumber = tableNum,
            description = desc,
            status = "Pending"
        )
        _extraRequests.update { it + newReq }
        showToastMessage("Sent request: $desc for approval!")
    }

    fun approveExtra(requestId: String) {
        _extraRequests.update { list ->
            list.map { req ->
                if (req.id == requestId) {
                    _logs.update { curLogs ->
                        val log = ActivityLog(
                            id = UUID.randomUUID().toString(),
                            message = "Extra Request Approved",
                            detail = "${req.tableNumber} - ${req.description}",
                            timestamp = "Just now",
                            type = LogType.INFO
                        )
                        listOf(log) + curLogs
                    }
                    req.copy(status = "Accepted")
                } else req
            }
        }
        showToastMessage("Approved extra additions!")
    }

    fun rejectExtra(requestId: String) {
        _extraRequests.update { list ->
            list.map { req ->
                if (req.id == requestId) req.copy(status = "Rejected") else req
            }
        }
        showToastMessage("Rejected extra additions.")
    }

    // --- STAFF MANAGEMENT ---
    fun toggleStaffStatus(staffId: String) {
        _staffMembers.update { list ->
            list.map { staff ->
                if (staff.id == staffId) {
                    val nextStatus = when (staff.status) {
                        StaffStatus.ACTIVE -> StaffStatus.BUSY
                        StaffStatus.BUSY -> StaffStatus.OFFLINE
                        StaffStatus.OFFLINE -> StaffStatus.ACTIVE
                        else -> staff.status
                    }
                    staff.copy(status = nextStatus)
                } else staff
            }
        }
    }

    fun addStaffMember(name: String, role: StaffRole) {
        val newStaff = StaffMember(
            id = "staff_${(100..999).random()}",
            name = name,
            role = role,
            status = StaffStatus.ACTIVE,
            ordersHandled = 0,
            completionRate = 100.0,
            revenueContribution = 0.0
        )
        _staffMembers.update { it + newStaff }
        showToastMessage("Added $name as ${role.name}")
    }

    fun deleteStaffMember(staffId: String) {
        _staffMembers.update { it.filter { s -> s.id != staffId } }
        showToastMessage("Staff member removed.")
    }

    // --- DELIVERY SETTINGS ---
    fun updateDeliverySettings(radius: Double, charge: Double, threshold: Double) {
        deliveryRadius.value = radius
        deliveryCharge.value = charge
        freeDeliveryThreshold.value = threshold
        showToastMessage("Delivery radius/charge limits updated!")
    }

    // --- OFFERS MANAGEMENT ---
    fun addOffer(code: String, title: String, type: String, value: Double) {
        val newOffer = OfferTemplate(
            id = "off_${(100..999).random()}",
            code = code.uppercase().trim(),
            title = title,
            type = type,
            value = value,
            isActive = true
        )
        _offers.update { it + newOffer }
        showToastMessage("Created admin offer card: ${code.uppercase()}")
    }

    fun toggleOfferActive(offerId: String) {
        _offers.update { list ->
            list.map { off ->
                if (off.id == offerId) off.copy(isActive = !off.isActive) else off
            }
        }
        val target = _offers.value.find { it.id == offerId }
        if (target != null) {
            val statusStr = if (target.isActive) "ACTIVATED for Customers" else "DISABLED"
            showToastMessage("Offer ${target.code} is now $statusStr")
        }
    }

    fun applyOffer(offer: OfferTemplate) {
        if (!offer.isActive) {
            showToastMessage("Offer ${offer.code} is currently inactive.")
            return
        }
        if (_appliedOffer.value?.id == offer.id) {
            _appliedOffer.value = null
            showToastMessage("Removed promo code: ${offer.code}")
        } else {
            _appliedOffer.value = offer
            showToastMessage("Applied Offer Code: ${offer.code}! Discount added.")
        }
    }

    fun removeAppliedOffer() {
        _appliedOffer.value = null
    }

    // --- MENU MANAGEMENT ---
    fun addMenuItem(name: String, category: String, price: Int, description: String, prepTime: String = "15-20 min") {
        val newItem = MenuItem(
            id = "item_${(100..999).random()}",
            name = name,
            category = category,
            price = price,
            rating = 5.0,
            reviewsCount = "1 review",
            prepTime = prepTime,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCKX_O9qTebdzpoHXcmk1Sr4PD1VAooKXa43QXiqmbzR27CvFo8M-5-PfZ9Ffg5aZBlX5HS6ICXepMChh1knuUP7Vl28nXI9Qu9ucJ-rYvo4BtgzKYCkaOh4WtufwkRB1BKR8TAlRp6x8eaDGGhUf6OA2d2NPHkVdsUDRMfbsmQS-2TS2NoPJ6GKAwstHF1Ve3GUXTQrvwAeR5xiAFXhsAWM2z82bOrsdcKvCMLKXttpUmQw0wT_ctiVQ",
            description = description
        )
        _menuItems.update { it + newItem }
        showToastMessage("Added $name to menu category $category")
    }

    fun deleteMenuItem(itemId: String) {
        _menuItems.update { it.filter { item -> item.id != itemId } }
        showToastMessage("Dish removed from live menu.")
    }

    // --- CHAT SUPPORT ---
    fun sendSupportMessage(sender: String, text: String) {
        if (text.isBlank()) return
        val msg = SupportMessage(UUID.randomUUID().toString(), sender, text, "Just now")
        _supportMessages.update { it + msg }
    }

    // --- CUSTOMER FEEDBACK ---
    fun submitFeedback(customerName: String, rating: Double, review: String, dishName: String? = null) {
        val fb = CustomerFeedback(
            id = UUID.randomUUID().toString(),
            customerName = customerName,
            rating = rating,
            review = review,
            type = if (dishName != null) "Food" else "Overall",
            dishName = dishName,
            timestamp = "Just now"
        )
        _customerFeedback.update { listOf(fb) + it }
        showToastMessage("Feedback submitted! Rating: $rating ★")
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
