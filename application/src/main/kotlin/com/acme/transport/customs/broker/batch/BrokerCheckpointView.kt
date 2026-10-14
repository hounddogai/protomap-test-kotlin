package com.acme.transport.customs.broker.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker checkpoint. */
data class BrokerCheckpointView(
    var code: String,
    var name: String,
    var maximumText: String,
    var rate: String,
    var contactEmail: String,
    var distanceKm: String,
    var externalRefText: String,
    var budgetText: String,
    var capacity: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toBrokerCheckpointView(item: BrokerCheckpoint): BrokerCheckpointView {
    return BrokerCheckpointView(
        code = truncate(item.code, 40),
        name = item.name,
        maximumText = "%.2f".format(item.maximum),
        rate = "%.2f".format(item.rate),
        contactEmail = truncate(item.contactEmail, 64),
        distanceKm = "%.2f".format(item.distanceKm),
        externalRefText = truncate(item.externalRef, 40),
        budgetText = "%.2f".format(item.budget),
        capacity = item.capacity.toString(),
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerCheckpointViews(items: MutableList<BrokerCheckpoint>): MutableList<BrokerCheckpointView> {
    val views = mutableListOf<BrokerCheckpointView>()
    for (item in items) {
        views.add(toBrokerCheckpointView(item))
    }
    return views
}

fun sumBrokerCheckpointView(view: BrokerCheckpointView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedRate = view.rate.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedMaximum + parsedRate + parsedDistanceKm
}
