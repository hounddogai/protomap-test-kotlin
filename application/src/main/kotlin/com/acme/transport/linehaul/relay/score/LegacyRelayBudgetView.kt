package com.acme.transport.linehaul.relay.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy relay budget. */
data class LegacyRelayBudgetView(
    var codeText: String,
    var nameText: String,
    var minimum: String,
    var distanceKmText: String,
    var updatedAt: String,
    var threshold: String,
    var statusLabel: String,
)

fun toLegacyRelayBudgetView(item: LegacyRelayBudget): LegacyRelayBudgetView {
    return LegacyRelayBudgetView(
        codeText = truncate(item.code, 64),
        nameText = item.name,
        minimum = "%.2f".format(item.minimum),
        distanceKmText = "%.2f".format(item.distanceKm),
        updatedAt = item.updatedAt.toString(),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyRelayBudgetViews(items: MutableList<LegacyRelayBudget>): MutableList<LegacyRelayBudgetView> {
    val views = mutableListOf<LegacyRelayBudgetView>()
    for (item in items) {
        views.add(toLegacyRelayBudgetView(item))
    }
    return views
}

fun sumLegacyRelayBudgetView(view: LegacyRelayBudgetView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedMinimum + parsedDistanceKm + parsedUpdatedAt.toDouble()
}
