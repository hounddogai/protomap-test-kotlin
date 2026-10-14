package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard threshold. */
data class TrailerYardThresholdView(
    var code: String,
    var titleText: String,
    var pallets: String,
    var approved: String,
    var etaMinutes: String,
    var carrier: String,
    var statusLabel: String,
)

fun toTrailerYardThresholdView(item: TrailerYardThreshold): TrailerYardThresholdView {
    return TrailerYardThresholdView(
        code = item.code,
        titleText = item.title,
        pallets = item.pallets.toString(),
        approved = "${item.approved}",
        etaMinutes = item.etaMinutes.toString(),
        carrier = truncate(item.carrier, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardThresholdViews(items: MutableList<TrailerYardThreshold>): MutableList<TrailerYardThresholdView> {
    val views = mutableListOf<TrailerYardThresholdView>()
    for (item in items) {
        views.add(toTrailerYardThresholdView(item))
    }
    return views
}

fun sumTrailerYardThresholdView(view: TrailerYardThresholdView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedPallets.toDouble() + parsedEtaMinutes.toDouble()
}
