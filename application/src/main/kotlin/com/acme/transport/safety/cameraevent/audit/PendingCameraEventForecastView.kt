package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending camera event forecast. */
data class PendingCameraEventForecastView(
    var code: String,
    var titleText: String,
    var contactEmail: String,
    var loadKgText: String,
    var graceDays: String,
    var unitText: String,
    var depot: String,
    var summary: String,
    var batchSizeText: String,
    var version: String,
    var statusLabel: String,
)

fun toPendingCameraEventForecastView(item: PendingCameraEventForecast): PendingCameraEventForecastView {
    return PendingCameraEventForecastView(
        code = truncate(item.code, 80),
        titleText = truncate(item.title, 120),
        contactEmail = item.contactEmail,
        loadKgText = "%.2f".format(item.loadKg),
        graceDays = item.graceDays.toString(),
        unitText = item.unit,
        depot = truncate(item.depot, 40),
        summary = item.summary,
        batchSizeText = item.batchSize.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingCameraEventForecastViews(items: MutableList<PendingCameraEventForecast>): MutableList<PendingCameraEventForecastView> {
    val views = mutableListOf<PendingCameraEventForecastView>()
    for (item in items) {
        views.add(toPendingCameraEventForecastView(item))
    }
    return views
}

fun sumPendingCameraEventForecastView(view: PendingCameraEventForecastView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedLoadKg + parsedGraceDays.toDouble() + parsedBatchSize.toDouble()
}
