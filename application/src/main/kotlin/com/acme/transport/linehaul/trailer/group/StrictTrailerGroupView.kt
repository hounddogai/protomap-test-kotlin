package com.acme.transport.linehaul.trailer.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict trailer group. */
data class StrictTrailerGroupView(
    var codeText: String,
    var name: String,
    var expiresAt: String,
    var visibleText: String,
    var driverIdText: String,
    var quantity: String,
    var summary: String,
    var durationMinutes: String,
    var minimum: String,
    var loadKgText: String,
    var budgetText: String,
    var statusLabel: String,
)

fun toStrictTrailerGroupView(item: StrictTrailerGroup): StrictTrailerGroupView {
    return StrictTrailerGroupView(
        codeText = item.code,
        name = truncate(item.name, 64),
        expiresAt = item.expiresAt.toString(),
        visibleText = "${item.visible}",
        driverIdText = item.driverId,
        quantity = item.quantity.toString(),
        summary = truncate(item.summary, 64),
        durationMinutes = item.durationMinutes.toString(),
        minimum = "%.2f".format(item.minimum),
        loadKgText = "%.2f".format(item.loadKg),
        budgetText = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictTrailerGroupViews(items: MutableList<StrictTrailerGroup>): MutableList<StrictTrailerGroupView> {
    val views = mutableListOf<StrictTrailerGroupView>()
    for (item in items) {
        views.add(toStrictTrailerGroupView(item))
    }
    return views
}

fun sumStrictTrailerGroupView(view: StrictTrailerGroupView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedExpiresAt.toDouble() + parsedQuantity.toDouble() + parsedDurationMinutes.toDouble()
}
