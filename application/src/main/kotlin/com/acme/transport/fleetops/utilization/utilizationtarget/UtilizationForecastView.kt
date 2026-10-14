package com.acme.transport.fleetops.utilization.utilizationtarget

/** How screens and exports show a utilization forecast. */
data class UtilizationForecastView(
    var code: String,
    var labelText: String,
    var endsAtText: String,
    var version: String,
    var budget: String,
    var statusLabel: String,
)

fun toUtilizationForecastView(item: UtilizationForecast): UtilizationForecastView {
    return UtilizationForecastView(
        code = item.code,
        labelText = item.label,
        endsAtText = item.endsAt.toString(),
        version = item.version.toString(),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationForecastViews(items: MutableList<UtilizationForecast>): MutableList<UtilizationForecastView> {
    val views = mutableListOf<UtilizationForecastView>()
    for (item in items) {
        views.add(toUtilizationForecastView(item))
    }
    return views
}

fun sumUtilizationForecastView(view: UtilizationForecastView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedVersion = view.version.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedEndsAt.toDouble() + parsedVersion.toDouble() + parsedBudget
}
