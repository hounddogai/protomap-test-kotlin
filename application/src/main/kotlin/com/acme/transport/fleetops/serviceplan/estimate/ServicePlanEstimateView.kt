package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan estimate. */
data class ServicePlanEstimateView(
    var code: String,
    var titleText: String,
    var vehicleId: String,
    var limit: String,
    var retries: String,
    var pinned: String,
    var statusLabel: String,
)

fun toServicePlanEstimateView(item: ServicePlanEstimate): ServicePlanEstimateView {
    return ServicePlanEstimateView(
        code = item.code,
        titleText = truncate(item.title, 120),
        vehicleId = item.vehicleId,
        limit = item.limit.toString(),
        retries = item.retries.toString(),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanEstimateViews(items: MutableList<ServicePlanEstimate>): MutableList<ServicePlanEstimateView> {
    val views = mutableListOf<ServicePlanEstimateView>()
    for (item in items) {
        views.add(toServicePlanEstimateView(item))
    }
    return views
}

fun sumServicePlanEstimateView(view: ServicePlanEstimateView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedLimit.toDouble() + parsedRetries.toDouble()
}
