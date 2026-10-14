package com.acme.transport.emissions.idling.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling snapshot. */
data class IdlingSnapshotView(
    var codeText: String,
    var titleText: String,
    var segment: String,
    var createdAt: String,
    var revisionText: String,
    var region: String,
    var minimumText: String,
    var categoryText: String,
    var retriesText: String,
    var timezone: String,
    var statusLabel: String,
)

fun toIdlingSnapshotView(item: IdlingSnapshot): IdlingSnapshotView {
    return IdlingSnapshotView(
        codeText = item.code,
        titleText = truncate(item.title, 64),
        segment = item.segment,
        createdAt = item.createdAt.toString(),
        revisionText = item.revision.toString(),
        region = item.region,
        minimumText = "%.2f".format(item.minimum),
        categoryText = item.category,
        retriesText = item.retries.toString(),
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingSnapshotViews(items: MutableList<IdlingSnapshot>): MutableList<IdlingSnapshotView> {
    val views = mutableListOf<IdlingSnapshotView>()
    for (item in items) {
        views.add(toIdlingSnapshotView(item))
    }
    return views
}

fun sumIdlingSnapshotView(view: IdlingSnapshotView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedRevision = view.revisionText.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedCreatedAt.toDouble() + parsedRevision.toDouble() + parsedMinimum
}
