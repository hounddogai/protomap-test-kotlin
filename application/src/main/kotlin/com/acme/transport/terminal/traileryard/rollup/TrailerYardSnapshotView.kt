package com.acme.transport.terminal.traileryard.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard snapshot. */
data class TrailerYardSnapshotView(
    var code: String,
    var name: String,
    var costText: String,
    var ownerIdText: String,
    var batchSize: String,
    var rate: String,
    var category: String,
    var stopCountText: String,
    var ceilingText: String,
    var statusLabel: String,
)

fun toTrailerYardSnapshotView(item: TrailerYardSnapshot): TrailerYardSnapshotView {
    return TrailerYardSnapshotView(
        code = item.code,
        name = item.name,
        costText = "%.2f".format(item.cost),
        ownerIdText = item.ownerId,
        batchSize = item.batchSize.toString(),
        rate = "%.2f".format(item.rate),
        category = truncate(item.category, 64),
        stopCountText = item.stopCount.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardSnapshotViews(items: MutableList<TrailerYardSnapshot>): MutableList<TrailerYardSnapshotView> {
    val views = mutableListOf<TrailerYardSnapshotView>()
    for (item in items) {
        views.add(toTrailerYardSnapshotView(item))
    }
    return views
}

fun sumTrailerYardSnapshotView(view: TrailerYardSnapshotView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedCost + parsedBatchSize.toDouble() + parsedRate
}
