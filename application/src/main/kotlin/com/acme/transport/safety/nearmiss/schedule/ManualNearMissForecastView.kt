package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual near miss forecast. */
data class ManualNearMissForecastView(
    var codeText: String,
    var label: String,
    var dock: String,
    var batchSize: String,
    var quantity: String,
    var minimumText: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toManualNearMissForecastView(item: ManualNearMissForecast): ManualNearMissForecastView {
    return ManualNearMissForecastView(
        codeText = item.code,
        label = item.label,
        dock = truncate(item.dock, 40),
        batchSize = item.batchSize.toString(),
        quantity = item.quantity.toString(),
        minimumText = "%.2f".format(item.minimum),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualNearMissForecastViews(items: MutableList<ManualNearMissForecast>): MutableList<ManualNearMissForecastView> {
    val views = mutableListOf<ManualNearMissForecastView>()
    for (item in items) {
        views.add(toManualNearMissForecastView(item))
    }
    return views
}

fun sumManualNearMissForecastView(view: ManualNearMissForecastView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedBatchSize.toDouble() + parsedQuantity.toDouble() + parsedMinimum
}
