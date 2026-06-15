package com.cartharsis

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Surface
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cartharsis.ui.screens.AccountScreen
import com.cartharsis.ui.screens.CartScreen
import com.cartharsis.ui.screens.CheckoutScreen
import com.cartharsis.ui.screens.HomeScreen
import com.cartharsis.ui.screens.MilestonesScreen
import com.cartharsis.ui.screens.OnboardingScreen
import com.cartharsis.ui.screens.OrdersScreen
import com.cartharsis.ui.screens.ProductDetailScreen
import com.cartharsis.ui.screens.TrackingScreen
import com.cartharsis.ui.screens.WishlistScreen
import com.cartharsis.ui.theme.CartharsisTheme
import com.cartharsis.ui.theme.Motion
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val shopViewModel: ShopViewModel by viewModels()

    /** Route requested by a tapped notification; consumed (nulled) once navigated. */
    private val pendingRoute = MutableStateFlow<String?>(null)

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Hold the splash through the DataStore profile read so cold start
        // hands straight from the brand mark to real content — the old flow
        // flashed the default icon on white, then a blank window while
        // `profile` was still null.
        splash.setKeepOnScreenCondition { shopViewModel.profile.value == null }
        // A soft exit: the splash lifts and fades instead of blinking off.
        splash.setOnExitAnimationListener { provider ->
            provider.view.animate()
                .alpha(0f)
                .scaleX(1.06f)
                .scaleY(1.06f)
                .setDuration(220)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction { provider.remove() }
                .start()
        }
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
                // A root Surface paints the themed background AND sets the
                // default content color (onBackground). Without it, screens
                // that aren't inside a Scaffold — onboarding — fall back to
                // Compose's Color.Black default, so their untinted text went
                // black and vanished on the dark background.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
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
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingRoute.value = intent.getStringExtra(Notifier.EXTRA_ROUTE)
    }
}

// Pushed-screen motion (M3 Expressive shared-axis X): the detail slides the full
// width in from the right on a snappy spatial spring while the screen it covers
// slides a quarter-width left (see the NavHost exit/popEnter) — so a tap reads
// as a stack push, not a crossfade. spatialFast keeps it responsive, not slow.
private fun pushEnter() = slideInHorizontally(animationSpec = Motion.spatialFast(), initialOffsetX = { it }) +
    fadeIn(Motion.effects())

private fun pushPopExit() = slideOutHorizontally(animationSpec = Motion.spatialFast(), targetOffsetX = { it }) +
    fadeOut(Motion.effects())

private data class Tab(val route: String, val emoji: String, val label: String)

private val tabs = listOf(
    Tab("home", "🏬", "Shop"),
    Tab("wishlist", "💖", "Wishlist"),
    Tab("cart", "🛒", "Cart"),
    Tab("orders", "📦", "Orders"),
)

// The bottom-bar destinations. Anything else is a pushed detail screen, which
// the source slides aside for (vs a flat crossfade between tabs).
private val tabRoutes = tabs.map { it.route }.toSet()

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
            // Tab switches scale+fade in place. Forward nav to a detail screen
            // is a shared-axis push: the screen being left slides a quarter-width
            // aside while the detail slides fully in from the right (pushEnter),
            // so it reads as a stack instead of a crossfade.
            enterTransition = {
                fadeIn(Motion.effects()) + scaleIn(initialScale = 0.97f, animationSpec = Motion.spatial())
            },
            exitTransition = {
                if (targetState.destination.route !in tabRoutes) {
                    slideOutHorizontally(Motion.spatialFast()) { -it / 4 } + fadeOut(Motion.effects())
                } else {
                    fadeOut(Motion.effects())
                }
            },
            popEnterTransition = {
                if (initialState.destination.route !in tabRoutes) {
                    slideInHorizontally(Motion.spatialFast()) { -it / 4 } + fadeIn(Motion.effects())
                } else {
                    fadeIn(Motion.effects()) + scaleIn(initialScale = 0.97f, animationSpec = Motion.spatial())
                }
            },
            popExitTransition = { fadeOut(Motion.effects()) },
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onProductClick = { navController.navigate("product/$it") },
                    onAccount = { navController.navigate("account") },
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
                    onBrowse = { navController.navigate("home") { popUpTo("home") } },
                    onMilestones = { navController.navigate("milestones") },
                )
            }
            composable(
                "milestones",
                enterTransition = { pushEnter() },
                popExitTransition = { pushPopExit() },
            ) {
                MilestonesScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                "account",
                enterTransition = { pushEnter() },
                popExitTransition = { pushPopExit() },
            ) {
                AccountScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
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
