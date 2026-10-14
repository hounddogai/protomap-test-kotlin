package com.acme.transport.freightpay.accessorial.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling accessorial estimate. */
data class RollingAccessorialEstimateView(
    var codeText: String,
    var title: String,
    var ownerId: String,
    var description: String,
    var margin: String,
    var startsAt: String,
    var quantity: String,
    var unitText: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toRollingAccessorialEstimateView(item: RollingAccessorialEstimate): RollingAccessorialEstimateView {
    return RollingAccessorialEstimateView(
        codeText = item.code,
        title = truncate(item.title, 120),
        ownerId = item.ownerId,
        description = item.description,
        margin = "%.2f".format(item.margin),
        startsAt = item.startsAt.toString(),
        quantity = item.quantity.toString(),
        unitText = truncate(item.unit, 40),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingAccessorialEstimateViews(items: MutableList<RollingAccessorialEstimate>): MutableList<RollingAccessorialEstimateView> {
    val views = mutableListOf<RollingAccessorialEstimateView>()
    for (item in items) {
        views.add(toRollingAccessorialEstimateView(item))
    }
    return views
}

fun sumRollingAccessorialEstimateView(view: RollingAccessorialEstimateView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedMargin + parsedStartsAt.toDouble() + parsedQuantity.toDouble()
}
