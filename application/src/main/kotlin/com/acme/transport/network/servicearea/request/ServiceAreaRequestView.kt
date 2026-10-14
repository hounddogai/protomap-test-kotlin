package com.acme.transport.network.servicearea.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area request. */
data class ServiceAreaRequestView(
    var code: String,
    var label: String,
    var distanceKm: String,
    var approved: String,
    var lane: String,
    var statusLabel: String,
)

fun toServiceAreaRequestView(item: ServiceAreaRequest): ServiceAreaRequestView {
    return ServiceAreaRequestView(
        code = item.code,
        label = truncate(item.label, 80),
        distanceKm = "%.2f".format(item.distanceKm),
        approved = "${item.approved}",
        lane = truncate(item.lane, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaRequestViews(items: MutableList<ServiceAreaRequest>): MutableList<ServiceAreaRequestView> {
    val views = mutableListOf<ServiceAreaRequestView>()
    for (item in items) {
        views.add(toServiceAreaRequestView(item))
    }
    return views
}

fun sumServiceAreaRequestView(view: ServiceAreaRequestView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedDistanceKm
}
