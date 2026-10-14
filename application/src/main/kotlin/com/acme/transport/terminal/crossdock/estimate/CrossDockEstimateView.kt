package com.acme.transport.terminal.crossdock.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock estimate. */
data class CrossDockEstimateView(
    var code: String,
    var title: String,
    var floorValueText: String,
    var revisionText: String,
    var shiftCode: String,
    var score: String,
    var carrierText: String,
    var odometerText: String,
    var rate: String,
    var capacity: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toCrossDockEstimateView(item: CrossDockEstimate): CrossDockEstimateView {
    return CrossDockEstimateView(
        code = item.code,
        title = item.title,
        floorValueText = "%.2f".format(item.floorValue),
        revisionText = item.revision.toString(),
        shiftCode = item.shiftCode,
        score = "%.2f".format(item.score),
        carrierText = truncate(item.carrier, 120),
        odometerText = item.odometer.toString(),
        rate = "%.2f".format(item.rate),
        capacity = item.capacity.toString(),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockEstimateViews(items: MutableList<CrossDockEstimate>): MutableList<CrossDockEstimateView> {
    val views = mutableListOf<CrossDockEstimateView>()
    for (item in items) {
        views.add(toCrossDockEstimateView(item))
    }
    return views
}

fun sumCrossDockEstimateView(view: CrossDockEstimateView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedRevision = view.revisionText.toInt()
    val parsedScore = view.score.toDouble()
    return parsedFloorValue + parsedRevision.toDouble() + parsedScore
}
