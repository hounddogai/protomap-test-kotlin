package com.acme.transport.safety.nearmiss.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary near miss budget. */
data class PrimaryNearMissBudgetView(
    var code: String,
    var titleText: String,
    var distanceKm: String,
    var etaMinutesText: String,
    var timezone: String,
    var palletsText: String,
    var reviewer: String,
    var category: String,
    var source: String,
    var capacity: String,
    var ratio: String,
    var statusLabel: String,
)

fun toPrimaryNearMissBudgetView(item: PrimaryNearMissBudget): PrimaryNearMissBudgetView {
    return PrimaryNearMissBudgetView(
        code = item.code,
        titleText = item.title,
        distanceKm = "%.2f".format(item.distanceKm),
        etaMinutesText = item.etaMinutes.toString(),
        timezone = item.timezone,
        palletsText = item.pallets.toString(),
        reviewer = item.reviewer,
        category = truncate(item.category, 80),
        source = item.source,
        capacity = item.capacity.toString(),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryNearMissBudgetViews(items: MutableList<PrimaryNearMissBudget>): MutableList<PrimaryNearMissBudgetView> {
    val views = mutableListOf<PrimaryNearMissBudgetView>()
    for (item in items) {
        views.add(toPrimaryNearMissBudgetView(item))
    }
    return views
}

fun sumPrimaryNearMissBudgetView(view: PrimaryNearMissBudgetView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedPallets = view.palletsText.toInt()
    return parsedDistanceKm + parsedEtaMinutes.toDouble() + parsedPallets.toDouble()
}
