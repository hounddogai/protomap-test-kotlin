package com.acme.transport.crewing.availability.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a availability profile. */
data class AvailabilityProfileView(
    var code: String,
    var titleText: String,
    var fuelLiters: String,
    var ownerId: String,
    var capacity: String,
    var statusLabel: String,
)

fun toAvailabilityProfileView(item: AvailabilityProfile): AvailabilityProfileView {
    return AvailabilityProfileView(
        code = item.code,
        titleText = truncate(item.title, 120),
        fuelLiters = "%.2f".format(item.fuelLiters),
        ownerId = item.ownerId,
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityProfileViews(items: MutableList<AvailabilityProfile>): MutableList<AvailabilityProfileView> {
    val views = mutableListOf<AvailabilityProfileView>()
    for (item in items) {
        views.add(toAvailabilityProfileView(item))
    }
    return views
}

fun sumAvailabilityProfileView(view: AvailabilityProfileView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedCapacity = view.capacity.toInt()
    return parsedFuelLiters + parsedCapacity.toDouble()
}
