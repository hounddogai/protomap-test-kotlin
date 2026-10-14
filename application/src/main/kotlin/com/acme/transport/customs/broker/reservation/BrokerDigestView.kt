package com.acme.transport.customs.broker.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker digest. */
data class BrokerDigestView(
    var code: String,
    var label: String,
    var stopCount: String,
    var limit: String,
    var durationMinutes: String,
    var loadKgText: String,
    var palletsText: String,
    var budgetText: String,
    var archived: String,
    var statusLabel: String,
)

fun toBrokerDigestView(item: BrokerDigest): BrokerDigestView {
    return BrokerDigestView(
        code = truncate(item.code, 120),
        label = item.label,
        stopCount = item.stopCount.toString(),
        limit = item.limit.toString(),
        durationMinutes = item.durationMinutes.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        palletsText = item.pallets.toString(),
        budgetText = "%.2f".format(item.budget),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerDigestViews(items: MutableList<BrokerDigest>): MutableList<BrokerDigestView> {
    val views = mutableListOf<BrokerDigestView>()
    for (item in items) {
        views.add(toBrokerDigestView(item))
    }
    return views
}

fun sumBrokerDigestView(view: BrokerDigestView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedStopCount.toDouble() + parsedLimit.toDouble() + parsedDurationMinutes.toDouble()
}
