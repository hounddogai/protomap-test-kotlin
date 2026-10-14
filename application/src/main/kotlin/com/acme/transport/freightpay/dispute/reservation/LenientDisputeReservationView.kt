package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient dispute reservation. */
data class LenientDisputeReservationView(
    var codeText: String,
    var name: String,
    var version: String,
    var trailerId: String,
    var priority: String,
    var sourceText: String,
    var channel: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toLenientDisputeReservationView(item: LenientDisputeReservation): LenientDisputeReservationView {
    return LenientDisputeReservationView(
        codeText = item.code,
        name = item.name,
        version = item.version.toString(),
        trailerId = truncate(item.trailerId, 80),
        priority = item.priority.toString(),
        sourceText = truncate(item.source, 40),
        channel = item.channel,
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDisputeReservationViews(items: MutableList<LenientDisputeReservation>): MutableList<LenientDisputeReservationView> {
    val views = mutableListOf<LenientDisputeReservationView>()
    for (item in items) {
        views.add(toLenientDisputeReservationView(item))
    }
    return views
}

fun sumLenientDisputeReservationView(view: LenientDisputeReservationView): Double {
    val parsedVersion = view.version.toInt()
    val parsedPriority = view.priority.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedVersion.toDouble() + parsedPriority.toDouble() + parsedAttempts.toDouble()
}
