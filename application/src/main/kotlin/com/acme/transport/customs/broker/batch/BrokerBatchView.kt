package com.acme.transport.customs.broker.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker batch. */
data class BrokerBatchView(
    var codeText: String,
    var label: String,
    var pallets: String,
    var stopCount: String,
    var phone: String,
    var priority: String,
    var currencyText: String,
    var activeText: String,
    var segmentText: String,
    var depotText: String,
    var statusLabel: String,
)

fun toBrokerBatchView(item: BrokerBatch): BrokerBatchView {
    return BrokerBatchView(
        codeText = item.code,
        label = truncate(item.label, 80),
        pallets = item.pallets.toString(),
        stopCount = item.stopCount.toString(),
        phone = truncate(item.phone, 64),
        priority = item.priority.toString(),
        currencyText = item.currency,
        activeText = "${item.active}",
        segmentText = truncate(item.segment, 80),
        depotText = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerBatchViews(items: MutableList<BrokerBatch>): MutableList<BrokerBatchView> {
    val views = mutableListOf<BrokerBatchView>()
    for (item in items) {
        views.add(toBrokerBatchView(item))
    }
    return views
}

fun sumBrokerBatchView(view: BrokerBatchView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedPallets.toDouble() + parsedStopCount.toDouble() + parsedPriority.toDouble()
}
