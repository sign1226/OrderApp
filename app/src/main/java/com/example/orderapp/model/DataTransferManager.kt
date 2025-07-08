package com.example.orderapp.model

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

enum class ExportFormat {
    JSON, CSV
}

@Serializable
data class AppData(
    val products: List<Product>,
    val categories: List<Category>
)

@Singleton
class DataTransferManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val json = Json { ignoreUnknownKeys = true }



    fun exportData(products: List<Product>, categories: List<Category>, format: ExportFormat): String {
        Log.d("DataTransferManager", "Exporting data. Products count: ${products.size}, Categories count: ${categories.size}")
        val exportedString = when (format) {
            ExportFormat.JSON -> {
                val appData = AppData(products, categories)
                json.encodeToString(appData)
            }
            ExportFormat.CSV -> {
                val stringBuilder = StringBuilder()
                stringBuilder.append("---PRODUCTS---\n")
                stringBuilder.append("id,name,price,unit,amount,categoryId,order,imageUri\n")
                products.forEach { product ->
                    stringBuilder.append("${product.id},\"${product.name}\",${product.price},\"${product.unit}\",${product.amount},${product.categoryId},${product.order},\"${product.imageUri ?: ""}\"\n")
                }
                stringBuilder.append("---CATEGORIES---\n")
                stringBuilder.append("id,name,order\n")
                categories.forEach { category ->
                    stringBuilder.append("${category.id},\"${category.name}\",${category.order}\n")
                }
                stringBuilder.toString()
            }
        }
        Log.d("DataTransferManager", "Exported string (first 500 chars): ${exportedString.take(500)}")
        return exportedString
    }

    suspend fun importData(uri: Uri, productRepository: ProductRepository, categoryRepository: CategoryRepository, format: ExportFormat) {
        val contentResolver = context.contentResolver
        val dataString = contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
        }

        if (dataString != null) {
            try {
                when (format) {
                    ExportFormat.JSON -> {
                        val appData = json.decodeFromString<AppData>(dataString)
                        productRepository.deleteAllProducts()
                        categoryRepository.deleteAllCategories()
                        productRepository.addProducts(appData.products)
                        categoryRepository.addCategories(appData.categories)
                    }
                    ExportFormat.CSV -> {
                        val lines = dataString.split("\n").filter { it.isNotBlank() }
                        val products = mutableListOf<Product>()
                        val categories = mutableListOf<Category>()

                        var currentSection = ""
                        lines.forEach { line ->
                            when (line.trim()) {
                                "---PRODUCTS---" -> {
                                    currentSection = "PRODUCTS"
                                    return@forEach
                                }
                                "---CATEGORIES---" -> {
                                    currentSection = "CATEGORIES"
                                    return@forEach
                                }
                                "id,name,price,unit,amount,categoryId,order,imageUri" -> {
                                    if (currentSection == "PRODUCTS") return@forEach // Skip product header
                                }
                                "id,name,order" -> {
                                    if (currentSection == "CATEGORIES") return@forEach // Skip category header
                                }
                            }

                            val parts = line.split(",")
                            if (currentSection == "PRODUCTS" && parts.size >= 8) {
                                try {
                                    products.add(
                                        Product(
                                            id = parts[0].toLong(),
                                            name = parts[1].trim('"'),
                                            price = parts[2].toInt(),
                                            unit = parts[3].trim('"'),
                                            amount = parts[4].toInt(),
                                            categoryId = parts[5].toLong(),
                                            order = parts[6].toInt(),
                                            imageUri = parts[7].trim('"').let { if (it.isEmpty()) null else it }
                                        )
                                    )
                                } catch (e: Exception) {
                                    Log.e("DataTransferManager", "Error parsing product CSV line: $line, ${e.message}")
                                }
                            } else if (currentSection == "CATEGORIES" && parts.size >= 3) {
                                try {
                                    categories.add(
                                        Category(
                                            id = parts[0].toLong(),
                                            name = parts[1].trim('"'),
                                            order = parts[2].toInt()
                                        )
                                    )
                                } catch (e: Exception) {
                                    Log.e("DataTransferManager", "Error parsing category CSV line: $line, ${e.message}")
                                }
                            }
                        }
                        productRepository.deleteAllProducts()
                        categoryRepository.deleteAllCategories()
                        productRepository.addProducts(products)
                        categoryRepository.addCategories(categories)
                    }
                }
            } catch (e: SerializationException) {
                Log.e("DataTransferManager", "Error deserializing data: ${e.message}")
                // Optionally, show a toast message to the user
            } catch (e: Exception) {
                Log.e("DataTransferManager", "Error importing data: ${e.message}")
                // Optionally, show a toast message to the user
            }
        }
    }
}