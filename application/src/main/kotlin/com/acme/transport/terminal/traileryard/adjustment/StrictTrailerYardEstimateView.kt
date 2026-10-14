package com.acme.transport.terminal.traileryard.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict trailer yard estimate. */
data class StrictTrailerYardEstimateView(
    var codeText: String,
    var name: String,
    var attempts: String,
    var loadKg: String,
    var expiresAtText: String,
    var odometer: String,
    var depot: String,
    var priority: String,
    var pallets: String,
    var lane: String,
    var statusLabel: String,
)

fun toStrictTrailerYardEstimateView(item: StrictTrailerYardEstimate): StrictTrailerYardEstimateView {
    return StrictTrailerYardEstimateView(
        codeText = item.code,
        name = item.name,
        attempts = item.attempts.toString(),
        loadKg = "%.2f".format(item.loadKg),
        expiresAtText = item.expiresAt.toString(),
        odometer = item.odometer.toString(),
        depot = truncate(item.depot, 40),
        priority = item.priority.toString(),
        pallets = item.pallets.toString(),
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictTrailerYardEstimateViews(items: MutableList<StrictTrailerYardEstimate>): MutableList<StrictTrailerYardEstimateView> {
    val views = mutableListOf<StrictTrailerYardEstimateView>()
    for (item in items) {
        views.add(toStrictTrailerYardEstimateView(item))
    }
    return views
}

fun sumStrictTrailerYardEstimateView(view: StrictTrailerYardEstimateView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedAttempts.toDouble() + parsedLoadKg + parsedExpiresAt.toDouble()
}
