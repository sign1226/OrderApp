package com.example.orderapp.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY `order` ASC, name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)

    @Query("DELETE FROM products")
    suspend fun deleteAll()

    @Update
    suspend fun updateProducts(products: List<Product>)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: Long)
}
