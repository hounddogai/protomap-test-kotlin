package com.acme.transport.network.hub.batchedhubmilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive hub milestone. */
data class AdaptiveHubMilestoneView(
    var code: String,
    var label: String,
    var limit: String,
    var pallets: String,
    var sequenceText: String,
    var maximumText: String,
    var odometer: String,
    var durationMinutes: String,
    var depot: String,
    var statusLabel: String,
)

fun toAdaptiveHubMilestoneView(item: AdaptiveHubMilestone): AdaptiveHubMilestoneView {
    return AdaptiveHubMilestoneView(
        code = item.code,
        label = truncate(item.label, 40),
        limit = item.limit.toString(),
        pallets = item.pallets.toString(),
        sequenceText = item.sequence.toString(),
        maximumText = "%.2f".format(item.maximum),
        odometer = item.odometer.toString(),
        durationMinutes = item.durationMinutes.toString(),
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveHubMilestoneViews(items: MutableList<AdaptiveHubMilestone>): MutableList<AdaptiveHubMilestoneView> {
    val views = mutableListOf<AdaptiveHubMilestoneView>()
    for (item in items) {
        views.add(toAdaptiveHubMilestoneView(item))
    }
    return views
}

fun sumAdaptiveHubMilestoneView(view: AdaptiveHubMilestoneView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedSequence = view.sequenceText.toInt()
    return parsedLimit.toDouble() + parsedPallets.toDouble() + parsedSequence.toDouble()
}
