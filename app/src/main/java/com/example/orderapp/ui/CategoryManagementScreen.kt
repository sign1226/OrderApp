package com.example.orderapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.orderapp.model.Category
import com.example.orderapp.viewmodel.CategoryViewModel
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(categoryViewModel: CategoryViewModel) {
    val categories by categoryViewModel.allCategories.collectAsState(initial = emptyList())
    var showAddEditDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    

    val state = rememberReorderableLazyListState(onMove = { from, to ->
        val reorderedCategories = categories.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }.mapIndexed { index, category ->
            category.copy(order = index)
        }
        categoryViewModel.updateCategoryOrder(reorderedCategories)
    })

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("確認") },
            text = { Text("${categoryToDelete?.name}を削除しますか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        categoryToDelete?.let { category -> categoryViewModel.deleteCategory(category) }
                        showDeleteDialog = false
                        categoryToDelete = null
                    }
                ) {
                    Text("はい")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        categoryToDelete = null
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
                    selectedCategory = null
                    showAddEditDialog = true
                },
                modifier = Modifier
            ) {
                Icon(Icons.Filled.Add, "カテゴリを追加")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) { // Boxでラップしてチュートリアルオーバーレイを重ねる
            Column(modifier = Modifier.padding(paddingValues)) {
                Text(text = "カテゴリ管理", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    state = state.listState,
                    modifier = Modifier
                        .reorderable(state)
                ) {
                    items(categories, { it.id }) { itemCategory ->
                        Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = itemCategory.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row {
                                        IconButton(
                                            onClick = {
                                                selectedCategory = itemCategory
                                                showAddEditDialog = true
                                            },
                                            modifier = Modifier
                                        ) {
                                            Icon(Icons.Filled.Edit, "編集", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(
                                            onClick = {
                                                categoryToDelete = itemCategory
                                                showDeleteDialog = true
                                            },
                                            modifier = Modifier
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

    if (showAddEditDialog) {
        CategoryAddEditDialog(
            category = selectedCategory,
            onDismiss = { showAddEditDialog = false },
            onSave = { savedCategory ->
                if (savedCategory.id == 0L) {
                    val newOrder = categories.maxOfOrNull { category -> category.order }?.plus(1) ?: 0
                    categoryViewModel.insertCategory(savedCategory.copy(order = newOrder))
                } else {
                    categoryViewModel.updateCategory(savedCategory)
                }
                showAddEditDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryAddEditDialog(category: Category?, onDismiss: () -> Unit, onSave: (Category) -> Unit) {
    var categoryName by remember { mutableStateOf(category?.name ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "カテゴリ追加" else "カテゴリ編集") },
        text = {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("カテゴリ名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (categoryName.isNotBlank()) {
                    val newCategory = category?.copy(name = categoryName) ?: Category(name = categoryName)
                    onSave(newCategory)
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