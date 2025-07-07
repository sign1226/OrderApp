package com.example.orderapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val order: Int = 0
)