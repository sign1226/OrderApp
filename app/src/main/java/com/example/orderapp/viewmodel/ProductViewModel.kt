package com.example.orderapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderapp.model.Product
import com.example.orderapp.model.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.example.orderapp.model.Category
import com.example.orderapp.model.CategoryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message

    private val _taxRate = MutableStateFlow(8)
    val taxRate: StateFlow<Int> = _taxRate

    private val _searchQuery = MutableStateFlow("") // 検索クエリ
    val searchQuery: StateFlow<String> = _searchQuery

    val products: Flow<List<Product>>
    val allCategories: Flow<List<Category>>

    private val _selectedCategoryId = MutableStateFlow(0L)
    val selectedCategoryId: StateFlow<Long> = _selectedCategoryId

    private val _expandedCategories = MutableStateFlow<Map<Long, Boolean>>(emptyMap())

    init {
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
        _message.emit("商品を追加しました")
    }

    fun updateProduct(product: Product) = viewModelScope.launch {
        productRepository.updateProducts(listOf(product))
        _message.emit("商品を更新しました")
    }

    fun updateProductOrder(products: List<Product>) = viewModelScope.launch {
        productRepository.updateProducts(products)
        _message.emit("商品の順序を更新しました")
    }

    fun deleteProduct(product: Product) = viewModelScope.launch {
        productRepository.deleteProduct(product)
        _message.emit("商品を削除しました")
    }

    fun setTaxRate(rate: Int) {
        _taxRate.value = rate
    }

    fun filterByCategory(categoryId: Long) {
        _selectedCategoryId.value = categoryId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}


