package com.example.orderapp.model

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

class OrderHistoryRepository @Inject constructor(
    private val orderHistoryDao: OrderHistoryDao
) {

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

    fun getOrderHistoryById(id: Long): Flow<OrderHistoryWithLines> {
        return orderHistoryDao.getOrderHistoryById(id)
    }
}
