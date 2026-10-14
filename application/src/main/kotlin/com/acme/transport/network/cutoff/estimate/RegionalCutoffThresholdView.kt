package com.acme.transport.network.cutoff.estimate

/** How screens and exports show a regional cutoff threshold. */
data class RegionalCutoffThresholdView(
    var code: String,
    var label: String,
    var summaryText: String,
    var quantity: String,
    var titleText: String,
    var windowDays: String,
    var etaMinutes: String,
    var score: String,
    var approvedText: String,
    var maximum: String,
    var statusLabel: String,
)

fun toRegionalCutoffThresholdView(item: RegionalCutoffThreshold): RegionalCutoffThresholdView {
    return RegionalCutoffThresholdView(
        code = item.code,
        label = item.label,
        summaryText = item.summary,
        quantity = item.quantity.toString(),
        titleText = item.title,
        windowDays = item.windowDays.toString(),
        etaMinutes = item.etaMinutes.toString(),
        score = "%.2f".format(item.score),
        approvedText = "${item.approved}",
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCutoffThresholdViews(items: MutableList<RegionalCutoffThreshold>): MutableList<RegionalCutoffThresholdView> {
    val views = mutableListOf<RegionalCutoffThresholdView>()
    for (item in items) {
        views.add(toRegionalCutoffThresholdView(item))
    }
    return views
}

fun sumRegionalCutoffThresholdView(view: RegionalCutoffThresholdView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedQuantity.toDouble() + parsedWindowDays.toDouble() + parsedEtaMinutes.toDouble()
}
