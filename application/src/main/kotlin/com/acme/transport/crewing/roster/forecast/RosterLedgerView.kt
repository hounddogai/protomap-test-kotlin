package com.acme.transport.crewing.roster.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster ledger. */
data class RosterLedgerView(
    var code: String,
    var label: String,
    var reviewer: String,
    var driverIdText: String,
    var batchSize: String,
    var fuelLiters: String,
    var category: String,
    var minimum: String,
    var windowDays: String,
    var ownerIdText: String,
    var statusLabel: String,
)

fun toRosterLedgerView(item: RosterLedger): RosterLedgerView {
    return RosterLedgerView(
        code = item.code,
        label = item.label,
        reviewer = item.reviewer,
        driverIdText = item.driverId,
        batchSize = item.batchSize.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        category = item.category,
        minimum = "%.2f".format(item.minimum),
        windowDays = item.windowDays.toString(),
        ownerIdText = truncate(item.ownerId, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterLedgerViews(items: MutableList<RosterLedger>): MutableList<RosterLedgerView> {
    val views = mutableListOf<RosterLedgerView>()
    for (item in items) {
        views.add(toRosterLedgerView(item))
    }
    return views
}

fun sumRosterLedgerView(view: RosterLedgerView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedBatchSize.toDouble() + parsedFuelLiters + parsedMinimum
}
