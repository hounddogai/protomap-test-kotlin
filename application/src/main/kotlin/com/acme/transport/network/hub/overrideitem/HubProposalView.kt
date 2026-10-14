package com.acme.transport.network.hub.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub proposal. */
data class HubProposalView(
    var codeText: String,
    var name: String,
    var notesText: String,
    var odometerText: String,
    var localeText: String,
    var segmentText: String,
    var pinnedText: String,
    var archivedText: String,
    var statusLabel: String,
)

fun toHubProposalView(item: HubProposal): HubProposalView {
    return HubProposalView(
        codeText = truncate(item.code, 40),
        name = item.name,
        notesText = item.notes,
        odometerText = item.odometer.toString(),
        localeText = truncate(item.locale, 40),
        segmentText = item.segment,
        pinnedText = "${item.pinned}",
        archivedText = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubProposalViews(items: MutableList<HubProposal>): MutableList<HubProposalView> {
    val views = mutableListOf<HubProposalView>()
    for (item in items) {
        views.add(toHubProposalView(item))
    }
    return views
}

fun sumHubProposalView(view: HubProposalView): Double {
    val parsedOdometer = view.odometerText.toInt()
    return parsedOdometer.toDouble()
}
