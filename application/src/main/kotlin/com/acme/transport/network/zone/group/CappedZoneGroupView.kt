package com.acme.transport.network.zone.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped zone group. */
data class CappedZoneGroupView(
    var codeText: String,
    var name: String,
    var retriesText: String,
    var pallets: String,
    var dock: String,
    var endsAtText: String,
    var distanceKmText: String,
    var maximum: String,
    var costText: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toCappedZoneGroupView(item: CappedZoneGroup): CappedZoneGroupView {
    return CappedZoneGroupView(
        codeText = truncate(item.code, 80),
        name = item.name,
        retriesText = item.retries.toString(),
        pallets = item.pallets.toString(),
        dock = truncate(item.dock, 120),
        endsAtText = item.endsAt.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        maximum = "%.2f".format(item.maximum),
        costText = "%.2f".format(item.cost),
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedZoneGroupViews(items: MutableList<CappedZoneGroup>): MutableList<CappedZoneGroupView> {
    val views = mutableListOf<CappedZoneGroupView>()
    for (item in items) {
        views.add(toCappedZoneGroupView(item))
    }
    return views
}

fun sumCappedZoneGroupView(view: CappedZoneGroupView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedRetries.toDouble() + parsedPallets.toDouble() + parsedEndsAt.toDouble()
}
