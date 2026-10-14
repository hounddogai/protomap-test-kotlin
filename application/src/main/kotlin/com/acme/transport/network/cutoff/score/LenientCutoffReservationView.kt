package com.acme.transport.network.cutoff.score

/** How screens and exports show a lenient cutoff reservation. */
data class LenientCutoffReservationView(
    var code: String,
    var title: String,
    var updatedAt: String,
    var sequence: String,
    var attempts: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toLenientCutoffReservationView(item: LenientCutoffReservation): LenientCutoffReservationView {
    return LenientCutoffReservationView(
        code = item.code,
        title = item.title,
        updatedAt = item.updatedAt.toString(),
        sequence = item.sequence.toString(),
        attempts = item.attempts.toString(),
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientCutoffReservationViews(items: MutableList<LenientCutoffReservation>): MutableList<LenientCutoffReservationView> {
    val views = mutableListOf<LenientCutoffReservationView>()
    for (item in items) {
        views.add(toLenientCutoffReservationView(item))
    }
    return views
}

fun sumLenientCutoffReservationView(view: LenientCutoffReservationView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedUpdatedAt.toDouble() + parsedSequence.toDouble() + parsedAttempts.toDouble()
}
