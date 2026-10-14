package com.acme.transport.customs.clearance.clearancethreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient clearance threshold. */
data class LenientClearanceThresholdView(
    var code: String,
    var labelText: String,
    var routeCode: String,
    var externalRefText: String,
    var thresholdText: String,
    var dock: String,
    var fuelLiters: String,
    var windowDays: String,
    var archived: String,
    var statusLabel: String,
)

fun toLenientClearanceThresholdView(item: LenientClearanceThreshold): LenientClearanceThresholdView {
    return LenientClearanceThresholdView(
        code = truncate(item.code, 64),
        labelText = item.label,
        routeCode = truncate(item.routeCode, 40),
        externalRefText = item.externalRef,
        thresholdText = "%.2f".format(item.threshold),
        dock = truncate(item.dock, 80),
        fuelLiters = "%.2f".format(item.fuelLiters),
        windowDays = item.windowDays.toString(),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientClearanceThresholdViews(items: MutableList<LenientClearanceThreshold>): MutableList<LenientClearanceThresholdView> {
    val views = mutableListOf<LenientClearanceThresholdView>()
    for (item in items) {
        views.add(toLenientClearanceThresholdView(item))
    }
    return views
}

fun sumLenientClearanceThresholdView(view: LenientClearanceThresholdView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedThreshold + parsedFuelLiters + parsedWindowDays.toDouble()
}
