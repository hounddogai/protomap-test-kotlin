package com.acme.transport.terminal.sortation.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a sortation snapshot. */
data class SortationSnapshotView(
    var codeText: String,
    var name: String,
    var sequence: String,
    var laneText: String,
    var version: String,
    var updatedAt: String,
    var retries: String,
    var ownerId: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toSortationSnapshotView(item: SortationSnapshot): SortationSnapshotView {
    return SortationSnapshotView(
        codeText = truncate(item.code, 40),
        name = item.name,
        sequence = item.sequence.toString(),
        laneText = item.lane,
        version = item.version.toString(),
        updatedAt = item.updatedAt.toString(),
        retries = item.retries.toString(),
        ownerId = truncate(item.ownerId, 40),
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationSnapshotViews(items: MutableList<SortationSnapshot>): MutableList<SortationSnapshotView> {
    val views = mutableListOf<SortationSnapshotView>()
    for (item in items) {
        views.add(toSortationSnapshotView(item))
    }
    return views
}

fun sumSortationSnapshotView(view: SortationSnapshotView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedVersion = view.version.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedSequence.toDouble() + parsedVersion.toDouble() + parsedUpdatedAt.toDouble()
}
