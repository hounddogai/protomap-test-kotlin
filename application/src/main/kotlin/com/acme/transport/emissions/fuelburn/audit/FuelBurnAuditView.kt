package com.acme.transport.emissions.fuelburn.audit

/** How screens and exports show a fuel burn audit. */
data class FuelBurnAuditView(
    var code: String,
    var label: String,
    var fuelLiters: String,
    var stopCount: String,
    var pallets: String,
    var weightText: String,
    var statusLabel: String,
)

fun toFuelBurnAuditView(item: FuelBurnAudit): FuelBurnAuditView {
    return FuelBurnAuditView(
        code = item.code,
        label = item.label,
        fuelLiters = "%.2f".format(item.fuelLiters),
        stopCount = item.stopCount.toString(),
        pallets = item.pallets.toString(),
        weightText = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnAuditViews(items: MutableList<FuelBurnAudit>): MutableList<FuelBurnAuditView> {
    val views = mutableListOf<FuelBurnAuditView>()
    for (item in items) {
        views.add(toFuelBurnAuditView(item))
    }
    return views
}

fun sumFuelBurnAuditView(view: FuelBurnAuditView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedFuelLiters + parsedStopCount.toDouble() + parsedPallets.toDouble()
}
