package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted route offset quota. */
data class WeightedRouteOffsetQuotaView(
    var code: String,
    var label: String,
    var distanceKmText: String,
    var archivedText: String,
    var driverId: String,
    var phoneText: String,
    var cost: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toWeightedRouteOffsetQuotaView(item: WeightedRouteOffsetQuota): WeightedRouteOffsetQuotaView {
    return WeightedRouteOffsetQuotaView(
        code = truncate(item.code, 120),
        label = item.label,
        distanceKmText = "%.2f".format(item.distanceKm),
        archivedText = "${item.archived}",
        driverId = item.driverId,
        phoneText = item.phone,
        cost = "%.2f".format(item.cost),
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedRouteOffsetQuotaViews(items: MutableList<WeightedRouteOffsetQuota>): MutableList<WeightedRouteOffsetQuotaView> {
    val views = mutableListOf<WeightedRouteOffsetQuotaView>()
    for (item in items) {
        views.add(toWeightedRouteOffsetQuotaView(item))
    }
    return views
}

fun sumWeightedRouteOffsetQuotaView(view: WeightedRouteOffsetQuotaView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedDistanceKm + parsedCost
}
