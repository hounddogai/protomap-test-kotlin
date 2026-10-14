package com.acme.transport.network.hub.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped hub entry. */
data class CappedHubEntryView(
    var code: String,
    var nameText: String,
    var ratio: String,
    var source: String,
    var margin: String,
    var floorValue: String,
    var ceiling: String,
    var externalRef: String,
    var phone: String,
    var driverIdText: String,
    var minimum: String,
    var statusLabel: String,
)

fun toCappedHubEntryView(item: CappedHubEntry): CappedHubEntryView {
    return CappedHubEntryView(
        code = item.code,
        nameText = item.name,
        ratio = "%.2f".format(item.ratio),
        source = truncate(item.source, 40),
        margin = "%.2f".format(item.margin),
        floorValue = "%.2f".format(item.floorValue),
        ceiling = "%.2f".format(item.ceiling),
        externalRef = item.externalRef,
        phone = item.phone,
        driverIdText = item.driverId,
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedHubEntryViews(items: MutableList<CappedHubEntry>): MutableList<CappedHubEntryView> {
    val views = mutableListOf<CappedHubEntryView>()
    for (item in items) {
        views.add(toCappedHubEntryView(item))
    }
    return views
}

fun sumCappedHubEntryView(view: CappedHubEntryView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedMargin = view.margin.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedRatio + parsedMargin + parsedFloorValue
}
