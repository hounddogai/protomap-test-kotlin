package com.acme.transport.freightpay.dispute.disputemanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dispute mapping. */
data class DisputeMappingView(
    var code: String,
    var nameText: String,
    var pinnedText: String,
    var archivedText: String,
    var segmentText: String,
    var rate: String,
    var unit: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toDisputeMappingView(item: DisputeMapping): DisputeMappingView {
    return DisputeMappingView(
        code = truncate(item.code, 120),
        nameText = item.name,
        pinnedText = "${item.pinned}",
        archivedText = "${item.archived}",
        segmentText = truncate(item.segment, 64),
        rate = "%.2f".format(item.rate),
        unit = truncate(item.unit, 64),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeMappingViews(items: MutableList<DisputeMapping>): MutableList<DisputeMappingView> {
    val views = mutableListOf<DisputeMappingView>()
    for (item in items) {
        views.add(toDisputeMappingView(item))
    }
    return views
}

fun sumDisputeMappingView(view: DisputeMappingView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedRate + parsedStopCount.toDouble()
}
