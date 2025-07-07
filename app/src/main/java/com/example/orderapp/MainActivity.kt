package com.example.orderapp

import com.example.orderapp.ui.theme.OrderAppTheme



import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.orderapp.model.Order
import com.example.orderapp.model.Product
import com.example.orderapp.model.ProductRepository
import com.example.orderapp.ui.OrderHistoryScreen
import com.example.orderapp.ui.OrderScreen
import com.example.orderapp.ui.ProductEditScreen
import com.example.orderapp.ui.ProductListScreen
import com.example.orderapp.ui.Screen



import com.example.orderapp.viewmodel.OrderHistoryViewModel
import com.example.orderapp.viewmodel.OrderHistoryViewModelFactory
import com.example.orderapp.viewmodel.ProductViewModel
import com.example.orderapp.viewmodel.ProductViewModelFactory
import com.example.orderapp.viewmodel.CategoryViewModel
import com.example.orderapp.viewmodel.CategoryViewModelFactory
import com.example.orderapp.ui.CategoryManagementScreen
import com.example.orderapp.ui.SettingScreen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.height
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

            key(currentThemeState.value) {
                OrderAppTheme(appTheme = currentThemeState.value) {
                val navController = rememberNavController()
                val screens = listOf(Screen.Order, Screen.Edit, Screen.CategoryManagement, Screen.History, Screen.Settings)
                val application = LocalContext.current.applicationContext as Application
                val productViewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory(application))
                val orderHistoryViewModel: OrderHistoryViewModel = viewModel(factory = OrderHistoryViewModelFactory(application))
                val categoryViewModel: CategoryViewModel = viewModel(factory = CategoryViewModelFactory(application))

                var showThemeDialog by remember { mutableStateOf(false) }

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
                        composable(Screen.History.route) { OrderHistoryScreen(orderHistoryViewModel) }
                        composable(Screen.CategoryManagement.route) { CategoryManagementScreen(categoryViewModel) }
                        composable(Screen.Settings.route) { SettingScreen() }
                    }
                }
            }
        }
        }
    }
}

@Composable
fun OrderApp(productViewModel: ProductViewModel, orderHistoryViewModel: OrderHistoryViewModel) {
    val products by productViewModel.products.collectAsState(initial = emptyList<Product>()) // Explicitly specify type
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