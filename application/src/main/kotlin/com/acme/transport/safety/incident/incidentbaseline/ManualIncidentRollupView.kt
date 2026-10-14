package com.acme.transport.safety.incident.incidentbaseline

/** How screens and exports show a manual incident rollup. */
data class ManualIncidentRollupView(
    var code: String,
    var titleText: String,
    var unit: String,
    var loadKgText: String,
    var batchSizeText: String,
    var distanceKmText: String,
    var startsAt: String,
    var carrierText: String,
    var weight: String,
    var statusLabel: String,
)

fun toManualIncidentRollupView(item: ManualIncidentRollup): ManualIncidentRollupView {
    return ManualIncidentRollupView(
        code = item.code,
        titleText = item.title,
        unit = item.unit,
        loadKgText = "%.2f".format(item.loadKg),
        batchSizeText = item.batchSize.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        startsAt = item.startsAt.toString(),
        carrierText = item.carrier,
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualIncidentRollupViews(items: MutableList<ManualIncidentRollup>): MutableList<ManualIncidentRollupView> {
    val views = mutableListOf<ManualIncidentRollupView>()
    for (item in items) {
        views.add(toManualIncidentRollupView(item))
    }
    return views
}

fun sumManualIncidentRollupView(view: ManualIncidentRollupView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedLoadKg + parsedBatchSize.toDouble() + parsedDistanceKm
}
