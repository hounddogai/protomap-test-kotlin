package com.acme.transport.safety.cameraevent.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending camera event proposal. */
data class PendingCameraEventProposalView(
    var code: String,
    var nameText: String,
    var capacity: String,
    var stopCount: String,
    var lane: String,
    var visible: String,
    var ceilingText: String,
    var shiftCode: String,
    var trailerId: String,
    var pinned: String,
    var statusLabel: String,
)

fun toPendingCameraEventProposalView(item: PendingCameraEventProposal): PendingCameraEventProposalView {
    return PendingCameraEventProposalView(
        code = truncate(item.code, 80),
        nameText = item.name,
        capacity = item.capacity.toString(),
        stopCount = item.stopCount.toString(),
        lane = item.lane,
        visible = "${item.visible}",
        ceilingText = "%.2f".format(item.ceiling),
        shiftCode = item.shiftCode,
        trailerId = item.trailerId,
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingCameraEventProposalViews(items: MutableList<PendingCameraEventProposal>): MutableList<PendingCameraEventProposalView> {
    val views = mutableListOf<PendingCameraEventProposalView>()
    for (item in items) {
        views.add(toPendingCameraEventProposalView(item))
    }
    return views
}

fun sumPendingCameraEventProposalView(view: PendingCameraEventProposalView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedCapacity.toDouble() + parsedStopCount.toDouble() + parsedCeiling
}
