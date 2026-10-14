package com.acme.transport.fleetops.serviceplan.planmetric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan window. */
data class ServicePlanWindowView(
    var code: String,
    var name: String,
    var odometer: String,
    var carrier: String,
    var summary: String,
    var statusLabel: String,
)

fun toServicePlanWindowView(item: ServicePlanWindow): ServicePlanWindowView {
    return ServicePlanWindowView(
        code = truncate(item.code, 40),
        name = item.name,
        odometer = item.odometer.toString(),
        carrier = item.carrier,
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanWindowViews(items: MutableList<ServicePlanWindow>): MutableList<ServicePlanWindowView> {
    val views = mutableListOf<ServicePlanWindowView>()
    for (item in items) {
        views.add(toServicePlanWindowView(item))
    }
    return views
}

fun sumServicePlanWindowView(view: ServicePlanWindowView): Double {
    val parsedOdometer = view.odometer.toInt()
    return parsedOdometer.toDouble()
}
