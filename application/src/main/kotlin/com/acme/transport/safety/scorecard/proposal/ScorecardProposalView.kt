package com.acme.transport.safety.scorecard.proposal

/** How screens and exports show a scorecard proposal. */
data class ScorecardProposalView(
    var code: String,
    var label: String,
    var reviewerText: String,
    var externalRef: String,
    var lane: String,
    var etaMinutesText: String,
    var statusLabel: String,
)

fun toScorecardProposalView(item: ScorecardProposal): ScorecardProposalView {
    return ScorecardProposalView(
        code = item.code,
        label = item.label,
        reviewerText = item.reviewer,
        externalRef = item.externalRef,
        lane = item.lane,
        etaMinutesText = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardProposalViews(items: MutableList<ScorecardProposal>): MutableList<ScorecardProposalView> {
    val views = mutableListOf<ScorecardProposalView>()
    for (item in items) {
        views.add(toScorecardProposalView(item))
    }
    return views
}

fun sumScorecardProposalView(view: ScorecardProposalView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedEtaMinutes.toDouble()
}
