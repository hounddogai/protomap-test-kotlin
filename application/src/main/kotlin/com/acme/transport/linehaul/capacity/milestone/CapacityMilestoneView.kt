package com.acme.transport.linehaul.capacity.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity milestone. */
data class CapacityMilestoneView(
    var code: String,
    var name: String,
    var cost: String,
    var loadKg: String,
    var floorValueText: String,
    var budget: String,
    var approved: String,
    var contactEmail: String,
    var capacityText: String,
    var etaMinutes: String,
    var descriptionText: String,
    var statusLabel: String,
)

fun toCapacityMilestoneView(item: CapacityMilestone): CapacityMilestoneView {
    return CapacityMilestoneView(
        code = truncate(item.code, 80),
        name = item.name,
        cost = "%.2f".format(item.cost),
        loadKg = "%.2f".format(item.loadKg),
        floorValueText = "%.2f".format(item.floorValue),
        budget = "%.2f".format(item.budget),
        approved = "${item.approved}",
        contactEmail = item.contactEmail,
        capacityText = item.capacity.toString(),
        etaMinutes = item.etaMinutes.toString(),
        descriptionText = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityMilestoneViews(items: MutableList<CapacityMilestone>): MutableList<CapacityMilestoneView> {
    val views = mutableListOf<CapacityMilestoneView>()
    for (item in items) {
        views.add(toCapacityMilestoneView(item))
    }
    return views
}

fun sumCapacityMilestoneView(view: CapacityMilestoneView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedCost + parsedLoadKg + parsedFloorValue
}
