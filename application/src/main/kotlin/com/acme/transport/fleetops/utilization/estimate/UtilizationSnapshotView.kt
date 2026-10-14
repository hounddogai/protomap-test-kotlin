package com.acme.transport.fleetops.utilization.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization snapshot. */
data class UtilizationSnapshotView(
    var codeText: String,
    var title: String,
    var revision: String,
    var laneText: String,
    var loadKg: String,
    var limit: String,
    var phone: String,
    var reviewer: String,
    var source: String,
    var description: String,
    var unitText: String,
    var statusLabel: String,
)

fun toUtilizationSnapshotView(item: UtilizationSnapshot): UtilizationSnapshotView {
    return UtilizationSnapshotView(
        codeText = truncate(item.code, 80),
        title = truncate(item.title, 64),
        revision = item.revision.toString(),
        laneText = truncate(item.lane, 64),
        loadKg = "%.2f".format(item.loadKg),
        limit = item.limit.toString(),
        phone = item.phone,
        reviewer = item.reviewer,
        source = item.source,
        description = item.description,
        unitText = truncate(item.unit, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationSnapshotViews(items: MutableList<UtilizationSnapshot>): MutableList<UtilizationSnapshotView> {
    val views = mutableListOf<UtilizationSnapshotView>()
    for (item in items) {
        views.add(toUtilizationSnapshotView(item))
    }
    return views
}

fun sumUtilizationSnapshotView(view: UtilizationSnapshotView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedRevision.toDouble() + parsedLoadKg + parsedLimit.toDouble()
}
