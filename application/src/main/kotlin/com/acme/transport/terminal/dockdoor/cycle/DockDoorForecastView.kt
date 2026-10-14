package com.acme.transport.terminal.dockdoor.cycle

/** How screens and exports show a dock door forecast. */
data class DockDoorForecastView(
    var codeText: String,
    var nameText: String,
    var loadKg: String,
    var budget: String,
    var maximum: String,
    var statusLabel: String,
)

fun toDockDoorForecastView(item: DockDoorForecast): DockDoorForecastView {
    return DockDoorForecastView(
        codeText = item.code,
        nameText = item.name,
        loadKg = "%.2f".format(item.loadKg),
        budget = "%.2f".format(item.budget),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorForecastViews(items: MutableList<DockDoorForecast>): MutableList<DockDoorForecastView> {
    val views = mutableListOf<DockDoorForecastView>()
    for (item in items) {
        views.add(toDockDoorForecastView(item))
    }
    return views
}

fun sumDockDoorForecastView(view: DockDoorForecastView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedBudget = view.budget.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedLoadKg + parsedBudget + parsedMaximum
}
