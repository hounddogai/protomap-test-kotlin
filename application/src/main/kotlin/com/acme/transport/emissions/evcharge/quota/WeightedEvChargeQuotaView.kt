package com.acme.transport.emissions.evcharge.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted ev charge quota. */
data class WeightedEvChargeQuotaView(
    var code: String,
    var label: String,
    var palletsText: String,
    var threshold: String,
    var cost: String,
    var source: String,
    var capacity: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toWeightedEvChargeQuotaView(item: WeightedEvChargeQuota): WeightedEvChargeQuotaView {
    return WeightedEvChargeQuotaView(
        code = item.code,
        label = item.label,
        palletsText = item.pallets.toString(),
        threshold = "%.2f".format(item.threshold),
        cost = "%.2f".format(item.cost),
        source = truncate(item.source, 40),
        capacity = item.capacity.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedEvChargeQuotaViews(items: MutableList<WeightedEvChargeQuota>): MutableList<WeightedEvChargeQuotaView> {
    val views = mutableListOf<WeightedEvChargeQuotaView>()
    for (item in items) {
        views.add(toWeightedEvChargeQuotaView(item))
    }
    return views
}

fun sumWeightedEvChargeQuotaView(view: WeightedEvChargeQuotaView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedPallets.toDouble() + parsedThreshold + parsedCost
}
