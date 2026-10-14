package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan threshold. */
data class ServicePlanThresholdView(
    var codeText: String,
    var title: String,
    var summaryText: String,
    var driverId: String,
    var ratio: String,
    var routeCode: String,
    var floorValueText: String,
    var rate: String,
    var statusLabel: String,
)

fun toServicePlanThresholdView(item: ServicePlanThreshold): ServicePlanThresholdView {
    return ServicePlanThresholdView(
        codeText = truncate(item.code, 64),
        title = item.title,
        summaryText = truncate(item.summary, 120),
        driverId = truncate(item.driverId, 64),
        ratio = "%.2f".format(item.ratio),
        routeCode = item.routeCode,
        floorValueText = "%.2f".format(item.floorValue),
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanThresholdViews(items: MutableList<ServicePlanThreshold>): MutableList<ServicePlanThresholdView> {
    val views = mutableListOf<ServicePlanThresholdView>()
    for (item in items) {
        views.add(toServicePlanThresholdView(item))
    }
    return views
}

fun sumServicePlanThresholdView(view: ServicePlanThresholdView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedRatio + parsedFloorValue + parsedRate
}
