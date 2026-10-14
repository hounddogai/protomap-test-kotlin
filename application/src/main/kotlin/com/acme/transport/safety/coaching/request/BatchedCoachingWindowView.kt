package com.acme.transport.safety.coaching.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched coaching window. */
data class BatchedCoachingWindowView(
    var codeText: String,
    var labelText: String,
    var channel: String,
    var odometerText: String,
    var minimumText: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toBatchedCoachingWindowView(item: BatchedCoachingWindow): BatchedCoachingWindowView {
    return BatchedCoachingWindowView(
        codeText = truncate(item.code, 40),
        labelText = item.label,
        channel = item.channel,
        odometerText = item.odometer.toString(),
        minimumText = "%.2f".format(item.minimum),
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedCoachingWindowViews(items: MutableList<BatchedCoachingWindow>): MutableList<BatchedCoachingWindowView> {
    val views = mutableListOf<BatchedCoachingWindowView>()
    for (item in items) {
        views.add(toBatchedCoachingWindowView(item))
    }
    return views
}

fun sumBatchedCoachingWindowView(view: BatchedCoachingWindowView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedOdometer.toDouble() + parsedMinimum + parsedDistanceKm
}
