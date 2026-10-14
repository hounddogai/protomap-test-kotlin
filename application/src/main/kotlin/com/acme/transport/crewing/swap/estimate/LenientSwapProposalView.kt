package com.acme.transport.crewing.swap.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient swap proposal. */
data class LenientSwapProposalView(
    var code: String,
    var name: String,
    var createdAtText: String,
    var threshold: String,
    var weightText: String,
    var maximumText: String,
    var statusLabel: String,
)

fun toLenientSwapProposalView(item: LenientSwapProposal): LenientSwapProposalView {
    return LenientSwapProposalView(
        code = item.code,
        name = truncate(item.name, 80),
        createdAtText = item.createdAt.toString(),
        threshold = "%.2f".format(item.threshold),
        weightText = "%.2f".format(item.weight),
        maximumText = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientSwapProposalViews(items: MutableList<LenientSwapProposal>): MutableList<LenientSwapProposalView> {
    val views = mutableListOf<LenientSwapProposalView>()
    for (item in items) {
        views.add(toLenientSwapProposalView(item))
    }
    return views
}

fun sumLenientSwapProposalView(view: LenientSwapProposalView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedWeight = view.weightText.toDouble()
    return parsedCreatedAt.toDouble() + parsedThreshold + parsedWeight
}
