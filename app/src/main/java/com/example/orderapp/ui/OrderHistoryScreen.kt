package com.example.orderapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.orderapp.viewmodel.OrderHistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.Card
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.example.orderapp.model.OrderHistory
import com.example.orderapp.model.OrderHistoryWithLines
import kotlin.collections.emptyList
import kotlin.collections.sumOf

@Composable
fun OrderHistoryScreen(viewModel: OrderHistoryViewModel) {
    val orderHistories by viewModel.orderHistories.collectAsState(initial = emptyList())
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var orderHistoryToDelete by remember { mutableStateOf<OrderHistoryWithLines?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "発注履歴", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(orderHistories) { history ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column {
                                val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
                                Text(text = "発注日時: ${dateFormat.format(Date(history.orderHistory.timestamp))}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                history.lines.forEach { line ->
                                    Text(text = "  ${line.productName} x ${line.quantity} (¥${line.productPrice * line.quantity}, ${line.productAmount * line.quantity}${line.productUnit})", style = MaterialTheme.typography.bodyMedium)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "小計: ¥${history.subtotal}", style = MaterialTheme.typography.bodyLarge)
                                Text(text = "消費税: ¥${history.tax}", style = MaterialTheme.typography.bodyLarge)
                                Text(text = "税込合計: ¥${history.totalWithTax}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                
                            }
                            IconButton(onClick = {
                                orderHistoryToDelete = history
                                showDeleteConfirmDialog = true
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "削除")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("履歴を削除") },
            text = { Text("この発注履歴を削除してもよろしいですか？") },
            confirmButton = {
                TextButton(onClick = {
                    orderHistoryToDelete?.let { history -> viewModel.deleteOrderHistory(history.orderHistory) }
                    showDeleteConfirmDialog = false
                    orderHistoryToDelete = null
                }) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}
