package com.acme.transport.network.zone.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy zone record. */
data class LegacyZoneRecordView(
    var codeText: String,
    var title: String,
    var pinnedText: String,
    var region: String,
    var routeCode: String,
    var capacity: String,
    var channel: String,
    var fuelLiters: String,
    var maximumText: String,
    var statusLabel: String,
)

fun toLegacyZoneRecordView(item: LegacyZoneRecord): LegacyZoneRecordView {
    return LegacyZoneRecordView(
        codeText = item.code,
        title = item.title,
        pinnedText = "${item.pinned}",
        region = item.region,
        routeCode = truncate(item.routeCode, 64),
        capacity = item.capacity.toString(),
        channel = item.channel,
        fuelLiters = "%.2f".format(item.fuelLiters),
        maximumText = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyZoneRecordViews(items: MutableList<LegacyZoneRecord>): MutableList<LegacyZoneRecordView> {
    val views = mutableListOf<LegacyZoneRecordView>()
    for (item in items) {
        views.add(toLegacyZoneRecordView(item))
    }
    return views
}

fun sumLegacyZoneRecordView(view: LegacyZoneRecordView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedCapacity.toDouble() + parsedFuelLiters + parsedMaximum
}
