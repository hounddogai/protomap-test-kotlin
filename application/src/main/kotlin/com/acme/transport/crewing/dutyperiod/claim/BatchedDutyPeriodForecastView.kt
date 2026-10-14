package com.acme.transport.crewing.dutyperiod.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched duty period forecast. */
data class BatchedDutyPeriodForecastView(
    var codeText: String,
    var name: String,
    var attempts: String,
    var maximum: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toBatchedDutyPeriodForecastView(item: BatchedDutyPeriodForecast): BatchedDutyPeriodForecastView {
    return BatchedDutyPeriodForecastView(
        codeText = truncate(item.code, 40),
        name = item.name,
        attempts = item.attempts.toString(),
        maximum = "%.2f".format(item.maximum),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedDutyPeriodForecastViews(items: MutableList<BatchedDutyPeriodForecast>): MutableList<BatchedDutyPeriodForecastView> {
    val views = mutableListOf<BatchedDutyPeriodForecastView>()
    for (item in items) {
        views.add(toBatchedDutyPeriodForecastView(item))
    }
    return views
}

fun sumBatchedDutyPeriodForecastView(view: BatchedDutyPeriodForecastView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedAttempts.toDouble() + parsedMaximum
}
