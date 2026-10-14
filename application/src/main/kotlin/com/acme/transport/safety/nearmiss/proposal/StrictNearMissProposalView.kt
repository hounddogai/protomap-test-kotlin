package com.acme.transport.safety.nearmiss.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict near miss proposal. */
data class StrictNearMissProposalView(
    var code: String,
    var titleText: String,
    var fuelLiters: String,
    var region: String,
    var reviewerText: String,
    var pallets: String,
    var weight: String,
    var revision: String,
    var source: String,
    var statusLabel: String,
)

fun toStrictNearMissProposalView(item: StrictNearMissProposal): StrictNearMissProposalView {
    return StrictNearMissProposalView(
        code = item.code,
        titleText = truncate(item.title, 64),
        fuelLiters = "%.2f".format(item.fuelLiters),
        region = truncate(item.region, 40),
        reviewerText = truncate(item.reviewer, 64),
        pallets = item.pallets.toString(),
        weight = "%.2f".format(item.weight),
        revision = item.revision.toString(),
        source = truncate(item.source, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictNearMissProposalViews(items: MutableList<StrictNearMissProposal>): MutableList<StrictNearMissProposalView> {
    val views = mutableListOf<StrictNearMissProposalView>()
    for (item in items) {
        views.add(toStrictNearMissProposalView(item))
    }
    return views
}

fun sumStrictNearMissProposalView(view: StrictNearMissProposalView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedFuelLiters + parsedPallets.toDouble() + parsedWeight
}
