package com.acme.transport.network.hub.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub batch. */
data class HubBatchView(
    var code: String,
    var name: String,
    var channel: String,
    var loadKg: String,
    var vehicleId: String,
    var rate: String,
    var trailerId: String,
    var carrier: String,
    var amount: String,
    var retriesText: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toHubBatchView(item: HubBatch): HubBatchView {
    return HubBatchView(
        code = truncate(item.code, 64),
        name = item.name,
        channel = truncate(item.channel, 120),
        loadKg = "%.2f".format(item.loadKg),
        vehicleId = truncate(item.vehicleId, 80),
        rate = "%.2f".format(item.rate),
        trailerId = item.trailerId,
        carrier = item.carrier,
        amount = "%.2f".format(item.amount),
        retriesText = item.retries.toString(),
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubBatchViews(items: MutableList<HubBatch>): MutableList<HubBatchView> {
    val views = mutableListOf<HubBatchView>()
    for (item in items) {
        views.add(toHubBatchView(item))
    }
    return views
}

fun sumHubBatchView(view: HubBatchView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedRate = view.rate.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedLoadKg + parsedRate + parsedAmount
}
