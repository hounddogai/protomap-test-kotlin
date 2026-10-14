package com.acme.transport.fleetops.serviceplan.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan rollup. */
data class ServicePlanRollupView(
    var codeText: String,
    var name: String,
    var shiftCode: String,
    var vehicleId: String,
    var sequence: String,
    var distanceKm: String,
    var externalRef: String,
    var threshold: String,
    var pinnedText: String,
    var depotText: String,
    var statusLabel: String,
)

fun toServicePlanRollupView(item: ServicePlanRollup): ServicePlanRollupView {
    return ServicePlanRollupView(
        codeText = item.code,
        name = item.name,
        shiftCode = item.shiftCode,
        vehicleId = truncate(item.vehicleId, 120),
        sequence = item.sequence.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        externalRef = item.externalRef,
        threshold = "%.2f".format(item.threshold),
        pinnedText = "${item.pinned}",
        depotText = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanRollupViews(items: MutableList<ServicePlanRollup>): MutableList<ServicePlanRollupView> {
    val views = mutableListOf<ServicePlanRollupView>()
    for (item in items) {
        views.add(toServicePlanRollupView(item))
    }
    return views
}

fun sumServicePlanRollupView(view: ServicePlanRollupView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedSequence.toDouble() + parsedDistanceKm + parsedThreshold
}
