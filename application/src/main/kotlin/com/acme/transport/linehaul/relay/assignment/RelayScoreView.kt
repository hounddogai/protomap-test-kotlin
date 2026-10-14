package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a relay score. */
data class RelayScoreView(
    var code: String,
    var label: String,
    var fuelLitersText: String,
    var discount: String,
    var checksum: String,
    var loadKgText: String,
    var version: String,
    var endsAt: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toRelayScoreView(item: RelayScore): RelayScoreView {
    return RelayScoreView(
        code = truncate(item.code, 120),
        label = truncate(item.label, 40),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        discount = "%.2f".format(item.discount),
        checksum = item.checksum,
        loadKgText = "%.2f".format(item.loadKg),
        version = item.version.toString(),
        endsAt = item.endsAt.toString(),
        segmentText = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRelayScoreViews(items: MutableList<RelayScore>): MutableList<RelayScoreView> {
    val views = mutableListOf<RelayScoreView>()
    for (item in items) {
        views.add(toRelayScoreView(item))
    }
    return views
}

fun sumRelayScoreView(view: RelayScoreView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedDiscount = view.discount.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedFuelLiters + parsedDiscount + parsedLoadKg
}
