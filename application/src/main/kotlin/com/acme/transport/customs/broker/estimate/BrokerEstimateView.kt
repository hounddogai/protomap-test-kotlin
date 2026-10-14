package com.acme.transport.customs.broker.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker estimate. */
data class BrokerEstimateView(
    var code: String,
    var label: String,
    var driverId: String,
    var weight: String,
    var channel: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toBrokerEstimateView(item: BrokerEstimate): BrokerEstimateView {
    return BrokerEstimateView(
        code = item.code,
        label = item.label,
        driverId = truncate(item.driverId, 40),
        weight = "%.2f".format(item.weight),
        channel = item.channel,
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerEstimateViews(items: MutableList<BrokerEstimate>): MutableList<BrokerEstimateView> {
    val views = mutableListOf<BrokerEstimateView>()
    for (item in items) {
        views.add(toBrokerEstimateView(item))
    }
    return views
}

fun sumBrokerEstimateView(view: BrokerEstimateView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedWeight + parsedStartsAt.toDouble()
}
