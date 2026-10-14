package com.acme.transport.safety.incident.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient incident proposal. */
data class LenientIncidentProposalView(
    var code: String,
    var name: String,
    var stopCountText: String,
    var externalRef: String,
    var country: String,
    var ownerIdText: String,
    var revision: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toLenientIncidentProposalView(item: LenientIncidentProposal): LenientIncidentProposalView {
    return LenientIncidentProposalView(
        code = item.code,
        name = truncate(item.name, 80),
        stopCountText = item.stopCount.toString(),
        externalRef = truncate(item.externalRef, 40),
        country = truncate(item.country, 40),
        ownerIdText = item.ownerId,
        revision = item.revision.toString(),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientIncidentProposalViews(items: MutableList<LenientIncidentProposal>): MutableList<LenientIncidentProposalView> {
    val views = mutableListOf<LenientIncidentProposalView>()
    for (item in items) {
        views.add(toLenientIncidentProposalView(item))
    }
    return views
}

fun sumLenientIncidentProposalView(view: LenientIncidentProposalView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedStopCount.toDouble() + parsedRevision.toDouble() + parsedFloorValue
}
