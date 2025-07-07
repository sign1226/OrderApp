package com.example.orderapp.model

data class Order(
    val lines: List<OrderLine> = emptyList()
) {
    

    fun addProducts(productsToOrder: Map<Product, Int>): Order {
        val newLines = productsToOrder.map { (product, quantity) ->
            OrderLine(product, quantity)
        }
        val updatedLines = (this.lines + newLines)
            .groupBy { it.product.id }
            .map { (_, lines) ->
                lines.reduce { acc, orderLine ->
                    acc.copy(quantity = acc.quantity + orderLine.quantity)
                }
            }
        return this.copy(lines = updatedLines)
    }

    fun updateProductQuantity(product: Product, newQuantity: Int): Order {
        val updatedLines = if (newQuantity <= 0) {
            lines.filter { it.product.id != product.id }
        } else {
            lines.map { orderLine ->
                if (orderLine.product.id == product.id) {
                    orderLine.copy(quantity = newQuantity)
                } else {
                    orderLine
                }
            }
        }
        return this.copy(lines = updatedLines)
    }

    fun removeProduct(product: Product): Order {
        val updatedLines = lines.filter { it.product.id != product.id }
        return this.copy(lines = updatedLines)
    }
}
