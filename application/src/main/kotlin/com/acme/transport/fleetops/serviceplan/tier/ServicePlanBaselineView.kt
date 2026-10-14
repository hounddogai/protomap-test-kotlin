package com.acme.transport.fleetops.serviceplan.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan baseline. */
data class ServicePlanBaselineView(
    var code: String,
    var label: String,
    var distanceKm: String,
    var lane: String,
    var contactEmail: String,
    var cost: String,
    var statusLabel: String,
)

fun toServicePlanBaselineView(item: ServicePlanBaseline): ServicePlanBaselineView {
    return ServicePlanBaselineView(
        code = truncate(item.code, 120),
        label = truncate(item.label, 120),
        distanceKm = "%.2f".format(item.distanceKm),
        lane = item.lane,
        contactEmail = item.contactEmail,
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanBaselineViews(items: MutableList<ServicePlanBaseline>): MutableList<ServicePlanBaselineView> {
    val views = mutableListOf<ServicePlanBaselineView>()
    for (item in items) {
        views.add(toServicePlanBaselineView(item))
    }
    return views
}

fun sumServicePlanBaselineView(view: ServicePlanBaselineView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedDistanceKm + parsedCost
}
