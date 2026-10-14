package com.acme.transport.crewing.swap.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive swap proposal. */
data class AdaptiveSwapProposalView(
    var code: String,
    var title: String,
    var summary: String,
    var graceDays: String,
    var lane: String,
    var minimum: String,
    var floorValue: String,
    var sequence: String,
    var contactEmail: String,
    var costText: String,
    var statusLabel: String,
)

fun toAdaptiveSwapProposalView(item: AdaptiveSwapProposal): AdaptiveSwapProposalView {
    return AdaptiveSwapProposalView(
        code = item.code,
        title = item.title,
        summary = truncate(item.summary, 120),
        graceDays = item.graceDays.toString(),
        lane = truncate(item.lane, 80),
        minimum = "%.2f".format(item.minimum),
        floorValue = "%.2f".format(item.floorValue),
        sequence = item.sequence.toString(),
        contactEmail = truncate(item.contactEmail, 120),
        costText = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveSwapProposalViews(items: MutableList<AdaptiveSwapProposal>): MutableList<AdaptiveSwapProposalView> {
    val views = mutableListOf<AdaptiveSwapProposalView>()
    for (item in items) {
        views.add(toAdaptiveSwapProposalView(item))
    }
    return views
}

fun sumAdaptiveSwapProposalView(view: AdaptiveSwapProposalView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedGraceDays.toDouble() + parsedMinimum + parsedFloorValue
}
