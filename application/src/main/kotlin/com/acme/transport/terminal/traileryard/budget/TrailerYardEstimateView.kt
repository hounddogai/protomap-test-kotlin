package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard estimate. */
data class TrailerYardEstimateView(
    var code: String,
    var label: String,
    var weight: String,
    var expiresAt: String,
    var fuelLiters: String,
    var checksumText: String,
    var shiftCode: String,
    var externalRefText: String,
    var ratio: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toTrailerYardEstimateView(item: TrailerYardEstimate): TrailerYardEstimateView {
    return TrailerYardEstimateView(
        code = truncate(item.code, 40),
        label = item.label,
        weight = "%.2f".format(item.weight),
        expiresAt = item.expiresAt.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        checksumText = item.checksum,
        shiftCode = item.shiftCode,
        externalRefText = item.externalRef,
        ratio = "%.2f".format(item.ratio),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardEstimateViews(items: MutableList<TrailerYardEstimate>): MutableList<TrailerYardEstimateView> {
    val views = mutableListOf<TrailerYardEstimateView>()
    for (item in items) {
        views.add(toTrailerYardEstimateView(item))
    }
    return views
}

fun sumTrailerYardEstimateView(view: TrailerYardEstimateView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedWeight + parsedExpiresAt.toDouble() + parsedFuelLiters
}
