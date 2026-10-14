package com.acme.transport.customs.inspection.allocation

/** How screens and exports show a pending inspection rollup. */
data class PendingInspectionRollupView(
    var code: String,
    var label: String,
    var vehicleIdText: String,
    var retriesText: String,
    var trailerId: String,
    var weightText: String,
    var loadKgText: String,
    var statusLabel: String,
)

fun toPendingInspectionRollupView(item: PendingInspectionRollup): PendingInspectionRollupView {
    return PendingInspectionRollupView(
        code = item.code,
        label = item.label,
        vehicleIdText = item.vehicleId,
        retriesText = item.retries.toString(),
        trailerId = item.trailerId,
        weightText = "%.2f".format(item.weight),
        loadKgText = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingInspectionRollupViews(items: MutableList<PendingInspectionRollup>): MutableList<PendingInspectionRollupView> {
    val views = mutableListOf<PendingInspectionRollupView>()
    for (item in items) {
        views.add(toPendingInspectionRollupView(item))
    }
    return views
}

fun sumPendingInspectionRollupView(view: PendingInspectionRollupView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedWeight = view.weightText.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedRetries.toDouble() + parsedWeight + parsedLoadKg
}
