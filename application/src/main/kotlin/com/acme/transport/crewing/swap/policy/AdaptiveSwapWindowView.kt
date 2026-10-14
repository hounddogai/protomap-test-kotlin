package com.acme.transport.crewing.swap.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive swap window. */
data class AdaptiveSwapWindowView(
    var code: String,
    var labelText: String,
    var cost: String,
    var country: String,
    var checksum: String,
    var dock: String,
    var distanceKm: String,
    var source: String,
    var priorityText: String,
    var statusLabel: String,
)

fun toAdaptiveSwapWindowView(item: AdaptiveSwapWindow): AdaptiveSwapWindowView {
    return AdaptiveSwapWindowView(
        code = item.code,
        labelText = truncate(item.label, 40),
        cost = "%.2f".format(item.cost),
        country = item.country,
        checksum = item.checksum,
        dock = item.dock,
        distanceKm = "%.2f".format(item.distanceKm),
        source = item.source,
        priorityText = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveSwapWindowViews(items: MutableList<AdaptiveSwapWindow>): MutableList<AdaptiveSwapWindowView> {
    val views = mutableListOf<AdaptiveSwapWindowView>()
    for (item in items) {
        views.add(toAdaptiveSwapWindowView(item))
    }
    return views
}

fun sumAdaptiveSwapWindowView(view: AdaptiveSwapWindowView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedPriority = view.priorityText.toInt()
    return parsedCost + parsedDistanceKm + parsedPriority.toDouble()
}
