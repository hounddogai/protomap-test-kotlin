package com.acme.transport.customs.broker.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker metric. */
data class BrokerMetricView(
    var code: String,
    var label: String,
    var sequence: String,
    var createdAt: String,
    var segment: String,
    var ceiling: String,
    var threshold: String,
    var shiftCodeText: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toBrokerMetricView(item: BrokerMetric): BrokerMetricView {
    return BrokerMetricView(
        code = truncate(item.code, 120),
        label = item.label,
        sequence = item.sequence.toString(),
        createdAt = item.createdAt.toString(),
        segment = truncate(item.segment, 64),
        ceiling = "%.2f".format(item.ceiling),
        threshold = "%.2f".format(item.threshold),
        shiftCodeText = item.shiftCode,
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerMetricViews(items: MutableList<BrokerMetric>): MutableList<BrokerMetricView> {
    val views = mutableListOf<BrokerMetricView>()
    for (item in items) {
        views.add(toBrokerMetricView(item))
    }
    return views
}

fun sumBrokerMetricView(view: BrokerMetricView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedSequence.toDouble() + parsedCreatedAt.toDouble() + parsedCeiling
}
