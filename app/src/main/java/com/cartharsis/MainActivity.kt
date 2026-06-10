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
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cartharsis.ui.screens.CartScreen
import com.cartharsis.ui.screens.CheckoutScreen
import com.cartharsis.ui.screens.HomeScreen
import com.cartharsis.ui.screens.OnboardingScreen
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
        Chime.init(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        pendingRoute.value = intent.getStringExtra(Notifier.EXTRA_ROUTE)
        setContent {
            CartharsisTheme {
                // First run walks the fake signup; after that, straight to the shop.
                val profile by shopViewModel.profile.collectAsState()
                when {
                    profile == null -> Unit // DataStore loads in a blink; render nothing.
                    profile?.onboarded == false -> OnboardingScreen(shopViewModel)
                    else -> CartharsisApp(shopViewModel, pendingRoute)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingRoute.value = intent.getStringExtra(Notifier.EXTRA_ROUTE)
    }
}

private fun pushEnter() =
    slideInHorizontally(animationSpec = tween(280), initialOffsetX = { it / 3 }) + fadeIn(tween(280))

private fun pushPopExit() =
    slideOutHorizontally(animationSpec = tween(240), targetOffsetX = { it / 3 }) + fadeOut(tween(240))

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
        // Tab switches fade through; pushed screens (product, checkout, tracking)
        // slide in from the right like a stack. One motion language everywhere.
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(tween(220)) + scaleIn(initialScale = 0.97f, animationSpec = tween(220))
            },
            exitTransition = { fadeOut(tween(120)) },
            popEnterTransition = {
                fadeIn(tween(220)) + scaleIn(initialScale = 0.97f, animationSpec = tween(220))
            },
            popExitTransition = { fadeOut(tween(120)) },
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onProductClick = { navController.navigate("product/$it") },
                )
            }
            composable(
                "product/{id}",
                enterTransition = { pushEnter() },
                popExitTransition = { pushPopExit() },
            ) { entry ->
                val id = entry.arguments?.getString("id")?.toIntOrNull() ?: return@composable
                ProductDetailScreen(
                    viewModel = viewModel,
                    productId = id,
                    onBack = { navController.popBackStack() },
                    onProductClick = { navController.navigate("product/$it") },
                    onBuyNow = { navController.navigate("checkout") },
                    onViewCart = { navController.navigate("cart") { popUpTo("home") } },
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
            composable(
                "checkout",
                enterTransition = { pushEnter() },
                popExitTransition = { pushPopExit() },
            ) {
                CheckoutScreen(
                    viewModel = viewModel,
                    onTrackOrder = { orderId ->
                        navController.navigate("tracking/$orderId") {
                            popUpTo("home")
                        }
                    },
                    onBack = { navController.popBackStack() },
                    onKeepShopping = {
                        navController.navigate("home") { popUpTo("home") { inclusive = true } }
                    },
                )
            }
            composable(
                "tracking/{orderId}",
                enterTransition = { pushEnter() },
                popExitTransition = { pushPopExit() },
            ) { entry ->
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
private fun BottomBar(navController: NavHostController, viewModel: ShopViewModel, currentRoute: String?) {
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

    // M3 Expressive's compact 64dp bar; badges carry plain counts per spec.
    ShortNavigationBar {
        tabs.forEach { tab ->
            val isCart = tab.route == "cart"
            val selected = currentRoute == tab.route
            ShortNavigationBarItem(
                selected = selected,
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
                                Badge { Text("$dropCount") }
                            }
                        },
                    ) {
                        TabEmoji(
                            emoji = tab.emoji,
                            selected = selected,
                            extraScale = if (isCart) scale else 1f,
                        )
                    }
                },
                label = { Text(tab.label) },
            )
        }
    }
}

/**
 * Emoji can't switch between outlined and filled variants the way M3 icons
 * mark selection, so the active state reads through emphasis instead: full
 * opacity plus a springy nudge up.
 */
@Composable
private fun TabEmoji(emoji: String, selected: Boolean, extraScale: Float = 1f) {
    val alpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.55f,
        label = "tabAlpha",
    )
    val selectionScale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tabScale",
    )
    Text(
        text = emoji,
        fontSize = 20.sp,
        modifier = Modifier
            .alpha(alpha)
            .scale(selectionScale * extraScale),
    )
}
