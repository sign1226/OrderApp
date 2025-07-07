package com.example.orderapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.orderapp.model.AppDatabase
import com.example.orderapp.model.Category
import com.example.orderapp.model.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CategoryRepository

    val allCategories: Flow<List<Category>>

    init {
        val categoryDao = AppDatabase.getDatabase(application).categoryDao()
        repository = CategoryRepository(categoryDao)
        allCategories = repository.allCategories
    }

    fun insertCategory(category: Category) = viewModelScope.launch {
        repository.insertCategory(category)
    }

    fun updateCategory(category: Category) = viewModelScope.launch {
        repository.updateCategories(listOf(category))
    }

    fun updateCategoryOrder(categories: List<Category>) = viewModelScope.launch {
        repository.updateCategories(categories)
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }
}

class CategoryViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}