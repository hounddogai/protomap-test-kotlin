package com.acme.transport.fleetops.tire.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary tire request. */
data class PrimaryTireRequestView(
    var code: String,
    var title: String,
    var category: String,
    var budgetText: String,
    var loadKg: String,
    var stopCountText: String,
    var trailerId: String,
    var countryText: String,
    var timezone: String,
    var statusLabel: String,
)

fun toPrimaryTireRequestView(item: PrimaryTireRequest): PrimaryTireRequestView {
    return PrimaryTireRequestView(
        code = item.code,
        title = item.title,
        category = item.category,
        budgetText = "%.2f".format(item.budget),
        loadKg = "%.2f".format(item.loadKg),
        stopCountText = item.stopCount.toString(),
        trailerId = truncate(item.trailerId, 40),
        countryText = item.country,
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryTireRequestViews(items: MutableList<PrimaryTireRequest>): MutableList<PrimaryTireRequestView> {
    val views = mutableListOf<PrimaryTireRequestView>()
    for (item in items) {
        views.add(toPrimaryTireRequestView(item))
    }
    return views
}

fun sumPrimaryTireRequestView(view: PrimaryTireRequestView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedBudget + parsedLoadKg + parsedStopCount.toDouble()
}
