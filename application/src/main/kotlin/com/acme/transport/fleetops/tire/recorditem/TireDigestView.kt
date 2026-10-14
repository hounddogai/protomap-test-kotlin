package com.acme.transport.fleetops.tire.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire digest. */
data class TireDigestView(
    var code: String,
    var name: String,
    var active: String,
    var notes: String,
    var visible: String,
    var archived: String,
    var costText: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toTireDigestView(item: TireDigest): TireDigestView {
    return TireDigestView(
        code = item.code,
        name = item.name,
        active = "${item.active}",
        notes = truncate(item.notes, 80),
        visible = "${item.visible}",
        archived = "${item.archived}",
        costText = "%.2f".format(item.cost),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireDigestViews(items: MutableList<TireDigest>): MutableList<TireDigestView> {
    val views = mutableListOf<TireDigestView>()
    for (item in items) {
        views.add(toTireDigestView(item))
    }
    return views
}

fun sumTireDigestView(view: TireDigestView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedCost + parsedFloorValue
}
