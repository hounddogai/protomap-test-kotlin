package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending dispute claim. */
data class PendingDisputeClaimView(
    var code: String,
    var name: String,
    var lane: String,
    var timezone: String,
    var odometer: String,
    var stopCount: String,
    var routeCode: String,
    var expiresAt: String,
    var notes: String,
    var statusLabel: String,
)

fun toPendingDisputeClaimView(item: PendingDisputeClaim): PendingDisputeClaimView {
    return PendingDisputeClaimView(
        code = item.code,
        name = item.name,
        lane = item.lane,
        timezone = item.timezone,
        odometer = item.odometer.toString(),
        stopCount = item.stopCount.toString(),
        routeCode = truncate(item.routeCode, 40),
        expiresAt = item.expiresAt.toString(),
        notes = truncate(item.notes, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDisputeClaimViews(items: MutableList<PendingDisputeClaim>): MutableList<PendingDisputeClaimView> {
    val views = mutableListOf<PendingDisputeClaimView>()
    for (item in items) {
        views.add(toPendingDisputeClaimView(item))
    }
    return views
}

fun sumPendingDisputeClaimView(view: PendingDisputeClaimView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedOdometer.toDouble() + parsedStopCount.toDouble() + parsedExpiresAt.toDouble()
}
