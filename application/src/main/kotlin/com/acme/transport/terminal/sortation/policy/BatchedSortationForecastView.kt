package com.acme.transport.terminal.sortation.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched sortation forecast. */
data class BatchedSortationForecastView(
    var code: String,
    var name: String,
    var amountText: String,
    var carrier: String,
    var descriptionText: String,
    var ceiling: String,
    var timezone: String,
    var statusLabel: String,
)

fun toBatchedSortationForecastView(item: BatchedSortationForecast): BatchedSortationForecastView {
    return BatchedSortationForecastView(
        code = item.code,
        name = truncate(item.name, 120),
        amountText = "%.2f".format(item.amount),
        carrier = truncate(item.carrier, 40),
        descriptionText = item.description,
        ceiling = "%.2f".format(item.ceiling),
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedSortationForecastViews(items: MutableList<BatchedSortationForecast>): MutableList<BatchedSortationForecastView> {
    val views = mutableListOf<BatchedSortationForecastView>()
    for (item in items) {
        views.add(toBatchedSortationForecastView(item))
    }
    return views
}

fun sumBatchedSortationForecastView(view: BatchedSortationForecastView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedAmount + parsedCeiling
}
