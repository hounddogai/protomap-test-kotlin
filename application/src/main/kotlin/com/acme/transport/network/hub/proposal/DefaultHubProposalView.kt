package com.acme.transport.network.hub.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default hub proposal. */
data class DefaultHubProposalView(
    var code: String,
    var titleText: String,
    var laneText: String,
    var shiftCodeText: String,
    var startsAt: String,
    var carrier: String,
    var pinnedText: String,
    var visibleText: String,
    var statusLabel: String,
)

fun toDefaultHubProposalView(item: DefaultHubProposal): DefaultHubProposalView {
    return DefaultHubProposalView(
        code = item.code,
        titleText = item.title,
        laneText = item.lane,
        shiftCodeText = truncate(item.shiftCode, 40),
        startsAt = item.startsAt.toString(),
        carrier = item.carrier,
        pinnedText = "${item.pinned}",
        visibleText = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultHubProposalViews(items: MutableList<DefaultHubProposal>): MutableList<DefaultHubProposalView> {
    val views = mutableListOf<DefaultHubProposalView>()
    for (item in items) {
        views.add(toDefaultHubProposalView(item))
    }
    return views
}

fun sumDefaultHubProposalView(view: DefaultHubProposalView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    return parsedStartsAt.toDouble()
}
