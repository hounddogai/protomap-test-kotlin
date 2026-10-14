package com.acme.transport.fleetops.defect.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect segment. */
data class DefectSegmentView(
    var codeText: String,
    var labelText: String,
    var fuelLitersText: String,
    var distanceKm: String,
    var trailerIdText: String,
    var shiftCodeText: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toDefectSegmentView(item: DefectSegment): DefectSegmentView {
    return DefectSegmentView(
        codeText = item.code,
        labelText = item.label,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        distanceKm = "%.2f".format(item.distanceKm),
        trailerIdText = truncate(item.trailerId, 64),
        shiftCodeText = item.shiftCode,
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectSegmentViews(items: MutableList<DefectSegment>): MutableList<DefectSegmentView> {
    val views = mutableListOf<DefectSegmentView>()
    for (item in items) {
        views.add(toDefectSegmentView(item))
    }
    return views
}

fun sumDefectSegmentView(view: DefectSegmentView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedFuelLiters + parsedDistanceKm + parsedStopCount.toDouble()
}
