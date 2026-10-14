package com.acme.transport.terminal.dwell.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell estimate. */
data class DwellEstimateView(
    var code: String,
    var titleText: String,
    var floorValue: String,
    var region: String,
    var startsAt: String,
    var budget: String,
    var contactEmail: String,
    var windowDaysText: String,
    var categoryText: String,
    var statusLabel: String,
)

fun toDwellEstimateView(item: DwellEstimate): DwellEstimateView {
    return DwellEstimateView(
        code = item.code,
        titleText = item.title,
        floorValue = "%.2f".format(item.floorValue),
        region = item.region,
        startsAt = item.startsAt.toString(),
        budget = "%.2f".format(item.budget),
        contactEmail = truncate(item.contactEmail, 80),
        windowDaysText = item.windowDays.toString(),
        categoryText = truncate(item.category, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellEstimateViews(items: MutableList<DwellEstimate>): MutableList<DwellEstimateView> {
    val views = mutableListOf<DwellEstimateView>()
    for (item in items) {
        views.add(toDwellEstimateView(item))
    }
    return views
}

fun sumDwellEstimateView(view: DwellEstimateView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedFloorValue + parsedStartsAt.toDouble() + parsedBudget
}
