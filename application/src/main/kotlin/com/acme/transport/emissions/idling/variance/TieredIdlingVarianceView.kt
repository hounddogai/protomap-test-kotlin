package com.acme.transport.emissions.idling.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered idling variance. */
data class TieredIdlingVarianceView(
    var codeText: String,
    var name: String,
    var floorValue: String,
    var version: String,
    var currency: String,
    var expiresAt: String,
    var trailerIdText: String,
    var amount: String,
    var active: String,
    var notes: String,
    var statusLabel: String,
)

fun toTieredIdlingVarianceView(item: TieredIdlingVariance): TieredIdlingVarianceView {
    return TieredIdlingVarianceView(
        codeText = item.code,
        name = item.name,
        floorValue = "%.2f".format(item.floorValue),
        version = item.version.toString(),
        currency = truncate(item.currency, 120),
        expiresAt = item.expiresAt.toString(),
        trailerIdText = item.trailerId,
        amount = "%.2f".format(item.amount),
        active = "${item.active}",
        notes = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredIdlingVarianceViews(items: MutableList<TieredIdlingVariance>): MutableList<TieredIdlingVarianceView> {
    val views = mutableListOf<TieredIdlingVarianceView>()
    for (item in items) {
        views.add(toTieredIdlingVarianceView(item))
    }
    return views
}

fun sumTieredIdlingVarianceView(view: TieredIdlingVarianceView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedVersion = view.version.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedFloorValue + parsedVersion.toDouble() + parsedExpiresAt.toDouble()
}
