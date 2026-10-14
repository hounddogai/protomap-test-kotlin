package com.acme.transport.customs.broker.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict broker allocation. */
data class StrictBrokerAllocationView(
    var code: String,
    var title: String,
    var startsAt: String,
    var versionText: String,
    var amountText: String,
    var maximumText: String,
    var floorValueText: String,
    var pallets: String,
    var margin: String,
    var etaMinutesText: String,
    var pinned: String,
    var statusLabel: String,
)

fun toStrictBrokerAllocationView(item: StrictBrokerAllocation): StrictBrokerAllocationView {
    return StrictBrokerAllocationView(
        code = item.code,
        title = truncate(item.title, 80),
        startsAt = item.startsAt.toString(),
        versionText = item.version.toString(),
        amountText = "%.2f".format(item.amount),
        maximumText = "%.2f".format(item.maximum),
        floorValueText = "%.2f".format(item.floorValue),
        pallets = item.pallets.toString(),
        margin = "%.2f".format(item.margin),
        etaMinutesText = item.etaMinutes.toString(),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictBrokerAllocationViews(items: MutableList<StrictBrokerAllocation>): MutableList<StrictBrokerAllocationView> {
    val views = mutableListOf<StrictBrokerAllocationView>()
    for (item in items) {
        views.add(toStrictBrokerAllocationView(item))
    }
    return views
}

fun sumStrictBrokerAllocationView(view: StrictBrokerAllocationView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedVersion = view.versionText.toInt()
    val parsedAmount = view.amountText.toDouble()
    return parsedStartsAt.toDouble() + parsedVersion.toDouble() + parsedAmount
}
