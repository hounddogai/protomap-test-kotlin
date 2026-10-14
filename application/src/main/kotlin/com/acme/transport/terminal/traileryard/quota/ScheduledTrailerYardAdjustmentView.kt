package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled trailer yard adjustment. */
data class ScheduledTrailerYardAdjustmentView(
    var code: String,
    var name: String,
    var ownerId: String,
    var discount: String,
    var limitText: String,
    var dock: String,
    var segment: String,
    var carrier: String,
    var statusLabel: String,
)

fun toScheduledTrailerYardAdjustmentView(item: ScheduledTrailerYardAdjustment): ScheduledTrailerYardAdjustmentView {
    return ScheduledTrailerYardAdjustmentView(
        code = item.code,
        name = item.name,
        ownerId = truncate(item.ownerId, 80),
        discount = "%.2f".format(item.discount),
        limitText = item.limit.toString(),
        dock = item.dock,
        segment = truncate(item.segment, 120),
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledTrailerYardAdjustmentViews(items: MutableList<ScheduledTrailerYardAdjustment>): MutableList<ScheduledTrailerYardAdjustmentView> {
    val views = mutableListOf<ScheduledTrailerYardAdjustmentView>()
    for (item in items) {
        views.add(toScheduledTrailerYardAdjustmentView(item))
    }
    return views
}

fun sumScheduledTrailerYardAdjustmentView(view: ScheduledTrailerYardAdjustmentView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedLimit = view.limitText.toInt()
    return parsedDiscount + parsedLimit.toDouble()
}
