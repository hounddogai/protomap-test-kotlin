package com.acme.transport.network.transittime.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time batch. */
data class TransitTimeBatchView(
    var code: String,
    var name: String,
    var vehicleIdText: String,
    var loadKg: String,
    var odometer: String,
    var sequence: String,
    var statusLabel: String,
)

fun toTransitTimeBatchView(item: TransitTimeBatch): TransitTimeBatchView {
    return TransitTimeBatchView(
        code = item.code,
        name = item.name,
        vehicleIdText = truncate(item.vehicleId, 80),
        loadKg = "%.2f".format(item.loadKg),
        odometer = item.odometer.toString(),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeBatchViews(items: MutableList<TransitTimeBatch>): MutableList<TransitTimeBatchView> {
    val views = mutableListOf<TransitTimeBatchView>()
    for (item in items) {
        views.add(toTransitTimeBatchView(item))
    }
    return views
}

fun sumTransitTimeBatchView(view: TransitTimeBatchView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedOdometer = view.odometer.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedLoadKg + parsedOdometer.toDouble() + parsedSequence.toDouble()
}
