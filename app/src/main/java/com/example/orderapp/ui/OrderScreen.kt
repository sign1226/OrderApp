package com.example.orderapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.orderapp.model.Order
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.CardDefaults

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import com.example.orderapp.viewmodel.ProductViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.Alignment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.example.orderapp.model.Product
import android.content.Intent
import android.content.Context
import androidx.compose.ui.platform.LocalContext

@Composable
fun OrderScreen(order: Order, onContinueShopping: () -> Unit, onPlaceOrder: () -> Unit, viewModel: ProductViewModel, onOrderChange: (Order) -> Unit) {
    val taxRate by viewModel.taxRate.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var showExportDialog by remember { mutableStateOf(false) } // Re-introduce showExportDialog
    val context = LocalContext.current

    val totalPrice = order.lines.sumOf { it.product.price * it.quantity }
    val tax = (totalPrice * taxRate / 100.0).toInt()
    val totalPriceWithTax = totalPrice + tax

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("確認") },
            text = { Text("${productToDelete?.name}を削除しますか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        productToDelete?.let {
                            val updatedOrder = order.removeProduct(it)
                            onOrderChange(updatedOrder)
                        }
                        showDialog = false
                        productToDelete = null
                    }
                ) {
                    Text("はい")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        productToDelete = null
                    }
                ) {
                    Text("いいえ")
                }
            }
        )
    }

    

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("注文内容のエクスポート") },
            text = { Text(formatOrderForEmail(order, taxRate, totalPrice, tax, totalPriceWithTax)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        shareOrderViaEmail(context, formatOrderForEmail(order, taxRate, totalPrice, tax, totalPriceWithTax))
                        showExportDialog = false
                        onContinueShopping() // 共有後、商品リストに戻る
                        onOrderChange(Order()) // 注文をクリア
                    }
                ) {
                    Text("共有")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExportDialog = false
                        onContinueShopping() // キャンセル後、商品リストに戻る
                        onOrderChange(Order()) // 注文をクリア
                    }
                ) {
                    Text("キャンセル")
                }
            }
        )
    }

    Column(Modifier.fillMaxSize()) {
        Text(text = "注文内容", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(order.lines) { orderLine ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp), // Reduced vertical padding
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp), // Reduced padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = orderLine.product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = "単価: ¥${orderLine.product.price} / ${orderLine.product.amount}${orderLine.product.unit}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "金額: ¥${orderLine.product.price * orderLine.quantity}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "合計量: ${orderLine.product.amount * orderLine.quantity}${orderLine.product.unit}", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            var quantityText by remember(orderLine.quantity) { mutableStateOf(orderLine.quantity.toString()) }
                            OutlinedTextField(
                                value = quantityText,
                                onValueChange = {
                                    quantityText = it.filter { char -> char.isDigit() }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                singleLine = true,
                                modifier = Modifier.width(80.dp) // Reduced width
                            )
                            Button(onClick = {
                                val newQuantity = quantityText.toIntOrNull() ?: 0
                                val updatedOrder = order.updateProductQuantity(orderLine.product, newQuantity)
                                onOrderChange(updatedOrder)
                            },
                            modifier = Modifier.padding(start = 8.dp)) {
                                Text("変更")
                            }
                        }
                        IconButton(onClick = {
                            productToDelete = orderLine.product
                            showDialog = true
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "削除")
                        }
                    }
                }
            }
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    OutlinedTextField(
                        value = taxRate.toString(),
                        onValueChange = { viewModel.setTaxRate(it.toIntOrNull() ?: 0) },
                        label = { Text("税率(%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "合計: ¥$totalPrice", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = "消費税: ¥$tax", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = "税込合計: ¥$totalPriceWithTax", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = onContinueShopping) {
                Text(text = "注文を続ける")
            }
            Button(onClick = {
                onPlaceOrder() // まず発注履歴に保存
                showExportDialog = true // その後共有ダイアログを表示
            }) {
                Text(text = "発注")
            }
        }
    }
}

fun formatOrderForEmail(order: Order, taxRate: Int, totalPrice: Int, tax: Int, totalPriceWithTax: Int): String {
    val stringBuilder = StringBuilder()
    stringBuilder.append("注文内容\n")
    stringBuilder.append("------------------------------------\n")
    order.lines.forEachIndexed { index, orderLine ->
        stringBuilder.append("${index + 1}. ${orderLine.product.name} ${orderLine.product.amount}${orderLine.product.unit} x ${orderLine.quantity}\n")
    }
    stringBuilder.append("------------------------------------\n")
    return stringBuilder.toString()
}

fun shareOrderViaEmail(context: Context, orderDetails: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "注文内容")
        putExtra(Intent.EXTRA_TEXT, orderDetails)
    }
    context.startActivity(Intent.createChooser(intent, "メールで注文内容を共有"))
}
