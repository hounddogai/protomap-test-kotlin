package com.acme.transport.emissions.evcharge.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a ev charge baseline. */
data class EvChargeBaselineView(
    var codeText: String,
    var label: String,
    var fuelLiters: String,
    var notes: String,
    var vehicleIdText: String,
    var amountText: String,
    var statusLabel: String,
)

fun toEvChargeBaselineView(item: EvChargeBaseline): EvChargeBaselineView {
    return EvChargeBaselineView(
        codeText = item.code,
        label = item.label,
        fuelLiters = "%.2f".format(item.fuelLiters),
        notes = item.notes,
        vehicleIdText = truncate(item.vehicleId, 64),
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeBaselineViews(items: MutableList<EvChargeBaseline>): MutableList<EvChargeBaselineView> {
    val views = mutableListOf<EvChargeBaselineView>()
    for (item in items) {
        views.add(toEvChargeBaselineView(item))
    }
    return views
}

fun sumEvChargeBaselineView(view: EvChargeBaselineView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedAmount = view.amountText.toDouble()
    return parsedFuelLiters + parsedAmount
}
