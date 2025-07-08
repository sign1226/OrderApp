package com.example.orderapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderapp.model.Category
import com.example.orderapp.model.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {

    val allCategories: Flow<List<Category>> = repository.allCategories

    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message

    fun insertCategory(category: Category) = viewModelScope.launch {
        repository.insertCategory(category)
        _message.emit("カテゴリを追加しました")
    }

    fun updateCategory(category: Category) = viewModelScope.launch {
        repository.updateCategories(listOf(category))
        _message.emit("カテゴリを更新しました")
    }

    fun updateCategoryOrder(categories: List<Category>) = viewModelScope.launch {
        repository.updateCategories(categories)
        _message.emit("カテゴリの順序を更新しました")
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.deleteCategory(category)
        _message.emit("カテゴリを削除しました")
    }
}