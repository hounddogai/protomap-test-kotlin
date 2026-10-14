package com.acme.transport.customs.broker.brokergroup

/** How screens and exports show a broker threshold. */
data class BrokerThresholdView(
    var code: String,
    var title: String,
    var createdAt: String,
    var phone: String,
    var loadKgText: String,
    var statusLabel: String,
)

fun toBrokerThresholdView(item: BrokerThreshold): BrokerThresholdView {
    return BrokerThresholdView(
        code = item.code,
        title = item.title,
        createdAt = item.createdAt.toString(),
        phone = item.phone,
        loadKgText = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerThresholdViews(items: MutableList<BrokerThreshold>): MutableList<BrokerThresholdView> {
    val views = mutableListOf<BrokerThresholdView>()
    for (item in items) {
        views.add(toBrokerThresholdView(item))
    }
    return views
}

fun sumBrokerThresholdView(view: BrokerThresholdView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedCreatedAt.toDouble() + parsedLoadKg
}
