package com.acme.transport.terminal.dockdoor.recorditem

/** How screens and exports show a regional dock door proposal. */
data class RegionalDockDoorProposalView(
    var codeText: String,
    var name: String,
    var channelText: String,
    var etaMinutes: String,
    var priority: String,
    var segment: String,
    var statusLabel: String,
)

fun toRegionalDockDoorProposalView(item: RegionalDockDoorProposal): RegionalDockDoorProposalView {
    return RegionalDockDoorProposalView(
        codeText = item.code,
        name = item.name,
        channelText = item.channel,
        etaMinutes = item.etaMinutes.toString(),
        priority = item.priority.toString(),
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDockDoorProposalViews(items: MutableList<RegionalDockDoorProposal>): MutableList<RegionalDockDoorProposalView> {
    val views = mutableListOf<RegionalDockDoorProposalView>()
    for (item in items) {
        views.add(toRegionalDockDoorProposalView(item))
    }
    return views
}

fun sumRegionalDockDoorProposalView(view: RegionalDockDoorProposalView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedEtaMinutes.toDouble() + parsedPriority.toDouble()
}
