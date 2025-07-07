package com.example.orderapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Int,
    val unit: String = "",
    val amount: Int = 1, // 新しく追加するフィールド
    val categoryId: Long = 0L,
    val order: Int = 0
)