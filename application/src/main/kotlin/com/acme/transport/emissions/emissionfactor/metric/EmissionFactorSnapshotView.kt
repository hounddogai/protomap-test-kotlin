package com.acme.transport.emissions.emissionfactor.metric

/** How screens and exports show a emission factor snapshot. */
data class EmissionFactorSnapshotView(
    var code: String,
    var name: String,
    var channel: String,
    var cost: String,
    var dockText: String,
    var graceDays: String,
    var odometer: String,
    var loadKg: String,
    var description: String,
    var statusLabel: String,
)

fun toEmissionFactorSnapshotView(item: EmissionFactorSnapshot): EmissionFactorSnapshotView {
    return EmissionFactorSnapshotView(
        code = item.code,
        name = item.name,
        channel = item.channel,
        cost = "%.2f".format(item.cost),
        dockText = item.dock,
        graceDays = item.graceDays.toString(),
        odometer = item.odometer.toString(),
        loadKg = "%.2f".format(item.loadKg),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorSnapshotViews(items: MutableList<EmissionFactorSnapshot>): MutableList<EmissionFactorSnapshotView> {
    val views = mutableListOf<EmissionFactorSnapshotView>()
    for (item in items) {
        views.add(toEmissionFactorSnapshotView(item))
    }
    return views
}

fun sumEmissionFactorSnapshotView(view: EmissionFactorSnapshotView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedCost + parsedGraceDays.toDouble() + parsedOdometer.toDouble()
}
