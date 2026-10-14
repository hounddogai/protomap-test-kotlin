package com.acme.transport.freightpay.accessorial.metric

/** How screens and exports show a accessorial exception. */
data class AccessorialExceptionView(
    var code: String,
    var label: String,
    var priority: String,
    var lane: String,
    var quantityText: String,
    var discount: String,
    var statusLabel: String,
)

fun toAccessorialExceptionView(item: AccessorialException): AccessorialExceptionView {
    return AccessorialExceptionView(
        code = item.code,
        label = item.label,
        priority = item.priority.toString(),
        lane = item.lane,
        quantityText = item.quantity.toString(),
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialExceptionViews(items: MutableList<AccessorialException>): MutableList<AccessorialExceptionView> {
    val views = mutableListOf<AccessorialExceptionView>()
    for (item in items) {
        views.add(toAccessorialExceptionView(item))
    }
    return views
}

fun sumAccessorialExceptionView(view: AccessorialExceptionView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedQuantity = view.quantityText.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedPriority.toDouble() + parsedQuantity.toDouble() + parsedDiscount
}
