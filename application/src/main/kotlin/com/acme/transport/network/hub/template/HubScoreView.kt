package com.acme.transport.network.hub.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub score. */
data class HubScoreView(
    var code: String,
    var label: String,
    var archivedText: String,
    var cost: String,
    var expiresAt: String,
    var ownerId: String,
    var summaryText: String,
    var source: String,
    var thresholdText: String,
    var statusLabel: String,
)

fun toHubScoreView(item: HubScore): HubScoreView {
    return HubScoreView(
        code = item.code,
        label = truncate(item.label, 40),
        archivedText = "${item.archived}",
        cost = "%.2f".format(item.cost),
        expiresAt = item.expiresAt.toString(),
        ownerId = item.ownerId,
        summaryText = item.summary,
        source = item.source,
        thresholdText = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubScoreViews(items: MutableList<HubScore>): MutableList<HubScoreView> {
    val views = mutableListOf<HubScoreView>()
    for (item in items) {
        views.add(toHubScoreView(item))
    }
    return views
}

fun sumHubScoreView(view: HubScoreView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedCost + parsedExpiresAt.toDouble() + parsedThreshold
}
