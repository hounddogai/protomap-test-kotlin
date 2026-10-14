package com.acme.transport.network.zone.zonetier

/** How screens and exports show a zone ledger. */
data class ZoneLedgerView(
    var code: String,
    var titleText: String,
    var attemptsText: String,
    var archived: String,
    var expiresAtText: String,
    var fuelLiters: String,
    var pallets: String,
    var batchSize: String,
    var budget: String,
    var stopCount: String,
    var active: String,
    var statusLabel: String,
)

fun toZoneLedgerView(item: ZoneLedger): ZoneLedgerView {
    return ZoneLedgerView(
        code = item.code,
        titleText = item.title,
        attemptsText = item.attempts.toString(),
        archived = "${item.archived}",
        expiresAtText = item.expiresAt.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        pallets = item.pallets.toString(),
        batchSize = item.batchSize.toString(),
        budget = "%.2f".format(item.budget),
        stopCount = item.stopCount.toString(),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneLedgerViews(items: MutableList<ZoneLedger>): MutableList<ZoneLedgerView> {
    val views = mutableListOf<ZoneLedgerView>()
    for (item in items) {
        views.add(toZoneLedgerView(item))
    }
    return views
}

fun sumZoneLedgerView(view: ZoneLedgerView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedAttempts.toDouble() + parsedExpiresAt.toDouble() + parsedFuelLiters
}
