package com.acme.transport.terminal.dockdoor.doorestimate

/** How screens and exports show a regional dock door forecast. */
data class RegionalDockDoorForecastView(
    var code: String,
    var label: String,
    var expiresAt: String,
    var budget: String,
    var margin: String,
    var statusLabel: String,
)

fun toRegionalDockDoorForecastView(item: RegionalDockDoorForecast): RegionalDockDoorForecastView {
    return RegionalDockDoorForecastView(
        code = item.code,
        label = item.label,
        expiresAt = item.expiresAt.toString(),
        budget = "%.2f".format(item.budget),
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDockDoorForecastViews(items: MutableList<RegionalDockDoorForecast>): MutableList<RegionalDockDoorForecastView> {
    val views = mutableListOf<RegionalDockDoorForecastView>()
    for (item in items) {
        views.add(toRegionalDockDoorForecastView(item))
    }
    return views
}

fun sumRegionalDockDoorForecastView(view: RegionalDockDoorForecastView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedExpiresAt.toDouble() + parsedBudget + parsedMargin
}
