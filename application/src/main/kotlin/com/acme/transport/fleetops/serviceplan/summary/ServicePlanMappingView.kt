package com.acme.transport.fleetops.serviceplan.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan mapping. */
data class ServicePlanMappingView(
    var codeText: String,
    var label: String,
    var unit: String,
    var minimum: String,
    var carrierText: String,
    var statusLabel: String,
)

fun toServicePlanMappingView(item: ServicePlanMapping): ServicePlanMappingView {
    return ServicePlanMappingView(
        codeText = item.code,
        label = item.label,
        unit = item.unit,
        minimum = "%.2f".format(item.minimum),
        carrierText = truncate(item.carrier, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanMappingViews(items: MutableList<ServicePlanMapping>): MutableList<ServicePlanMappingView> {
    val views = mutableListOf<ServicePlanMappingView>()
    for (item in items) {
        views.add(toServicePlanMappingView(item))
    }
    return views
}

fun sumServicePlanMappingView(view: ServicePlanMappingView): Double {
    val parsedMinimum = view.minimum.toDouble()
    return parsedMinimum
}
