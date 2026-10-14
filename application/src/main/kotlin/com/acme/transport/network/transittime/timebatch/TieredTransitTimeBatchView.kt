package com.acme.transport.network.transittime.timebatch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered transit time batch. */
data class TieredTransitTimeBatchView(
    var code: String,
    var name: String,
    var vehicleIdText: String,
    var quantityText: String,
    var timezone: String,
    var carrier: String,
    var statusLabel: String,
)

fun toTieredTransitTimeBatchView(item: TieredTransitTimeBatch): TieredTransitTimeBatchView {
    return TieredTransitTimeBatchView(
        code = item.code,
        name = item.name,
        vehicleIdText = truncate(item.vehicleId, 40),
        quantityText = item.quantity.toString(),
        timezone = truncate(item.timezone, 80),
        carrier = truncate(item.carrier, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredTransitTimeBatchViews(items: MutableList<TieredTransitTimeBatch>): MutableList<TieredTransitTimeBatchView> {
    val views = mutableListOf<TieredTransitTimeBatchView>()
    for (item in items) {
        views.add(toTieredTransitTimeBatchView(item))
    }
    return views
}

fun sumTieredTransitTimeBatchView(view: TieredTransitTimeBatchView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
