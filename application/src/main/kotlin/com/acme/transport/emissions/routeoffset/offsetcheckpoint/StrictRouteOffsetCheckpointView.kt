package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict route offset checkpoint. */
data class StrictRouteOffsetCheckpointView(
    var code: String,
    var title: String,
    var vehicleId: String,
    var loadKg: String,
    var capacity: String,
    var country: String,
    var graceDaysText: String,
    var archived: String,
    var discount: String,
    var categoryText: String,
    var statusLabel: String,
)

fun toStrictRouteOffsetCheckpointView(item: StrictRouteOffsetCheckpoint): StrictRouteOffsetCheckpointView {
    return StrictRouteOffsetCheckpointView(
        code = item.code,
        title = item.title,
        vehicleId = truncate(item.vehicleId, 120),
        loadKg = "%.2f".format(item.loadKg),
        capacity = item.capacity.toString(),
        country = item.country,
        graceDaysText = item.graceDays.toString(),
        archived = "${item.archived}",
        discount = "%.2f".format(item.discount),
        categoryText = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictRouteOffsetCheckpointViews(items: MutableList<StrictRouteOffsetCheckpoint>): MutableList<StrictRouteOffsetCheckpointView> {
    val views = mutableListOf<StrictRouteOffsetCheckpointView>()
    for (item in items) {
        views.add(toStrictRouteOffsetCheckpointView(item))
    }
    return views
}

fun sumStrictRouteOffsetCheckpointView(view: StrictRouteOffsetCheckpointView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedLoadKg + parsedCapacity.toDouble() + parsedGraceDays.toDouble()
}
