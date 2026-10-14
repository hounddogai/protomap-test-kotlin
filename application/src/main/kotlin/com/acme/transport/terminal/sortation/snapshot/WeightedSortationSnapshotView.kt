package com.acme.transport.terminal.sortation.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted sortation snapshot. */
data class WeightedSortationSnapshotView(
    var codeText: String,
    var nameText: String,
    var endsAt: String,
    var limit: String,
    var trailerIdText: String,
    var loadKg: String,
    var summary: String,
    var statusLabel: String,
)

fun toWeightedSortationSnapshotView(item: WeightedSortationSnapshot): WeightedSortationSnapshotView {
    return WeightedSortationSnapshotView(
        codeText = item.code,
        nameText = item.name,
        endsAt = item.endsAt.toString(),
        limit = item.limit.toString(),
        trailerIdText = truncate(item.trailerId, 64),
        loadKg = "%.2f".format(item.loadKg),
        summary = truncate(item.summary, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedSortationSnapshotViews(items: MutableList<WeightedSortationSnapshot>): MutableList<WeightedSortationSnapshotView> {
    val views = mutableListOf<WeightedSortationSnapshotView>()
    for (item in items) {
        views.add(toWeightedSortationSnapshotView(item))
    }
    return views
}

fun sumWeightedSortationSnapshotView(view: WeightedSortationSnapshotView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedEndsAt.toDouble() + parsedLimit.toDouble() + parsedLoadKg
}
