package com.acme.transport.emissions.routeoffset.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual route offset profile. */
data class ManualRouteOffsetProfileView(
    var codeText: String,
    var nameText: String,
    var externalRef: String,
    var currency: String,
    var odometerText: String,
    var quantityText: String,
    var depot: String,
    var statusLabel: String,
)

fun toManualRouteOffsetProfileView(item: ManualRouteOffsetProfile): ManualRouteOffsetProfileView {
    return ManualRouteOffsetProfileView(
        codeText = item.code,
        nameText = truncate(item.name, 40),
        externalRef = item.externalRef,
        currency = item.currency,
        odometerText = item.odometer.toString(),
        quantityText = item.quantity.toString(),
        depot = truncate(item.depot, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualRouteOffsetProfileViews(items: MutableList<ManualRouteOffsetProfile>): MutableList<ManualRouteOffsetProfileView> {
    val views = mutableListOf<ManualRouteOffsetProfileView>()
    for (item in items) {
        views.add(toManualRouteOffsetProfileView(item))
    }
    return views
}

fun sumManualRouteOffsetProfileView(view: ManualRouteOffsetProfileView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedQuantity = view.quantityText.toInt()
    return parsedOdometer.toDouble() + parsedQuantity.toDouble()
}
