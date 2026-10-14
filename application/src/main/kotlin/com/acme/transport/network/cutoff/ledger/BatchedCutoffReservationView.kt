package com.acme.transport.network.cutoff.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched cutoff reservation. */
data class BatchedCutoffReservationView(
    var code: String,
    var name: String,
    var fuelLiters: String,
    var externalRef: String,
    var driverId: String,
    var channelText: String,
    var statusLabel: String,
)

fun toBatchedCutoffReservationView(item: BatchedCutoffReservation): BatchedCutoffReservationView {
    return BatchedCutoffReservationView(
        code = item.code,
        name = item.name,
        fuelLiters = "%.2f".format(item.fuelLiters),
        externalRef = item.externalRef,
        driverId = item.driverId,
        channelText = truncate(item.channel, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedCutoffReservationViews(items: MutableList<BatchedCutoffReservation>): MutableList<BatchedCutoffReservationView> {
    val views = mutableListOf<BatchedCutoffReservationView>()
    for (item in items) {
        views.add(toBatchedCutoffReservationView(item))
    }
    return views
}

fun sumBatchedCutoffReservationView(view: BatchedCutoffReservationView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedFuelLiters
}
