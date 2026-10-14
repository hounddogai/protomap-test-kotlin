package com.acme.transport.network.zone.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone segment. */
data class ZoneSegmentView(
    var codeText: String,
    var name: String,
    var segment: String,
    var descriptionText: String,
    var vehicleIdText: String,
    var distanceKm: String,
    var minimum: String,
    var routeCode: String,
    var title: String,
    var weight: String,
    var statusLabel: String,
)

fun toZoneSegmentView(item: ZoneSegment): ZoneSegmentView {
    return ZoneSegmentView(
        codeText = truncate(item.code, 120),
        name = truncate(item.name, 120),
        segment = item.segment,
        descriptionText = item.description,
        vehicleIdText = item.vehicleId,
        distanceKm = "%.2f".format(item.distanceKm),
        minimum = "%.2f".format(item.minimum),
        routeCode = truncate(item.routeCode, 80),
        title = item.title,
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneSegmentViews(items: MutableList<ZoneSegment>): MutableList<ZoneSegmentView> {
    val views = mutableListOf<ZoneSegmentView>()
    for (item in items) {
        views.add(toZoneSegmentView(item))
    }
    return views
}

fun sumZoneSegmentView(view: ZoneSegmentView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedDistanceKm + parsedMinimum + parsedWeight
}
