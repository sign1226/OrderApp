package com.example.orderapp


import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.orderapp.model.Order
import com.example.orderapp.ui.CategoryManagementScreen
import com.example.orderapp.ui.OrderDetailScreen
import com.example.orderapp.ui.OrderHistoryScreen
import com.example.orderapp.ui.OrderScreen
import com.example.orderapp.ui.ProductEditScreen
import com.example.orderapp.ui.ProductListScreen
import com.example.orderapp.ui.Screen
import com.example.orderapp.ui.SettingScreen
import com.example.orderapp.ui.theme.OrderAppTheme
import com.example.orderapp.viewmodel.CategoryViewModel
import com.example.orderapp.viewmodel.CategoryViewModelFactory
import com.example.orderapp.viewmodel.OrderHistoryViewModel
import com.example.orderapp.viewmodel.OrderHistoryViewModelFactory
import com.example.orderapp.viewmodel.ProductViewModel
import com.example.orderapp.viewmodel.ProductViewModelFactory
import com.example.orderapp.viewmodel.SettingViewModel
import com.example.orderapp.viewmodel.SettingViewModelFactory

enum class AppTheme {
    SYSTEM_DEFAULT, LIGHT, DARK
}



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val application = LocalContext.current.applicationContext as Application
            val settingViewModel: SettingViewModel = viewModel(factory = SettingViewModelFactory(application))
            val currentThemeState = settingViewModel.theme.collectAsState()

            OrderAppTheme(appTheme = currentThemeState.value) {
                val navController = rememberNavController()
                val screens = listOf(Screen.Order, Screen.Edit, Screen.CategoryManagement, Screen.History, Screen.Settings)
                val productViewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory(application))
                val orderHistoryViewModel: OrderHistoryViewModel = viewModel(factory = OrderHistoryViewModelFactory(application))
                val categoryViewModel: CategoryViewModel = viewModel(factory = CategoryViewModelFactory(application))

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            screens.forEach { screen ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = when (screen) {
                                                Screen.Order -> Icons.Filled.ShoppingCart
                                                Screen.Edit -> Icons.Filled.Edit
                                                Screen.CategoryManagement -> Icons.Filled.Category
                                                Screen.History -> Icons.Filled.History
                                                Screen.Settings -> Icons.Filled.Settings
                                                else -> Icons.Filled.Settings // Fallback for other screens not in bottom nav
                                            },
                                            contentDescription = screen.title
                                        )
                                    },
                                    label = { Text(screen.title) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(navController = navController, startDestination = Screen.Order.route, modifier = Modifier.padding(innerPadding)) {
                        composable(Screen.Order.route) { OrderApp(productViewModel, orderHistoryViewModel) }
                        composable(Screen.Edit.route) { ProductEditScreen(productViewModel, categoryViewModel) }
                        composable(Screen.History.route) { OrderHistoryScreen(orderHistoryViewModel, navController) }
                        composable(Screen.CategoryManagement.route) { CategoryManagementScreen(categoryViewModel) }
                        composable(Screen.Settings.route) { SettingScreen(viewModel = settingViewModel) }
                        composable(Screen.OrderDetail.route) {
                            val orderHistoryId = it.arguments?.getString("orderHistoryId")?.toLongOrNull()
                            if (orderHistoryId != null) {
                                OrderDetailScreen(orderHistoryId = orderHistoryId, viewModel = orderHistoryViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderApp(productViewModel: ProductViewModel, orderHistoryViewModel: OrderHistoryViewModel) {
    var order by remember { mutableStateOf(Order()) }
    var showOrderSummary by remember { mutableStateOf(false) }

    if (showOrderSummary) {
        OrderScreen(order = order, onContinueShopping = {
            showOrderSummary = false
        }, onPlaceOrder = {
            orderHistoryViewModel.addOrderHistory(order)
            // showOrderSummary = false は OrderScreen の共有ダイアログが閉じた後に設定する
        }, viewModel = productViewModel, onOrderChange = { updatedOrder ->
            order = updatedOrder
        })
    } else {
        ProductListScreen(productViewModel = productViewModel, onOrderClick = { productsToOrder ->
            order = order.addProducts(productsToOrder)
            showOrderSummary = true
        })
    }
}