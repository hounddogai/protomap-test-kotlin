package com.acme.transport.safety.cameraevent.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a camera event reservation. */
data class CameraEventReservationView(
    var code: String,
    var titleText: String,
    var batchSizeText: String,
    var reviewerText: String,
    var durationMinutes: String,
    var locale: String,
    var cost: String,
    var ratio: String,
    var endsAt: String,
    var palletsText: String,
    var notes: String,
    var statusLabel: String,
)

fun toCameraEventReservationView(item: CameraEventReservation): CameraEventReservationView {
    return CameraEventReservationView(
        code = item.code,
        titleText = item.title,
        batchSizeText = item.batchSize.toString(),
        reviewerText = truncate(item.reviewer, 80),
        durationMinutes = item.durationMinutes.toString(),
        locale = item.locale,
        cost = "%.2f".format(item.cost),
        ratio = "%.2f".format(item.ratio),
        endsAt = item.endsAt.toString(),
        palletsText = item.pallets.toString(),
        notes = truncate(item.notes, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventReservationViews(items: MutableList<CameraEventReservation>): MutableList<CameraEventReservationView> {
    val views = mutableListOf<CameraEventReservationView>()
    for (item in items) {
        views.add(toCameraEventReservationView(item))
    }
    return views
}

fun sumCameraEventReservationView(view: CameraEventReservationView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedBatchSize.toDouble() + parsedDurationMinutes.toDouble() + parsedCost
}
