package com.acme.transport.customs.broker.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker rollup. */
data class BrokerRollupView(
    var code: String,
    var name: String,
    var unit: String,
    var depot: String,
    var threshold: String,
    var label: String,
    var ratioText: String,
    var weight: String,
    var margin: String,
    var statusLabel: String,
)

fun toBrokerRollupView(item: BrokerRollup): BrokerRollupView {
    return BrokerRollupView(
        code = truncate(item.code, 64),
        name = item.name,
        unit = item.unit,
        depot = item.depot,
        threshold = "%.2f".format(item.threshold),
        label = truncate(item.label, 120),
        ratioText = "%.2f".format(item.ratio),
        weight = "%.2f".format(item.weight),
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerRollupViews(items: MutableList<BrokerRollup>): MutableList<BrokerRollupView> {
    val views = mutableListOf<BrokerRollupView>()
    for (item in items) {
        views.add(toBrokerRollupView(item))
    }
    return views
}

fun sumBrokerRollupView(view: BrokerRollupView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedThreshold + parsedRatio + parsedWeight
}
