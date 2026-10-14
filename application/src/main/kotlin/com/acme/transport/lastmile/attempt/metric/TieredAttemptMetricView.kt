package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered attempt metric. */
data class TieredAttemptMetricView(
    var code: String,
    var labelText: String,
    var startsAtText: String,
    var limitText: String,
    var amountText: String,
    var country: String,
    var weight: String,
    var dockText: String,
    var retriesText: String,
    var statusLabel: String,
)

fun toTieredAttemptMetricView(item: TieredAttemptMetric): TieredAttemptMetricView {
    return TieredAttemptMetricView(
        code = truncate(item.code, 40),
        labelText = item.label,
        startsAtText = item.startsAt.toString(),
        limitText = item.limit.toString(),
        amountText = "%.2f".format(item.amount),
        country = item.country,
        weight = "%.2f".format(item.weight),
        dockText = truncate(item.dock, 80),
        retriesText = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredAttemptMetricViews(items: MutableList<TieredAttemptMetric>): MutableList<TieredAttemptMetricView> {
    val views = mutableListOf<TieredAttemptMetricView>()
    for (item in items) {
        views.add(toTieredAttemptMetricView(item))
    }
    return views
}

fun sumTieredAttemptMetricView(view: TieredAttemptMetricView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedLimit = view.limitText.toInt()
    val parsedAmount = view.amountText.toDouble()
    return parsedStartsAt.toDouble() + parsedLimit.toDouble() + parsedAmount
}
