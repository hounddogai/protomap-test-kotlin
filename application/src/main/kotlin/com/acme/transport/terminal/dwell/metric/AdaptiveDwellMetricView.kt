package com.acme.transport.terminal.dwell.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive dwell metric. */
data class AdaptiveDwellMetricView(
    var code: String,
    var nameText: String,
    var endsAtText: String,
    var depot: String,
    var windowDays: String,
    var source: String,
    var countryText: String,
    var rate: String,
    var label: String,
    var statusLabel: String,
)

fun toAdaptiveDwellMetricView(item: AdaptiveDwellMetric): AdaptiveDwellMetricView {
    return AdaptiveDwellMetricView(
        code = item.code,
        nameText = truncate(item.name, 64),
        endsAtText = item.endsAt.toString(),
        depot = item.depot,
        windowDays = item.windowDays.toString(),
        source = truncate(item.source, 64),
        countryText = truncate(item.country, 80),
        rate = "%.2f".format(item.rate),
        label = item.label,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveDwellMetricViews(items: MutableList<AdaptiveDwellMetric>): MutableList<AdaptiveDwellMetricView> {
    val views = mutableListOf<AdaptiveDwellMetricView>()
    for (item in items) {
        views.add(toAdaptiveDwellMetricView(item))
    }
    return views
}

fun sumAdaptiveDwellMetricView(view: AdaptiveDwellMetricView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedEndsAt.toDouble() + parsedWindowDays.toDouble() + parsedRate
}
