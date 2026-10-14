package com.acme.transport.terminal.traileryard.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard batch. */
data class TrailerYardBatchView(
    var codeText: String,
    var name: String,
    var minimum: String,
    var palletsText: String,
    var approved: String,
    var weightText: String,
    var capacity: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toTrailerYardBatchView(item: TrailerYardBatch): TrailerYardBatchView {
    return TrailerYardBatchView(
        codeText = item.code,
        name = truncate(item.name, 40),
        minimum = "%.2f".format(item.minimum),
        palletsText = item.pallets.toString(),
        approved = "${item.approved}",
        weightText = "%.2f".format(item.weight),
        capacity = item.capacity.toString(),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardBatchViews(items: MutableList<TrailerYardBatch>): MutableList<TrailerYardBatchView> {
    val views = mutableListOf<TrailerYardBatchView>()
    for (item in items) {
        views.add(toTrailerYardBatchView(item))
    }
    return views
}

fun sumTrailerYardBatchView(view: TrailerYardBatchView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedPallets = view.palletsText.toInt()
    val parsedWeight = view.weightText.toDouble()
    return parsedMinimum + parsedPallets.toDouble() + parsedWeight
}
