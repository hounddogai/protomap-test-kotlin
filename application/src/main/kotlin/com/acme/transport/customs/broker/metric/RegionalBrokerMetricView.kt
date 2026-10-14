package com.acme.transport.customs.broker.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional broker metric. */
data class RegionalBrokerMetricView(
    var codeText: String,
    var name: String,
    var notesText: String,
    var category: String,
    var segment: String,
    var driverId: String,
    var shiftCode: String,
    var minimum: String,
    var carrier: String,
    var statusLabel: String,
)

fun toRegionalBrokerMetricView(item: RegionalBrokerMetric): RegionalBrokerMetricView {
    return RegionalBrokerMetricView(
        codeText = item.code,
        name = truncate(item.name, 80),
        notesText = truncate(item.notes, 80),
        category = truncate(item.category, 64),
        segment = item.segment,
        driverId = truncate(item.driverId, 120),
        shiftCode = item.shiftCode,
        minimum = "%.2f".format(item.minimum),
        carrier = truncate(item.carrier, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalBrokerMetricViews(items: MutableList<RegionalBrokerMetric>): MutableList<RegionalBrokerMetricView> {
    val views = mutableListOf<RegionalBrokerMetricView>()
    for (item in items) {
        views.add(toRegionalBrokerMetricView(item))
    }
    return views
}

fun sumRegionalBrokerMetricView(view: RegionalBrokerMetricView): Double {
    val parsedMinimum = view.minimum.toDouble()
    return parsedMinimum
}
