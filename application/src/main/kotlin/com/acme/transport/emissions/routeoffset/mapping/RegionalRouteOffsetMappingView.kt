package com.acme.transport.emissions.routeoffset.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional route offset mapping. */
data class RegionalRouteOffsetMappingView(
    var code: String,
    var title: String,
    var expiresAtText: String,
    var endsAtText: String,
    var vehicleId: String,
    var timezone: String,
    var phone: String,
    var distanceKmText: String,
    var sourceText: String,
    var laneText: String,
    var statusLabel: String,
)

fun toRegionalRouteOffsetMappingView(item: RegionalRouteOffsetMapping): RegionalRouteOffsetMappingView {
    return RegionalRouteOffsetMappingView(
        code = item.code,
        title = item.title,
        expiresAtText = item.expiresAt.toString(),
        endsAtText = item.endsAt.toString(),
        vehicleId = item.vehicleId,
        timezone = item.timezone,
        phone = truncate(item.phone, 64),
        distanceKmText = "%.2f".format(item.distanceKm),
        sourceText = item.source,
        laneText = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalRouteOffsetMappingViews(items: MutableList<RegionalRouteOffsetMapping>): MutableList<RegionalRouteOffsetMappingView> {
    val views = mutableListOf<RegionalRouteOffsetMappingView>()
    for (item in items) {
        views.add(toRegionalRouteOffsetMappingView(item))
    }
    return views
}

fun sumRegionalRouteOffsetMappingView(view: RegionalRouteOffsetMappingView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedExpiresAt.toDouble() + parsedEndsAt.toDouble() + parsedDistanceKm
}
