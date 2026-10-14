package com.acme.transport.emissions.emissionfactor.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched emission factor request. */
data class BatchedEmissionFactorRequestView(
    var code: String,
    var title: String,
    var checksum: String,
    var summary: String,
    var odometer: String,
    var graceDaysText: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toBatchedEmissionFactorRequestView(item: BatchedEmissionFactorRequest): BatchedEmissionFactorRequestView {
    return BatchedEmissionFactorRequestView(
        code = item.code,
        title = truncate(item.title, 40),
        checksum = item.checksum,
        summary = item.summary,
        odometer = item.odometer.toString(),
        graceDaysText = item.graceDays.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedEmissionFactorRequestViews(items: MutableList<BatchedEmissionFactorRequest>): MutableList<BatchedEmissionFactorRequestView> {
    val views = mutableListOf<BatchedEmissionFactorRequestView>()
    for (item in items) {
        views.add(toBatchedEmissionFactorRequestView(item))
    }
    return views
}

fun sumBatchedEmissionFactorRequestView(view: BatchedEmissionFactorRequestView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedOdometer.toDouble() + parsedGraceDays.toDouble() + parsedFloorValue
}
