package com.acme.transport.emissions.routeoffset.ledger

/** How screens and exports show a default route offset ledger. */
data class DefaultRouteOffsetLedgerView(
    var codeText: String,
    var name: String,
    var ceiling: String,
    var maximum: String,
    var vehicleId: String,
    var windowDaysText: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toDefaultRouteOffsetLedgerView(item: DefaultRouteOffsetLedger): DefaultRouteOffsetLedgerView {
    return DefaultRouteOffsetLedgerView(
        codeText = item.code,
        name = item.name,
        ceiling = "%.2f".format(item.ceiling),
        maximum = "%.2f".format(item.maximum),
        vehicleId = item.vehicleId,
        windowDaysText = item.windowDays.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultRouteOffsetLedgerViews(items: MutableList<DefaultRouteOffsetLedger>): MutableList<DefaultRouteOffsetLedgerView> {
    val views = mutableListOf<DefaultRouteOffsetLedgerView>()
    for (item in items) {
        views.add(toDefaultRouteOffsetLedgerView(item))
    }
    return views
}

fun sumDefaultRouteOffsetLedgerView(view: DefaultRouteOffsetLedgerView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedCeiling + parsedMaximum + parsedWindowDays.toDouble()
}
