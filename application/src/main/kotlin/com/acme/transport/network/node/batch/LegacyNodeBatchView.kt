package com.acme.transport.network.node.batch

/** How screens and exports show a legacy node batch. */
data class LegacyNodeBatchView(
    var code: String,
    var title: String,
    var depot: String,
    var phoneText: String,
    var description: String,
    var distanceKm: String,
    var weightText: String,
    var batchSize: String,
    var archivedText: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toLegacyNodeBatchView(item: LegacyNodeBatch): LegacyNodeBatchView {
    return LegacyNodeBatchView(
        code = item.code,
        title = item.title,
        depot = item.depot,
        phoneText = item.phone,
        description = item.description,
        distanceKm = "%.2f".format(item.distanceKm),
        weightText = "%.2f".format(item.weight),
        batchSize = item.batchSize.toString(),
        archivedText = "${item.archived}",
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyNodeBatchViews(items: MutableList<LegacyNodeBatch>): MutableList<LegacyNodeBatchView> {
    val views = mutableListOf<LegacyNodeBatchView>()
    for (item in items) {
        views.add(toLegacyNodeBatchView(item))
    }
    return views
}

fun sumLegacyNodeBatchView(view: LegacyNodeBatchView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedWeight = view.weightText.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedDistanceKm + parsedWeight + parsedBatchSize.toDouble()
}
