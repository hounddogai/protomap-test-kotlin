package com.acme.transport.fleetops.tire.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire bundle. */
data class TireBundleView(
    var code: String,
    var nameText: String,
    var source: String,
    var routeCode: String,
    var durationMinutes: String,
    var region: String,
    var odometer: String,
    var statusLabel: String,
)

fun toTireBundleView(item: TireBundle): TireBundleView {
    return TireBundleView(
        code = truncate(item.code, 64),
        nameText = truncate(item.name, 64),
        source = item.source,
        routeCode = item.routeCode,
        durationMinutes = item.durationMinutes.toString(),
        region = truncate(item.region, 40),
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireBundleViews(items: MutableList<TireBundle>): MutableList<TireBundleView> {
    val views = mutableListOf<TireBundleView>()
    for (item in items) {
        views.add(toTireBundleView(item))
    }
    return views
}

fun sumTireBundleView(view: TireBundleView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedDurationMinutes.toDouble() + parsedOdometer.toDouble()
}
