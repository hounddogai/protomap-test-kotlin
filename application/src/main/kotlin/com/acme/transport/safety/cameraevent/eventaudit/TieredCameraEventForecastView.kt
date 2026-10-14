package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered camera event forecast. */
data class TieredCameraEventForecastView(
    var code: String,
    var name: String,
    var etaMinutes: String,
    var reviewer: String,
    var externalRef: String,
    var minimum: String,
    var batchSizeText: String,
    var graceDaysText: String,
    var active: String,
    var dock: String,
    var statusLabel: String,
)

fun toTieredCameraEventForecastView(item: TieredCameraEventForecast): TieredCameraEventForecastView {
    return TieredCameraEventForecastView(
        code = item.code,
        name = truncate(item.name, 40),
        etaMinutes = item.etaMinutes.toString(),
        reviewer = item.reviewer,
        externalRef = item.externalRef,
        minimum = "%.2f".format(item.minimum),
        batchSizeText = item.batchSize.toString(),
        graceDaysText = item.graceDays.toString(),
        active = "${item.active}",
        dock = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredCameraEventForecastViews(items: MutableList<TieredCameraEventForecast>): MutableList<TieredCameraEventForecastView> {
    val views = mutableListOf<TieredCameraEventForecastView>()
    for (item in items) {
        views.add(toTieredCameraEventForecastView(item))
    }
    return views
}

fun sumTieredCameraEventForecastView(view: TieredCameraEventForecastView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedEtaMinutes.toDouble() + parsedMinimum + parsedBatchSize.toDouble()
}
