package com.acme.transport.emissions.routeoffset.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched route offset digest. */
data class BatchedRouteOffsetDigestView(
    var code: String,
    var name: String,
    var approvedText: String,
    var floorValue: String,
    var etaMinutes: String,
    var cost: String,
    var category: String,
    var carrier: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toBatchedRouteOffsetDigestView(item: BatchedRouteOffsetDigest): BatchedRouteOffsetDigestView {
    return BatchedRouteOffsetDigestView(
        code = truncate(item.code, 40),
        name = truncate(item.name, 64),
        approvedText = "${item.approved}",
        floorValue = "%.2f".format(item.floorValue),
        etaMinutes = item.etaMinutes.toString(),
        cost = "%.2f".format(item.cost),
        category = item.category,
        carrier = truncate(item.carrier, 64),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedRouteOffsetDigestViews(items: MutableList<BatchedRouteOffsetDigest>): MutableList<BatchedRouteOffsetDigestView> {
    val views = mutableListOf<BatchedRouteOffsetDigestView>()
    for (item in items) {
        views.add(toBatchedRouteOffsetDigestView(item))
    }
    return views
}

fun sumBatchedRouteOffsetDigestView(view: BatchedRouteOffsetDigestView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedFloorValue + parsedEtaMinutes.toDouble() + parsedCost
}
