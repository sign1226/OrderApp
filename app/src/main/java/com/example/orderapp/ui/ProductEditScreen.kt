package com.example.orderapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.orderapp.model.Product
import com.example.orderapp.viewmodel.ProductViewModel
import com.example.orderapp.viewmodel.CategoryViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.text.font.FontWeight
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlin.collections.emptyList
import kotlin.collections.toMutableList
import kotlin.collections.maxOfOrNull
import kotlin.text.isNotBlank
import kotlin.text.isDigit
import kotlin.text.toIntOrNull

import androidx.compose.material3.ExposedDropdownMenuBox
import android.content.SharedPreferences
import androidx.compose.ui.platform.LocalContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(viewModel: ProductViewModel, categoryViewModel: CategoryViewModel) {
    val products by viewModel.products.collectAsState(initial = emptyList())
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val allCategories by categoryViewModel.allCategories.collectAsState(initial = emptyList())
    val categoryMap = remember(allCategories) { allCategories.associateBy { it.id } }

    val context = LocalContext.current
    val prefs: SharedPreferences = context.getSharedPreferences("OrderAppPrefs", android.content.Context.MODE_PRIVATE)

    val state = rememberReorderableLazyListState(onMove = { from, to ->
        val reorderedProducts = products.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }.mapIndexed { index, product ->
            product.copy(order = index)
        }
        viewModel.updateProductOrder(reorderedProducts)
    })

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("確認") },
            text = { Text("${productToDelete?.name}を削除しますか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        productToDelete?.let { product -> viewModel.deleteProduct(product) }
                        showDeleteDialog = false
                        productToDelete = null
                    }
                ) {
                    Text("はい")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        productToDelete = null
                    }
                ) {
                    Text("いいえ")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedProduct = null
                    isEditing = true
                }
            ) {
                Icon(Icons.Filled.Add, "商品を追加")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) { // Boxでラップしてチュートリアルオーバーレイを重ねる
            Column(modifier = Modifier.padding(paddingValues)) {
                Text(text = "商品編集", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    state = state.listState,
                    modifier = Modifier
                        .weight(1f)
                        .reorderable(state),
                    contentPadding = PaddingValues(bottom = 72.dp)
                ) {
                    items(products, { it.id }) { product ->
                        Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = product.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "¥${product.price} / ${product.amount}${product.unit}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        // カテゴリ名を表示
                                        val categoryName = categoryMap[product.categoryId]?.name ?: "カテゴリなし"
                                        Text(
                                            text = "カテゴリ: $categoryName",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row {
                                        IconButton(
                                            onClick = {
                                                selectedProduct = product
                                                isEditing = true
                                            }
                                        ) {
                                            Icon(Icons.Filled.Edit, "編集", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(
                                            onClick = {
                                                productToDelete = product
                                                showDeleteDialog = true
                                            }
                                        ) {
                                            Icon(Icons.Filled.Delete, "削除", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                    }
                }
            }
        }
    }

    if (isEditing) {
        ProductEditDialog(
            product = selectedProduct,
            onDismiss = { isEditing = false },
            onSave = { product ->
                if (selectedProduct == null) {
                    val newOrder = products.maxOfOrNull { product -> product.order }?.plus(1) ?: 0
                    viewModel.addProduct(product.copy(order = newOrder))
                } else {
                    viewModel.updateProduct(product)
                }
                isEditing = false
            },
            categoryViewModel = categoryViewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditDialog(product: Product?, onDismiss: () -> Unit, onSave: (Product) -> Unit, categoryViewModel: CategoryViewModel) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var unit by remember { mutableStateOf(product?.unit ?: "") }
    var amount by remember { mutableStateOf(product?.amount?.toString() ?: "1") } // amountのstateを追加
    var selectedCategoryId by remember { mutableStateOf(product?.categoryId ?: 0L) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var unitError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) } // amountのエラーstateを追加

    val categories by categoryViewModel.allCategories.collectAsState(initial = emptyList())
    var expanded by remember { mutableStateOf(false) }

    fun validate(): Boolean {
        var isValid = true
        if (name.isBlank()) {
            nameError = "商品名を入力してください"
            isValid = false
        } else {
            nameError = null
        }
        if (price.isBlank()) {
            priceError = "価格を入力してください"
            isValid = false
        } else if (price.toIntOrNull() == null) {
            priceError = "有効な数値を入力してください"
            isValid = false
        } else {
            priceError = null
        }
        if (unit.isBlank()) {
            unitError = "単位を入力してください"
            isValid = false
        } else {
            unitError = null
        }
        if (amount.isBlank()) {
            amountError = "量を入力してください"
            isValid = false
        } else if (amount.toIntOrNull() == null) {
            amountError = "有効な数値を入力してください"
            isValid = false
        } else {
            amountError = null
        }
        return isValid
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "商品追加" else "商品編集") },
        text = {
            Column(modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)) {
                // カテゴリ選択ドロップダウン
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = categories.find { category -> category.id == selectedCategoryId }?.name ?: "カテゴリを選択",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("カテゴリ") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("カテゴリなし") },
                            onClick = {
                                selectedCategoryId = 0L
                                expanded = false
                            }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { newName -> name = newName; nameError = null },
                    label = { Text("商品名") },
                    singleLine = true,
                    isError = nameError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                nameError?.let { errorText -> Text(errorText, color = MaterialTheme.colorScheme.error) }
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = amount,
                                                onValueChange = { newAmount -> amount = newAmount.filter { char -> char.isDigit() }; amountError = null },
                        label = { Text("量") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = amountError != null,
                        modifier = Modifier.weight(1f)
                    )
                    amountError?.let { errorText -> Text(errorText, color = MaterialTheme.colorScheme.error) }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = unit,
                                                onValueChange = { newUnit -> unit = newUnit; unitError = null },
                        label = { Text("単位") },
                        singleLine = true,
                        isError = unitError != null,
                        modifier = Modifier.weight(1f)
                    )
                    unitError?.let { errorText -> Text(errorText, color = MaterialTheme.colorScheme.error) }
                }
                OutlinedTextField(
                    value = price,
                    onValueChange = { newPrice -> price = newPrice.filter { char -> char.isDigit() }; priceError = null },
                    label = { Text("価格") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = priceError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                priceError?.let { errorText -> Text(errorText, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (validate()) {
                    val newProduct = product?.copy(
                        name = name,
                        price = price.toInt(),
                        unit = unit,
                        amount = amount.toInt(),
                        categoryId = selectedCategoryId
                    ) ?: Product(
                        name = name,
                        price = price.toInt(),
                        unit = unit,
                        amount = amount.toInt(),
                        categoryId = selectedCategoryId,
                        order = 0 // 新規追加時はorderを0に設定
                    )
                    onSave(newProduct)
                }
            }) {
                Text("保存")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )

}