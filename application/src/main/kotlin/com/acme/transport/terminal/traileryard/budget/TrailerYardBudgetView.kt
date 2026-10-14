package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard budget. */
data class TrailerYardBudgetView(
    var code: String,
    var name: String,
    var floorValueText: String,
    var maximum: String,
    var countryText: String,
    var region: String,
    var active: String,
    var statusLabel: String,
)

fun toTrailerYardBudgetView(item: TrailerYardBudget): TrailerYardBudgetView {
    return TrailerYardBudgetView(
        code = truncate(item.code, 80),
        name = item.name,
        floorValueText = "%.2f".format(item.floorValue),
        maximum = "%.2f".format(item.maximum),
        countryText = item.country,
        region = item.region,
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardBudgetViews(items: MutableList<TrailerYardBudget>): MutableList<TrailerYardBudgetView> {
    val views = mutableListOf<TrailerYardBudgetView>()
    for (item in items) {
        views.add(toTrailerYardBudgetView(item))
    }
    return views
}

fun sumTrailerYardBudgetView(view: TrailerYardBudgetView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedFloorValue + parsedMaximum
}
