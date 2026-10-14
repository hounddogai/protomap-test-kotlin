package com.acme.transport.terminal.dwell.summary

/** How screens and exports show a manual dwell forecast. */
data class ManualDwellForecastView(
    var codeText: String,
    var title: String,
    var trailerIdText: String,
    var graceDaysText: String,
    var fuelLiters: String,
    var region: String,
    var maximum: String,
    var stopCountText: String,
    var version: String,
    var loadKg: String,
    var depot: String,
    var statusLabel: String,
)

fun toManualDwellForecastView(item: ManualDwellForecast): ManualDwellForecastView {
    return ManualDwellForecastView(
        codeText = item.code,
        title = item.title,
        trailerIdText = item.trailerId,
        graceDaysText = item.graceDays.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        region = item.region,
        maximum = "%.2f".format(item.maximum),
        stopCountText = item.stopCount.toString(),
        version = item.version.toString(),
        loadKg = "%.2f".format(item.loadKg),
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualDwellForecastViews(items: MutableList<ManualDwellForecast>): MutableList<ManualDwellForecastView> {
    val views = mutableListOf<ManualDwellForecastView>()
    for (item in items) {
        views.add(toManualDwellForecastView(item))
    }
    return views
}

fun sumManualDwellForecastView(view: ManualDwellForecastView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedGraceDays.toDouble() + parsedFuelLiters + parsedMaximum
}
