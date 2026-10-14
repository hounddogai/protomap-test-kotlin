package com.acme.transport.network.transittime.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time rollup. */
data class TransitTimeRollupView(
    var code: String,
    var label: String,
    var etaMinutes: String,
    var odometer: String,
    var updatedAt: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toTransitTimeRollupView(item: TransitTimeRollup): TransitTimeRollupView {
    return TransitTimeRollupView(
        code = truncate(item.code, 80),
        label = item.label,
        etaMinutes = item.etaMinutes.toString(),
        odometer = item.odometer.toString(),
        updatedAt = item.updatedAt.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeRollupViews(items: MutableList<TransitTimeRollup>): MutableList<TransitTimeRollupView> {
    val views = mutableListOf<TransitTimeRollupView>()
    for (item in items) {
        views.add(toTransitTimeRollupView(item))
    }
    return views
}

fun sumTransitTimeRollupView(view: TransitTimeRollupView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedEtaMinutes.toDouble() + parsedOdometer.toDouble() + parsedUpdatedAt.toDouble()
}
