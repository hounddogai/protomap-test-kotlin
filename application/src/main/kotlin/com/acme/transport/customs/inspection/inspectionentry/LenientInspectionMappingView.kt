package com.acme.transport.customs.inspection.inspectionentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient inspection mapping. */
data class LenientInspectionMappingView(
    var code: String,
    var title: String,
    var fuelLitersText: String,
    var visible: String,
    var durationMinutes: String,
    var odometer: String,
    var rateText: String,
    var cost: String,
    var retriesText: String,
    var statusLabel: String,
)

fun toLenientInspectionMappingView(item: LenientInspectionMapping): LenientInspectionMappingView {
    return LenientInspectionMappingView(
        code = item.code,
        title = truncate(item.title, 80),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        visible = "${item.visible}",
        durationMinutes = item.durationMinutes.toString(),
        odometer = item.odometer.toString(),
        rateText = "%.2f".format(item.rate),
        cost = "%.2f".format(item.cost),
        retriesText = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientInspectionMappingViews(items: MutableList<LenientInspectionMapping>): MutableList<LenientInspectionMappingView> {
    val views = mutableListOf<LenientInspectionMappingView>()
    for (item in items) {
        views.add(toLenientInspectionMappingView(item))
    }
    return views
}

fun sumLenientInspectionMappingView(view: LenientInspectionMappingView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedFuelLiters + parsedDurationMinutes.toDouble() + parsedOdometer.toDouble()
}
