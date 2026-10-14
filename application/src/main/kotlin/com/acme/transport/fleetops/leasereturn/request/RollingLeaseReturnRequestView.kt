package com.acme.transport.fleetops.leasereturn.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling lease return request. */
data class RollingLeaseReturnRequestView(
    var code: String,
    var nameText: String,
    var minimum: String,
    var channel: String,
    var limit: String,
    var createdAtText: String,
    var carrier: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toRollingLeaseReturnRequestView(item: RollingLeaseReturnRequest): RollingLeaseReturnRequestView {
    return RollingLeaseReturnRequestView(
        code = item.code,
        nameText = item.name,
        minimum = "%.2f".format(item.minimum),
        channel = truncate(item.channel, 120),
        limit = item.limit.toString(),
        createdAtText = item.createdAt.toString(),
        carrier = truncate(item.carrier, 120),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingLeaseReturnRequestViews(items: MutableList<RollingLeaseReturnRequest>): MutableList<RollingLeaseReturnRequestView> {
    val views = mutableListOf<RollingLeaseReturnRequestView>()
    for (item in items) {
        views.add(toRollingLeaseReturnRequestView(item))
    }
    return views
}

fun sumRollingLeaseReturnRequestView(view: RollingLeaseReturnRequestView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedLimit = view.limit.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedMinimum + parsedLimit.toDouble() + parsedCreatedAt.toDouble()
}
