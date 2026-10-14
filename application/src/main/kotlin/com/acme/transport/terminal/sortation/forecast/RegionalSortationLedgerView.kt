package com.acme.transport.terminal.sortation.forecast

/** How screens and exports show a regional sortation ledger. */
data class RegionalSortationLedgerView(
    var code: String,
    var nameText: String,
    var fuelLitersText: String,
    var ceiling: String,
    var limit: String,
    var statusLabel: String,
)

fun toRegionalSortationLedgerView(item: RegionalSortationLedger): RegionalSortationLedgerView {
    return RegionalSortationLedgerView(
        code = item.code,
        nameText = item.name,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        ceiling = "%.2f".format(item.ceiling),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalSortationLedgerViews(items: MutableList<RegionalSortationLedger>): MutableList<RegionalSortationLedgerView> {
    val views = mutableListOf<RegionalSortationLedgerView>()
    for (item in items) {
        views.add(toRegionalSortationLedgerView(item))
    }
    return views
}

fun sumRegionalSortationLedgerView(view: RegionalSortationLedgerView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedFuelLiters + parsedCeiling + parsedLimit.toDouble()
}
