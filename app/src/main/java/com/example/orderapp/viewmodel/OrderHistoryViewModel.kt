package com.example.orderapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.orderapp.model.Order
import com.example.orderapp.model.OrderHistory
import com.example.orderapp.model.OrderHistoryRepository
import kotlinx.coroutines.launch

class OrderHistoryViewModel(application: Application) : ViewModel() {
    private val repository = OrderHistoryRepository(application)

    val orderHistories = repository.getAllOrderHistories()

    fun addOrderHistory(order: Order) = viewModelScope.launch {
        repository.addOrderHistory(order)
    }

    fun deleteOrderHistory(orderHistory: OrderHistory) = viewModelScope.launch {
        repository.deleteOrderHistory(orderHistory.id)
    }

    fun getOrderHistoryById(id: Long) = repository.getOrderHistoryById(id)
}

class OrderHistoryViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrderHistoryViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
