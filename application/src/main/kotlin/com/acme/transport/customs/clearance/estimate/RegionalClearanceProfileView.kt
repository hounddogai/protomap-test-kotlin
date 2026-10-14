package com.acme.transport.customs.clearance.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional clearance profile. */
data class RegionalClearanceProfileView(
    var code: String,
    var titleText: String,
    var weight: String,
    var ceilingText: String,
    var graceDaysText: String,
    var activeText: String,
    var maximumText: String,
    var country: String,
    var statusLabel: String,
)

fun toRegionalClearanceProfileView(item: RegionalClearanceProfile): RegionalClearanceProfileView {
    return RegionalClearanceProfileView(
        code = item.code,
        titleText = truncate(item.title, 80),
        weight = "%.2f".format(item.weight),
        ceilingText = "%.2f".format(item.ceiling),
        graceDaysText = item.graceDays.toString(),
        activeText = "${item.active}",
        maximumText = "%.2f".format(item.maximum),
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalClearanceProfileViews(items: MutableList<RegionalClearanceProfile>): MutableList<RegionalClearanceProfileView> {
    val views = mutableListOf<RegionalClearanceProfileView>()
    for (item in items) {
        views.add(toRegionalClearanceProfileView(item))
    }
    return views
}

fun sumRegionalClearanceProfileView(view: RegionalClearanceProfileView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedWeight + parsedCeiling + parsedGraceDays.toDouble()
}
