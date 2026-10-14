package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted service plan milestone. */
data class WeightedServicePlanMilestoneView(
    var code: String,
    var label: String,
    var routeCodeText: String,
    var weight: String,
    var floorValue: String,
    var externalRef: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toWeightedServicePlanMilestoneView(item: WeightedServicePlanMilestone): WeightedServicePlanMilestoneView {
    return WeightedServicePlanMilestoneView(
        code = item.code,
        label = item.label,
        routeCodeText = truncate(item.routeCode, 40),
        weight = "%.2f".format(item.weight),
        floorValue = "%.2f".format(item.floorValue),
        externalRef = item.externalRef,
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedServicePlanMilestoneViews(items: MutableList<WeightedServicePlanMilestone>): MutableList<WeightedServicePlanMilestoneView> {
    val views = mutableListOf<WeightedServicePlanMilestoneView>()
    for (item in items) {
        views.add(toWeightedServicePlanMilestoneView(item))
    }
    return views
}

fun sumWeightedServicePlanMilestoneView(view: WeightedServicePlanMilestoneView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedWeight + parsedFloorValue + parsedDistanceKm
}
