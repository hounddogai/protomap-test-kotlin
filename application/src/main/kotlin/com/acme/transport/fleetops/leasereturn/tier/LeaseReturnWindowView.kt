package com.acme.transport.fleetops.leasereturn.tier

/** How screens and exports show a lease return window. */
data class LeaseReturnWindowView(
    var code: String,
    var labelText: String,
    var title: String,
    var durationMinutes: String,
    var timezone: String,
    var floorValueText: String,
    var ownerIdText: String,
    var statusLabel: String,
)

fun toLeaseReturnWindowView(item: LeaseReturnWindow): LeaseReturnWindowView {
    return LeaseReturnWindowView(
        code = item.code,
        labelText = item.label,
        title = item.title,
        durationMinutes = item.durationMinutes.toString(),
        timezone = item.timezone,
        floorValueText = "%.2f".format(item.floorValue),
        ownerIdText = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnWindowViews(items: MutableList<LeaseReturnWindow>): MutableList<LeaseReturnWindowView> {
    val views = mutableListOf<LeaseReturnWindowView>()
    for (item in items) {
        views.add(toLeaseReturnWindowView(item))
    }
    return views
}

fun sumLeaseReturnWindowView(view: LeaseReturnWindowView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedDurationMinutes.toDouble() + parsedFloorValue
}
