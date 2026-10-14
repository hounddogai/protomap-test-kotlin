package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard tier. */
data class TrailerYardTierView(
    var codeText: String,
    var name: String,
    var batchSize: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toTrailerYardTierView(item: TrailerYardTier): TrailerYardTierView {
    return TrailerYardTierView(
        codeText = truncate(item.code, 64),
        name = item.name,
        batchSize = item.batchSize.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardTierViews(items: MutableList<TrailerYardTier>): MutableList<TrailerYardTierView> {
    val views = mutableListOf<TrailerYardTierView>()
    for (item in items) {
        views.add(toTrailerYardTierView(item))
    }
    return views
}

fun sumTrailerYardTierView(view: TrailerYardTierView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedBatchSize.toDouble() + parsedFuelLiters
}
