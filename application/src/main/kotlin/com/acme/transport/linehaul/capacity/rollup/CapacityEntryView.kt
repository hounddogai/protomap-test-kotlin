package com.acme.transport.linehaul.capacity.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity entry. */
data class CapacityEntryView(
    var code: String,
    var title: String,
    var driverId: String,
    var ceiling: String,
    var category: String,
    var quantity: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toCapacityEntryView(item: CapacityEntry): CapacityEntryView {
    return CapacityEntryView(
        code = item.code,
        title = truncate(item.title, 64),
        driverId = item.driverId,
        ceiling = "%.2f".format(item.ceiling),
        category = item.category,
        quantity = item.quantity.toString(),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityEntryViews(items: MutableList<CapacityEntry>): MutableList<CapacityEntryView> {
    val views = mutableListOf<CapacityEntryView>()
    for (item in items) {
        views.add(toCapacityEntryView(item))
    }
    return views
}

fun sumCapacityEntryView(view: CapacityEntryView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedCeiling + parsedQuantity.toDouble() + parsedWindowDays.toDouble()
}
