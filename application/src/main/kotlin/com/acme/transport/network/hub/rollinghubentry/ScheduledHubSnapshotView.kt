package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled hub snapshot. */
data class ScheduledHubSnapshotView(
    var codeText: String,
    var labelText: String,
    var costText: String,
    var revision: String,
    var source: String,
    var statusLabel: String,
)

fun toScheduledHubSnapshotView(item: ScheduledHubSnapshot): ScheduledHubSnapshotView {
    return ScheduledHubSnapshotView(
        codeText = item.code,
        labelText = truncate(item.label, 40),
        costText = "%.2f".format(item.cost),
        revision = item.revision.toString(),
        source = truncate(item.source, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledHubSnapshotViews(items: MutableList<ScheduledHubSnapshot>): MutableList<ScheduledHubSnapshotView> {
    val views = mutableListOf<ScheduledHubSnapshotView>()
    for (item in items) {
        views.add(toScheduledHubSnapshotView(item))
    }
    return views
}

fun sumScheduledHubSnapshotView(view: ScheduledHubSnapshotView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedCost + parsedRevision.toDouble()
}
