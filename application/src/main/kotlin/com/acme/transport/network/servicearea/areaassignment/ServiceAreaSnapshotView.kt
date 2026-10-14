package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area snapshot. */
data class ServiceAreaSnapshotView(
    var code: String,
    var titleText: String,
    var cost: String,
    var regionText: String,
    var odometer: String,
    var version: String,
    var unit: String,
    var statusLabel: String,
)

fun toServiceAreaSnapshotView(item: ServiceAreaSnapshot): ServiceAreaSnapshotView {
    return ServiceAreaSnapshotView(
        code = truncate(item.code, 120),
        titleText = item.title,
        cost = "%.2f".format(item.cost),
        regionText = item.region,
        odometer = item.odometer.toString(),
        version = item.version.toString(),
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaSnapshotViews(items: MutableList<ServiceAreaSnapshot>): MutableList<ServiceAreaSnapshotView> {
    val views = mutableListOf<ServiceAreaSnapshotView>()
    for (item in items) {
        views.add(toServiceAreaSnapshotView(item))
    }
    return views
}

fun sumServiceAreaSnapshotView(view: ServiceAreaSnapshotView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedOdometer = view.odometer.toInt()
    val parsedVersion = view.version.toInt()
    return parsedCost + parsedOdometer.toDouble() + parsedVersion.toDouble()
}
