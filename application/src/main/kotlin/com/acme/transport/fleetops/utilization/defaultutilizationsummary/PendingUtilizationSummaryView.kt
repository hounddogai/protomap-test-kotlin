package com.acme.transport.fleetops.utilization.defaultutilizationsummary

/** How screens and exports show a pending utilization summary. */
data class PendingUtilizationSummaryView(
    var code: String,
    var nameText: String,
    var fuelLiters: String,
    var ratio: String,
    var batchSize: String,
    var activeText: String,
    var floorValue: String,
    var limit: String,
    var regionText: String,
    var dock: String,
    var endsAtText: String,
    var statusLabel: String,
)

fun toPendingUtilizationSummaryView(item: PendingUtilizationSummary): PendingUtilizationSummaryView {
    return PendingUtilizationSummaryView(
        code = item.code,
        nameText = item.name,
        fuelLiters = "%.2f".format(item.fuelLiters),
        ratio = "%.2f".format(item.ratio),
        batchSize = item.batchSize.toString(),
        activeText = "${item.active}",
        floorValue = "%.2f".format(item.floorValue),
        limit = item.limit.toString(),
        regionText = item.region,
        dock = item.dock,
        endsAtText = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingUtilizationSummaryViews(items: MutableList<PendingUtilizationSummary>): MutableList<PendingUtilizationSummaryView> {
    val views = mutableListOf<PendingUtilizationSummaryView>()
    for (item in items) {
        views.add(toPendingUtilizationSummaryView(item))
    }
    return views
}

fun sumPendingUtilizationSummaryView(view: PendingUtilizationSummaryView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedRatio = view.ratio.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedFuelLiters + parsedRatio + parsedBatchSize.toDouble()
}
