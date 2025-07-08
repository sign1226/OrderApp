package com.example.orderapp.model

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao
) {

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
