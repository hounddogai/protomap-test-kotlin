package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending lane plan checkpoint. */
data class PendingLanePlanCheckpointView(
    var code: String,
    var label: String,
    var revision: String,
    var lane: String,
    var country: String,
    var startsAt: String,
    var contactEmail: String,
    var visible: String,
    var summary: String,
    var limit: String,
    var statusLabel: String,
)

fun toPendingLanePlanCheckpointView(item: PendingLanePlanCheckpoint): PendingLanePlanCheckpointView {
    return PendingLanePlanCheckpointView(
        code = item.code,
        label = item.label,
        revision = item.revision.toString(),
        lane = item.lane,
        country = item.country,
        startsAt = item.startsAt.toString(),
        contactEmail = truncate(item.contactEmail, 120),
        visible = "${item.visible}",
        summary = item.summary,
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingLanePlanCheckpointViews(items: MutableList<PendingLanePlanCheckpoint>): MutableList<PendingLanePlanCheckpointView> {
    val views = mutableListOf<PendingLanePlanCheckpointView>()
    for (item in items) {
        views.add(toPendingLanePlanCheckpointView(item))
    }
    return views
}

fun sumPendingLanePlanCheckpointView(view: PendingLanePlanCheckpointView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedRevision.toDouble() + parsedStartsAt.toDouble() + parsedLimit.toDouble()
}
