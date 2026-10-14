package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional zone segment. */
data class RegionalZoneSegmentView(
    var code: String,
    var titleText: String,
    var unitText: String,
    var createdAt: String,
    var attempts: String,
    var quantity: String,
    var label: String,
    var minimum: String,
    var currency: String,
    var statusLabel: String,
)

fun toRegionalZoneSegmentView(item: RegionalZoneSegment): RegionalZoneSegmentView {
    return RegionalZoneSegmentView(
        code = item.code,
        titleText = item.title,
        unitText = truncate(item.unit, 80),
        createdAt = item.createdAt.toString(),
        attempts = item.attempts.toString(),
        quantity = item.quantity.toString(),
        label = truncate(item.label, 120),
        minimum = "%.2f".format(item.minimum),
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalZoneSegmentViews(items: MutableList<RegionalZoneSegment>): MutableList<RegionalZoneSegmentView> {
    val views = mutableListOf<RegionalZoneSegmentView>()
    for (item in items) {
        views.add(toRegionalZoneSegmentView(item))
    }
    return views
}

fun sumRegionalZoneSegmentView(view: RegionalZoneSegmentView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedCreatedAt.toDouble() + parsedAttempts.toDouble() + parsedQuantity.toDouble()
}
