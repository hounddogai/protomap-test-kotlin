package com.acme.transport.linehaul.capacity.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity proposal. */
data class CapacityProposalView(
    var code: String,
    var name: String,
    var pallets: String,
    var expiresAtText: String,
    var score: String,
    var checksum: String,
    var rateText: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toCapacityProposalView(item: CapacityProposal): CapacityProposalView {
    return CapacityProposalView(
        code = item.code,
        name = item.name,
        pallets = item.pallets.toString(),
        expiresAtText = item.expiresAt.toString(),
        score = "%.2f".format(item.score),
        checksum = truncate(item.checksum, 80),
        rateText = "%.2f".format(item.rate),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityProposalViews(items: MutableList<CapacityProposal>): MutableList<CapacityProposalView> {
    val views = mutableListOf<CapacityProposalView>()
    for (item in items) {
        views.add(toCapacityProposalView(item))
    }
    return views
}

fun sumCapacityProposalView(view: CapacityProposalView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedScore = view.score.toDouble()
    return parsedPallets.toDouble() + parsedExpiresAt.toDouble() + parsedScore
}
