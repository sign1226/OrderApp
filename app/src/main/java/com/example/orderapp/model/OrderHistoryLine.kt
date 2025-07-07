package com.example.orderapp.model

import androidx.room.Entity

@Entity(tableName = "order_history_lines", primaryKeys = ["orderHistoryId", "productId"])
data class OrderHistoryLine(
    val orderHistoryId: Long,
    val productId: Long,
    val productName: String,
    val productPrice: Int,
    val productUnit: String,
    val productAmount: Int,
    val quantity: Int
)
