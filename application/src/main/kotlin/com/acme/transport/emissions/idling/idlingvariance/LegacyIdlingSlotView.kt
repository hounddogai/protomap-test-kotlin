package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy idling slot. */
data class LegacyIdlingSlotView(
    var code: String,
    var titleText: String,
    var budget: String,
    var graceDaysText: String,
    var thresholdText: String,
    var segmentText: String,
    var timezone: String,
    var statusLabel: String,
)

fun toLegacyIdlingSlotView(item: LegacyIdlingSlot): LegacyIdlingSlotView {
    return LegacyIdlingSlotView(
        code = item.code,
        titleText = truncate(item.title, 80),
        budget = "%.2f".format(item.budget),
        graceDaysText = item.graceDays.toString(),
        thresholdText = "%.2f".format(item.threshold),
        segmentText = item.segment,
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyIdlingSlotViews(items: MutableList<LegacyIdlingSlot>): MutableList<LegacyIdlingSlotView> {
    val views = mutableListOf<LegacyIdlingSlotView>()
    for (item in items) {
        views.add(toLegacyIdlingSlotView(item))
    }
    return views
}

fun sumLegacyIdlingSlotView(view: LegacyIdlingSlotView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedBudget + parsedGraceDays.toDouble() + parsedThreshold
}
