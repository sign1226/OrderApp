package com.example.orderapp.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import androidx.room.Ignore

@Dao
interface OrderHistoryDao {
    @Insert
    suspend fun insertOrderHistory(orderHistory: OrderHistory): Long

    @Insert
    suspend fun insertOrderHistoryLines(lines: List<OrderHistoryLine>)

    @Transaction
    @Query("SELECT * FROM order_history ORDER BY timestamp DESC")
    fun getAllOrderHistories(): Flow<List<OrderHistoryWithLines>>

    @Query("DELETE FROM order_history WHERE id = :id")
    suspend fun deleteOrderHistoryById(id: Long)
}

data class OrderHistoryWithLines(
    @Embedded val orderHistory: OrderHistory,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderHistoryId"
    )
    val lines: List<OrderHistoryLine>) {
    val subtotal: Int
        get() = lines.sumOf { it.productPrice * it.quantity }
    @Ignore
    val taxRate = 8 // 仮の税率
    val tax: Int
        get() = (subtotal * taxRate / 100.0).toInt()
    val totalWithTax: Int
        get() = subtotal + tax
}
