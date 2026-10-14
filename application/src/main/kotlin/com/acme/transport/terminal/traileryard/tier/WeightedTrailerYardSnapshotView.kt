package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted trailer yard snapshot. */
data class WeightedTrailerYardSnapshotView(
    var code: String,
    var titleText: String,
    var quantity: String,
    var vehicleId: String,
    var batchSize: String,
    var unitText: String,
    var statusLabel: String,
)

fun toWeightedTrailerYardSnapshotView(item: WeightedTrailerYardSnapshot): WeightedTrailerYardSnapshotView {
    return WeightedTrailerYardSnapshotView(
        code = truncate(item.code, 40),
        titleText = item.title,
        quantity = item.quantity.toString(),
        vehicleId = truncate(item.vehicleId, 80),
        batchSize = item.batchSize.toString(),
        unitText = truncate(item.unit, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedTrailerYardSnapshotViews(items: MutableList<WeightedTrailerYardSnapshot>): MutableList<WeightedTrailerYardSnapshotView> {
    val views = mutableListOf<WeightedTrailerYardSnapshotView>()
    for (item in items) {
        views.add(toWeightedTrailerYardSnapshotView(item))
    }
    return views
}

fun sumWeightedTrailerYardSnapshotView(view: WeightedTrailerYardSnapshotView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedQuantity.toDouble() + parsedBatchSize.toDouble()
}
