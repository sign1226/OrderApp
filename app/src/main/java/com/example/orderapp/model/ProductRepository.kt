package com.example.orderapp.model

import android.content.Context
import kotlinx.coroutines.flow.Flow

class ProductRepository(context: Context) {
    private val productDao = AppDatabase.getDatabase(context).productDao()

    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
    }

    suspend fun addProduct(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun addProducts(products: List<Product>) {
        productDao.insertAll(products)
    }

    suspend fun deleteAllProducts() {
        productDao.deleteAll()
    }

    suspend fun updateProducts(products: List<Product>) {
        productDao.updateProducts(products)
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProductById(product.id)
    }
}
