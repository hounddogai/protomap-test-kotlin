package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered fuel burn ledger. */
data class TieredFuelBurnLedgerView(
    var code: String,
    var labelText: String,
    var channel: String,
    var phone: String,
    var odometer: String,
    var statusLabel: String,
)

fun toTieredFuelBurnLedgerView(item: TieredFuelBurnLedger): TieredFuelBurnLedgerView {
    return TieredFuelBurnLedgerView(
        code = item.code,
        labelText = item.label,
        channel = truncate(item.channel, 64),
        phone = item.phone,
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredFuelBurnLedgerViews(items: MutableList<TieredFuelBurnLedger>): MutableList<TieredFuelBurnLedgerView> {
    val views = mutableListOf<TieredFuelBurnLedgerView>()
    for (item in items) {
        views.add(toTieredFuelBurnLedgerView(item))
    }
    return views
}

fun sumTieredFuelBurnLedgerView(view: TieredFuelBurnLedgerView): Double {
    val parsedOdometer = view.odometer.toInt()
    return parsedOdometer.toDouble()
}
