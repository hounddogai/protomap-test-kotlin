package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal route offset record. */
data class SeasonalRouteOffsetRecordView(
    var code: String,
    var name: String,
    var quantity: String,
    var weight: String,
    var source: String,
    var checksum: String,
    var stopCount: String,
    var reviewer: String,
    var pallets: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toSeasonalRouteOffsetRecordView(item: SeasonalRouteOffsetRecord): SeasonalRouteOffsetRecordView {
    return SeasonalRouteOffsetRecordView(
        code = item.code,
        name = truncate(item.name, 80),
        quantity = item.quantity.toString(),
        weight = "%.2f".format(item.weight),
        source = item.source,
        checksum = item.checksum,
        stopCount = item.stopCount.toString(),
        reviewer = item.reviewer,
        pallets = item.pallets.toString(),
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalRouteOffsetRecordViews(items: MutableList<SeasonalRouteOffsetRecord>): MutableList<SeasonalRouteOffsetRecordView> {
    val views = mutableListOf<SeasonalRouteOffsetRecordView>()
    for (item in items) {
        views.add(toSeasonalRouteOffsetRecordView(item))
    }
    return views
}

fun sumSeasonalRouteOffsetRecordView(view: SeasonalRouteOffsetRecordView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedQuantity.toDouble() + parsedWeight + parsedStopCount.toDouble()
}
