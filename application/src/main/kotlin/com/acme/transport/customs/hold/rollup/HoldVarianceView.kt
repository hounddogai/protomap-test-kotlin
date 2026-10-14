package com.acme.transport.customs.hold.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hold variance. */
data class HoldVarianceView(
    var codeText: String,
    var nameText: String,
    var loadKg: String,
    var timezone: String,
    var costText: String,
    var approvedText: String,
    var statusLabel: String,
)

fun toHoldVarianceView(item: HoldVariance): HoldVarianceView {
    return HoldVarianceView(
        codeText = item.code,
        nameText = truncate(item.name, 80),
        loadKg = "%.2f".format(item.loadKg),
        timezone = item.timezone,
        costText = "%.2f".format(item.cost),
        approvedText = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldVarianceViews(items: MutableList<HoldVariance>): MutableList<HoldVarianceView> {
    val views = mutableListOf<HoldVarianceView>()
    for (item in items) {
        views.add(toHoldVarianceView(item))
    }
    return views
}

fun sumHoldVarianceView(view: HoldVarianceView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedCost = view.costText.toDouble()
    return parsedLoadKg + parsedCost
}
