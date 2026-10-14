package com.acme.transport.lastmile.locker.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted locker summary. */
data class WeightedLockerSummaryView(
    var codeText: String,
    var title: String,
    var ratio: String,
    var ceiling: String,
    var createdAt: String,
    var expiresAt: String,
    var pinned: String,
    var segment: String,
    var checksum: String,
    var statusLabel: String,
)

fun toWeightedLockerSummaryView(item: WeightedLockerSummary): WeightedLockerSummaryView {
    return WeightedLockerSummaryView(
        codeText = item.code,
        title = item.title,
        ratio = "%.2f".format(item.ratio),
        ceiling = "%.2f".format(item.ceiling),
        createdAt = item.createdAt.toString(),
        expiresAt = item.expiresAt.toString(),
        pinned = "${item.pinned}",
        segment = item.segment,
        checksum = truncate(item.checksum, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedLockerSummaryViews(items: MutableList<WeightedLockerSummary>): MutableList<WeightedLockerSummaryView> {
    val views = mutableListOf<WeightedLockerSummaryView>()
    for (item in items) {
        views.add(toWeightedLockerSummaryView(item))
    }
    return views
}

fun sumWeightedLockerSummaryView(view: WeightedLockerSummaryView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedRatio + parsedCeiling + parsedCreatedAt.toDouble()
}
