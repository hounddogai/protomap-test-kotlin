package com.acme.transport.fleetops.inspection.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection bundle. */
data class InspectionBundleView(
    var codeText: String,
    var name: String,
    var fuelLiters: String,
    var batchSizeText: String,
    var segment: String,
    var shiftCode: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toInspectionBundleView(item: InspectionBundle): InspectionBundleView {
    return InspectionBundleView(
        codeText = item.code,
        name = item.name,
        fuelLiters = "%.2f".format(item.fuelLiters),
        batchSizeText = item.batchSize.toString(),
        segment = truncate(item.segment, 120),
        shiftCode = item.shiftCode,
        trailerId = truncate(item.trailerId, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionBundleViews(items: MutableList<InspectionBundle>): MutableList<InspectionBundleView> {
    val views = mutableListOf<InspectionBundleView>()
    for (item in items) {
        views.add(toInspectionBundleView(item))
    }
    return views
}

fun sumInspectionBundleView(view: InspectionBundleView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedFuelLiters + parsedBatchSize.toDouble()
}
