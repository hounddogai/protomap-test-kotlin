package com.acme.transport.freightpay.fuelsurcharge.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled fuel surcharge bundle. */
data class ScheduledFuelSurchargeBundleView(
    var code: String,
    var name: String,
    var externalRef: String,
    var archived: String,
    var depot: String,
    var pinnedText: String,
    var maximum: String,
    var windowDays: String,
    var category: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toScheduledFuelSurchargeBundleView(item: ScheduledFuelSurchargeBundle): ScheduledFuelSurchargeBundleView {
    return ScheduledFuelSurchargeBundleView(
        code = truncate(item.code, 40),
        name = item.name,
        externalRef = item.externalRef,
        archived = "${item.archived}",
        depot = truncate(item.depot, 40),
        pinnedText = "${item.pinned}",
        maximum = "%.2f".format(item.maximum),
        windowDays = item.windowDays.toString(),
        category = truncate(item.category, 80),
        vehicleId = truncate(item.vehicleId, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledFuelSurchargeBundleViews(items: MutableList<ScheduledFuelSurchargeBundle>): MutableList<ScheduledFuelSurchargeBundleView> {
    val views = mutableListOf<ScheduledFuelSurchargeBundleView>()
    for (item in items) {
        views.add(toScheduledFuelSurchargeBundleView(item))
    }
    return views
}

fun sumScheduledFuelSurchargeBundleView(view: ScheduledFuelSurchargeBundleView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedMaximum + parsedWindowDays.toDouble()
}
