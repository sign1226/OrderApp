package com.example.orderapp.ui

sealed class Screen(val route: String, val title: String) {
    object Order : Screen("order", "注文")
    object Edit : Screen("edit", "商品編集")
    object History : Screen("history", "発注履歴")
    object CategoryManagement : Screen("category_management", "カテゴリ管理")
    object Settings : Screen("settings", "設定")
}
