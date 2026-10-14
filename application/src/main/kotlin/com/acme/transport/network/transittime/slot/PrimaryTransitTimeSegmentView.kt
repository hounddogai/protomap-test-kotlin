package com.acme.transport.network.transittime.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary transit time segment. */
data class PrimaryTransitTimeSegmentView(
    var code: String,
    var label: String,
    var ownerIdText: String,
    var etaMinutes: String,
    var budget: String,
    var source: String,
    var pinnedText: String,
    var archivedText: String,
    var revisionText: String,
    var description: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toPrimaryTransitTimeSegmentView(item: PrimaryTransitTimeSegment): PrimaryTransitTimeSegmentView {
    return PrimaryTransitTimeSegmentView(
        code = truncate(item.code, 64),
        label = item.label,
        ownerIdText = item.ownerId,
        etaMinutes = item.etaMinutes.toString(),
        budget = "%.2f".format(item.budget),
        source = truncate(item.source, 40),
        pinnedText = "${item.pinned}",
        archivedText = "${item.archived}",
        revisionText = item.revision.toString(),
        description = item.description,
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryTransitTimeSegmentViews(items: MutableList<PrimaryTransitTimeSegment>): MutableList<PrimaryTransitTimeSegmentView> {
    val views = mutableListOf<PrimaryTransitTimeSegmentView>()
    for (item in items) {
        views.add(toPrimaryTransitTimeSegmentView(item))
    }
    return views
}

fun sumPrimaryTransitTimeSegmentView(view: PrimaryTransitTimeSegmentView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedRevision = view.revisionText.toInt()
    return parsedEtaMinutes.toDouble() + parsedBudget + parsedRevision.toDouble()
}
