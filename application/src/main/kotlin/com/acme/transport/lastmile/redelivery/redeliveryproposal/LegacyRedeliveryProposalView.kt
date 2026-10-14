package com.acme.transport.lastmile.redelivery.redeliveryproposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy redelivery proposal. */
data class LegacyRedeliveryProposalView(
    var code: String,
    var title: String,
    var description: String,
    var segment: String,
    var threshold: String,
    var ratio: String,
    var source: String,
    var depot: String,
    var loadKgText: String,
    var capacityText: String,
    var statusLabel: String,
)

fun toLegacyRedeliveryProposalView(item: LegacyRedeliveryProposal): LegacyRedeliveryProposalView {
    return LegacyRedeliveryProposalView(
        code = item.code,
        title = item.title,
        description = item.description,
        segment = item.segment,
        threshold = "%.2f".format(item.threshold),
        ratio = "%.2f".format(item.ratio),
        source = item.source,
        depot = truncate(item.depot, 64),
        loadKgText = "%.2f".format(item.loadKg),
        capacityText = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyRedeliveryProposalViews(items: MutableList<LegacyRedeliveryProposal>): MutableList<LegacyRedeliveryProposalView> {
    val views = mutableListOf<LegacyRedeliveryProposalView>()
    for (item in items) {
        views.add(toLegacyRedeliveryProposalView(item))
    }
    return views
}

fun sumLegacyRedeliveryProposalView(view: LegacyRedeliveryProposalView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedRatio = view.ratio.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedThreshold + parsedRatio + parsedLoadKg
}
