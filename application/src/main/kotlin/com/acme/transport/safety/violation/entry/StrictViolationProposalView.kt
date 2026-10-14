package com.acme.transport.safety.violation.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict violation proposal. */
data class StrictViolationProposalView(
    var code: String,
    var nameText: String,
    var createdAt: String,
    var scoreText: String,
    var ceilingText: String,
    var statusLabel: String,
)

fun toStrictViolationProposalView(item: StrictViolationProposal): StrictViolationProposalView {
    return StrictViolationProposalView(
        code = item.code,
        nameText = truncate(item.name, 80),
        createdAt = item.createdAt.toString(),
        scoreText = "%.2f".format(item.score),
        ceilingText = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictViolationProposalViews(items: MutableList<StrictViolationProposal>): MutableList<StrictViolationProposalView> {
    val views = mutableListOf<StrictViolationProposalView>()
    for (item in items) {
        views.add(toStrictViolationProposalView(item))
    }
    return views
}

fun sumStrictViolationProposalView(view: StrictViolationProposalView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedScore = view.scoreText.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedCreatedAt.toDouble() + parsedScore + parsedCeiling
}
