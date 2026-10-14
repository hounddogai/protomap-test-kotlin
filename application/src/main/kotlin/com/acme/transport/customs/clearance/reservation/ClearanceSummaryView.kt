package com.acme.transport.customs.clearance.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance summary. */
data class ClearanceSummaryView(
    var code: String,
    var nameText: String,
    var description: String,
    var costText: String,
    var dock: String,
    var etaMinutes: String,
    var label: String,
    var vehicleId: String,
    var batchSize: String,
    var unitText: String,
    var attempts: String,
    var statusLabel: String,
)

fun toClearanceSummaryView(item: ClearanceSummary): ClearanceSummaryView {
    return ClearanceSummaryView(
        code = item.code,
        nameText = item.name,
        description = truncate(item.description, 120),
        costText = "%.2f".format(item.cost),
        dock = item.dock,
        etaMinutes = item.etaMinutes.toString(),
        label = item.label,
        vehicleId = item.vehicleId,
        batchSize = item.batchSize.toString(),
        unitText = item.unit,
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceSummaryViews(items: MutableList<ClearanceSummary>): MutableList<ClearanceSummaryView> {
    val views = mutableListOf<ClearanceSummaryView>()
    for (item in items) {
        views.add(toClearanceSummaryView(item))
    }
    return views
}

fun sumClearanceSummaryView(view: ClearanceSummaryView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedCost + parsedEtaMinutes.toDouble() + parsedBatchSize.toDouble()
}
