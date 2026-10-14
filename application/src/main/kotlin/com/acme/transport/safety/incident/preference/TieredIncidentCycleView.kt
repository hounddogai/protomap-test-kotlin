package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered incident cycle. */
data class TieredIncidentCycleView(
    var code: String,
    var name: String,
    var pinned: String,
    var batchSizeText: String,
    var externalRef: String,
    var quantityText: String,
    var visible: String,
    var minimum: String,
    var laneText: String,
    var timezone: String,
    var statusLabel: String,
)

fun toTieredIncidentCycleView(item: TieredIncidentCycle): TieredIncidentCycleView {
    return TieredIncidentCycleView(
        code = item.code,
        name = item.name,
        pinned = "${item.pinned}",
        batchSizeText = item.batchSize.toString(),
        externalRef = truncate(item.externalRef, 120),
        quantityText = item.quantity.toString(),
        visible = "${item.visible}",
        minimum = "%.2f".format(item.minimum),
        laneText = item.lane,
        timezone = truncate(item.timezone, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredIncidentCycleViews(items: MutableList<TieredIncidentCycle>): MutableList<TieredIncidentCycleView> {
    val views = mutableListOf<TieredIncidentCycleView>()
    for (item in items) {
        views.add(toTieredIncidentCycleView(item))
    }
    return views
}

fun sumTieredIncidentCycleView(view: TieredIncidentCycleView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedQuantity = view.quantityText.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedBatchSize.toDouble() + parsedQuantity.toDouble() + parsedMinimum
}
