package com.example.orderapp.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.orderapp.model.CategoryRepository
import com.example.orderapp.model.DataTransferManager
import com.example.orderapp.model.ProductRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.orderapp.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.orderapp.model.ExportFormat // Import ExportFormat

class SettingViewModel(application: Application) : AndroidViewModel(application) {

    private val productRepository = ProductRepository(application)
    private val categoryRepository = CategoryRepository(com.example.orderapp.model.AppDatabase.getDatabase(application).categoryDao())
    private val dataTransferManager = DataTransferManager(application.applicationContext)
    private val sharedPreferences = application.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(getThemeFromPreferences())
    val theme: StateFlow<AppTheme> = _theme

    suspend fun exportData(format: ExportFormat): String = withContext(Dispatchers.IO) {
        val products = productRepository.getAllProducts().first()
        val categories = categoryRepository.allCategories.first()
        dataTransferManager.exportData(products, categories, format)
    }

    suspend fun importData(uri: Uri, format: ExportFormat) = withContext(Dispatchers.IO) {
        dataTransferManager.importData(uri, productRepository, categoryRepository, format)
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            println("setTheme called. Old theme: ${_theme.value}, New theme: $theme")
            _theme.value = theme
            withContext(Dispatchers.IO) { // Add withContext(Dispatchers.IO) for sharedPreferences operations
                with(sharedPreferences.edit()) {
                    putString("theme", theme.name)
                    commit()
                }
            }
        }
    }

    private fun getThemeFromPreferences(): AppTheme {
        val themeName = sharedPreferences.getString("theme", AppTheme.SYSTEM_DEFAULT.name)
        return AppTheme.valueOf(themeName ?: AppTheme.SYSTEM_DEFAULT.name)
    }
}

class SettingViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}