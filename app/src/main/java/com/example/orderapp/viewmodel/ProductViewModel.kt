package com.example.orderapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.orderapp.model.Product
import com.example.orderapp.model.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.example.orderapp.model.AppDatabase
import com.example.orderapp.model.Category
import com.example.orderapp.model.CategoryRepository

class ProductViewModel(application: Application) : ViewModel() {
    private val productRepository = ProductRepository(application)
    private val categoryRepository: CategoryRepository

    private val _taxRate = MutableStateFlow(8)
    val taxRate: StateFlow<Int> = _taxRate

    private val _searchQuery = MutableStateFlow("") // 検索クエリ
    val searchQuery: StateFlow<String> = _searchQuery

    val products: Flow<List<Product>>
    val allCategories: Flow<List<Category>>

    private val _selectedCategoryId = MutableStateFlow(0L)
    val selectedCategoryId: StateFlow<Long> = _selectedCategoryId

    private val _expandedCategories = MutableStateFlow<Map<Long, Boolean>>(emptyMap())
    val expandedCategories: StateFlow<Map<Long, Boolean>> = _expandedCategories

    init {
        val database = AppDatabase.getDatabase(application)
        categoryRepository = CategoryRepository(database.categoryDao())

        allCategories = categoryRepository.allCategories

        products = productRepository.getAllProducts()
            .combine(_selectedCategoryId) { products, categoryId ->
                if (categoryId == 0L) {
                    products
                } else {
                    products.filter { it.categoryId == categoryId }
                }
            }
            .combine(_searchQuery) { products, query -> // 検索クエリでフィルタリング
                if (query.isBlank()) {
                    products
                } else {
                    products.filter { it.name.contains(query, ignoreCase = true) }
                }
            }

        viewModelScope.launch {
            allCategories.collect { categories ->
                val initialExpandedState = mutableMapOf<Long, Boolean>()
                initialExpandedState[0L] = false // カテゴリなしをデフォルトで折りたたむ
                categories.forEach { category -> initialExpandedState[category.id] = false } // 他のカテゴリもデフォルトで折りたたむ
                _expandedCategories.value = initialExpandedState
            }
        }
    }

    fun addProduct(product: Product) = viewModelScope.launch {
        productRepository.addProduct(product)
    }

    fun updateProduct(product: Product) = viewModelScope.launch {
        productRepository.updateProducts(listOf(product))
    }

    fun updateProductOrder(products: List<Product>) = viewModelScope.launch {
        productRepository.updateProducts(products)
    }

    fun deleteProduct(product: Product) = viewModelScope.launch {
        productRepository.deleteProduct(product)
    }

    fun setTaxRate(rate: Int) {
        _taxRate.value = rate
    }

    fun filterByCategory(categoryId: Long) {
        _selectedCategoryId.value = categoryId
    }

    fun toggleCategoryExpansion(categoryId: Long) {
        _expandedCategories.value = _expandedCategories.value.toMutableMap().apply {
            this[categoryId] = !(this[categoryId] ?: false)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

class ProductViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
