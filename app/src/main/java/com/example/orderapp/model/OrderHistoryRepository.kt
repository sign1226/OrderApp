package com.example.orderapp.model

import android.content.Context
import kotlinx.coroutines.flow.Flow

class OrderHistoryRepository(context: Context) {
    private val orderHistoryDao = AppDatabase.getDatabase(context).orderHistoryDao()

    fun getAllOrderHistories(): Flow<List<OrderHistoryWithLines>> {
        return orderHistoryDao.getAllOrderHistories()
    }

    suspend fun addOrderHistory(order: Order) {
        val orderHistoryId = orderHistoryDao.insertOrderHistory(OrderHistory())
        val lines = order.lines.map { it: OrderLine ->
            OrderHistoryLine(
                orderHistoryId = orderHistoryId,
                productId = it.product.id,
                productName = it.product.name,
                productPrice = it.product.price,
                productUnit = it.product.unit,
                productAmount = it.product.amount,
                quantity = it.quantity
            )
        }
        orderHistoryDao.insertOrderHistoryLines(lines)
    }

    suspend fun deleteOrderHistory(id: Long) {
        orderHistoryDao.deleteOrderHistoryById(id)
    }
}
