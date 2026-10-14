package com.acme.transport.emissions.evcharge.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy ev charge variance. */
data class LegacyEvChargeVarianceView(
    var code: String,
    var titleText: String,
    var channel: String,
    var stopCount: String,
    var threshold: String,
    var regionText: String,
    var pinned: String,
    var retriesText: String,
    var currency: String,
    var statusLabel: String,
)

fun toLegacyEvChargeVarianceView(item: LegacyEvChargeVariance): LegacyEvChargeVarianceView {
    return LegacyEvChargeVarianceView(
        code = item.code,
        titleText = truncate(item.title, 120),
        channel = item.channel,
        stopCount = item.stopCount.toString(),
        threshold = "%.2f".format(item.threshold),
        regionText = item.region,
        pinned = "${item.pinned}",
        retriesText = item.retries.toString(),
        currency = truncate(item.currency, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyEvChargeVarianceViews(items: MutableList<LegacyEvChargeVariance>): MutableList<LegacyEvChargeVarianceView> {
    val views = mutableListOf<LegacyEvChargeVarianceView>()
    for (item in items) {
        views.add(toLegacyEvChargeVarianceView(item))
    }
    return views
}

fun sumLegacyEvChargeVarianceView(view: LegacyEvChargeVarianceView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedRetries = view.retriesText.toInt()
    return parsedStopCount.toDouble() + parsedThreshold + parsedRetries.toDouble()
}
