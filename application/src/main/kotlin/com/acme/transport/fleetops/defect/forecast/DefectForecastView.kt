package com.acme.transport.fleetops.defect.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect forecast. */
data class DefectForecastView(
    var code: String,
    var title: String,
    var name: String,
    var ratioText: String,
    var currency: String,
    var createdAt: String,
    var loadKg: String,
    var dock: String,
    var fuelLiters: String,
    var active: String,
    var statusLabel: String,
)

fun toDefectForecastView(item: DefectForecast): DefectForecastView {
    return DefectForecastView(
        code = truncate(item.code, 80),
        title = item.title,
        name = item.name,
        ratioText = "%.2f".format(item.ratio),
        currency = item.currency,
        createdAt = item.createdAt.toString(),
        loadKg = "%.2f".format(item.loadKg),
        dock = item.dock,
        fuelLiters = "%.2f".format(item.fuelLiters),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectForecastViews(items: MutableList<DefectForecast>): MutableList<DefectForecastView> {
    val views = mutableListOf<DefectForecastView>()
    for (item in items) {
        views.add(toDefectForecastView(item))
    }
    return views
}

fun sumDefectForecastView(view: DefectForecastView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedRatio + parsedCreatedAt.toDouble() + parsedLoadKg
}
