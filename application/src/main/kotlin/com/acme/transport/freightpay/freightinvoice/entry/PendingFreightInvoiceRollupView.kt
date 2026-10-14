package com.acme.transport.freightpay.freightinvoice.entry

/** How screens and exports show a pending freight invoice rollup. */
data class PendingFreightInvoiceRollupView(
    var codeText: String,
    var label: String,
    var startsAt: String,
    var odometer: String,
    var fuelLiters: String,
    var loadKg: String,
    var limitText: String,
    var cost: String,
    var statusLabel: String,
)

fun toPendingFreightInvoiceRollupView(item: PendingFreightInvoiceRollup): PendingFreightInvoiceRollupView {
    return PendingFreightInvoiceRollupView(
        codeText = item.code,
        label = item.label,
        startsAt = item.startsAt.toString(),
        odometer = item.odometer.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        loadKg = "%.2f".format(item.loadKg),
        limitText = item.limit.toString(),
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingFreightInvoiceRollupViews(items: MutableList<PendingFreightInvoiceRollup>): MutableList<PendingFreightInvoiceRollupView> {
    val views = mutableListOf<PendingFreightInvoiceRollupView>()
    for (item in items) {
        views.add(toPendingFreightInvoiceRollupView(item))
    }
    return views
}

fun sumPendingFreightInvoiceRollupView(view: PendingFreightInvoiceRollupView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedStartsAt.toDouble() + parsedOdometer.toDouble() + parsedFuelLiters
}
