package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn allocation. */
data class FuelBurnAllocationView(
    var code: String,
    var nameText: String,
    var limit: String,
    var summary: String,
    var active: String,
    var label: String,
    var batchSize: String,
    var windowDays: String,
    var retries: String,
    var statusLabel: String,
)

fun toFuelBurnAllocationView(item: FuelBurnAllocation): FuelBurnAllocationView {
    return FuelBurnAllocationView(
        code = truncate(item.code, 40),
        nameText = truncate(item.name, 40),
        limit = item.limit.toString(),
        summary = item.summary,
        active = "${item.active}",
        label = item.label,
        batchSize = item.batchSize.toString(),
        windowDays = item.windowDays.toString(),
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnAllocationViews(items: MutableList<FuelBurnAllocation>): MutableList<FuelBurnAllocationView> {
    val views = mutableListOf<FuelBurnAllocationView>()
    for (item in items) {
        views.add(toFuelBurnAllocationView(item))
    }
    return views
}

fun sumFuelBurnAllocationView(view: FuelBurnAllocationView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedLimit.toDouble() + parsedBatchSize.toDouble() + parsedWindowDays.toDouble()
}
