package com.example.orderapp.viewmodel

import androidx.lifecycle.ViewModel
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.core.content.edit

import android.net.Uri

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val dataTransferManager: DataTransferManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val sharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    private val taxRateSharedPreferences = context.getSharedPreferences("tax_rate_prefs", Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(getThemeFromPreferences())
    val theme: StateFlow<AppTheme> = _theme

    private val _taxRate = MutableStateFlow(getTaxRateFromPreferences())
    val taxRate: StateFlow<Int> = _taxRate

    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message

    suspend fun exportData(format: ExportFormat): String = withContext(Dispatchers.IO) {
        val products = productRepository.getAllProducts().first()
        val categories = categoryRepository.allCategories.first()
        val exportedString = dataTransferManager.exportData(products, categories, format)
        _message.emit("データのエクスポートが完了しました")
        exportedString
    }

    suspend fun importData(uri: Uri, format: ExportFormat) = withContext(Dispatchers.IO) {
        try {
            dataTransferManager.importData(uri, productRepository, categoryRepository, format)
            _message.emit("データのインポートが完了しました")
        } catch (e: Exception) {
            _message.emit("データのインポートに失敗しました: ${e.localizedMessage}")
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            println("setTheme called. Old theme: ${_theme.value}, New theme: $theme")
            _theme.value = theme
            withContext(Dispatchers.IO) { // Add withContext(Dispatchers.IO) for sharedPreferences operations
                sharedPreferences.edit { putString("theme", theme.name) }
            }
        }
    }

    fun setTaxRate(rate: Int) {
        viewModelScope.launch {
            _taxRate.value = rate
            withContext(Dispatchers.IO) {
                taxRateSharedPreferences.edit { putInt("tax_rate", rate) }
            }
        }
    }

    private fun getThemeFromPreferences(): AppTheme {
        val themeName = sharedPreferences.getString("theme", AppTheme.SYSTEM_DEFAULT.name)
        return AppTheme.valueOf(themeName ?: AppTheme.SYSTEM_DEFAULT.name)
    }

    private fun getTaxRateFromPreferences(): Int {
        return taxRateSharedPreferences.getInt("tax_rate", 8) // Default tax rate is 8%
    }
}

