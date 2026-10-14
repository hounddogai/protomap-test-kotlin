package com.acme.transport.safety.violation.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional violation profile. */
data class RegionalViolationProfileView(
    var code: String,
    var name: String,
    var timezone: String,
    var ceiling: String,
    var weight: String,
    var statusLabel: String,
)

fun toRegionalViolationProfileView(item: RegionalViolationProfile): RegionalViolationProfileView {
    return RegionalViolationProfileView(
        code = truncate(item.code, 40),
        name = truncate(item.name, 40),
        timezone = item.timezone,
        ceiling = "%.2f".format(item.ceiling),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalViolationProfileViews(items: MutableList<RegionalViolationProfile>): MutableList<RegionalViolationProfileView> {
    val views = mutableListOf<RegionalViolationProfileView>()
    for (item in items) {
        views.add(toRegionalViolationProfileView(item))
    }
    return views
}

fun sumRegionalViolationProfileView(view: RegionalViolationProfileView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedCeiling + parsedWeight
}
