package com.acme.transport.customs.broker.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker allocation. */
data class BrokerAllocationView(
    var codeText: String,
    var title: String,
    var name: String,
    var driverIdText: String,
    var segment: String,
    var amount: String,
    var statusLabel: String,
)

fun toBrokerAllocationView(item: BrokerAllocation): BrokerAllocationView {
    return BrokerAllocationView(
        codeText = truncate(item.code, 40),
        title = item.title,
        name = item.name,
        driverIdText = item.driverId,
        segment = truncate(item.segment, 40),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerAllocationViews(items: MutableList<BrokerAllocation>): MutableList<BrokerAllocationView> {
    val views = mutableListOf<BrokerAllocationView>()
    for (item in items) {
        views.add(toBrokerAllocationView(item))
    }
    return views
}

fun sumBrokerAllocationView(view: BrokerAllocationView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
