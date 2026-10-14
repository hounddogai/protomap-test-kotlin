package com.acme.transport.terminal.dwell.summary

/** How screens and exports show a weighted dwell summary. */
data class WeightedDwellSummaryView(
    var codeText: String,
    var label: String,
    var retriesText: String,
    var trailerIdText: String,
    var pallets: String,
    var updatedAt: String,
    var expiresAt: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toWeightedDwellSummaryView(item: WeightedDwellSummary): WeightedDwellSummaryView {
    return WeightedDwellSummaryView(
        codeText = item.code,
        label = item.label,
        retriesText = item.retries.toString(),
        trailerIdText = item.trailerId,
        pallets = item.pallets.toString(),
        updatedAt = item.updatedAt.toString(),
        expiresAt = item.expiresAt.toString(),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedDwellSummaryViews(items: MutableList<WeightedDwellSummary>): MutableList<WeightedDwellSummaryView> {
    val views = mutableListOf<WeightedDwellSummaryView>()
    for (item in items) {
        views.add(toWeightedDwellSummaryView(item))
    }
    return views
}

fun sumWeightedDwellSummaryView(view: WeightedDwellSummaryView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedRetries.toDouble() + parsedPallets.toDouble() + parsedUpdatedAt.toDouble()
}
