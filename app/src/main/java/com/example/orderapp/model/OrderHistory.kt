package com.example.orderapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_history")
data class OrderHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)
