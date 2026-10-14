package com.acme.transport.emissions.idling.idlingwindow

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched idling threshold. */
data class BatchedIdlingThresholdView(
    var codeText: String,
    var name: String,
    var contactEmail: String,
    var lane: String,
    var batchSize: String,
    var externalRef: String,
    var notes: String,
    var fuelLiters: String,
    var depotText: String,
    var statusLabel: String,
)

fun toBatchedIdlingThresholdView(item: BatchedIdlingThreshold): BatchedIdlingThresholdView {
    return BatchedIdlingThresholdView(
        codeText = item.code,
        name = item.name,
        contactEmail = item.contactEmail,
        lane = item.lane,
        batchSize = item.batchSize.toString(),
        externalRef = item.externalRef,
        notes = truncate(item.notes, 40),
        fuelLiters = "%.2f".format(item.fuelLiters),
        depotText = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedIdlingThresholdViews(items: MutableList<BatchedIdlingThreshold>): MutableList<BatchedIdlingThresholdView> {
    val views = mutableListOf<BatchedIdlingThresholdView>()
    for (item in items) {
        views.add(toBatchedIdlingThresholdView(item))
    }
    return views
}

fun sumBatchedIdlingThresholdView(view: BatchedIdlingThresholdView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedBatchSize.toDouble() + parsedFuelLiters
}
