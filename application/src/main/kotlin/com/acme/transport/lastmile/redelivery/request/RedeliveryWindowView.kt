package com.acme.transport.lastmile.redelivery.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a redelivery window. */
data class RedeliveryWindowView(
    var code: String,
    var label: String,
    var archivedText: String,
    var active: String,
    var windowDays: String,
    var currency: String,
    var distanceKmText: String,
    var updatedAt: String,
    var limitText: String,
    var statusLabel: String,
)

fun toRedeliveryWindowView(item: RedeliveryWindow): RedeliveryWindowView {
    return RedeliveryWindowView(
        code = item.code,
        label = truncate(item.label, 40),
        archivedText = "${item.archived}",
        active = "${item.active}",
        windowDays = item.windowDays.toString(),
        currency = item.currency,
        distanceKmText = "%.2f".format(item.distanceKm),
        updatedAt = item.updatedAt.toString(),
        limitText = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryWindowViews(items: MutableList<RedeliveryWindow>): MutableList<RedeliveryWindowView> {
    val views = mutableListOf<RedeliveryWindowView>()
    for (item in items) {
        views.add(toRedeliveryWindowView(item))
    }
    return views
}

fun sumRedeliveryWindowView(view: RedeliveryWindowView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedWindowDays.toDouble() + parsedDistanceKm + parsedUpdatedAt.toDouble()
}
