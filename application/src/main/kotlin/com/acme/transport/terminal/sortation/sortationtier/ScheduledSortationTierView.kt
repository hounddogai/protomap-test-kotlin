package com.acme.transport.terminal.sortation.sortationtier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled sortation tier. */
data class ScheduledSortationTierView(
    var codeText: String,
    var name: String,
    var score: String,
    var dock: String,
    var margin: String,
    var batchSize: String,
    var createdAt: String,
    var quantityText: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toScheduledSortationTierView(item: ScheduledSortationTier): ScheduledSortationTierView {
    return ScheduledSortationTierView(
        codeText = item.code,
        name = item.name,
        score = "%.2f".format(item.score),
        dock = truncate(item.dock, 120),
        margin = "%.2f".format(item.margin),
        batchSize = item.batchSize.toString(),
        createdAt = item.createdAt.toString(),
        quantityText = item.quantity.toString(),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledSortationTierViews(items: MutableList<ScheduledSortationTier>): MutableList<ScheduledSortationTierView> {
    val views = mutableListOf<ScheduledSortationTierView>()
    for (item in items) {
        views.add(toScheduledSortationTierView(item))
    }
    return views
}

fun sumScheduledSortationTierView(view: ScheduledSortationTierView): Double {
    val parsedScore = view.score.toDouble()
    val parsedMargin = view.margin.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedScore + parsedMargin + parsedBatchSize.toDouble()
}
