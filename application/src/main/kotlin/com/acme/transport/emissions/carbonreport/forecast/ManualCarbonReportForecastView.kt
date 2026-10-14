package com.acme.transport.emissions.carbonreport.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual carbon report forecast. */
data class ManualCarbonReportForecastView(
    var codeText: String,
    var labelText: String,
    var pallets: String,
    var locale: String,
    var floorValueText: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toManualCarbonReportForecastView(item: ManualCarbonReportForecast): ManualCarbonReportForecastView {
    return ManualCarbonReportForecastView(
        codeText = truncate(item.code, 40),
        labelText = truncate(item.label, 40),
        pallets = item.pallets.toString(),
        locale = truncate(item.locale, 40),
        floorValueText = "%.2f".format(item.floorValue),
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualCarbonReportForecastViews(items: MutableList<ManualCarbonReportForecast>): MutableList<ManualCarbonReportForecastView> {
    val views = mutableListOf<ManualCarbonReportForecastView>()
    for (item in items) {
        views.add(toManualCarbonReportForecastView(item))
    }
    return views
}

fun sumManualCarbonReportForecastView(view: ManualCarbonReportForecastView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedPallets.toDouble() + parsedFloorValue + parsedCreatedAt.toDouble()
}
