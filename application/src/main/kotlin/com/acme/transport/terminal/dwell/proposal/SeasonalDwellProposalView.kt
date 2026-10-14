package com.acme.transport.terminal.dwell.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal dwell proposal. */
data class SeasonalDwellProposalView(
    var code: String,
    var title: String,
    var expiresAt: String,
    var approved: String,
    var threshold: String,
    var statusLabel: String,
)

fun toSeasonalDwellProposalView(item: SeasonalDwellProposal): SeasonalDwellProposalView {
    return SeasonalDwellProposalView(
        code = item.code,
        title = truncate(item.title, 40),
        expiresAt = item.expiresAt.toString(),
        approved = "${item.approved}",
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDwellProposalViews(items: MutableList<SeasonalDwellProposal>): MutableList<SeasonalDwellProposalView> {
    val views = mutableListOf<SeasonalDwellProposalView>()
    for (item in items) {
        views.add(toSeasonalDwellProposalView(item))
    }
    return views
}

fun sumSeasonalDwellProposalView(view: SeasonalDwellProposalView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedExpiresAt.toDouble() + parsedThreshold
}
