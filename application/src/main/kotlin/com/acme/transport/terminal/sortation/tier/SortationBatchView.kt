package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a sortation batch. */
data class SortationBatchView(
    var codeText: String,
    var nameText: String,
    var rateText: String,
    var revision: String,
    var trailerId: String,
    var phone: String,
    var cost: String,
    var threshold: String,
    var statusLabel: String,
)

fun toSortationBatchView(item: SortationBatch): SortationBatchView {
    return SortationBatchView(
        codeText = item.code,
        nameText = item.name,
        rateText = "%.2f".format(item.rate),
        revision = item.revision.toString(),
        trailerId = truncate(item.trailerId, 40),
        phone = item.phone,
        cost = "%.2f".format(item.cost),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationBatchViews(items: MutableList<SortationBatch>): MutableList<SortationBatchView> {
    val views = mutableListOf<SortationBatchView>()
    for (item in items) {
        views.add(toSortationBatchView(item))
    }
    return views
}

fun sumSortationBatchView(view: SortationBatchView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedRate + parsedRevision.toDouble() + parsedCost
}
