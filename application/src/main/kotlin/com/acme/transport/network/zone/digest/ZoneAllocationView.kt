package com.acme.transport.network.zone.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone allocation. */
data class ZoneAllocationView(
    var codeText: String,
    var titleText: String,
    var vehicleId: String,
    var distanceKm: String,
    var externalRefText: String,
    var dockText: String,
    var carrier: String,
    var statusLabel: String,
)

fun toZoneAllocationView(item: ZoneAllocation): ZoneAllocationView {
    return ZoneAllocationView(
        codeText = truncate(item.code, 80),
        titleText = item.title,
        vehicleId = item.vehicleId,
        distanceKm = "%.2f".format(item.distanceKm),
        externalRefText = item.externalRef,
        dockText = item.dock,
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneAllocationViews(items: MutableList<ZoneAllocation>): MutableList<ZoneAllocationView> {
    val views = mutableListOf<ZoneAllocationView>()
    for (item in items) {
        views.add(toZoneAllocationView(item))
    }
    return views
}

fun sumZoneAllocationView(view: ZoneAllocationView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedDistanceKm
}
