package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual scorecard proposal. */
data class ManualScorecardProposalView(
    var code: String,
    var label: String,
    var contactEmail: String,
    var capacity: String,
    var limitText: String,
    var statusLabel: String,
)

fun toManualScorecardProposalView(item: ManualScorecardProposal): ManualScorecardProposalView {
    return ManualScorecardProposalView(
        code = item.code,
        label = item.label,
        contactEmail = truncate(item.contactEmail, 40),
        capacity = item.capacity.toString(),
        limitText = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualScorecardProposalViews(items: MutableList<ManualScorecardProposal>): MutableList<ManualScorecardProposalView> {
    val views = mutableListOf<ManualScorecardProposalView>()
    for (item in items) {
        views.add(toManualScorecardProposalView(item))
    }
    return views
}

fun sumManualScorecardProposalView(view: ManualScorecardProposalView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedLimit = view.limitText.toInt()
    return parsedCapacity.toDouble() + parsedLimit.toDouble()
}
