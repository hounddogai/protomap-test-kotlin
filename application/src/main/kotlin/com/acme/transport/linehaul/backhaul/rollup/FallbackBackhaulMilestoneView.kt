package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback backhaul milestone. */
data class FallbackBackhaulMilestoneView(
    var code: String,
    var title: String,
    var vehicleId: String,
    var ownerId: String,
    var driverIdText: String,
    var cost: String,
    var statusLabel: String,
)

fun toFallbackBackhaulMilestoneView(item: FallbackBackhaulMilestone): FallbackBackhaulMilestoneView {
    return FallbackBackhaulMilestoneView(
        code = item.code,
        title = item.title,
        vehicleId = item.vehicleId,
        ownerId = truncate(item.ownerId, 64),
        driverIdText = item.driverId,
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackBackhaulMilestoneViews(items: MutableList<FallbackBackhaulMilestone>): MutableList<FallbackBackhaulMilestoneView> {
    val views = mutableListOf<FallbackBackhaulMilestoneView>()
    for (item in items) {
        views.add(toFallbackBackhaulMilestoneView(item))
    }
    return views
}

fun sumFallbackBackhaulMilestoneView(view: FallbackBackhaulMilestoneView): Double {
    val parsedCost = view.cost.toDouble()
    return parsedCost
}
