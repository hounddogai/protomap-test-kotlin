package com.acme.transport.linehaul.trailer.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional trailer proposal. */
data class RegionalTrailerProposalView(
    var code: String,
    var nameText: String,
    var capacityText: String,
    var pinnedText: String,
    var country: String,
    var thresholdText: String,
    var loadKg: String,
    var fuelLitersText: String,
    var checksum: String,
    var statusLabel: String,
)

fun toRegionalTrailerProposalView(item: RegionalTrailerProposal): RegionalTrailerProposalView {
    return RegionalTrailerProposalView(
        code = item.code,
        nameText = truncate(item.name, 80),
        capacityText = item.capacity.toString(),
        pinnedText = "${item.pinned}",
        country = item.country,
        thresholdText = "%.2f".format(item.threshold),
        loadKg = "%.2f".format(item.loadKg),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalTrailerProposalViews(items: MutableList<RegionalTrailerProposal>): MutableList<RegionalTrailerProposalView> {
    val views = mutableListOf<RegionalTrailerProposalView>()
    for (item in items) {
        views.add(toRegionalTrailerProposalView(item))
    }
    return views
}

fun sumRegionalTrailerProposalView(view: RegionalTrailerProposalView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedCapacity.toDouble() + parsedThreshold + parsedLoadKg
}
