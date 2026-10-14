package com.acme.transport.customs.hold.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending hold proposal. */
data class PendingHoldProposalView(
    var code: String,
    var nameText: String,
    var labelText: String,
    var trailerId: String,
    var archived: String,
    var checksum: String,
    var weight: String,
    var windowDaysText: String,
    var channelText: String,
    var statusLabel: String,
)

fun toPendingHoldProposalView(item: PendingHoldProposal): PendingHoldProposalView {
    return PendingHoldProposalView(
        code = item.code,
        nameText = item.name,
        labelText = item.label,
        trailerId = item.trailerId,
        archived = "${item.archived}",
        checksum = item.checksum,
        weight = "%.2f".format(item.weight),
        windowDaysText = item.windowDays.toString(),
        channelText = truncate(item.channel, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingHoldProposalViews(items: MutableList<PendingHoldProposal>): MutableList<PendingHoldProposalView> {
    val views = mutableListOf<PendingHoldProposalView>()
    for (item in items) {
        views.add(toPendingHoldProposalView(item))
    }
    return views
}

fun sumPendingHoldProposalView(view: PendingHoldProposalView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedWeight + parsedWindowDays.toDouble()
}
