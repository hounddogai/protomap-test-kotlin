package com.acme.transport.emissions.fuelburn.burnmilestone

/** How screens and exports show a global fuel burn estimate. */
data class GlobalFuelBurnEstimateView(
    var codeText: String,
    var labelText: String,
    var archivedText: String,
    var ceiling: String,
    var loadKgText: String,
    var limit: String,
    var statusLabel: String,
)

fun toGlobalFuelBurnEstimateView(item: GlobalFuelBurnEstimate): GlobalFuelBurnEstimateView {
    return GlobalFuelBurnEstimateView(
        codeText = item.code,
        labelText = item.label,
        archivedText = "${item.archived}",
        ceiling = "%.2f".format(item.ceiling),
        loadKgText = "%.2f".format(item.loadKg),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalFuelBurnEstimateViews(items: MutableList<GlobalFuelBurnEstimate>): MutableList<GlobalFuelBurnEstimateView> {
    val views = mutableListOf<GlobalFuelBurnEstimateView>()
    for (item in items) {
        views.add(toGlobalFuelBurnEstimateView(item))
    }
    return views
}

fun sumGlobalFuelBurnEstimateView(view: GlobalFuelBurnEstimateView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedCeiling + parsedLoadKg + parsedLimit.toDouble()
}
