package com.acme.transport.lastmile.parcel.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a parcel group. */
data class ParcelGroupView(
    var code: String,
    var name: String,
    var routeCode: String,
    var active: String,
    var startsAt: String,
    var description: String,
    var vehicleId: String,
    var costText: String,
    var expiresAt: String,
    var capacity: String,
    var statusLabel: String,
)

fun toParcelGroupView(item: ParcelGroup): ParcelGroupView {
    return ParcelGroupView(
        code = truncate(item.code, 120),
        name = item.name,
        routeCode = item.routeCode,
        active = "${item.active}",
        startsAt = item.startsAt.toString(),
        description = item.description,
        vehicleId = item.vehicleId,
        costText = "%.2f".format(item.cost),
        expiresAt = item.expiresAt.toString(),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelGroupViews(items: MutableList<ParcelGroup>): MutableList<ParcelGroupView> {
    val views = mutableListOf<ParcelGroupView>()
    for (item in items) {
        views.add(toParcelGroupView(item))
    }
    return views
}

fun sumParcelGroupView(view: ParcelGroupView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedCost = view.costText.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedStartsAt.toDouble() + parsedCost + parsedExpiresAt.toDouble()
}
