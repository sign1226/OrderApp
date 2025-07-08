package com.example.orderapp.model

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }

    suspend fun addCategories(categories: List<Category>) {
        categoryDao.insertAll(categories)
    }

    suspend fun deleteAllCategories() {
        categoryDao.deleteAll()
    }

    suspend fun updateCategories(categories: List<Category>) {
        categoryDao.updateCategories(categories)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategoryById(category.id)
    }
}