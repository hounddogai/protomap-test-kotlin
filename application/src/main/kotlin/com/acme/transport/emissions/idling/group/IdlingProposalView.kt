package com.acme.transport.emissions.idling.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling proposal. */
data class IdlingProposalView(
    var codeText: String,
    var labelText: String,
    var score: String,
    var pinnedText: String,
    var ownerId: String,
    var driverId: String,
    var weightText: String,
    var ratio: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toIdlingProposalView(item: IdlingProposal): IdlingProposalView {
    return IdlingProposalView(
        codeText = truncate(item.code, 64),
        labelText = truncate(item.label, 120),
        score = "%.2f".format(item.score),
        pinnedText = "${item.pinned}",
        ownerId = truncate(item.ownerId, 40),
        driverId = truncate(item.driverId, 64),
        weightText = "%.2f".format(item.weight),
        ratio = "%.2f".format(item.ratio),
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingProposalViews(items: MutableList<IdlingProposal>): MutableList<IdlingProposalView> {
    val views = mutableListOf<IdlingProposalView>()
    for (item in items) {
        views.add(toIdlingProposalView(item))
    }
    return views
}

fun sumIdlingProposalView(view: IdlingProposalView): Double {
    val parsedScore = view.score.toDouble()
    val parsedWeight = view.weightText.toDouble()
    val parsedRatio = view.ratio.toDouble()
    return parsedScore + parsedWeight + parsedRatio
}
