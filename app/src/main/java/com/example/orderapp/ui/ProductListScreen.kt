package com.example.orderapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.orderapp.model.Product
import com.example.orderapp.viewmodel.ProductViewModel


const val NO_CATEGORY_ID = 0L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    productViewModel: ProductViewModel,
    onOrderClick: (Map<Product, Int>) -> Unit
) {
    val products by productViewModel.products.collectAsState(initial = emptyList())
    var quantities by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }

    val categories by productViewModel.allCategories.collectAsState(initial = emptyList())
    val searchQuery by productViewModel.searchQuery.collectAsState()
    val selectedCategoryId by productViewModel.selectedCategoryId.collectAsState()

    val expandedCategories = remember { mutableStateMapOf<Long, Boolean>() }

    LaunchedEffect(categories) {
        expandedCategories.clear()
        expandedCategories[NO_CATEGORY_ID] = false // カテゴリなしをデフォルトで折りたたむ
        categories.forEach { category -> expandedCategories[category.id] = false } // 他のカテゴリもデフォルトで折りたたむ
    }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var showFilterOptions by remember { mutableStateOf(false) } // フィルターオプションの表示/非表示を制御

    Box(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End // 右寄せ
            ) {
                IconButton(onClick = { showFilterOptions = !showFilterOptions }) {
                    Icon(
                        imageVector = if (showFilterOptions) Icons.Filled.KeyboardArrowUp else Icons.Filled.Search,
                        contentDescription = if (showFilterOptions) "フィルターを隠す" else "フィルターを表示"
                    )
                }
            }

            AnimatedVisibility(
                visible = showFilterOptions,
                enter = fadeIn() + expandVertically(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    // 検索バー
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { productViewModel.setSearchQuery(it) },
                        label = { Text("商品検索") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { productViewModel.setSearchQuery("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "クリア")
                                }
                            } else {
                                Icon(Icons.Filled.Search, contentDescription = "検索")
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                    )

                    // カテゴリフィルター
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "カテゴリ:", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box {
                            Button(onClick = { categoryDropdownExpanded = true }) {
                                Text(text = categories.find { it.id == selectedCategoryId }?.name ?: "すべて")
                                Icon(
                                    imageVector = if (categoryDropdownExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "カテゴリ選択"
                                )
                            }
                            DropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("すべて") },
                                    onClick = {
                                        productViewModel.filterByCategory(0L)
                                        categoryDropdownExpanded = false
                                    }
                                )
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name) },
                                        onClick = {
                                            productViewModel.filterByCategory(category.id)
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                // 「カテゴリなし」の商品を表示
                val noCategoryProducts = products.filter { it.categoryId == NO_CATEGORY_ID }
                if (noCategoryProducts.isNotEmpty()) {
                    item {
                        CategoryHeader(
                            categoryName = "カテゴリなし",
                            isExpanded = expandedCategories[NO_CATEGORY_ID] == true,
                            onToggleExpand = {
                                expandedCategories[NO_CATEGORY_ID] = expandedCategories[NO_CATEGORY_ID] == false
                            }
                        )
                    }
                    if (expandedCategories[NO_CATEGORY_ID] == true) {
                        items(noCategoryProducts) { product ->
                            ProductListItem(
                                product = product,
                                initialQuantity = quantities[product.id] ?: "",
                                onQuantityChange = { newQuantity ->
                                    quantities = quantities + (product.id to newQuantity)
                                }
                            )
                        }
                    }
                }

                // 各カテゴリの商品を表示
                categories.forEach { category ->
                    val categoryProducts = products.filter { it.categoryId == category.id }
                    if (categoryProducts.isNotEmpty()) {
                        item {
                            CategoryHeader(
                                categoryName = category.name,
                                isExpanded = expandedCategories[category.id] == true,
                                onToggleExpand = {
                                    expandedCategories[category.id] = expandedCategories[category.id] == false
                                }
                            )
                        }
                        if (expandedCategories[category.id] == true) {
                            items(categoryProducts) { product ->
                                ProductListItem(
                                    product = product,
                                    initialQuantity = quantities[product.id] ?: "",
                                    onQuantityChange = { newQuantity ->
                                        quantities = quantities + (product.id to newQuantity)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val productsToOrder = quantities.mapNotNull { (id, quantityStr) ->
                        val product = products.find { it.id == id }
                        val quantity = quantityStr.toIntOrNull()
                        if (product != null && quantity != null && quantity > 0) {
                            product to quantity
                        } else {
                            null
                        }
                    }.toMap()
                    onOrderClick(productsToOrder)
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally)
            ) {
                Text(text = "注文", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun CategoryHeader(categoryName: String, isExpanded: Boolean, onToggleExpand: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpand) // ここをタップで展開
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animateContentSize(animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = categoryName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Icon(
                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand"
            )
        }
    }
}

@Composable
fun ProductListItem(
    product: Product,
    initialQuantity: String,
    onQuantityChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var quantity by remember { mutableStateOf(initialQuantity) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "¥${product.price} / ${product.amount}${product.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedTextField(
                value = quantity,
                onValueChange = {
                    quantity = it
                    onQuantityChange(it)
                },
                label = { Text("数量") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

