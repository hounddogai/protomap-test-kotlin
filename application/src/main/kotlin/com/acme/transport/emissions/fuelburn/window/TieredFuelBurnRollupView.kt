package com.acme.transport.emissions.fuelburn.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered fuel burn rollup. */
data class TieredFuelBurnRollupView(
    var code: String,
    var label: String,
    var laneText: String,
    var revision: String,
    var vehicleIdText: String,
    var margin: String,
    var carrier: String,
    var pallets: String,
    var batchSize: String,
    var country: String,
    var budgetText: String,
    var statusLabel: String,
)

fun toTieredFuelBurnRollupView(item: TieredFuelBurnRollup): TieredFuelBurnRollupView {
    return TieredFuelBurnRollupView(
        code = truncate(item.code, 80),
        label = item.label,
        laneText = truncate(item.lane, 40),
        revision = item.revision.toString(),
        vehicleIdText = item.vehicleId,
        margin = "%.2f".format(item.margin),
        carrier = item.carrier,
        pallets = item.pallets.toString(),
        batchSize = item.batchSize.toString(),
        country = item.country,
        budgetText = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredFuelBurnRollupViews(items: MutableList<TieredFuelBurnRollup>): MutableList<TieredFuelBurnRollupView> {
    val views = mutableListOf<TieredFuelBurnRollupView>()
    for (item in items) {
        views.add(toTieredFuelBurnRollupView(item))
    }
    return views
}

fun sumTieredFuelBurnRollupView(view: TieredFuelBurnRollupView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedRevision.toDouble() + parsedMargin + parsedPallets.toDouble()
}
