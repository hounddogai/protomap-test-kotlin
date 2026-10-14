package com.acme.transport.safety.coaching.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered coaching proposal. */
data class TieredCoachingProposalView(
    var code: String,
    var nameText: String,
    var updatedAt: String,
    var checksum: String,
    var retriesText: String,
    var statusLabel: String,
)

fun toTieredCoachingProposalView(item: TieredCoachingProposal): TieredCoachingProposalView {
    return TieredCoachingProposalView(
        code = truncate(item.code, 80),
        nameText = truncate(item.name, 80),
        updatedAt = item.updatedAt.toString(),
        checksum = item.checksum,
        retriesText = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredCoachingProposalViews(items: MutableList<TieredCoachingProposal>): MutableList<TieredCoachingProposalView> {
    val views = mutableListOf<TieredCoachingProposalView>()
    for (item in items) {
        views.add(toTieredCoachingProposalView(item))
    }
    return views
}

fun sumTieredCoachingProposalView(view: TieredCoachingProposalView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedRetries = view.retriesText.toInt()
    return parsedUpdatedAt.toDouble() + parsedRetries.toDouble()
}
