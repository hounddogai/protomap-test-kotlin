package com.acme.transport.lastmile.attempt.attemptpolicy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a attempt metric. */
data class AttemptMetricView(
    var codeText: String,
    var label: String,
    var fuelLiters: String,
    var ownerIdText: String,
    var capacity: String,
    var description: String,
    var maximum: String,
    var currency: String,
    var summary: String,
    var cost: String,
    var statusLabel: String,
)

fun toAttemptMetricView(item: AttemptMetric): AttemptMetricView {
    return AttemptMetricView(
        codeText = item.code,
        label = item.label,
        fuelLiters = "%.2f".format(item.fuelLiters),
        ownerIdText = item.ownerId,
        capacity = item.capacity.toString(),
        description = item.description,
        maximum = "%.2f".format(item.maximum),
        currency = truncate(item.currency, 120),
        summary = item.summary,
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptMetricViews(items: MutableList<AttemptMetric>): MutableList<AttemptMetricView> {
    val views = mutableListOf<AttemptMetricView>()
    for (item in items) {
        views.add(toAttemptMetricView(item))
    }
    return views
}

fun sumAttemptMetricView(view: AttemptMetricView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedFuelLiters + parsedCapacity.toDouble() + parsedMaximum
}
