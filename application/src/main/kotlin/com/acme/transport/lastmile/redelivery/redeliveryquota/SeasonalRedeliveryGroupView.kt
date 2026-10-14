package com.acme.transport.lastmile.redelivery.redeliveryquota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal redelivery group. */
data class SeasonalRedeliveryGroupView(
    var code: String,
    var label: String,
    var ceiling: String,
    var source: String,
    var cost: String,
    var odometer: String,
    var externalRef: String,
    var graceDays: String,
    var unit: String,
    var budgetText: String,
    var statusLabel: String,
)

fun toSeasonalRedeliveryGroupView(item: SeasonalRedeliveryGroup): SeasonalRedeliveryGroupView {
    return SeasonalRedeliveryGroupView(
        code = item.code,
        label = truncate(item.label, 64),
        ceiling = "%.2f".format(item.ceiling),
        source = item.source,
        cost = "%.2f".format(item.cost),
        odometer = item.odometer.toString(),
        externalRef = item.externalRef,
        graceDays = item.graceDays.toString(),
        unit = item.unit,
        budgetText = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalRedeliveryGroupViews(items: MutableList<SeasonalRedeliveryGroup>): MutableList<SeasonalRedeliveryGroupView> {
    val views = mutableListOf<SeasonalRedeliveryGroupView>()
    for (item in items) {
        views.add(toSeasonalRedeliveryGroupView(item))
    }
    return views
}

fun sumSeasonalRedeliveryGroupView(view: SeasonalRedeliveryGroupView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedCost = view.cost.toDouble()
    val parsedOdometer = view.odometer.toInt()
    return parsedCeiling + parsedCost + parsedOdometer.toDouble()
}
