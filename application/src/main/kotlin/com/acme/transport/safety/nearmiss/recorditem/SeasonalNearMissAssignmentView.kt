package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal near miss assignment. */
data class SeasonalNearMissAssignmentView(
    var codeText: String,
    var name: String,
    var cost: String,
    var floorValueText: String,
    var maximum: String,
    var active: String,
    var batchSize: String,
    var ownerId: String,
    var notes: String,
    var budget: String,
    var contactEmailText: String,
    var statusLabel: String,
)

fun toSeasonalNearMissAssignmentView(item: SeasonalNearMissAssignment): SeasonalNearMissAssignmentView {
    return SeasonalNearMissAssignmentView(
        codeText = truncate(item.code, 64),
        name = item.name,
        cost = "%.2f".format(item.cost),
        floorValueText = "%.2f".format(item.floorValue),
        maximum = "%.2f".format(item.maximum),
        active = "${item.active}",
        batchSize = item.batchSize.toString(),
        ownerId = item.ownerId,
        notes = truncate(item.notes, 120),
        budget = "%.2f".format(item.budget),
        contactEmailText = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalNearMissAssignmentViews(items: MutableList<SeasonalNearMissAssignment>): MutableList<SeasonalNearMissAssignmentView> {
    val views = mutableListOf<SeasonalNearMissAssignmentView>()
    for (item in items) {
        views.add(toSeasonalNearMissAssignmentView(item))
    }
    return views
}

fun sumSeasonalNearMissAssignmentView(view: SeasonalNearMissAssignmentView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedCost + parsedFloorValue + parsedMaximum
}
