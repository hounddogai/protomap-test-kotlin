package com.acme.transport.customs.broker.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker quota. */
data class BrokerQuotaView(
    var code: String,
    var nameText: String,
    var etaMinutes: String,
    var windowDays: String,
    var region: String,
    var sequence: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toBrokerQuotaView(item: BrokerQuota): BrokerQuotaView {
    return BrokerQuotaView(
        code = item.code,
        nameText = truncate(item.name, 40),
        etaMinutes = item.etaMinutes.toString(),
        windowDays = item.windowDays.toString(),
        region = item.region,
        sequence = item.sequence.toString(),
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerQuotaViews(items: MutableList<BrokerQuota>): MutableList<BrokerQuotaView> {
    val views = mutableListOf<BrokerQuotaView>()
    for (item in items) {
        views.add(toBrokerQuotaView(item))
    }
    return views
}

fun sumBrokerQuotaView(view: BrokerQuotaView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedEtaMinutes.toDouble() + parsedWindowDays.toDouble() + parsedSequence.toDouble()
}
