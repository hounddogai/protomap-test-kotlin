package com.acme.transport.network.zone.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone request. */
data class ZoneRequestView(
    var code: String,
    var name: String,
    var active: String,
    var floorValueText: String,
    var archivedText: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toZoneRequestView(item: ZoneRequest): ZoneRequestView {
    return ZoneRequestView(
        code = item.code,
        name = truncate(item.name, 64),
        active = "${item.active}",
        floorValueText = "%.2f".format(item.floorValue),
        archivedText = "${item.archived}",
        externalRef = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneRequestViews(items: MutableList<ZoneRequest>): MutableList<ZoneRequestView> {
    val views = mutableListOf<ZoneRequestView>()
    for (item in items) {
        views.add(toZoneRequestView(item))
    }
    return views
}

fun sumZoneRequestView(view: ZoneRequestView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedFloorValue
}
