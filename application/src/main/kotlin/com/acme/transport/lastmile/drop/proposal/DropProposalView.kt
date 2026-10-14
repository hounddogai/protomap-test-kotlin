package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop proposal. */
data class DropProposalView(
    var code: String,
    var label: String,
    var discount: String,
    var scoreText: String,
    var source: String,
    var pinned: String,
    var budget: String,
    var statusLabel: String,
)

fun toDropProposalView(item: DropProposal): DropProposalView {
    return DropProposalView(
        code = item.code,
        label = truncate(item.label, 64),
        discount = "%.2f".format(item.discount),
        scoreText = "%.2f".format(item.score),
        source = item.source,
        pinned = "${item.pinned}",
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropProposalViews(items: MutableList<DropProposal>): MutableList<DropProposalView> {
    val views = mutableListOf<DropProposalView>()
    for (item in items) {
        views.add(toDropProposalView(item))
    }
    return views
}

fun sumDropProposalView(view: DropProposalView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedScore = view.scoreText.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedDiscount + parsedScore + parsedBudget
}
