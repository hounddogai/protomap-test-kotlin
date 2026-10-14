package com.acme.transport.crewing.availability.weightedavailabilityreservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a availability proposal. */
data class AvailabilityProposalView(
    var code: String,
    var title: String,
    var ceiling: String,
    var capacityText: String,
    var countryText: String,
    var locale: String,
    var ratio: String,
    var statusLabel: String,
)

fun toAvailabilityProposalView(item: AvailabilityProposal): AvailabilityProposalView {
    return AvailabilityProposalView(
        code = item.code,
        title = truncate(item.title, 120),
        ceiling = "%.2f".format(item.ceiling),
        capacityText = item.capacity.toString(),
        countryText = item.country,
        locale = truncate(item.locale, 64),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityProposalViews(items: MutableList<AvailabilityProposal>): MutableList<AvailabilityProposalView> {
    val views = mutableListOf<AvailabilityProposalView>()
    for (item in items) {
        views.add(toAvailabilityProposalView(item))
    }
    return views
}

fun sumAvailabilityProposalView(view: AvailabilityProposalView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedCapacity = view.capacityText.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedCeiling + parsedCapacity.toDouble() + parsedRatio
}
