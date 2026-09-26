package com.karthi.stockmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.karthi.stockmonitor.ui.screens.*
import com.karthi.stockmonitor.viewmodel.StockViewModel
import com.karthi.stockmonitor.worker.PriceAlertWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        schedulePriceAlertWork()
        setContent {
            MaterialTheme {
                StockMonitorApp()
            }
        }
    }

    /** Checks price alerts every 15 minutes — the minimum WorkManager periodic interval. */
    private fun schedulePriceAlertWork() {
        val request = PeriodicWorkRequestBuilder<PriceAlertWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "price_alert_check",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Watchlist : Screen("watchlist", "Watchlist", Icons.Default.Star)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Portfolio : Screen("portfolio", "Portfolio", Icons.Default.AccountBalanceWallet)
}

@Composable
fun StockMonitorApp() {
    val navController: NavHostController = rememberNavController()
    val viewModel: StockViewModel = hiltViewModel()

    val items = listOf(Screen.Watchlist, Screen.Search, Screen.Portfolio)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Watchlist.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Watchlist.route) {
                WatchlistScreen(
                    viewModel = viewModel,
                    onStockClick = { symbol -> navController.navigate("detail/$symbol") }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(viewModel = viewModel)
            }
            composable(Screen.Portfolio.route) {
                PortfolioScreen(viewModel = viewModel)
            }
            composable("detail/{symbol}") { backStackEntry ->
                val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
                StockDetailScreen(symbol = symbol, viewModel = viewModel)
            }
        }
    }
}
