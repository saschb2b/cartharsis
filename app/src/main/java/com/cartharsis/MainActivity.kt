package com.cartharsis

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cartharsis.ui.screens.CartScreen
import com.cartharsis.ui.screens.CheckoutScreen
import com.cartharsis.ui.screens.HomeScreen
import com.cartharsis.ui.screens.OrdersScreen
import com.cartharsis.ui.screens.ProductDetailScreen
import com.cartharsis.ui.screens.TrackingScreen
import com.cartharsis.ui.screens.WishlistScreen
import com.cartharsis.ui.theme.CartharsisTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val shopViewModel: ShopViewModel by viewModels()

    /** Route requested by a tapped notification; consumed (nulled) once navigated. */
    private val pendingRoute = MutableStateFlow<String?>(null)

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Notifier.ensureChannels(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        pendingRoute.value = intent.getStringExtra(Notifier.EXTRA_ROUTE)
        setContent {
            CartharsisTheme {
                CartharsisApp(shopViewModel, pendingRoute)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingRoute.value = intent.getStringExtra(Notifier.EXTRA_ROUTE)
    }
}

private data class Tab(val route: String, val emoji: String, val label: String)

private val tabs = listOf(
    Tab("home", "🏬", "Shop"),
    Tab("wishlist", "💖", "Wishlist"),
    Tab("cart", "🛒", "Cart"),
    Tab("orders", "📦", "Orders"),
)

@Composable
fun CartharsisApp(viewModel: ShopViewModel, pendingRoute: MutableStateFlow<String?>? = null) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in tabs.map { it.route }

    // Notification taps land on the order/product they announced.
    val requestedRoute = pendingRoute?.collectAsState()?.value
    LaunchedEffect(requestedRoute) {
        if (requestedRoute != null) {
            navController.navigate(requestedRoute) { launchSingleTop = true }
            pendingRoute.value = null
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BottomBar(navController, viewModel, currentRoute)
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onProductClick = { navController.navigate("product/$it") },
                )
            }
            composable("product/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toIntOrNull() ?: return@composable
                ProductDetailScreen(
                    viewModel = viewModel,
                    productId = id,
                    onBack = { navController.popBackStack() },
                    onProductClick = { navController.navigate("product/$it") },
                )
            }
            composable("wishlist") {
                WishlistScreen(
                    viewModel = viewModel,
                    onProductClick = { navController.navigate("product/$it") },
                    onBrowse = { navController.navigate("home") { popUpTo("home") } },
                )
            }
            composable("cart") {
                CartScreen(
                    viewModel = viewModel,
                    onCheckout = { navController.navigate("checkout") },
                    onBrowse = { navController.navigate("home") { popUpTo("home") } },
                )
            }
            composable("checkout") {
                CheckoutScreen(
                    viewModel = viewModel,
                    onTrackOrder = { orderId ->
                        navController.navigate("tracking/$orderId") {
                            popUpTo("home")
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable("tracking/{orderId}") { entry ->
                val orderId = entry.arguments?.getString("orderId")?.toIntOrNull() ?: return@composable
                TrackingScreen(
                    viewModel = viewModel,
                    orderId = orderId,
                    onBack = { navController.popBackStack() },
                    onShopMore = {
                        navController.navigate("home") { popUpTo("home") { inclusive = true } }
                    },
                )
            }
            composable("orders") {
                OrdersScreen(
                    viewModel = viewModel,
                    onOrderClick = { navController.navigate("tracking/$it") },
                )
            }
        }
    }
}

@Composable
private fun BottomBar(
    navController: NavHostController,
    viewModel: ShopViewModel,
    currentRoute: String?,
) {
    val cart by viewModel.cart.collectAsState()
    val pulse by viewModel.cartPulse.collectAsState()
    val priceDrops by viewModel.priceDrops.collectAsState()
    val cartCount = cart.sumOf { it.quantity }
    val dropCount = priceDrops.size

    // Bounce the cart icon every time something is added.
    var bounce by remember { mutableStateOf(false) }
    LaunchedEffect(pulse) {
        if (pulse > 0) bounce = true
    }
    val scale by animateFloatAsState(
        targetValue = if (bounce) 1.35f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        finishedListener = { bounce = false },
        label = "cartBounce",
    )

    NavigationBar {
        tabs.forEach { tab ->
            val isCart = tab.route == "cart"
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo("home")
                        launchSingleTop = true
                    }
                },
                icon = {
                    BadgedBox(
                        badge = {
                            if (isCart && cartCount > 0) {
                                Badge(modifier = Modifier.animateContentSize()) { Text("$cartCount") }
                            }
                            if (tab.route == "wishlist" && dropCount > 0) {
                                Badge { Text("🔻$dropCount") }
                            }
                        },
                    ) {
                        Text(
                            text = tab.emoji,
                            fontSize = 22.sp,
                            modifier = if (isCart) Modifier.scale(scale) else Modifier,
                        )
                    }
                },
                label = { Text(tab.label) },
            )
        }
    }
}
