package com.acme.transport.customs.clearance.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance snapshot. */
data class ClearanceSnapshotView(
    var code: String,
    var title: String,
    var retries: String,
    var ceiling: String,
    var updatedAt: String,
    var scoreText: String,
    var archived: String,
    var statusLabel: String,
)

fun toClearanceSnapshotView(item: ClearanceSnapshot): ClearanceSnapshotView {
    return ClearanceSnapshotView(
        code = item.code,
        title = truncate(item.title, 80),
        retries = item.retries.toString(),
        ceiling = "%.2f".format(item.ceiling),
        updatedAt = item.updatedAt.toString(),
        scoreText = "%.2f".format(item.score),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceSnapshotViews(items: MutableList<ClearanceSnapshot>): MutableList<ClearanceSnapshotView> {
    val views = mutableListOf<ClearanceSnapshotView>()
    for (item in items) {
        views.add(toClearanceSnapshotView(item))
    }
    return views
}

fun sumClearanceSnapshotView(view: ClearanceSnapshotView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedRetries.toDouble() + parsedCeiling + parsedUpdatedAt.toDouble()
}
