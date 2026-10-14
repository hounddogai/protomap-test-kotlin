package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional redelivery entry. */
data class RegionalRedeliveryEntryView(
    var code: String,
    var labelText: String,
    var windowDaysText: String,
    var checksum: String,
    var distanceKmText: String,
    var routeCode: String,
    var quantityText: String,
    var budget: String,
    var statusLabel: String,
)

fun toRegionalRedeliveryEntryView(item: RegionalRedeliveryEntry): RegionalRedeliveryEntryView {
    return RegionalRedeliveryEntryView(
        code = item.code,
        labelText = item.label,
        windowDaysText = item.windowDays.toString(),
        checksum = item.checksum,
        distanceKmText = "%.2f".format(item.distanceKm),
        routeCode = truncate(item.routeCode, 80),
        quantityText = item.quantity.toString(),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalRedeliveryEntryViews(items: MutableList<RegionalRedeliveryEntry>): MutableList<RegionalRedeliveryEntryView> {
    val views = mutableListOf<RegionalRedeliveryEntryView>()
    for (item in items) {
        views.add(toRegionalRedeliveryEntryView(item))
    }
    return views
}

fun sumRegionalRedeliveryEntryView(view: RegionalRedeliveryEntryView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    return parsedWindowDays.toDouble() + parsedDistanceKm + parsedQuantity.toDouble()
}
