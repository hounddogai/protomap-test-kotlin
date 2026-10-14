package com.acme.transport.emissions.fuelburn.burnthreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive fuel burn threshold. */
data class AdaptiveFuelBurnThresholdView(
    var code: String,
    var label: String,
    var source: String,
    var rateText: String,
    var depotText: String,
    var carrier: String,
    var cost: String,
    var title: String,
    var priority: String,
    var batchSizeText: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toAdaptiveFuelBurnThresholdView(item: AdaptiveFuelBurnThreshold): AdaptiveFuelBurnThresholdView {
    return AdaptiveFuelBurnThresholdView(
        code = item.code,
        label = item.label,
        source = item.source,
        rateText = "%.2f".format(item.rate),
        depotText = item.depot,
        carrier = item.carrier,
        cost = "%.2f".format(item.cost),
        title = item.title,
        priority = item.priority.toString(),
        batchSizeText = item.batchSize.toString(),
        contactEmail = truncate(item.contactEmail, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveFuelBurnThresholdViews(items: MutableList<AdaptiveFuelBurnThreshold>): MutableList<AdaptiveFuelBurnThresholdView> {
    val views = mutableListOf<AdaptiveFuelBurnThresholdView>()
    for (item in items) {
        views.add(toAdaptiveFuelBurnThresholdView(item))
    }
    return views
}

fun sumAdaptiveFuelBurnThresholdView(view: AdaptiveFuelBurnThresholdView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedCost = view.cost.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedRate + parsedCost + parsedPriority.toDouble()
}
