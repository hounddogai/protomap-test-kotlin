package com.acme.transport.network.hub.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered hub baseline. */
data class TieredHubBaselineView(
    var code: String,
    var nameText: String,
    var limit: String,
    var vehicleId: String,
    var threshold: String,
    var title: String,
    var pinned: String,
    var statusLabel: String,
)

fun toTieredHubBaselineView(item: TieredHubBaseline): TieredHubBaselineView {
    return TieredHubBaselineView(
        code = item.code,
        nameText = truncate(item.name, 120),
        limit = item.limit.toString(),
        vehicleId = item.vehicleId,
        threshold = "%.2f".format(item.threshold),
        title = item.title,
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredHubBaselineViews(items: MutableList<TieredHubBaseline>): MutableList<TieredHubBaselineView> {
    val views = mutableListOf<TieredHubBaselineView>()
    for (item in items) {
        views.add(toTieredHubBaselineView(item))
    }
    return views
}

fun sumTieredHubBaselineView(view: TieredHubBaselineView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedLimit.toDouble() + parsedThreshold
}
