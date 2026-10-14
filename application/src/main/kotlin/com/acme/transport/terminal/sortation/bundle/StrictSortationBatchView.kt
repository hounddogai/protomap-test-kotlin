package com.acme.transport.terminal.sortation.bundle

/** How screens and exports show a strict sortation batch. */
data class StrictSortationBatchView(
    var code: String,
    var title: String,
    var score: String,
    var externalRef: String,
    var trailerId: String,
    var capacity: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toStrictSortationBatchView(item: StrictSortationBatch): StrictSortationBatchView {
    return StrictSortationBatchView(
        code = item.code,
        title = item.title,
        score = "%.2f".format(item.score),
        externalRef = item.externalRef,
        trailerId = item.trailerId,
        capacity = item.capacity.toString(),
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictSortationBatchViews(items: MutableList<StrictSortationBatch>): MutableList<StrictSortationBatchView> {
    val views = mutableListOf<StrictSortationBatchView>()
    for (item in items) {
        views.add(toStrictSortationBatchView(item))
    }
    return views
}

fun sumStrictSortationBatchView(view: StrictSortationBatchView): Double {
    val parsedScore = view.score.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedScore + parsedCapacity.toDouble() + parsedUpdatedAt.toDouble()
}
