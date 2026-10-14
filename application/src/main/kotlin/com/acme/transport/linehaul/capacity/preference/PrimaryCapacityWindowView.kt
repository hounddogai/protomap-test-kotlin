package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary capacity window. */
data class PrimaryCapacityWindowView(
    var code: String,
    var title: String,
    var description: String,
    var retries: String,
    var segment: String,
    var quantity: String,
    var statusLabel: String,
)

fun toPrimaryCapacityWindowView(item: PrimaryCapacityWindow): PrimaryCapacityWindowView {
    return PrimaryCapacityWindowView(
        code = item.code,
        title = truncate(item.title, 120),
        description = truncate(item.description, 40),
        retries = item.retries.toString(),
        segment = item.segment,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryCapacityWindowViews(items: MutableList<PrimaryCapacityWindow>): MutableList<PrimaryCapacityWindowView> {
    val views = mutableListOf<PrimaryCapacityWindowView>()
    for (item in items) {
        views.add(toPrimaryCapacityWindowView(item))
    }
    return views
}

fun sumPrimaryCapacityWindowView(view: PrimaryCapacityWindowView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedRetries.toDouble() + parsedQuantity.toDouble()
}
