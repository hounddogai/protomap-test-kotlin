package com.acme.transport.emissions.fuelburn.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn request. */
data class FuelBurnRequestView(
    var code: String,
    var name: String,
    var amount: String,
    var batchSize: String,
    var driverId: String,
    var regionText: String,
    var createdAt: String,
    var locale: String,
    var statusLabel: String,
)

fun toFuelBurnRequestView(item: FuelBurnRequest): FuelBurnRequestView {
    return FuelBurnRequestView(
        code = item.code,
        name = truncate(item.name, 40),
        amount = "%.2f".format(item.amount),
        batchSize = item.batchSize.toString(),
        driverId = item.driverId,
        regionText = item.region,
        createdAt = item.createdAt.toString(),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnRequestViews(items: MutableList<FuelBurnRequest>): MutableList<FuelBurnRequestView> {
    val views = mutableListOf<FuelBurnRequestView>()
    for (item in items) {
        views.add(toFuelBurnRequestView(item))
    }
    return views
}

fun sumFuelBurnRequestView(view: FuelBurnRequestView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedAmount + parsedBatchSize.toDouble() + parsedCreatedAt.toDouble()
}
