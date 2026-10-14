package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan snapshot. */
data class LanePlanSnapshotView(
    var code: String,
    var title: String,
    var shiftCodeText: String,
    var updatedAt: String,
    var depot: String,
    var minimumText: String,
    var revision: String,
    var etaMinutesText: String,
    var segment: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toLanePlanSnapshotView(item: LanePlanSnapshot): LanePlanSnapshotView {
    return LanePlanSnapshotView(
        code = item.code,
        title = item.title,
        shiftCodeText = truncate(item.shiftCode, 64),
        updatedAt = item.updatedAt.toString(),
        depot = item.depot,
        minimumText = "%.2f".format(item.minimum),
        revision = item.revision.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        segment = truncate(item.segment, 80),
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanSnapshotViews(items: MutableList<LanePlanSnapshot>): MutableList<LanePlanSnapshotView> {
    val views = mutableListOf<LanePlanSnapshotView>()
    for (item in items) {
        views.add(toLanePlanSnapshotView(item))
    }
    return views
}

fun sumLanePlanSnapshotView(view: LanePlanSnapshotView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedUpdatedAt.toDouble() + parsedMinimum + parsedRevision.toDouble()
}
