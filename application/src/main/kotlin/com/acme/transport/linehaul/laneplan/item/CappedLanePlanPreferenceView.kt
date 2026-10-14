package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped lane plan preference. */
data class CappedLanePlanPreferenceView(
    var code: String,
    var titleText: String,
    var carrierText: String,
    var scoreText: String,
    var lane: String,
    var reviewer: String,
    var ceiling: String,
    var budget: String,
    var weight: String,
    var floorValue: String,
    var phone: String,
    var statusLabel: String,
)

fun toCappedLanePlanPreferenceView(item: CappedLanePlanPreference): CappedLanePlanPreferenceView {
    return CappedLanePlanPreferenceView(
        code = item.code,
        titleText = item.title,
        carrierText = truncate(item.carrier, 40),
        scoreText = "%.2f".format(item.score),
        lane = item.lane,
        reviewer = item.reviewer,
        ceiling = "%.2f".format(item.ceiling),
        budget = "%.2f".format(item.budget),
        weight = "%.2f".format(item.weight),
        floorValue = "%.2f".format(item.floorValue),
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedLanePlanPreferenceViews(items: MutableList<CappedLanePlanPreference>): MutableList<CappedLanePlanPreferenceView> {
    val views = mutableListOf<CappedLanePlanPreferenceView>()
    for (item in items) {
        views.add(toCappedLanePlanPreferenceView(item))
    }
    return views
}

fun sumCappedLanePlanPreferenceView(view: CappedLanePlanPreferenceView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedScore + parsedCeiling + parsedBudget
}
