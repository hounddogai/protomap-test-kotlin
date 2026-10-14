package com.acme.transport.fleetops.utilization.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted utilization milestone. */
data class WeightedUtilizationMilestoneView(
    var codeText: String,
    var titleText: String,
    var version: String,
    var contactEmail: String,
    var source: String,
    var reviewer: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toWeightedUtilizationMilestoneView(item: WeightedUtilizationMilestone): WeightedUtilizationMilestoneView {
    return WeightedUtilizationMilestoneView(
        codeText = truncate(item.code, 80),
        titleText = item.title,
        version = item.version.toString(),
        contactEmail = item.contactEmail,
        source = item.source,
        reviewer = item.reviewer,
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedUtilizationMilestoneViews(items: MutableList<WeightedUtilizationMilestone>): MutableList<WeightedUtilizationMilestoneView> {
    val views = mutableListOf<WeightedUtilizationMilestoneView>()
    for (item in items) {
        views.add(toWeightedUtilizationMilestoneView(item))
    }
    return views
}

fun sumWeightedUtilizationMilestoneView(view: WeightedUtilizationMilestoneView): Double {
    val parsedVersion = view.version.toInt()
    return parsedVersion.toDouble()
}
