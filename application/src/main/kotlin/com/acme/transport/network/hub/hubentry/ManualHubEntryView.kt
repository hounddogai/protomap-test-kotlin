package com.acme.transport.network.hub.hubentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual hub entry. */
data class ManualHubEntryView(
    var code: String,
    var titleText: String,
    var vehicleId: String,
    var lane: String,
    var capacityText: String,
    var statusLabel: String,
)

fun toManualHubEntryView(item: ManualHubEntry): ManualHubEntryView {
    return ManualHubEntryView(
        code = item.code,
        titleText = item.title,
        vehicleId = truncate(item.vehicleId, 80),
        lane = item.lane,
        capacityText = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualHubEntryViews(items: MutableList<ManualHubEntry>): MutableList<ManualHubEntryView> {
    val views = mutableListOf<ManualHubEntryView>()
    for (item in items) {
        views.add(toManualHubEntryView(item))
    }
    return views
}

fun sumManualHubEntryView(view: ManualHubEntryView): Double {
    val parsedCapacity = view.capacityText.toInt()
    return parsedCapacity.toDouble()
}
