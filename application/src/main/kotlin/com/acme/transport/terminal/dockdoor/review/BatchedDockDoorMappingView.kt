package com.acme.transport.terminal.dockdoor.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched dock door mapping. */
data class BatchedDockDoorMappingView(
    var code: String,
    var title: String,
    var minimum: String,
    var sourceText: String,
    var rate: String,
    var descriptionText: String,
    var currency: String,
    var odometerText: String,
    var startsAt: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toBatchedDockDoorMappingView(item: BatchedDockDoorMapping): BatchedDockDoorMappingView {
    return BatchedDockDoorMappingView(
        code = truncate(item.code, 64),
        title = item.title,
        minimum = "%.2f".format(item.minimum),
        sourceText = item.source,
        rate = "%.2f".format(item.rate),
        descriptionText = item.description,
        currency = item.currency,
        odometerText = item.odometer.toString(),
        startsAt = item.startsAt.toString(),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedDockDoorMappingViews(items: MutableList<BatchedDockDoorMapping>): MutableList<BatchedDockDoorMappingView> {
    val views = mutableListOf<BatchedDockDoorMappingView>()
    for (item in items) {
        views.add(toBatchedDockDoorMappingView(item))
    }
    return views
}

fun sumBatchedDockDoorMappingView(view: BatchedDockDoorMappingView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedRate = view.rate.toDouble()
    val parsedOdometer = view.odometerText.toInt()
    return parsedMinimum + parsedRate + parsedOdometer.toDouble()
}
