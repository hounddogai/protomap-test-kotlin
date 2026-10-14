package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity baseline. */
data class CapacityBaselineView(
    var code: String,
    var title: String,
    var quantity: String,
    var createdAt: String,
    var dock: String,
    var expiresAt: String,
    var region: String,
    var cost: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toCapacityBaselineView(item: CapacityBaseline): CapacityBaselineView {
    return CapacityBaselineView(
        code = item.code,
        title = item.title,
        quantity = item.quantity.toString(),
        createdAt = item.createdAt.toString(),
        dock = item.dock,
        expiresAt = item.expiresAt.toString(),
        region = truncate(item.region, 64),
        cost = "%.2f".format(item.cost),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityBaselineViews(items: MutableList<CapacityBaseline>): MutableList<CapacityBaselineView> {
    val views = mutableListOf<CapacityBaselineView>()
    for (item in items) {
        views.add(toCapacityBaselineView(item))
    }
    return views
}

fun sumCapacityBaselineView(view: CapacityBaselineView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedQuantity.toDouble() + parsedCreatedAt.toDouble() + parsedExpiresAt.toDouble()
}
