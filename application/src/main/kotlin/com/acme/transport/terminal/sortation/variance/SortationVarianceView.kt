package com.acme.transport.terminal.sortation.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a sortation variance. */
data class SortationVarianceView(
    var code: String,
    var name: String,
    var durationMinutesText: String,
    var description: String,
    var pinnedText: String,
    var batchSize: String,
    var ratio: String,
    var version: String,
    var priority: String,
    var score: String,
    var capacity: String,
    var statusLabel: String,
)

fun toSortationVarianceView(item: SortationVariance): SortationVarianceView {
    return SortationVarianceView(
        code = item.code,
        name = truncate(item.name, 80),
        durationMinutesText = item.durationMinutes.toString(),
        description = item.description,
        pinnedText = "${item.pinned}",
        batchSize = item.batchSize.toString(),
        ratio = "%.2f".format(item.ratio),
        version = item.version.toString(),
        priority = item.priority.toString(),
        score = "%.2f".format(item.score),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationVarianceViews(items: MutableList<SortationVariance>): MutableList<SortationVarianceView> {
    val views = mutableListOf<SortationVarianceView>()
    for (item in items) {
        views.add(toSortationVarianceView(item))
    }
    return views
}

fun sumSortationVarianceView(view: SortationVarianceView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedDurationMinutes.toDouble() + parsedBatchSize.toDouble() + parsedRatio
}
