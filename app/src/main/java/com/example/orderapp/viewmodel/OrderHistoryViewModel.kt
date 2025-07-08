package com.example.orderapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderapp.model.Order
import com.example.orderapp.model.OrderHistory
import com.example.orderapp.model.OrderHistoryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val repository: OrderHistoryRepository
) : ViewModel() {

    val orderHistories = repository.getAllOrderHistories()

    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message

    fun addOrderHistory(order: Order) = viewModelScope.launch {
        repository.addOrderHistory(order)
        _message.emit("注文履歴を追加しました")
    }

    fun deleteOrderHistory(orderHistory: OrderHistory) = viewModelScope.launch {
        repository.deleteOrderHistory(orderHistory.id)
        _message.emit("注文履歴を削除しました")
    }

    fun getOrderHistoryById(id: Long) = repository.getOrderHistoryById(id)
}