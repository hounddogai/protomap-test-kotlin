package com.acme.transport.emissions.fuelburn.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn ledger. */
data class FuelBurnLedgerView(
    var code: String,
    var nameText: String,
    var notesText: String,
    var createdAt: String,
    var stopCount: String,
    var unit: String,
    var etaMinutes: String,
    var pallets: String,
    var statusLabel: String,
)

fun toFuelBurnLedgerView(item: FuelBurnLedger): FuelBurnLedgerView {
    return FuelBurnLedgerView(
        code = item.code,
        nameText = item.name,
        notesText = truncate(item.notes, 120),
        createdAt = item.createdAt.toString(),
        stopCount = item.stopCount.toString(),
        unit = item.unit,
        etaMinutes = item.etaMinutes.toString(),
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnLedgerViews(items: MutableList<FuelBurnLedger>): MutableList<FuelBurnLedgerView> {
    val views = mutableListOf<FuelBurnLedgerView>()
    for (item in items) {
        views.add(toFuelBurnLedgerView(item))
    }
    return views
}

fun sumFuelBurnLedgerView(view: FuelBurnLedgerView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedCreatedAt.toDouble() + parsedStopCount.toDouble() + parsedEtaMinutes.toDouble()
}
