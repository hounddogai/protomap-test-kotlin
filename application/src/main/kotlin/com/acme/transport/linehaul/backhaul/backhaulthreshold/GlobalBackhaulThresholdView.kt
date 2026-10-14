package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global backhaul threshold. */
data class GlobalBackhaulThresholdView(
    var code: String,
    var name: String,
    var odometer: String,
    var visibleText: String,
    var category: String,
    var distanceKmText: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toGlobalBackhaulThresholdView(item: GlobalBackhaulThreshold): GlobalBackhaulThresholdView {
    return GlobalBackhaulThresholdView(
        code = item.code,
        name = truncate(item.name, 120),
        odometer = item.odometer.toString(),
        visibleText = "${item.visible}",
        category = item.category,
        distanceKmText = "%.2f".format(item.distanceKm),
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalBackhaulThresholdViews(items: MutableList<GlobalBackhaulThreshold>): MutableList<GlobalBackhaulThresholdView> {
    val views = mutableListOf<GlobalBackhaulThresholdView>()
    for (item in items) {
        views.add(toGlobalBackhaulThresholdView(item))
    }
    return views
}

fun sumGlobalBackhaulThresholdView(view: GlobalBackhaulThresholdView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedOdometer.toDouble() + parsedDistanceKm + parsedCreatedAt.toDouble()
}
