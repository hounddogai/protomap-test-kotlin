package com.acme.transport.linehaul.tender.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default tender metric. */
data class DefaultTenderMetricView(
    var codeText: String,
    var labelText: String,
    var amountText: String,
    var archived: String,
    var marginText: String,
    var fuelLitersText: String,
    var timezone: String,
    var statusLabel: String,
)

fun toDefaultTenderMetricView(item: DefaultTenderMetric): DefaultTenderMetricView {
    return DefaultTenderMetricView(
        codeText = truncate(item.code, 40),
        labelText = item.label,
        amountText = "%.2f".format(item.amount),
        archived = "${item.archived}",
        marginText = "%.2f".format(item.margin),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultTenderMetricViews(items: MutableList<DefaultTenderMetric>): MutableList<DefaultTenderMetricView> {
    val views = mutableListOf<DefaultTenderMetricView>()
    for (item in items) {
        views.add(toDefaultTenderMetricView(item))
    }
    return views
}

fun sumDefaultTenderMetricView(view: DefaultTenderMetricView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedMargin = view.marginText.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedAmount + parsedMargin + parsedFuelLiters
}
