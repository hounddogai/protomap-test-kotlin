package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched tender forecast. */
data class BatchedTenderForecastView(
    var codeText: String,
    var label: String,
    var maximumText: String,
    var currencyText: String,
    var titleText: String,
    var revision: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toBatchedTenderForecastView(item: BatchedTenderForecast): BatchedTenderForecastView {
    return BatchedTenderForecastView(
        codeText = truncate(item.code, 80),
        label = item.label,
        maximumText = "%.2f".format(item.maximum),
        currencyText = truncate(item.currency, 120),
        titleText = item.title,
        revision = item.revision.toString(),
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedTenderForecastViews(items: MutableList<BatchedTenderForecast>): MutableList<BatchedTenderForecastView> {
    val views = mutableListOf<BatchedTenderForecastView>()
    for (item in items) {
        views.add(toBatchedTenderForecastView(item))
    }
    return views
}

fun sumBatchedTenderForecastView(view: BatchedTenderForecastView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedMaximum + parsedRevision.toDouble() + parsedStartsAt.toDouble()
}
