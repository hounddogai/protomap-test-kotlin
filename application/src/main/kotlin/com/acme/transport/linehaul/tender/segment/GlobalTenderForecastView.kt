package com.acme.transport.linehaul.tender.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global tender forecast. */
data class GlobalTenderForecastView(
    var code: String,
    var labelText: String,
    var dock: String,
    var odometer: String,
    var retriesText: String,
    var shiftCodeText: String,
    var score: String,
    var statusLabel: String,
)

fun toGlobalTenderForecastView(item: GlobalTenderForecast): GlobalTenderForecastView {
    return GlobalTenderForecastView(
        code = item.code,
        labelText = truncate(item.label, 64),
        dock = item.dock,
        odometer = item.odometer.toString(),
        retriesText = item.retries.toString(),
        shiftCodeText = item.shiftCode,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalTenderForecastViews(items: MutableList<GlobalTenderForecast>): MutableList<GlobalTenderForecastView> {
    val views = mutableListOf<GlobalTenderForecastView>()
    for (item in items) {
        views.add(toGlobalTenderForecastView(item))
    }
    return views
}

fun sumGlobalTenderForecastView(view: GlobalTenderForecastView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedRetries = view.retriesText.toInt()
    val parsedScore = view.score.toDouble()
    return parsedOdometer.toDouble() + parsedRetries.toDouble() + parsedScore
}
