package com.acme.transport.customs.clearance.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional clearance rollup. */
data class RegionalClearanceRollupView(
    var code: String,
    var titleText: String,
    var nameText: String,
    var threshold: String,
    var active: String,
    var statusLabel: String,
)

fun toRegionalClearanceRollupView(item: RegionalClearanceRollup): RegionalClearanceRollupView {
    return RegionalClearanceRollupView(
        code = item.code,
        titleText = item.title,
        nameText = truncate(item.name, 80),
        threshold = "%.2f".format(item.threshold),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalClearanceRollupViews(items: MutableList<RegionalClearanceRollup>): MutableList<RegionalClearanceRollupView> {
    val views = mutableListOf<RegionalClearanceRollupView>()
    for (item in items) {
        views.add(toRegionalClearanceRollupView(item))
    }
    return views
}

fun sumRegionalClearanceRollupView(view: RegionalClearanceRollupView): Double {
    val parsedThreshold = view.threshold.toDouble()
    return parsedThreshold
}
