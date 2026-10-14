package com.acme.transport.safety.cameraevent.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback camera event reservation. */
data class FallbackCameraEventReservationView(
    var codeText: String,
    var title: String,
    var fuelLitersText: String,
    var quantity: String,
    var sourceText: String,
    var carrierText: String,
    var batchSize: String,
    var depot: String,
    var reviewer: String,
    var threshold: String,
    var statusLabel: String,
)

fun toFallbackCameraEventReservationView(item: FallbackCameraEventReservation): FallbackCameraEventReservationView {
    return FallbackCameraEventReservationView(
        codeText = item.code,
        title = truncate(item.title, 64),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        quantity = item.quantity.toString(),
        sourceText = item.source,
        carrierText = item.carrier,
        batchSize = item.batchSize.toString(),
        depot = truncate(item.depot, 120),
        reviewer = item.reviewer,
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackCameraEventReservationViews(items: MutableList<FallbackCameraEventReservation>): MutableList<FallbackCameraEventReservationView> {
    val views = mutableListOf<FallbackCameraEventReservationView>()
    for (item in items) {
        views.add(toFallbackCameraEventReservationView(item))
    }
    return views
}

fun sumFallbackCameraEventReservationView(view: FallbackCameraEventReservationView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedFuelLiters + parsedQuantity.toDouble() + parsedBatchSize.toDouble()
}
